package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffConsumer;
import com.github.vedunz.difftool.control.LineDiffConsumer;
import com.github.vedunz.difftool.control.LineDiffController;
import com.github.vedunz.difftool.diff.DiffInterval;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.diff.Interval;
import com.github.vedunz.difftool.ui.util.StyleManager;
import com.github.vedunz.difftool.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.util.*;

/**
 * Created by vedun on 22.07.2017.
 */
public class HighlightManager implements DiffConsumer, LineDiffConsumer {

    private final JTextPane firstEditor;
    private final JTextPane secondEditor;
    private final JScrollPane firstScrollPane;
    private final JScrollPane secondScrollPane;
    private final LinePanel firstLinePanel;
    private final LinePanel secondLinePanel;
    private final StyleContext styleContext = StyleManager.getStyleContext();
    private DiffResult diffResult;
    private final LineDiffController lineDiffController;
    private final Map<Integer, Integer> firstLines2secondLines = new HashMap<>();
    private final Map<Integer, Integer> secondLines2firstLines = new HashMap<>();

    public HighlightManager(DiffPanel firstDiffPanel, DiffPanel secondDiffPanel,
                            LineDiffController lineDiffController) {
        this.firstEditor = firstDiffPanel.getEditor();
        this.secondEditor = secondDiffPanel.getEditor();
        this.firstScrollPane = firstDiffPanel.getScrollPane();
        this.secondScrollPane = secondDiffPanel.getScrollPane();
        this.firstLinePanel = firstDiffPanel.getLinePanel();
        this.secondLinePanel = secondDiffPanel.getLinePanel();
        this.lineDiffController = lineDiffController;
        final ChangeListener changeListener = e -> {
            try {
                if (diffResult == null)
                    return;
                if (e.getSource() == firstScrollPane.getViewport()) {
                    requestLinesInVisibleAreaFirst();
                } else {
                    requestLinesInVisibleAreaSecond();
                }
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        };
        firstScrollPane.getViewport().addChangeListener(changeListener);
        secondScrollPane.getViewport().addChangeListener(changeListener);
        firstDiffPanel.getLinePanel().setColorProvider(getFirstBackgroundColorProvider());
        secondDiffPanel.getLinePanel().setColorProvider(getSecondBackgroundColorProvider());
    }

    @Override
    public void updateDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
        if (diffResult != null) {
            updateHighlightImpl(diffResult);
            firstLinePanel.repaint();
            secondLinePanel.repaint();
            calculateLinesForDiff(diffResult);
        } else {
            firstLines2secondLines.clear();
            secondLines2firstLines.clear();
        }
    }

    @Override
    public void updateDiffResult(DiffResult diffResult, int firstLineNo, int secondLineNo) {
        if (diffResult != null)
            updateHighlightInLineImpl(diffResult, firstLineNo, secondLineNo);
    }

    private void removeFromLinesForLineDiff(int first, int second) {
        firstLines2secondLines.remove(first);
        secondLines2firstLines.remove(second);
    }

    private String getLineText(JTextPane editor, int line) throws BadLocationException {
        Element rootElement = editor.getDocument().getDefaultRootElement();
        Element element = rootElement.getElement(line);
        return editor.getDocument().getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset() - 1);
    }

    private void requestLinesInVisibleAreaSecond() throws BadLocationException {
        Interval visibleLines = UIUtils.getVisibleLines(secondScrollPane.getViewport(), secondEditor);
        for (int secondLine = visibleLines.getStart(); secondLine <= visibleLines.getEnd(); ++secondLine) {
            if (secondLines2firstLines.containsKey(secondLine)) {
                int firstLine = secondLines2firstLines.get(secondLine);
                lineDiffController.requestDiff(getLineText(firstEditor, firstLine),
                        getLineText(secondEditor, secondLine), firstLine, secondLine);
                removeFromLinesForLineDiff(firstLine, secondLine);
            }
        }
    }

    private void requestLinesInVisibleAreaFirst() throws BadLocationException {
        Interval visibleLines = UIUtils.getVisibleLines(firstScrollPane.getViewport(), firstEditor);
        for (int firstLine = visibleLines.getStart(); firstLine <= visibleLines.getEnd(); ++firstLine) {
            if (firstLines2secondLines.containsKey(firstLine)) {
                int secondLine = firstLines2secondLines.get(firstLine);
                lineDiffController.requestDiff(getLineText(firstEditor, firstLine),
                        getLineText(secondEditor, secondLine), firstLine, secondLine);
                removeFromLinesForLineDiff(firstLine, secondLine);
            }
        }
    }

    private void changeStyleInRange(JTextPane textPane, int start, int end, Style style) {

        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(start, end - start + 1, style, true);
    }

    private void updateHighlightImpl(DiffResult diffResult) {
        Element firstRootElement = firstEditor.getDocument().getDefaultRootElement();
        Element secondRootElement = secondEditor.getDocument().getDefaultRootElement();
        int firstLineNo = diffResult.getFirstSize();
        int secondLineNo = diffResult.getSecondSize();
        int firstCurLine = 0, secondCurLine = 0;
        List<DiffInterval> intervals = diffResult.getIntervals();
        boolean isOdd = false;

        for (DiffInterval diffInterval : intervals) {
            isOdd = !isOdd;
            Interval firstInterval = diffInterval.getFirstInterval();
            Interval secondInterval = diffInterval.getSecondInterval();

            changeStyleForInterval(firstRootElement, firstInterval, firstCurLine, firstEditor, true);
            changeStyleForInterval(secondRootElement, secondInterval, secondCurLine, secondEditor, false);

            firstCurLine = firstInterval.getEnd() + 1;
            secondCurLine = secondInterval.getEnd() + 1;
        }

        if (firstCurLine < firstLineNo) {
            changeStyleInRange(firstEditor, firstRootElement.getElement(firstCurLine).getStartOffset(),
                    firstRootElement.getElement(firstLineNo - 1).getEndOffset(), styleContext.getStyle(StyleManager.DIFF_STYLE_NAME));
        }
        if (secondCurLine < secondLineNo) {
            changeStyleInRange(secondEditor, secondRootElement.getElement(secondCurLine).getStartOffset(),
                    secondRootElement.getElement(secondLineNo - 1).getEndOffset(), styleContext.getStyle(StyleManager.SAME_STYLE_NAME));
        }
    }

    private void changeStyleForInterval(Element rootElement, Interval interval, int curPos, JTextPane editor, boolean isFirst) {
        if (curPos < interval.getStart()) {
            int start = rootElement.getElement(curPos).getStartOffset();
            int end = rootElement.getElement(interval.getStart() - 1).getEndOffset();
            changeStyleInRange(editor, start, end, styleContext.getStyle(isFirst ? StyleManager.DIFF_STYLE_NAME : StyleManager.SAME_STYLE_NAME));
        }

        int start = rootElement.getElement(interval.getStart()).getStartOffset();
        int end = rootElement.getElement(interval.getEnd()).getEndOffset();
        changeStyleInRange(editor, start, end, styleContext.getStyle(StyleManager.MAIN_STYLE_NAME));
    }

    private void calculateLinesForDiff(DiffResult diffResult) {
        firstLines2secondLines.clear();
        secondLines2firstLines.clear();
        List<DiffInterval> intervals = diffResult.getIntervals();
        int firstPos = 0, secondPos = 0;
        int curInterval = 0;
        while (firstPos < diffResult.getFirstSize() && secondPos < diffResult.getSecondSize()) {
            DiffInterval interval = (curInterval < intervals.size()) ? intervals.get(curInterval) : null;
            if (interval == null || (interval.getFirstInterval().isLineBefore(firstPos) &&
                    interval.getSecondInterval().isLineBefore(secondPos))) {
                firstLines2secondLines.put(firstPos, secondPos);
                secondLines2firstLines.put(secondPos, firstPos);
                firstPos++;
                secondPos++;
            } else {
                firstPos = interval.getFirstInterval().getEnd() + 1;
                secondPos = interval.getSecondInterval().getEnd() + 1;
                curInterval++;
            }
        }
        try {
            requestLinesInVisibleAreaFirst();
            requestLinesInVisibleAreaSecond();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void updateHighlightInLineImpl(DiffResult diffResult, int firstLineNo, int secondLineNo) {
        Element firstRootElement = firstEditor.getDocument().getDefaultRootElement();
        Element secondRootElement = secondEditor.getDocument().getDefaultRootElement();
        int firstStartOffset = firstRootElement.getElement(firstLineNo).getStartOffset();
        int secondStartOffset = secondRootElement.getElement(secondLineNo).getStartOffset();
        int firstEndOffset = firstStartOffset + diffResult.getFirstSize() - 1;
        int secondEndOffset = secondStartOffset + diffResult.getSecondSize() - 1;
        int firstCurrentOffset = firstStartOffset;
        int secondCurrentOffset = secondStartOffset;

        List<DiffInterval> intervals = diffResult.getIntervals();

        for (DiffInterval diffInterval : intervals) {
            Interval firstInterval = diffInterval.getFirstInterval();
            Interval secondInterval = diffInterval.getSecondInterval();

            changeStyleForIntervalInLine(firstStartOffset, firstCurrentOffset, firstInterval, firstEditor, true);
            changeStyleForIntervalInLine(secondStartOffset, secondCurrentOffset, secondInterval, secondEditor, false);

            firstCurrentOffset = firstStartOffset + firstInterval.getEnd() + 1;
            secondCurrentOffset = secondStartOffset + secondInterval.getEnd() + 1;
        }
        if (firstStartOffset <= firstEndOffset)
            changeStyleInRange(firstEditor, firstCurrentOffset, firstEndOffset,
                    styleContext.getStyle(StyleManager.DIFF_STYLE_NAME_BOLD));

        if (secondStartOffset <= secondEndOffset)
            changeStyleInRange(secondEditor, secondCurrentOffset, secondEndOffset,
                    styleContext.getStyle(StyleManager.SAME_STYLE_NAME_BOLD));
    }

    private void changeStyleForIntervalInLine(int startOffset, int currentOffset, Interval interval, JTextPane editor,
                                              boolean isFirst) {
        int start = startOffset + interval.getStart();
        int end = startOffset + interval.getEnd();
        if (currentOffset < start) {
            changeStyleInRange(editor, currentOffset, start - 1,
                    styleContext.getStyle(isFirst ? StyleManager.DIFF_STYLE_NAME_BOLD : StyleManager.SAME_STYLE_NAME_BOLD));
        }
        changeStyleInRange(editor, start, end,
                styleContext.getStyle(isFirst ? StyleManager.DIFF_STYLE_NAME : StyleManager.SAME_STYLE_NAME));

    }

    private BackgroundColorProvider getFirstBackgroundColorProvider() {

        return  line -> styleContext.getBackground(styleContext.getStyle(
                (diffResult != null) && !diffResult.isLineInSame(line,true)
                ? StyleManager.DIFF_STYLE_NAME : StyleManager.MAIN_STYLE_NAME));
    }

    private BackgroundColorProvider getSecondBackgroundColorProvider() {
        return  line -> styleContext.getBackground(styleContext.getStyle(
                (diffResult != null) && !diffResult.isLineInSame(line,false)
                ? StyleManager.SAME_STYLE_NAME : StyleManager.MAIN_STYLE_NAME));
    }

}
