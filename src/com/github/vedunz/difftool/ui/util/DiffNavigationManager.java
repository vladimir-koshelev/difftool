package com.github.vedunz.difftool.ui.util;

import com.github.vedunz.difftool.diff.DiffInterval;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.Interval;
import com.github.vedunz.difftool.ui.DiffConsumer;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;

/**
 * Created by vedun on 25.07.2017.
 */
public class DiffNavigationManager implements DiffConsumer {

    private DiffResult diffResult;
    private JScrollPane scrollPane;
    private JTextPane textPane;
    private JButton prevButton;
    private JButton nextButton;
    private boolean isFirst;

    public DiffNavigationManager(JScrollPane scrollPane, JTextPane textPane, JButton prevButton, JButton nextButton, boolean isFirst) {
        this.scrollPane = scrollPane;
        this.textPane = textPane;
        this.prevButton = prevButton;
        this.nextButton = nextButton;
        this.isFirst = isFirst;

        prevButton.addActionListener((ActionEvent e) -> {
            if (diffResult == null)
                return;
            gotoPrevDiff();
        });

        nextButton.addActionListener((ActionEvent e) -> {
            if (diffResult == null)
                return;
            gotoNextDiff();
        });
    }

    private void gotoNextDiff(){
        try {
            Interval visibleLines = UIUtils.getVisibleLines(scrollPane.getViewport(), textPane);
            DiffInterval diffInterval = diffResult.getIntervalAfter(visibleLines.getStart(), isFirst);
            if (diffInterval == null)
                return;

            int end = diffInterval.getInterval(isFirst).getEnd();
            if (visibleLines.getStart() == end) {
                diffInterval = diffResult.getIntervalAfter(end + 1, isFirst);
                if (diffInterval == null)
                    return;
                end = diffInterval.getInterval(isFirst).getEnd();
            }
            int targetLine = end + 1;
            if (targetLine < diffResult.getLineNo(isFirst))
                UIUtils.showLine(scrollPane.getViewport(), textPane, targetLine - 1);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void gotoPrevDiff() {
        try {
            Interval visibleLines = UIUtils.getVisibleLines(scrollPane.getViewport(), textPane);
            DiffInterval diffInterval = diffResult.getIntervalBefore(visibleLines.getStart(), isFirst);
            if (diffInterval == null)
                return;
            int start = diffInterval.getInterval(isFirst).getStart() - 1;

            if (start < 0)
                return;

            diffInterval = diffResult.getIntervalBefore(start, isFirst);

            int line;

            if (diffInterval == null) {
                line = 0;
            } else {
                line = diffInterval.getInterval(isFirst).getEnd();
            }
            UIUtils.showLine(scrollPane.getViewport(), textPane, line);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
    }
}
