package com.github.vedunz.difftool.control;

import com.github.vedunz.difftool.diff.DiffResult;

/**
 * Created by vedun on 26.07.2017.
 */
public interface LineDiffConsumer {

    void updateDiffResult(DiffResult diffResult, int firstLineNo, int secondLineNo);
}
