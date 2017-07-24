package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.diff.DiffInterval;
import com.github.vedunz.difftool.diff.Interval;
import com.sun.istack.internal.NotNull;
import com.github.vedunz.difftool.diff.DiffResult;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

/**
 * Created by vedun on 22.07.2017.
 */
public class HighlightManager implements DiffConsumer {

    public static final String MAIN_STYLE_NAME = "MainStyle";
    public static final String SAME_STYLE_NAME = "SameStyle1";
    public static final String SAME_STYLE_NAME_2 = "SameStyle2";
    public static final String DIFF_STYLE_NAME = "DiffStyle";

    private JTextPane firstEditor;
    private JTextPane secondEditor;
    private StyleContext styleContext;

    public void createDocumentStyles() {
        styleContext = new StyleContext();
        Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);

        Style mainStyle = styleContext.addStyle(MAIN_STYLE_NAME, defaultStyle);
        StyleConstants.setFontFamily(mainStyle, "Monospaced");
        StyleConstants.setFontSize(mainStyle, 14);

        Style sameStyle = styleContext.addStyle(SAME_STYLE_NAME, defaultStyle);
        StyleConstants.setFontFamily(sameStyle, "Monospaced");
        StyleConstants.setBackground(sameStyle, new Color(0xD0,0xFF,0xD0));

        Style sameStyle2 = styleContext.addStyle(SAME_STYLE_NAME_2, defaultStyle);
        StyleConstants.setFontFamily(sameStyle2, "Monospaced");
        StyleConstants.setBackground(sameStyle2, new Color(0xA0,0xFF,0xA0));


        Style diffStyle = styleContext.addStyle(DIFF_STYLE_NAME, defaultStyle);
        StyleConstants.setFontFamily(diffStyle, "Monospaced");
        StyleConstants.setBackground(diffStyle, new Color(0xFF, 0xD0, 0xD0));
    }

    public HighlightManager(JTextPane firstEditor, JTextPane secondEditor) {
        this.firstEditor = firstEditor;
        this.secondEditor = secondEditor;
        createDocumentStyles();
    }

    public void updateHighlight(@NotNull DiffResult intervals) {
        updateHighlightImpl(intervals);
    }

    private void changeStyleInRange(JTextPane textPane, int start, int end, Style style) {

        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(start, end - start + 1, style, true);
    }

    private void updateHighlightImpl(DiffResult diffResult) {
        Element firstRootElement = firstEditor.getDocument().getDefaultRootElement();
        Element secondRootElement = secondEditor.getDocument().getDefaultRootElement();
        int firstLineNo = diffResult.getFirstLineNo();
        int secondLineNo = diffResult.getSecondLineNo();
        int firstCurLine = 0, secondCurLine = 0;
        List<DiffInterval> intervals = diffResult.getIntervals();
        boolean isOdd = false;

        for (DiffInterval diffInterval : intervals) {
            isOdd = !isOdd;
            Interval firstInterval = diffInterval.getFirstInterval();
            Interval secondInterval = diffInterval.getSecondInterval();

            changeStyleForInterval(firstRootElement, firstInterval, firstCurLine, firstEditor, isOdd);
            changeStyleForInterval(secondRootElement, secondInterval, secondCurLine, secondEditor, isOdd);

            firstCurLine = firstInterval.getEnd() + 1;
            secondCurLine =  secondInterval.getEnd() + 1;
        }

        if (firstCurLine < firstLineNo) {
            changeStyleInRange(firstEditor, firstRootElement.getElement(firstCurLine).getStartOffset(),
                    firstRootElement.getElement(firstLineNo - 1).getEndOffset(), styleContext.getStyle(DIFF_STYLE_NAME));
        }
        if (secondCurLine < secondLineNo) {
            changeStyleInRange(secondEditor, secondRootElement.getElement(secondCurLine).getStartOffset(),
                    secondRootElement.getElement(secondLineNo - 1).getEndOffset(), styleContext.getStyle(DIFF_STYLE_NAME));
        }
    }

    private void changeStyleForInterval(Element rootElement, Interval interval, int curPos, JTextPane editor, boolean isOdd) {
        if (curPos < interval.getStart()) {
            int start = rootElement.getElement(curPos).getStartOffset();
            int end = rootElement.getElement(interval.getStart() - 1).getEndOffset();
            changeStyleInRange(editor, start, end, styleContext.getStyle(DIFF_STYLE_NAME));
        }

        int start = rootElement.getElement(interval.getStart()).getStartOffset();
        int end = rootElement.getElement(interval.getEnd()).getEndOffset();
        changeStyleInRange(editor, start, end, styleContext.getStyle(isOdd ? SAME_STYLE_NAME : SAME_STYLE_NAME_2));
    }

    @Override
    public void updateDiffResult(DiffResult diffResult) {
        if (diffResult != null)
            updateHighlightImpl(diffResult);
    }
}
