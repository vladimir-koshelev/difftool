package com.github.vedunz.difftool.control;


import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.DiffService;
import com.github.vedunz.difftool.ui.VersionManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    public void replaceLineFirst(int lineno, String newValue) {
        executorService.submit(() -> {
            diffService.replaceLineFirst(lineno, newValue);
        });
    }

    public void replaceLineSecond(int lineno, String newValue) {
        executorService.submit(() -> {
            diffService.replaceLineSecond(lineno, newValue);
        });
    }

    public void removeLinesFirst(final int start, final int end) {
        executorService.submit(() -> {
            diffService.removeFirstLines(start, end - start + 1);
        });
    }

    public void removeLinesSecond(final int start, final int end) {
        executorService.submit(() -> {
            diffService.removeSecondLines(start, end - start + 1);
        });
    }

    public void insertLinesFirst(final int start, final List<String> elements) {
        executorService.submit(() -> {
            diffService.insertFirstLines(start, elements);
        });
    }

    public void insertLinesSecond(final int start, final List<String> elements) {
        executorService.submit(() -> {
            diffService.insertSecondLines(start, elements);
        });
    }

    public Future<String> getFirstText() {
        return executorService.submit(() -> diffService.getFirstText());
    }

    public Future<String> getSecondText() {
        return executorService.submit(() -> diffService.getSecondText());
    }

    public void requestDiff() {
        long currentVersion = versionManager.getVersion();
        executorService.submit(() -> {
            if (currentVersion == versionManager.getVersion()) {
                DiffResult results = diffService.getDiffResult();
                SwingUtilities.invokeLater(() -> {
                    if (currentVersion == versionManager.getVersion()) {
                        for (DiffConsumer diffConsumer : diffConsumerList)
                            diffConsumer.updateDiffResult(results);
                    }
                });
            }
        });
    }


    public void invalidate() {
        for (DiffConsumer diffConsumer : diffConsumerList)
            diffConsumer.updateDiffResult(null);
    }

}
