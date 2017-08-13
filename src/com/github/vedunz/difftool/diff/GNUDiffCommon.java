package com.github.vedunz.difftool.diff;

import com.bmsi.util.Diff;

import java.util.ArrayList;
import java.util.List;

public class GNUDiffCommon {
    public static <T> DiffResult getDiffResult(List<T> firstList, List<T> secondList) {
        @SuppressWarnings("unchecked")
        T [] first = firstList.toArray((T []) new Object[firstList.size()]);
        @SuppressWarnings("unchecked")
        T [] second = secondList.toArray((T []) new Object[secondList.size()]);
        Diff diff = new Diff(first, second);
        Diff.change change = diff.diff_2(false);
        List<DiffInterval> diffIntervals = new ArrayList<>();
        int firstPos = 0;
        int secondPos = 0;
        List<Interval> firstIntervals = new ArrayList<>();
        List<Interval> secondIntervals = new ArrayList<>();
        while (change != null) {
            if (firstPos < change.line0) {
                firstIntervals.add(new Interval(firstPos, change.line0 - 1));
            }
            if (secondPos < change.line1) {
                secondIntervals.add(new Interval(secondPos, change.line1 - 1));
            }
            firstPos = change.line0 + change.deleted;
            secondPos = change.line1 + change.inserted;
            change = change.link;
        }
        if (firstPos < first.length)
            firstIntervals.add(new Interval(firstPos, first.length - 1));
        if (secondPos < second.length)
            secondIntervals.add(new Interval(secondPos, second.length - 1));

        assert (firstIntervals.size() == secondIntervals.size());
        for (int i = 0; i < firstIntervals.size(); ++i) {
            Interval firstInterval = firstIntervals.get(i);
            Interval secondInterval = secondIntervals.get(i);
            assert (firstInterval.getEnd() - firstInterval.getStart() ==
                        secondInterval.getEnd() - secondInterval.getStart());
            diffIntervals.add(new MutableDiffInterval(firstInterval.getStart(), secondInterval.getStart(),
                    firstInterval.getEnd() - firstInterval.getStart() + 1));
        }

        return new DiffResult(diffIntervals, first.length, second.length);
    }
}
