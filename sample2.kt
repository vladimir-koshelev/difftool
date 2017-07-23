/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.cfg.pseudocode.PseudoValue
import org.jetbrains.kotlin.cfg.pseudocode.Pseudocode
import org.jetbrains.kotlin.cfg.pseudocode.PseudocodeUtil
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.eval.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.ConditionalJumpInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.LocalFunctionDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.*
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.analyzeFully
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getTextWithLocation
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.types.typeUtil.isBoolean
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

sealed class SymbolValue

data class ExpressionSymbolValue(val expression: KtElement): SymbolValue() {
    override fun toString(): String {
        return ("(${expression.getTextWithLocation()})")
    }
}

data class PseudoValueSymbolValue(val pseudoValue: PseudoValue) : SymbolValue()

data class MergeSymbolValue(val instruction: Instruction): SymbolValue()

data class NotSymbolValue(val operand: SymbolValue): SymbolValue() {
    companion object {
        fun create(value: SymbolValue) = if (value is NotSymbolValue) value.operand else NotSymbolValue(value)
    }
    override fun toString(): String {
        return ("not(${operand})")
    }
}

data class SymbolControlFlowInfo(val variables2SymbolValues: MutableMap<VariableDescriptor, SymbolValue> = LinkedHashMap<VariableDescriptor, SymbolValue>(),
                                 val pseudoValues2SymbolValues: MutableMap<PseudoValue, SymbolValue> = LinkedHashMap<PseudoValue, SymbolValue>(),
                                 val assumes: MutableSet<SymbolValue> = LinkedHashSet<SymbolValue>())
{
    fun assume(value: SymbolValue) {
        assumes.add(value)
    }

    fun clone(): SymbolControlFlowInfo {
        return SymbolControlFlowInfo(LinkedHashMap(variables2SymbolValues), LinkedHashMap(pseudoValues2SymbolValues), LinkedHashSet(assumes))
    }

    fun merge(with: SymbolControlFlowInfo, mergeInstruction: Instruction): SymbolControlFlowInfo {

        val newVariables2SymbolValues = LinkedHashMap<VariableDescriptor, SymbolValue>()
        for (varDesc in variables2SymbolValues.keys.union(with.variables2SymbolValues.keys)) {
            if (with.variables2SymbolValues.containsKey(varDesc) && variables2SymbolValues.containsKey(varDesc)) {
                if (variables2SymbolValues[varDesc] == with.variables2SymbolValues[varDesc])
                    newVariables2SymbolValues.put(varDesc, variables2SymbolValues[varDesc]!!)
                else
                    newVariables2SymbolValues.put(varDesc, MergeSymbolValue(mergeInstruction))
            }
        }

        val newPseudoValue2SymbolValue = LinkedHashMap(pseudoValues2SymbolValues)
        newPseudoValue2SymbolValue.putAll(with.pseudoValues2SymbolValues)
        val newAssuems = LinkedHashSet(assumes.intersect(with.assumes))
        return SymbolControlFlowInfo(newVariables2SymbolValues, newPseudoValue2SymbolValue, newAssuems)
    }

    fun readPseudoValue(pseudoValue: PseudoValue): SymbolValue {
        if (pseudoValues2SymbolValues.containsKey(pseudoValue))
            return pseudoValues2SymbolValues[pseudoValue]!!
        else {
            val element = pseudoValue.element;
            if (element != null)
                return ExpressionSymbolValue(element)
            else
                return PseudoValueSymbolValue(pseudoValue)
        }
    }

    fun writePseudoValue(pseudoValue: PseudoValue, symbolValue: SymbolValue) {
        pseudoValues2SymbolValues[pseudoValue] = symbolValue
    }

    fun readVariable(variableDescriptor: VariableDescriptor): SymbolValue {
        return variables2SymbolValues[variableDescriptor]!!
    }

    fun  writeVariable(description: VariableDescriptor, symbolValue: SymbolValue) {
        variables2SymbolValues[description] = symbolValue
    }
}
// symbol preconditions
class SymbolControlFlowProvider(private val pseudocode: Pseudocode,
                                private val bindingContext: BindingContext) {

    private val booleanVariableDescs: MutableSet<VariableDescriptor> = LinkedHashSet()

    private fun analyzePseudocode() {
        for (accessInstruction in pseudocode.instructionsIncludingDeadCode.filter { it is AccessValueInstruction }) {
            val target = (accessInstruction as AccessValueInstruction).target
            if (target is AccessTarget.Declaration) {
                val descriptor = target.descriptor
                if (isLocalVariable(descriptor) && descriptor.type.isBoolean()) {
                    booleanVariableDescs.add(descriptor)
                }
            }
        }
        for (variableDescriptor in getAllWritesToLocalVars()) {
            booleanVariableDescs.remove(variableDescriptor)
        }
    }

    private fun getAllWritesToLocalVars(): Set<VariableDescriptor> {
        val result = LinkedHashSet<VariableDescriptor>()
        for (localFun in pseudocode.instructionsIncludingDeadCode.filter { it is LocalFunctionDeclarationInstruction }) {
            val body = (localFun as LocalFunctionDeclarationInstruction).body
            analyzeAllWriteInstToLocalVars(body, result)
        }
        return result
    }

    private fun analyzeAllWriteInstToLocalVars(body: Pseudocode, result: LinkedHashSet<VariableDescriptor>) {
        for (instruction in body.instructionsIncludingDeadCode.filter { it is LocalFunctionDeclarationInstruction || it is WriteValueInstruction }) {
            if (instruction is WriteValueInstruction) {
                val target = instruction.target
                if (target is AccessTarget.Declaration) {
                    val descriptor = target.descriptor
                    if (isLocalVariable(descriptor) && descriptor.type.isBoolean() && descriptor.isVar) {
                        result.add(descriptor)
                    }
                }
            }
            if (instruction is LocalFunctionDeclarationInstruction) {
                analyzeAllWriteInstToLocalVars(instruction.body, result)
            }
        }
    }

    fun Pseudocode.collectData(
            traversalOrder: TraversalOrder,
            mergeEdges: (Instruction, Collection<SymbolControlFlowInfo>) -> SymbolControlFlowInfo,
            spliteEdge: (Instruction, Instruction, SymbolControlFlowInfo) -> SymbolControlFlowInfo,
            updateEdge: (Instruction, SymbolControlFlowInfo) -> SymbolControlFlowInfo,
            initialInfo: SymbolControlFlowInfo
    ): Map<Instruction, Edges<SymbolControlFlowInfo>> {



        val edgesMap = LinkedHashMap<Instruction, Edges<SymbolControlFlowInfo>>()
        edgesMap.put(getStartInstruction(traversalOrder), Edges(initialInfo, initialInfo))

        val changed = mutableMapOf<Instruction, Boolean>()
        do {
            collectDataFromSubgraph(
                    traversalOrder, edgesMap,
                    mergeEdges, spliteEdge, updateEdge, changed, false)
        } while (changed.any { it.value })

        return edgesMap
    }

    private fun Pseudocode.collectDataFromSubgraph(
            traversalOrder: TraversalOrder,
            edgesMap: MutableMap<Instruction, Edges<SymbolControlFlowInfo>>,
            mergeEdges: (Instruction, Collection<SymbolControlFlowInfo>) -> SymbolControlFlowInfo,
            spliteEdge: (Instruction, Instruction, SymbolControlFlowInfo) -> SymbolControlFlowInfo,
            updateEdge: (Instruction, SymbolControlFlowInfo) -> SymbolControlFlowInfo,
            changed: MutableMap<Instruction, Boolean>,
            isLocal: Boolean
    ) {
        val instructions = getInstructions(traversalOrder)

        for (instruction in instructions) {
            val isStart = instruction.isStartInstruction(traversalOrder)
            if (!isLocal && isStart)


                continue

            val previousInstructions = instruction.getPreviousInstructions(traversalOrder)

            if (instruction is LocalFunctionDeclarationInstruction) {
                continue
            }

            val previousDataValue = edgesMap[instruction]
            if (previousDataValue != null && previousInstructions.all { changed[it] == false }) {
                changed[instruction] = false
                continue
            }

            val incomingEdgesData = HashSet<SymbolControlFlowInfo>()

            val prevInstruction = previousInstructions.singleOrNull()
            if (prevInstruction != null) {
                val previousData = edgesMap[prevInstruction]
                if (previousData != null) {
                    if (prevInstruction.getNextInstructions(traversalOrder).size > 1) {
                            incomingEdgesData.add(spliteEdge(prevInstruction, instruction, previousData.outgoing))
                    }
                    else
                        incomingEdgesData.add(previousData.outgoing)
                }
            } else {
                for (previousInstruction in previousInstructions) {
                    val previousData = edgesMap[previousInstruction]
                    if (previousData != null) {
                        incomingEdgesData.add(previousData.outgoing)
                    }
                }
            }
            val mergedData = mergeEdges(instruction, incomingEdgesData)
            val outgoingData = updateEdge(instruction, mergedData)

            updateEdgeDataForInstruction(instruction, previousDataValue, Edges(mergedData, outgoingData), edgesMap, changed)
        }
    }



    private fun updateEdgeDataForInstruction(
            instruction: Instruction,
            previousValue: Edges<SymbolControlFlowInfo>?,
            newValue: Edges<SymbolControlFlowInfo>?,
            edgesMap: MutableMap<Instruction, Edges<SymbolControlFlowInfo>>,
            changed: MutableMap<Instruction, Boolean>
    ) {
        if (previousValue != newValue && newValue != null) {
            changed[instruction] = true
            edgesMap.put(instruction, newValue)
        }
        else {
            changed[instruction] = false
        }
    }

    fun computeInstructionsPreconditions(): Map<Instruction, Edges<SymbolControlFlowInfo>> {
        analyzePseudocode()
        return pseudocode.collectData(TraversalOrder.FORWARD,
           {
               instruction: Instruction, incomingEdgesData: Collection<SymbolControlFlowInfo> ->
               mergeAllSymbolControlFlowInfos(incomingEdgesData, instruction)
           },
           {
               from: Instruction, to: Instruction, symbolInfo: SymbolControlFlowInfo ->
                                          calculateAssume(from, symbolInfo, to)
           },
           {
               instruction: Instruction, symbolInfo: SymbolControlFlowInfo ->
               interpretInstruction(instruction, symbolInfo)
           },
           SymbolControlFlowInfo()
        )
    }

    private fun calculateAssume(from: Instruction, symbolInfo: SymbolControlFlowInfo, to: Instruction): SymbolControlFlowInfo {
        return if (from is ConditionalJumpInstruction && from.conditionValue != null) {
            val condition = from.conditionValue!!
            val newSymbolInfo = symbolInfo.clone()
            val isTrueBranch = from.nextOnTrue === to
            val createdAt = condition.createdAt
            if (createdAt != null && createdAt is MagicInstruction && (createdAt.kind == MagicKind.AND || createdAt.kind == MagicKind.OR)) {
                if (isTrueBranch && createdAt.kind == MagicKind.AND) {
                    for (assume in getAllAssumesForSameInstrs(createdAt, symbolInfo)) {
                        newSymbolInfo.assume(assume)
                    }
                }
                if (!isTrueBranch && createdAt.kind == MagicKind.OR) {
                    for (assume in getAllAssumesForSameInstrs(createdAt, symbolInfo)) {
                        newSymbolInfo.assume(NotSymbolValue.create(assume))
                    }
                }
            } else
            if (isTrueBranch) {
                newSymbolInfo.assume(symbolInfo.readPseudoValue(condition))
            }
            else {
                newSymbolInfo.assume(NotSymbolValue.create(symbolInfo.readPseudoValue(condition)))
            }
            newSymbolInfo
        }
        else
            symbolInfo
    }

    private fun getAllAssumesForSameInstrs(magicIntr: MagicInstruction, symbolInfo: SymbolControlFlowInfo): Set<SymbolValue> {
        val result = LinkedHashSet<SymbolValue>()
        val lhs = magicIntr.inputValues[0]
        val rhs = magicIntr.inputValues[1]
        val lhsInst = lhs.createdAt
        val rhsInst = rhs.createdAt
        if (lhsInst != null && lhsInst is MagicInstruction && lhsInst.kind == magicIntr.kind)
            result.addAll(getAllAssumesForSameInstrs(lhsInst, symbolInfo))
        else
            result.add(symbolInfo.readPseudoValue(lhs))
        if (rhsInst != null && rhsInst is MagicInstruction && rhsInst.kind == magicIntr.kind)
            result.addAll(getAllAssumesForSameInstrs(rhsInst, symbolInfo))
        else
            result.add(symbolInfo.readPseudoValue(rhs))
        return result
    }

    private fun interpretInstruction(instruction: Instruction, entrySymbolControlFlowInfo: SymbolControlFlowInfo): SymbolControlFlowInfo {
        val retValue = entrySymbolControlFlowInfo.clone();
        when (instruction) {
            is AccessValueInstruction -> {
                val target = instruction.target
                if (instruction is ReadValueInstruction) {
                    if (target is AccessTarget.Declaration && booleanVariableDescs.contains(target.descriptor))
                        retValue.writePseudoValue(instruction.outputValue, retValue.readVariable(target.descriptor))
                    if (target is AccessTarget.Call) {
                        val candidateDescriptor = target.resolvedCall.candidateDescriptor
                        if (candidateDescriptor != null && candidateDescriptor is VariableDescriptor
                            && booleanVariableDescs.contains(candidateDescriptor)) {
                            retValue.writePseudoValue(instruction.outputValue, retValue.readVariable(candidateDescriptor))
                        }
                    }
                }

                if (instruction is WriteValueInstruction) {
                    if (target is AccessTarget.Declaration
                        && booleanVariableDescs.contains(target.descriptor))
                        retValue.writeVariable(target.descriptor, retValue.readPseudoValue(instruction.rValue))
                    if (target is AccessTarget.Call) {
                        val candidateDescriptor = target.resolvedCall.candidateDescriptor
                        if (candidateDescriptor != null && candidateDescriptor is VariableDescriptor
                            && booleanVariableDescs.contains(candidateDescriptor)) {
                            retValue.writeVariable(candidateDescriptor, retValue.readPseudoValue(instruction.rValue))
                        }
                    }
                }
            }

            is MagicInstruction -> {
                when (instruction.kind) {
                    MagicKind.FAKE_INITIALIZER -> {
                        val description = bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, instruction.element]
                        if (description != null && description is VariableDescriptor)
                            retValue.writeVariable(description, ExpressionSymbolValue(instruction.element))
                    }
                }
            }

            is CallInstruction -> {
                val element = instruction.element
                val outputVal = instruction.outputValue
                val inputVal = instruction.inputValues.singleOrNull()
                if (element is KtUnaryExpression && bindingContext.getType(element)?.isBoolean() == true
                    && element.operationToken == KtTokens.EXCL  && outputVal != null && inputVal != null) {
                    retValue.writePseudoValue(outputVal, NotSymbolValue.create(retValue.readPseudoValue(inputVal)))
                }
            }

        }
        return retValue
    }

    private fun isLocalVariable(descriptor: VariableDescriptor): Boolean =
            descriptor is LocalVariableDescriptor || descriptor is ValueParameterDescriptor


    private fun mergeAllSymbolControlFlowInfos(incomingEdgesData: Collection<SymbolControlFlowInfo>,
                                               instruction: Instruction): SymbolControlFlowInfo {
        when (incomingEdgesData.size) {
            0 -> return SymbolControlFlowInfo()
            1 -> return incomingEdgesData.first()
            else -> {
                var mergedInfo = incomingEdgesData.first()
                for (curInfo in incomingEdgesData.drop(1)) {
                    mergedInfo = mergedInfo.merge(curInfo, instruction)
                }
                return mergedInfo

            }
        }
    }
}

class BooleanExpressionIsAlwaysConstantInspection : AbstractKotlinInspection() {

    private fun pseudocode(declaration: KtDeclaration, bindingContext: BindingContext): Pseudocode? {
         return PseudocodeUtil.generatePseudocode(declaration, bindingContext)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : KtVisitorVoid() {
            override fun visitDeclaration(dcl: KtDeclaration) {
                if (dcl is KtNamedFunction) {
                    val bindingContext = dcl.analyzeFully()
                    val pseudocode = pseudocode(dcl, bindingContext) ?: return
                    val scfp = SymbolControlFlowProvider(pseudocode, bindingContext)
                    val preconditions = scfp.computeInstructionsPreconditions()
                    reportConstantExpressions(pseudocode, bindingContext, preconditions)
                }
            }

            private fun reportConstantExpressions(pseudocode: Pseudocode, bindingContext: BindingContext, preconditions: Map<Instruction, Edges<SymbolControlFlowInfo>>) {
                val nodesToReport = LinkedHashMap<KtExpression, Boolean>()
                for (instruction in pseudocode.instructions) {
                    if (instruction is InstructionWithValue) {
                        val pseudoVal = instruction.outputValue
                        val element = instruction.element
                        if (pseudoVal != null && element is KtExpression && bindingContext.getType(element)?.isBoolean() == true) {
                            val symbolInfo = preconditions[instruction]
                            if (symbolInfo != null) {
                                val symbolValue = symbolInfo.outgoing.readPseudoValue(pseudoVal)
                                if (symbolInfo.incoming.assumes.contains(symbolValue)) {
                                    nodesToReport.put(element, true)
                                }
                                if (symbolInfo.incoming.assumes.contains(NotSymbolValue.create(symbolValue))) {
                                    nodesToReport.put(element, false)
                                }
                            }
                        }
                    }
                }
                for ((node, reason) in nodesToReport) {
                    if (nodesToReport.keys.contains(node.parent))
                        continue
                    holder.registerProblem(node, "the boolean expression is always $reason", ProblemHighlightType.WEAK_WARNING)
                }
            }
        }
    }
}