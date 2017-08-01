package com.github.vedunz.difftool.diff;

import com.sun.istack.internal.NotNull;

import java.util.List;

/**
 * Created by vedun on 22.07.2017.
 */
public class DiffResult {

    private final List<DiffInterval> intervals;
    private final int firstSize;
    private final int secondSize;

    public DiffResult(@NotNull List<DiffInterval> intervals, int firstSize, int secondSize) {
        this.intervals = intervals;
        this.firstSize = firstSize;
        this.secondSize = secondSize;
    }

    public DiffInterval getIntervalBefore(int position, boolean isFirst) {
        if (intervals.size() == 0)
            return null;

        int idx = getIntervalIndexForPosition(position, isFirst);
        Interval interval = (isFirst) ? intervals.get(idx).getFirstInterval() : intervals.get(idx).getSecondInterval();
        if (interval.isLineInside(position) || interval.isLineAfter(position))
            return intervals.get(idx);
        if (idx > 0)
            return intervals.get(idx - 1);

        return null;
    }

    public DiffInterval getIntervalAfter(int line, boolean isFirst) {
        if (intervals.size() == 0)
            return null;

        int idx = getIntervalIndexForPosition(line, isFirst);
        Interval interval = (isFirst) ? intervals.get(idx).getFirstInterval() : intervals.get(idx).getSecondInterval();
        if (interval.isLineInside(line) || interval.isLineBefore(line))
            return intervals.get(idx);
        if (idx < intervals.size() - 1)
            return intervals.get(idx + 1);

        return null;
    }

    public boolean isLineInSame(final int line, final boolean isFirst) {
        if (intervals.isEmpty())
            return false;
        int n =  getIntervalIndexForPosition(line, isFirst);
        return (intervals.get(n).getInterval(isFirst).isLineInside(line));
    }

    public List<DiffInterval> getIntervals() {
        return intervals;
    }

    public int getFirstSize() {
        return firstSize;
    }

    public int getSecondSize() {
        return secondSize;
    }

    public int getSize(boolean isFirst) {
        return isFirst ? getFirstSize() : getSecondSize();
    }



    private int getIntervalIndexForPosition(int position, boolean isFirst) {
        int l = 0;
        int r = intervals.size() - 1;

        while (l < r) {
            int m = (l + r) / 2;
            Interval interval = intervals.get(m).getInterval(isFirst);
            if (interval.isLineInside(position))
                return m;
            if (interval.isLineBefore(position))
                r = m - 1;
            else
                l = m + 1;
        }

        return (l + r) / 2;

    }
}
