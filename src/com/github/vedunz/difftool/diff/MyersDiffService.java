package com.github.vedunz.difftool.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by vedun on 24.07.2017.
 */
public class MyersDiffService extends AbstractDiffService {

    @Override
    public DiffResult getDiffResult() {
        return MyersDiffServiceCommon.getDiffResult(firstText, secondText);
    }
}
