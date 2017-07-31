package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffConsumer;
import com.github.vedunz.difftool.diff.DiffInterval;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.Interval;
import com.github.vedunz.difftool.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

/**
 * Created by vedun on 23.07.2017.
 */
public class ScrollManager implements DiffConsumer {

    private final JTextPane firstEditor;
    private final JTextPane secondEditor;

    private final JScrollPane firstScrollPane;
    private final JScrollPane secondScrollPane;

    private DiffResult diffResult;

    public ScrollManager(DiffPanel firstDiffPanel, DiffPanel secondDiffPanel) {
        this.firstEditor = firstDiffPanel.getEditor();
        this.secondEditor = secondDiffPanel.getEditor();
        this.firstScrollPane = firstDiffPanel.getScrollPane();
        this.secondScrollPane = secondDiffPanel.getScrollPane();
        final ChangeListener changeListener = e -> {
            if (diffResult == null)
                return;
            try {
                if (firstScrollPane.getViewport() == e.getSource()) {
                    Interval lines = UIUtils.getVisibleLines(firstScrollPane.getViewport(), firstEditor);
                    if (isLineNearTheEnd(lines.getEnd(), firstEditor))
                        return;

                    DiffInterval diffInterval = diffResult.getIntervalAfter(lines.getStart(), true);
                    if (diffInterval == null)
                        return;
                    if (lines.isLineAfter(diffInterval.getBeginFirst()))
                        return;
                    int delta = lines.getStart() - diffInterval.getBeginFirst();
                    int firstVisibleLineInSecondDiffInterval = diffInterval.getBeginSecond() + delta;

                    UIUtils.showLine(secondScrollPane.getViewport(), secondEditor, firstVisibleLineInSecondDiffInterval);
                    UIUtils.showLine(firstScrollPane.getViewport(), firstEditor, lines.getStart());
                } else {
                    Interval lines = UIUtils.getVisibleLines(secondScrollPane.getViewport(), secondEditor);
                    if (isLineNearTheEnd(lines.getEnd(), secondEditor))
                        return;

                    DiffInterval diffInterval = diffResult.getIntervalAfter(lines.getStart(), false);
                    if (diffInterval == null)
                        return;
                    if (lines.isLineAfter(diffInterval.getBeginSecond()))
                        return;
                    int delta = lines.getStart() - diffInterval.getBeginSecond();
                    int firstVisibleLineInFirstDiffInterval = diffInterval.getBeginFirst() + delta;
                    UIUtils.showLine(firstScrollPane.getViewport(), firstEditor, firstVisibleLineInFirstDiffInterval);
                    UIUtils.showLine(secondScrollPane.getViewport(), secondEditor, lines.getStart());
                }
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        };
        firstScrollPane.getViewport().addChangeListener(changeListener);
        secondScrollPane.getViewport().addChangeListener(changeListener);
    }

    @Override
    public void updateDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
    }

    private boolean isLineNearTheEnd(int line, JTextPane editor) {
        Element element = editor.getDocument().getDefaultRootElement();
        return element.getElementCount() - line < 3;
    }

}
