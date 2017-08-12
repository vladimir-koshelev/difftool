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
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by vedun on 22.07.2017.
 */
public class HighlightManager implements DiffConsumer, LineDiffConsumer {

    private final static boolean ENABLE_COLORS_IN_LINE_PANEL = false;

    private static final int CHUNK_SIZE = 128;
    private final JTextPane firstEditor;
    private final JTextPane secondEditor;
    private final JScrollPane firstScrollPane;
    private final JScrollPane secondScrollPane;
    private final StyleContext styleContext = StyleManager.getStyleContext();
    private DiffResult diffResult;
    private final LineDiffController lineDiffController;
    private final Map<Integer, Integer> firstLines2secondLines = new HashMap<>();
    private final Map<Integer, Integer> secondLines2firstLines = new HashMap<>();
    private final Set<Integer> linesWithInLineDiffFirst = new HashSet<>();
    private final Set<Integer> linesWithInLineDiffSecond = new HashSet<>();

    private final Set<Integer> firstRequestedChunks = new HashSet<>();
    private final Set<Integer> secondRequestedChunks  = new HashSet<>();
    private final Map<Integer, Color> firstLineColorCache = new HashMap<>();
    private final Map<Integer, Color> secondLineColorCache = new HashMap<>();


    public HighlightManager(DiffPanel firstDiffPanel, DiffPanel secondDiffPanel,
                            LineDiffController lineDiffController) {
        this.firstEditor = firstDiffPanel.getEditor();
        this.secondEditor = secondDiffPanel.getEditor();
        this.firstScrollPane = firstDiffPanel.getScrollPane();
        this.secondScrollPane = secondDiffPanel.getScrollPane();
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
        if (ENABLE_COLORS_IN_LINE_PANEL) {
            firstDiffPanel.getLinePanel().setColorProvider(getFirstBackgroundColorProvider());
            secondDiffPanel.getLinePanel().setColorProvider(getSecondBackgroundColorProvider());
        }
    }

    @Override
    public void updateDiffResult(DiffResult diffResult) {
        this.diffResult = diffResult;
        firstRequestedChunks.clear();
        secondRequestedChunks.clear();
        if (diffResult != null) {
            firstLineColorCache.clear();
            secondLineColorCache.clear();
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

    private void requestLinesInVisibleAreaFirst() throws BadLocationException {
        Interval visibleLines = UIUtils.getVisibleLines(firstScrollPane.getViewport(), firstEditor);
        int firstChunk = lineToChunk(visibleLines.getStart());
        int lastChunk = lineToChunk(visibleLines.getEnd());
        int maxChuck = lineToChunk(diffResult.getFirstSize() - 1);
        if (firstChunk - 1 >= 0)
            requestChunkFirst(firstChunk - 1);
        requestChunkFirst(firstChunk);
        requestChunkFirst(lastChunk);
        if (lastChunk + 1 <= maxChuck)
            requestChunkFirst(lastChunk + 1);

    }

    private void requestLinesInVisibleAreaSecond() throws BadLocationException {
        Interval visibleLines = UIUtils.getVisibleLines(secondScrollPane.getViewport(), secondEditor);
        int firstChunk = lineToChunk(visibleLines.getStart());
        int lastChunk = lineToChunk(visibleLines.getEnd());
        int maxChuck = lineToChunk(diffResult.getSecondSize() - 1);
        if (firstChunk - 1 >= 0)
            requestChunkSecond(firstChunk - 1);
        requestChunkSecond(firstChunk);
        requestChunkSecond(lastChunk);
        if (lastChunk + 1 <= maxChuck)
            requestChunkSecond(lastChunk + 1);
    }


    private void requestChunkFirst(int chunk) throws BadLocationException {
        if (firstRequestedChunks.contains(chunk))
            return;
        firstRequestedChunks.add(chunk);
        requestLinesInIntervalFirst(chunkToInterval(chunk));
    }

    private void requestChunkSecond(int chunk) throws BadLocationException {
        if (secondRequestedChunks.contains(chunk))
            return;
        secondRequestedChunks.add(chunk);
        requestLinesInIntervalSecond(chunkToInterval(chunk));
    }

    private void requestLinesInIntervalSecond(final Interval visibleLines) throws BadLocationException {
        int end = Math.min(diffResult.getSecondSize() - 1, visibleLines.getEnd());
        for (int secondLine = visibleLines.getStart(); secondLine <= end; ++secondLine) {
            if (secondLines2firstLines.containsKey(secondLine)) {
                int firstLine = secondLines2firstLines.get(secondLine);
                lineDiffController.requestDiff(getLineText(firstEditor, firstLine),
                        getLineText(secondEditor, secondLine), firstLine, secondLine);
                removeFromLinesForLineDiff(firstLine, secondLine);
                Style style = styleContext.getStyle(StyleManager.ADDED_STYLE_NAME);
                secondLineColorCache.put(secondLine, styleContext.getBackground(style));
            } else if (!linesWithInLineDiffSecond.contains(secondLine)) {
                if (diffResult.isLineInSame(secondLine, false)) {
                    Style style = styleContext.getStyle(StyleManager.MAIN_STYLE_NAME);
                    changeStyleForLine(secondEditor, secondLine, style);
                    secondLineColorCache.put(secondLine, styleContext.getBackground(style));
                }
                else {
                    Style style = styleContext.getStyle(StyleManager.ADDED_STYLE_NAME);
                    changeStyleForLine(secondEditor, secondLine, style);
                    secondLineColorCache.put(secondLine, styleContext.getBackground(style));
                }
            } else {
                Style style = styleContext.getStyle(StyleManager.ADDED_STYLE_NAME);
                secondLineColorCache.put(secondLine, styleContext.getBackground(style));
            }
        }
    }

    private void requestLinesInIntervalFirst(final Interval visibleLines) throws BadLocationException {
        int end = Math.min(diffResult.getFirstSize() - 1, visibleLines.getEnd());
        for (int firstLine = visibleLines.getStart(); firstLine <= end; ++firstLine) {
            if (firstLines2secondLines.containsKey(firstLine)) {
                int secondLine = firstLines2secondLines.get(firstLine);
                lineDiffController.requestDiff(getLineText(firstEditor, firstLine),
                        getLineText(secondEditor, secondLine), firstLine, secondLine);
                removeFromLinesForLineDiff(firstLine, secondLine);
                Style style = styleContext.getStyle(StyleManager.REMOVED_STYLE_NAME);
                firstLineColorCache.put(firstLine, styleContext.getBackground(style));
            } else if (!linesWithInLineDiffFirst.contains(firstLine)){
                if (diffResult.isLineInSame(firstLine, true)) {
                    Style style = styleContext.getStyle(StyleManager.MAIN_STYLE_NAME);
                    changeStyleForLine(firstEditor, firstLine, style);
                    firstLineColorCache.put(firstLine, styleContext.getBackground(style));
                }
                else {
                    Style style = styleContext.getStyle(StyleManager.REMOVED_STYLE_NAME);
                    changeStyleForLine(firstEditor, firstLine, style);
                    firstLineColorCache.put(firstLine, styleContext.getBackground(style));
                }
            } else {
                Style style = styleContext.getStyle(StyleManager.REMOVED_STYLE_NAME);
                firstLineColorCache.put(firstLine, styleContext.getBackground(style));
            }
        }
    }

    private void changeStyleForLine(final JTextPane editor, final int line, final Style style) {
        Element element = editor.getDocument().getDefaultRootElement().getElement(line);
        changeStyleInRange(editor, element.getStartOffset(), element.getEndOffset() - 1, style);
    }

    private void changeStyleInRange(JTextPane textPane, int start, int end, Style style) {

        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(start, end - start + 1, style, true);
    }

    private void calculateLinesForDiff(DiffResult diffResult) {
        firstLines2secondLines.clear();
        secondLines2firstLines.clear();
        linesWithInLineDiffFirst.clear();
        linesWithInLineDiffSecond.clear();
        List<DiffInterval> intervals = diffResult.getIntervals();
        int firstPos = 0, secondPos = 0;
        int curInterval = 0;
        while (firstPos < diffResult.getFirstSize() && secondPos < diffResult.getSecondSize()) {
            DiffInterval interval = (curInterval < intervals.size()) ? intervals.get(curInterval) : null;
            if (interval == null || (interval.getFirstInterval().isLineBefore(firstPos) &&
                    interval.getSecondInterval().isLineBefore(secondPos))) {
                firstLines2secondLines.put(firstPos, secondPos);
                secondLines2firstLines.put(secondPos, firstPos);
                linesWithInLineDiffFirst.add(firstPos);
                linesWithInLineDiffSecond.add(secondPos);
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
                    styleContext.getStyle(StyleManager.REMOVED_STYLE_NAME_BOLD));

        if (secondStartOffset <= secondEndOffset)
            changeStyleInRange(secondEditor, secondCurrentOffset, secondEndOffset,
                    styleContext.getStyle(StyleManager.ADDED_STYLE_NAME_BOLD));
    }

    private void changeStyleForIntervalInLine(int startOffset, int currentOffset, Interval interval, JTextPane editor,
                                              boolean isFirst) {
        int start = startOffset + interval.getStart();
        int end = startOffset + interval.getEnd();
        if (currentOffset < start) {
            changeStyleInRange(editor, currentOffset, start - 1,
                    styleContext.getStyle(isFirst ? StyleManager.REMOVED_STYLE_NAME_BOLD : StyleManager.ADDED_STYLE_NAME_BOLD));
        }
        changeStyleInRange(editor, start, end,
                styleContext.getStyle(isFirst ? StyleManager.REMOVED_STYLE_NAME : StyleManager.ADDED_STYLE_NAME));

    }
    
    private int lineToChunk(int line) {
        return line / CHUNK_SIZE;
    }
    
    private Interval chunkToInterval(int chunk) {
        return new Interval(chunk * CHUNK_SIZE, chunk * CHUNK_SIZE + CHUNK_SIZE - 1);
    }

    private BackgroundColorProvider getFirstBackgroundColorProvider() {

        return line -> {
            Color color = firstLineColorCache.get(line);
            if (color != null)
                return color;
            else {
                color = styleContext.getBackground(styleContext.getStyle(
                        (diffResult != null) && !diffResult.isLineInSame(line, true)
                                ? StyleManager.REMOVED_STYLE_NAME : StyleManager.MAIN_STYLE_NAME));
                firstLineColorCache.put(line, color);
            }
            return color;
        };
    }

    private BackgroundColorProvider getSecondBackgroundColorProvider() {
        return line -> {
            Color color = secondLineColorCache.get(line);
            if (color != null)
                return color;
            else {
                color = styleContext.getBackground(styleContext.getStyle(
                        (diffResult != null) && !diffResult.isLineInSame(line, false)
                                ? StyleManager.ADDED_STYLE_NAME : StyleManager.MAIN_STYLE_NAME));
                secondLineColorCache.put(line, color);
            }
            return color;
        };
    }

}
