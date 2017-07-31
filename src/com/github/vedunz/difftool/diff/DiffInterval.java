package com.github.vedunz.difftool.diff;

/**
 * Created by vedun on 22.07.2017.
 */
public abstract class DiffInterval {

    protected int beginFirst;
    protected int beginSecond;
    protected int length = 1;

    protected DiffInterval(int beginFirst, int beginSecond) {
        this.beginFirst = beginFirst;
        this.beginSecond = beginSecond;
    }

    public int getLength() {
        return length;
    }

    public int getBeginFirst() {
        return beginFirst;
    }

    public int getBeginSecond() {
        return beginSecond;
    }

    public Interval getFirstInterval() {
        return new Interval(beginFirst, beginFirst + length - 1);
    }

    public Interval getSecondInterval() {
        return new Interval(beginSecond, beginSecond + length - 1);
    }

    public String toString() {
        return String.format("[(%d, %d)<=>(%d, %d)]",
                beginFirst, beginFirst + length - 1, beginSecond, beginSecond + length - 1);
    }

    public Interval getInterval(boolean isFirst) {
        return isFirst ? getFirstInterval() : getSecondInterval();
    }
}
