package com.github.vedunz.difftool.control;

import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.LineDiffService;
import com.github.vedunz.difftool.ui.VersionManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vedun on 26.07.2017.
 */
public class LineDiffController {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final LineDiffService diffService = LineDiffService.createDefaultDiffService();
    private final VersionManager versionManager;
    private final List<LineDiffConsumer> lineDiffConsumerList = new ArrayList<>();

    public LineDiffController(VersionManager versionManager) {
        this.versionManager = versionManager;
    }

    public void addLineDiffConsumer(LineDiffConsumer lineDiffConsumer) {
        lineDiffConsumerList.add(lineDiffConsumer);
    }

    public void removeLineDiffConsumer(LineDiffConsumer lineDiffConsumer) {
        lineDiffConsumerList.remove(lineDiffConsumer);
    }

    public void requestDiff(String firstLine, String secondLine, int firstLineNo, int secondLineNo) {
        long currentVersion = versionManager.getVersion();
        executorService.submit(() -> {
            if (currentVersion != versionManager.getVersion())
                return;
            DiffResult results = diffService.getDiffResult(firstLine, secondLine);
            SwingUtilities.invokeLater(() -> {
                if (currentVersion == versionManager.getVersion()) {
                    for (LineDiffConsumer lineDiffConsumer : lineDiffConsumerList) {
                        lineDiffConsumer.updateDiffResult(results, firstLineNo, secondLineNo);
                    }
                }
            });
        });
    }


}
