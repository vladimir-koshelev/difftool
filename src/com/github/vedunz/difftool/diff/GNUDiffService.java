package com.github.vedunz.difftool.diff;

public class GNUDiffService extends AbstractDiffService {
    @Override
    public DiffResult getDiffResult() {
        return  GNUDiffCommon.getDiffResult(firstText, secondText);
    }
}
