package com.github.vedunz.difftool.diff;

/**
 * Created by vedun on 26.07.2017.
 */
public interface LineDiffService {

    DiffResult getDiffResult(String firstLine, String secondLine);

    static LineDiffService createDefaultDiffService() {
        return new MyersLineDiffService();
    }

}
