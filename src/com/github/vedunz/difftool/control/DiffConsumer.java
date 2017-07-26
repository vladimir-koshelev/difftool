package com.github.vedunz.difftool.control;

import com.github.vedunz.difftool.diff.DiffResult;

/**
 * Created by vedun on 25.07.2017.
 */
public interface DiffConsumer {

    void updateDiffResult(DiffResult diffResult);
}
