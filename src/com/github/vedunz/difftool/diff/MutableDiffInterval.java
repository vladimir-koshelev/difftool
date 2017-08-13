package com.github.vedunz.difftool.diff;

/**
 * Created by vedun on 22.07.2017.
 */
class MutableDiffInterval extends DiffInterval {
    public MutableDiffInterval(int startFirst, int startSecond, int length) {
        super(startFirst, startSecond, length);
    }

    public MutableDiffInterval(int endFirst, int endSecond) {
        super(endFirst, endSecond);
    }

    public void expand() {
        length++;
    }

    public void reverse() {
        beginFirst = beginFirst - length + 1;
        beginSecond = beginSecond - length + 1;
    }
}
