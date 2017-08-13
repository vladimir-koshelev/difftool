package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.diff.Interval;
import com.github.vedunz.difftool.ui.util.StyleManager;
import com.github.vedunz.difftool.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

public class LinePanel extends JPanel {

    private static final int VERTICAL_PADDING = -3;
    private final JTextPane textPane;
    private final JViewport viewport;
    private final StyleContext styleContext = StyleManager.getStyleContext();
    private final FontMetrics fontMetrics;

    private BackgroundColorProvider colorProvider = null;
    private int currentWidth = 0;
    private int numOfSymbols = 0;


    public LinePanel(JTextPane textPane, JViewport viewport) {
        this.textPane = textPane;
        this.viewport = viewport;
        Style mainStyle = styleContext.getStyle(StyleManager.MAIN_STYLE_NAME);
        Font font = styleContext.getFont(mainStyle);
        setFont(font);
        fontMetrics = getFontMetrics(font);
        adjustWidth();

        viewport.addChangeListener( e -> paintImmediately(getVisibleRect()) );

        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                adjustWidth();
                revalidate();
                repaint();

            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                adjustWidth();
                revalidate();
                repaint();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                adjustWidth();
                revalidate();
                repaint();
            }
        });
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(currentWidth, 0);
    }

    public void setColorProvider(final BackgroundColorProvider colorProvider) {
        this.colorProvider = colorProvider;
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        try {
            Interval interval = UIUtils.getVisibleLines(viewport, textPane);
            int starty = textPane.modelToView(UIUtils.lineToOffset(interval.getStart(), textPane)).y;
            int startyViewport = viewport.getViewPosition().y;
            int aligny = fontMetrics.getHeight() - (startyViewport - starty);


            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = interval.getStart(); i <= interval.getEnd(); ++i) {
                int y = fontMetrics.getHeight() * (i - interval.getStart());

                String lineMessage = String.valueOf(i + 1) + " ";

                while (lineMessage.length() < numOfSymbols)
                    lineMessage = " " + lineMessage;

                int alignx = currentWidth - fontMetrics.stringWidth(lineMessage);

                AttributedString stringToDisplay = new AttributedString(lineMessage);
                final StyleContext styleContext = StyleManager.getStyleContext();
                final Style style = styleContext.getStyle(StyleManager.MAIN_STYLE_NAME);
                final Font font = styleContext.getFont(style);
                stringToDisplay.addAttribute(TextAttribute.FONT, font);
                Color color = null;
                if (colorProvider != null) {
                    color = colorProvider.getColorForLine(i);
                    stringToDisplay.addAttribute(TextAttribute.BACKGROUND, color);
                }

                if (color != null) {
                    Color oldColor = g2d.getColor();
                    g2d.setColor(color);
                    g2d.fillRect(0, aligny + y + VERTICAL_PADDING - fontMetrics.getAscent(),
                            currentWidth, fontMetrics.getHeight());
                    g2d.setColor(oldColor);
                }


                g.drawString(stringToDisplay.getIterator(), alignx, aligny + y + VERTICAL_PADDING);
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

    }

    private int getNumberOfDigitsForLineNo() {
        int lines = textPane.getDocument().getDefaultRootElement().getElementCount();
        int ret = 0;
        while (lines > 0) {
            ret++;
            lines /= 10;
        }
        return ret;
    }

    private void adjustWidth() {
        JComponent component = (JComponent) getParent();
        int height = getHeight();
        if (component != null)
        {
            JScrollPane scrollPane = (JScrollPane) component.getParent();
            height = scrollPane.getViewport().getViewRect().height;
        }

        numOfSymbols = getNumberOfDigitsForLineNo() + 2;
        currentWidth = fontMetrics.charWidth(' ') * (numOfSymbols);
        setPreferredSize(new Dimension(currentWidth, height));
    }
}
