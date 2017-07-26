package com.github.vedunz.difftool.control;

import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.LineDiffService;
import com.github.vedunz.difftool.ui.VersionManager;

import javax.swing.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vedun on 26.07.2017.
 */
public class LineDiffController {

    private final LineDiffConsumerList diffConsumerList;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final LineDiffService diffService = LineDiffService.createDefaultDiffService();
    private final VersionManager versionManager;

    public LineDiffController(VersionManager versionManager, LineDiffConsumerList diffConsumerList) {
        this.diffConsumerList = diffConsumerList;
        this.versionManager = versionManager;
    }

    public void requestDiff(String firstLine, String secondLine, int firstLineNo, int secondLineNo) {
        long currentVersion = versionManager.getVersion();
        executorService.submit(() -> {
            DiffResult results = diffService.getDiffResult(firstLine, secondLine);
            SwingUtilities.invokeLater(() -> {
                if (currentVersion == versionManager.getVersion()) {
                    diffConsumerList.update(results, firstLineNo, secondLineNo);
                }
            });
        });
    }


}
