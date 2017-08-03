package com.github.vedunz.difftool.control;


import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.DiffService;
import com.github.vedunz.difftool.ui.VersionManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vedun on 22.07.2017.
 */
public class DiffController {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final DiffService diffService = DiffService.createDefaultDiffService();
    private final VersionManager versionManager;
    private final List<DiffConsumer> diffConsumerList = new ArrayList<>();

    public DiffController(VersionManager versionManager) {
        this.versionManager = versionManager;
    }

    public void addDiffConsumer(DiffConsumer diffConsumer) {
        diffConsumerList.add(diffConsumer);
    }

    public void removeDiffConsumer(DiffConsumer diffConsumer) {
        diffConsumerList.remove(diffConsumer);
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
                    for (DiffConsumer diffConsumer : diffConsumerList)
                        diffConsumer.updateDiffResult(results);
                }
            });
        });
    }


    public void invalidate() {
        for (DiffConsumer diffConsumer : diffConsumerList)
            diffConsumer.updateDiffResult(null);
    }
}
