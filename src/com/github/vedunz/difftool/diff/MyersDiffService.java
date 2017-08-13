package com.github.vedunz.difftool.diff;

/**
 * Created by vedun on 24.07.2017.
 */
public class MyersDiffService extends AbstractDiffService {

    @Override
    public DiffResult getDiffResult() {
        return MyersDiffCommon.getDiffResult(firstText, secondText);
    }
}
