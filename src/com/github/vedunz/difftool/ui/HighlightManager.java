package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.diff.DiffResult;

/**
 * Created by vedun on 22.07.2017.
 */
public interface HighlightManager {

    long getVersion();

    void updateHighlight(long version, DiffResult intervals);

}
