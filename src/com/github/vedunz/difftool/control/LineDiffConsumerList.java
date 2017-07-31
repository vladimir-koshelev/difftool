package com.github.vedunz.difftool.control;

import com.github.vedunz.difftool.diff.DiffResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vedun on 26.07.2017.
 */
public class LineDiffConsumerList {
    private final List<LineDiffConsumer> diffConsumerList = new ArrayList<>();

    public void add(LineDiffConsumer consumer) {
        diffConsumerList.add(consumer);
    }

    public void remove(LineDiffConsumer consumer) {
        diffConsumerList.remove(consumer);
    }

    public void update(DiffResult diffResult, int firstLineNo, int secondLineNo) {
        for (LineDiffConsumer diffConsumer : diffConsumerList) {
            diffConsumer.updateDiffResult(diffResult, firstLineNo, secondLineNo);
        }
    }

}
