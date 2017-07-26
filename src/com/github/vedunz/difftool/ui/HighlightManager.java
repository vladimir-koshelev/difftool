package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffConsumer;
import com.github.vedunz.difftool.control.LineDiffConsumer;
import com.github.vedunz.difftool.control.LineDiffController;
import com.github.vedunz.difftool.diff.DiffInterval;
import com.github.vedunz.difftool.diff.Interval;
import com.github.vedunz.difftool.diff.DiffResult;
import com.github.vedunz.difftool.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by vedun on 22.07.2017.
 */
public class HighlightManager implements DiffConsumer, LineDiffConsumer {

    public static final String MAIN_STYLE_NAME = "MainStyle";
    public static final String SAME_STYLE_NAME = "SameStyle1";
    public static final String SAME_STYLE_NAME_BOLD = "SameStyle2";
    public static final String DIFF_STYLE_NAME = "DiffStyle";
    public static final String DIFF_STYLE_NAME_BOLD = "DiffStyle2";

    private JTextPane firstEditor;
    private JTextPane secondEditor;
    private JScrollPane firstScrollPane;
    private JScrollPane secondScrollPane;
    private StyleContext styleContext;
    private DiffResult diffResult;
    private LineDiffController lineDiffController;
    private Map<Integer, Integer> firstLinesForLineDiff = new HashMap<>();
    private Map<Integer, Integer> secondLinesForLineDiff = new HashMap<>();

    private void removeFromLinesForLineDiff(int first, int second) {
        firstLinesForLineDiff.remove(first);
        secondLinesForLineDiff.remove(second);
    }

    private String getLineText(JTextPane editor, int line) throws BadLocationException {
        Element rootElement = editor.getDocument().getDefaultRootElement();
        Element element = rootElement.getElement(line);
        return editor.getDocument().getText(element.getStartOffset(), element.getEndOffset() - element.getStartOffset() - 1);
    }

    private ChangeListener changeListener = e -> {
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

    private void requestLinesInVisibleAreaSecond() throws BadLocationException {
        Interval visibleLines = UIUtils.getVisibleLines(secondScrollPane.getViewport(), secondEditor);
        for (int secondLine = visibleLines.getStart(); secondLine <= visibleLines.getEnd(); ++secondLine) {
            if (secondLinesForLineDiff.containsKey(secondLine)) {
                int firstLine = secondLinesForLineDiff.get(secondLine);
                lineDiffController.requestDiff(getLineText(firstEditor, firstLine),
                        getLineText(secondEditor,secondLine), firstLine, secondLine);
                removeFromLinesForLineDiff(firstLine, secondLine);
            }
        }
    }

    private void requestLinesInVisibleAreaFirst() throws BadLocationException {
        Interval visibleLines = UIUtils.getVisibleLines(firstScrollPane.getViewport(), firstEditor);
        for (int firstLine = visibleLines.getStart(); firstLine <= visibleLines.getEnd(); ++firstLine) {
            if (firstLinesForLineDiff.containsKey(firstLine)) {
                int secondLine = firstLinesForLineDiff.get(firstLine);
                lineDiffController.requestDiff(getLineText(firstEditor, firstLine),
                        getLineText(secondEditor, secondLine), firstLine, secondLine);
                removeFromLinesForLineDiff(firstLine, secondLine);
            }
        }
    }

    public void createDocumentStyles() {
        styleContext = new StyleContext();
        Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);

        Style mainStyle = styleContext.addStyle(MAIN_STYLE_NAME, defaultStyle);
        StyleConstants.setFontFamily(mainStyle, "Monospaced");
        StyleConstants.setFontSize(mainStyle, 12);

        Style sameStyle = styleContext.addStyle(SAME_STYLE_NAME, defaultStyle);
        StyleConstants.setFontFamily(sameStyle, "Monospaced");
        StyleConstants.setBackground(sameStyle, new Color(0xD0,0xFF,0xD0));
        StyleConstants.setFontSize(sameStyle, 12);

        Style sameStyle2 = styleContext.addStyle(SAME_STYLE_NAME_BOLD, defaultStyle);
        StyleConstants.setFontFamily(sameStyle2, "Monospaced");
        StyleConstants.setBackground(sameStyle2, new Color(0x90,0xFF,0xA0));
        StyleConstants.setFontSize(sameStyle2, 12);

        Style diffStyle = styleContext.addStyle(DIFF_STYLE_NAME, defaultStyle);
        StyleConstants.setFontFamily(diffStyle, "Monospaced");
        StyleConstants.setBackground(diffStyle, new Color(0xFF, 0xD0, 0xD0));
        StyleConstants.setFontSize(diffStyle, 12);

        Style diffStyle2 = styleContext.addStyle(DIFF_STYLE_NAME_BOLD, defaultStyle);
        StyleConstants.setFontFamily(diffStyle2, "Monospaced");
        StyleConstants.setBackground(diffStyle2, new Color(0xFF, 0xA0, 0x90));
        StyleConstants.setFontSize(diffStyle2, 12);
    }

    public HighlightManager(JTextPane firstEditor, JTextPane secondEditor,
                            JScrollPane firstScrollPane, JScrollPane secondScrollPane,
                            LineDiffController lineDiffController) {
        this.firstEditor = firstEditor;
        this.secondEditor = secondEditor;
        this.firstScrollPane = firstScrollPane;
        this.secondScrollPane = secondScrollPane;
        this.lineDiffController = lineDiffController;
        firstScrollPane.getViewport().addChangeListener(changeListener);
        secondScrollPane.getViewport().addChangeListener(changeListener);
        createDocumentStyles();
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
            secondCurLine =  secondInterval.getEnd() + 1;
        }

        if (firstCurLine < firstLineNo) {
            changeStyleInRange(firstEditor, firstRootElement.getElement(firstCurLine).getStartOffset(),
                    firstRootElement.getElement(firstLineNo - 1).getEndOffset(), styleContext.getStyle(DIFF_STYLE_NAME));
        }
        if (secondCurLine < secondLineNo) {
            changeStyleInRange(secondEditor, secondRootElement.getElement(secondCurLine).getStartOffset(),
                    secondRootElement.getElement(secondLineNo - 1).getEndOffset(), styleContext.getStyle(SAME_STYLE_NAME));
        }
    }

    private void changeStyleForInterval(Element rootElement, Interval interval, int curPos, JTextPane editor, boolean isFirst) {
        if (curPos < interval.getStart()) {
            int start = rootElement.getElement(curPos).getStartOffset();
            int end = rootElement.getElement(interval.getStart() - 1).getEndOffset();
            changeStyleInRange(editor, start, end, styleContext.getStyle(isFirst ? DIFF_STYLE_NAME : SAME_STYLE_NAME));
        }

        int start = rootElement.getElement(interval.getStart()).getStartOffset();
        int end = rootElement.getElement(interval.getEnd()).getEndOffset();
        changeStyleInRange(editor, start, end, styleContext.getStyle(MAIN_STYLE_NAME));
    }

    @Override
    public void updateDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
        if (diffResult != null) {
            updateHighlightImpl(diffResult);
            calculateLinesForDiff(diffResult);
        }
    }

    private void calculateLinesForDiff(DiffResult diffResult) {
        firstLinesForLineDiff.clear();
        secondLinesForLineDiff.clear();
        List<DiffInterval> intervals = diffResult.getIntervals();
        int firstPos = 0, secondPos = 0;
        int curInterval = 0;
        while (firstPos < diffResult.getFirstSize() && secondPos < diffResult.getSecondSize()) {
            DiffInterval interval = (curInterval < intervals.size()) ? intervals.get(curInterval) : null;
            if (interval == null || (interval.getFirstInterval().isLineBefore(firstPos) &&
                    interval.getSecondInterval().isLineBefore(secondPos))) {
                firstLinesForLineDiff.put(firstPos, secondPos);
                secondLinesForLineDiff.put(secondPos, firstPos);
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

    @Override
    public void updateDiffResult(DiffResult diffResult, int firstLineNo, int secondLineNo) {
        if (diffResult != null)
            updateHighlightInLineImpl(diffResult, firstLineNo, secondLineNo);

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
                    styleContext.getStyle(DIFF_STYLE_NAME_BOLD));

        if (secondStartOffset <= secondEndOffset)
            changeStyleInRange(secondEditor, secondCurrentOffset, secondEndOffset,
                    styleContext.getStyle(SAME_STYLE_NAME_BOLD));
    }

    private void changeStyleForIntervalInLine(int startOffset, int currentOffset, Interval interval, JTextPane editor,
                                              boolean isFirst) {
        int start = startOffset + interval.getStart();
        int end = startOffset + interval.getEnd();
        if (currentOffset < start) {
            changeStyleInRange(editor, currentOffset, start - 1,
                    styleContext.getStyle(isFirst ? DIFF_STYLE_NAME_BOLD: SAME_STYLE_NAME_BOLD));
        }
        changeStyleInRange(editor, start, end,
                styleContext.getStyle(isFirst ? DIFF_STYLE_NAME : SAME_STYLE_NAME));

    }
}
