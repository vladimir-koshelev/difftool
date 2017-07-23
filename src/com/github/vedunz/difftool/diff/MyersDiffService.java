package com.github.vedunz.difftool.diff;

import org.apache.commons.text.diff.*;

import java.util.ArrayList;
import java.util.Collection;

import java.util.List;

/**
 * Created by vedun on 24.07.2017.
 */
public class MyersDiffService implements DiffService {

    List<String> firstText = new ArrayList<>();
    List<String> secondText = new ArrayList<>();


    @Override
    public void uploadFirstText(Collection<String> lines) {
        firstText.clear();
        firstText.addAll(lines);
    }

    @Override
    public void uploadSecondText(Collection<String> lines) {
        secondText.clear();
        secondText.addAll(lines);

    }

    @Override
    public DiffResult calculateDiff() {
        DiffComparator<String> diffComparator = new DiffComparator<>(firstText, secondText);
        EditScript<String> script = diffComparator.getScript();
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
        return new DiffResult(diffIntervals, firstText.size(), secondText.size());
    }
}
