package com.github.vedunz.difftool.diff;

import com.sun.istack.internal.NotNull;

import java.util.List;

/**
 * Created by vedun on 22.07.2017.
 */
public class DiffResult {

    private List<DiffInterval> intervals;
    private int firstLineNo;
    private int secondLineNo;

    public DiffResult(@NotNull List<DiffInterval> intervals, int firstLineNo, int secondLineNo) {
        this.intervals = intervals;
        this.firstLineNo = firstLineNo;
        this.secondLineNo = secondLineNo;
    }

    int getIntervalIndexForLine(int line, boolean isFirst) {
        int l = 0;
        int r = intervals.size() - 1;

        while (l < r) {
            int m = (l + r) / 2;
            Interval interval =  (isFirst) ? intervals.get(m).getFirstInterval() : intervals.get(m).getSecondInterval();
            if (interval.isLineInside(line))
                return m;
            if (interval.isLineBefore(line))
                r = m - 1;
            else
                l = m + 1;
        }

        return (l + r) / 2;

    }

    public DiffInterval getIntervalBefore(int line, boolean isFirst) {
        if (intervals.size() == 0)
            return null;

        int idx = getIntervalIndexForLine(line, isFirst);
        Interval interval =  (isFirst) ? intervals.get(idx).getFirstInterval() : intervals.get(idx).getSecondInterval();
        if (interval.isLineInside(line) || interval.isLineAfter(line))
            return intervals.get(idx);
        if (interval.isLineBefore(idx) && idx > 0)
            return intervals.get(idx - 1);

        return null;
    }

    public DiffInterval getIntervalAfter(int line, boolean isFirst) {
        if (intervals.size() == 0)
            return null;

        int idx = getIntervalIndexForLine(line, isFirst);
        Interval interval =  (isFirst) ? intervals.get(idx).getFirstInterval() : intervals.get(idx).getSecondInterval();
        if (interval.isLineInside(line) || interval.isLineBefore(line))
            return intervals.get(idx);
        if (interval.isLineBefore(idx) && idx < intervals.size() - 1)
            return intervals.get(idx + 1);

        return null;

    }

    public List<DiffInterval> getIntervals() {
        return intervals;
    }

    public int getFirstLineNo() {
        return firstLineNo;
    }

    public int getSecondLineNo() {
        return secondLineNo;
    }

    public int getLineNo(boolean isFirst) {
        return  isFirst ? getFirstLineNo() : getSecondLineNo();
    }
}
