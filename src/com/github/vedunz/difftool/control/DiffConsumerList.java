package com.github.vedunz.difftool.control;

import com.github.vedunz.difftool.diff.DiffResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vedun on 25.07.2017.
 */
public class DiffConsumerList {

    private List<DiffConsumer> diffConsumerList = new ArrayList<>();

    public void add(DiffConsumer consumer) {
        diffConsumerList.add(consumer);
    }

    public void remove(DiffConsumer consumer) {
        diffConsumerList.remove(consumer);
    }

    public void update(DiffResult diffResult) {
        for (DiffConsumer diffConsumer : diffConsumerList) {
            diffConsumer.updateDiffResult(diffResult);
        }
    }

}
