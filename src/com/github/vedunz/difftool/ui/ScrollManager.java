package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.diff.DiffInterval;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.Interval;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;

/**
 * Created by vedun on 23.07.2017.
 */
public class ScrollManager {

    private JTextPane firstEditor;
    private JTextPane secondEditor;

    private JScrollPane firstScrollPane;
    private JScrollPane secondScrollPane;

    private DiffResult diffResult;

    private static Interval getVisibleLines(JViewport viewport, JTextPane jTextPane) throws BadLocationException {
      Rectangle viewRect = viewport.getViewRect();

      Point p = viewRect.getLocation();
      int startIndex = jTextPane.viewToModel(p);

      p.x += viewRect.width;
      p.y += viewRect.height;
      int endIndex = jTextPane.viewToModel(p);

      Element element = jTextPane.getDocument().getDefaultRootElement();

      return new Interval(element.getElementIndex(startIndex), element.getElementIndex(endIndex));
    }

    private static void showLine(JViewport viewport, JTextPane jTextPane, int line) throws BadLocationException {
      Element element = jTextPane.getDocument().getDefaultRootElement();
      int startOffset = element.getElement(line).getStartOffset();
      Rectangle rectangle = jTextPane.modelToView(startOffset);
      if (viewport.getViewPosition().getY() != (int) rectangle.getY()) {
          viewport.setViewPosition(new Point(0, (int) rectangle.getY()));
      }
    }

    private ChangeListener changeListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (diffResult == null)
                return;
            try {
                if (firstScrollPane.getViewport() == e.getSource()) {
                    Interval lines = getVisibleLines(firstScrollPane.getViewport(), firstEditor);
                    DiffInterval diffInterval = diffResult.getIntervalAfter(lines.getStart(), true);
                    if (diffInterval == null)
                        return;
                    int delta = lines.getStart() - diffInterval.getBeginFirst();
                    int firstVisibleLineInSecondDiffInterval = diffInterval.getBeginSecond() + delta;

                    showLine(secondScrollPane.getViewport(), secondEditor, firstVisibleLineInSecondDiffInterval);
                    showLine(firstScrollPane.getViewport(), firstEditor, lines.getStart());
                } else {
                    Interval lines = getVisibleLines(secondScrollPane.getViewport(), secondEditor);
                    DiffInterval diffInterval = diffResult.getIntervalAfter(lines.getStart(), false);
                    if (diffInterval == null)
                        return;
                    int delta = lines.getStart() - diffInterval.getBeginSecond();
                    int firstVisibleLineInFirstDiffInterval = diffInterval.getBeginFirst() + delta;
                    showLine(firstScrollPane.getViewport(), firstEditor, firstVisibleLineInFirstDiffInterval);
                    showLine(secondScrollPane.getViewport(), secondEditor, lines.getStart());
                }
            } catch (BadLocationException e1) {
                  e1.printStackTrace();
            }
        }
    };

    public void updateDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
    }

    public ScrollManager(JTextPane firstEditor, JTextPane secondEditor, JScrollPane firstScrollPane, JScrollPane secondScrollPane) {
        this.firstEditor = firstEditor;
        this.secondEditor = secondEditor;
        this.firstScrollPane = firstScrollPane;
        this.secondScrollPane = secondScrollPane;
        firstScrollPane.getViewport().addChangeListener(changeListener);
        secondScrollPane.getViewport().addChangeListener(changeListener);
    }
}
