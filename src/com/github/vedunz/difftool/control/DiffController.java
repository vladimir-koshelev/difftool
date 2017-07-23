package com.github.vedunz.difftool.control;


import com.github.vedunz.difftool.ui.HighlightManager;
import com.github.vedunz.difftool.ui.ScrollManager;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.DiffService;
import com.github.vedunz.difftool.ui.VersionManager;

import javax.swing.*;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vedun on 22.07.2017.
 */
public class DiffController {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DiffService diffService = DiffService.createDefaultDiffService();
    private HighlightManager highlightManager;
    private ScrollManager scrollManager;
    private VersionManager versionManager;


    public DiffController(HighlightManager highlightManager, ScrollManager scrollManager, VersionManager versionManager) {
        this.highlightManager = highlightManager;
        this.scrollManager = scrollManager;
        this.versionManager = versionManager;
    }

    public void uploadFirstText(Collection<String> text) {
        executorService.submit(() -> diffService.uploadFirstText(text));
    }

    public void uploadSecondText(Collection<String> text) {
        executorService.submit(() -> diffService.uploadSecondText(text));
    }

    public void getDiff() {
        long currentVersion = versionManager.getVersion();
        executorService.submit(() -> {
            DiffResult intervals = diffService.calculateDiff();
            SwingUtilities.invokeLater(() -> {
                if (currentVersion == versionManager.getVersion()) {
                    scrollManager.updateDiffResult(intervals);
                    highlightManager.updateHighlight(intervals);
                }
            });
        });
    }


}
