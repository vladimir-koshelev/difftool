package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.diff.DiffResult;

/**
 * Created by vedun on 25.07.2017.
 */
public interface DiffConsumer {

    void updateDiffResult(DiffResult diffResult);
}
