package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.diff.Interval;
import com.github.vedunz.difftool.ui.util.StyleManager;
import com.github.vedunz.difftool.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

public class LinePanel extends JPanel {

    private final JTextPane textPane;
    private final JViewport viewport;
    private final StyleContext styleContext = StyleManager.getStyleContext();
    private final FontMetrics fontMetrics;

    private BackgroundColorProvider colorProvider = null;
    private int minWidth = 0;


    public LinePanel(JTextPane textPane, JViewport viewport) {
        this.textPane = textPane;
        this.viewport = viewport;
        Style mainStyle = styleContext.getStyle(StyleManager.MAIN_STYLE_NAME);
        Font font = styleContext.getFont(mainStyle);
        setFont(font);
        fontMetrics = getFontMetrics(font);
        minWidth = fontMetrics.charWidth(' ') * 7;
        setMinimumSize(new Dimension(minWidth, 0));

        this.textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                LinePanel.this.repaint();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                LinePanel.this.repaint();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {

            }
        });

        viewport.addChangeListener(e -> { LinePanel.this.repaint();});
    }

    public void setColorProvider(final BackgroundColorProvider colorProvider) {
        this.colorProvider = colorProvider;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        try {
            Interval interval = UIUtils.getVisibleLines(viewport, textPane);
            int aligny = fontMetrics.getHeight();

            for (int i = interval.getStart(); i <= interval.getEnd(); ++i) {
                int y = fontMetrics.getHeight() * (i - interval.getStart());

                String lineMessage = String.valueOf(i + 1) + " ";
                int alignx =  minWidth - fontMetrics.stringWidth(lineMessage);

                AttributedString stringToDisplay = new AttributedString(lineMessage);
                if (colorProvider != null) {
                    stringToDisplay.addAttribute(TextAttribute.BACKGROUND, colorProvider.getColorForLine(i));

                }

                g.drawString(stringToDisplay.getIterator(), alignx, aligny + y);
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

    }
}