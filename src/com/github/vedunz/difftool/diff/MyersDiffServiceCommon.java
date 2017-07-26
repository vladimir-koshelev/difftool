package com.github.vedunz.difftool.diff;

import org.apache.commons.text.diff.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vedun on 26.07.2017.
 */
public abstract class MyersDiffServiceCommon {
    protected <T> DiffResult getDiffResult(List<T> firstList, List<T> secondList) {
        DiffComparator<T> diffComparator = new DiffComparator<>(firstList, secondList);
        EditScript<T> script = diffComparator.getScript();
        List<DiffInterval> diffIntervals = new ArrayList<>();
        MutableDiffInterval curDiffInterval = null;
        int firstPos = 0, secondPos = 0;
        for (EditCommand editCommand : script.getCommands()) {
            if (editCommand instanceof InsertCommand) {
                if (curDiffInterval != null) {
                    diffIntervals.add(curDiffInterval);
                    curDiffInterval = null;
                }
                secondPos++;
            }
            if (editCommand instanceof DeleteCommand) {
                if (curDiffInterval != null) {
                    diffIntervals.add(curDiffInterval);
                    curDiffInterval = null;
                }
                firstPos++;
            }
            if (editCommand instanceof KeepCommand) {
                if (curDiffInterval == null) {
                    curDiffInterval = new MutableDiffInterval(firstPos, secondPos);
                } else
                    curDiffInterval.expand();
                firstPos++;
                secondPos++;
            }
        }
        if (curDiffInterval != null)
            diffIntervals.add(curDiffInterval);
        return new DiffResult(diffIntervals, firstList.size(), secondList.size());
    }
}
