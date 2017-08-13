package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffConsumer;
import com.github.vedunz.difftool.diff.DiffInterval;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.Interval;
import com.github.vedunz.difftool.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import java.awt.*;

/**
 * Created by vedun on 23.07.2017.
 */
public class ScrollManager implements DiffConsumer {

    private final JTextPane firstEditor;
    private final JTextPane secondEditor;

    private final JScrollPane firstScrollPane;
    private final JScrollPane secondScrollPane;

    private DiffResult diffResult;

    private boolean ignoreUpdate = false;

    public ScrollManager(DiffPanel firstDiffPanel, DiffPanel secondDiffPanel) {
        this.firstEditor = firstDiffPanel.getEditor();
        this.secondEditor = secondDiffPanel.getEditor();
        this.firstScrollPane = firstDiffPanel.getScrollPane();
        this.secondScrollPane = secondDiffPanel.getScrollPane();
        final JViewport secondViewport = secondScrollPane.getViewport();
        final JViewport firstViewport = firstScrollPane.getViewport();
        final ChangeListener changeListener = e -> {
            if (diffResult == null || ignoreUpdate)
                return;
            try {
                ignoreUpdate = true;
                if (firstScrollPane.getViewport() == e.getSource()) {
                    syncScrollPanes(firstEditor, secondEditor, firstViewport, secondViewport, true);
                } else {
                    syncScrollPanes(secondEditor, firstEditor, secondViewport, firstViewport, false);
                }
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            } finally {
                ignoreUpdate = false;
            }
        };
        firstViewport.addChangeListener(changeListener);
        secondViewport.addChangeListener(changeListener);
    }

    @Override
    public void updateDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
    }


    private void syncScrollPanes(final JTextPane editor,
                                 final JTextPane anotherEditor,
                                 final JViewport viewport,
                                 final JViewport anotherViewport,
                                 boolean isFirst
    ) throws BadLocationException {
        Interval lines = UIUtils.getVisibleLines(viewport, editor);
        int yOffset = viewport.getViewPosition().y;
        int offset = editor.viewToModel(new Point(0, yOffset));
        int line = UIUtils.offsetToLine(offset, editor);

        DiffInterval diffInterval = diffResult.getIntervalAfter(line, isFirst);
        if (diffInterval == null)
            return;
        if (lines.isLineAfter(diffInterval.getInterval(isFirst).getStart()))
            return;
        int firstOffset = UIUtils.lineToOffset(diffInterval.getInterval(isFirst).getStart(), editor);
        int secondOffset = UIUtils.lineToOffset(diffInterval.getInterval(!isFirst).getStart(), anotherEditor);
        int firstYPos = editor.modelToView(firstOffset).y;
        int secondYPos = anotherEditor.modelToView(secondOffset).y;
        int newSecondPos = secondYPos - (firstYPos - yOffset);
        if (newSecondPos != anotherViewport.getViewPosition().y) {
            if (newSecondPos + anotherViewport.getViewRect().height < anotherEditor.getHeight() &&
                    newSecondPos >= 0) {
                anotherViewport.setViewPosition(new Point(
                        anotherViewport.getViewPosition().x, newSecondPos
                ));
                editor.paintImmediately(viewport.getViewRect());
                anotherEditor.paintImmediately(anotherViewport.getViewRect());
            }
        }
    }


}
