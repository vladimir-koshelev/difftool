package com.github.vedunz.difftool.diff;

/**
 * Created by vedun on 22.07.2017.
 */
public final class Interval {
    private final int start;
    private final int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Interval interval = (Interval) o;

        if (start != interval.start) return false;
        return end == interval.end;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isLineInside(int line) {
        return start <= line && line <= end;
    }

    public boolean isLineAfter(int line) {
        return end < line;
    }

    public boolean isLineBefore(int line) {
        return line < start;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", start, end);
    }
}
