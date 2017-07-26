package com.github.vedunz.difftool.control;


import com.github.vedunz.difftool.ui.*;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.DiffService;

import javax.swing.*;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vedun on 22.07.2017.
 */
public class DiffController {

    private final DiffConsumerList diffConsumerList;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final DiffService diffService = DiffService.createDefaultDiffService();
    private final VersionManager versionManager;


    public DiffController(VersionManager versionManager, DiffConsumerList diffConsumerList) {
        this.diffConsumerList = diffConsumerList;
        this.versionManager = versionManager;
    }

    public void uploadFirstText(Collection<String> text) {
        executorService.submit(() -> diffService.uploadFirstText(text));
    }

    public void uploadSecondText(Collection<String> text) {
        executorService.submit(() -> diffService.uploadSecondText(text));
    }

    public void requestDiff() {
        long currentVersion = versionManager.getVersion();
        executorService.submit(() -> {
            DiffResult results = diffService.getDiffResult();
            SwingUtilities.invokeLater(() -> {
                if (currentVersion == versionManager.getVersion()) {
                    diffConsumerList.update(results);
                }
            });
        });
    }


}
