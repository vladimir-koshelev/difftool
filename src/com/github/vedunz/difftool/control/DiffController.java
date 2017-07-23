package com.github.vedunz.difftool.control;


import com.github.vedunz.difftool.ui.ScrollManager;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.DiffService;
import com.github.vedunz.difftool.ui.HighlightManager;

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

    public DiffController(HighlightManager highlightManager, ScrollManager scrollManager) {
        this.highlightManager = highlightManager;
        this.scrollManager = scrollManager;
    }

    public void uploadFirstText(Collection<String> text) {
        executorService.submit(() -> diffService.uploadFirstText(text));
    }

    public void uploadSecondText(Collection<String> text) {
        executorService.submit(() -> diffService.uploadSecondText(text));
    }

    public void getDiff() {
        long currentVersion = highlightManager.getVersion();
        executorService.submit(() -> {
            DiffResult intervals = diffService.calculateDiff();
            SwingUtilities.invokeLater(() -> {
                if (currentVersion == highlightManager.getVersion()) {
                    scrollManager.updateDiffResult(intervals);
                    highlightManager.updateHighlight(currentVersion, intervals);
                }
            });
        });
    }


}
