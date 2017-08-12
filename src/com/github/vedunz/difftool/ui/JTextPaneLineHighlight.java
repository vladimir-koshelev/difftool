package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.diff.Interval;
import com.github.vedunz.difftool.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class JTextPaneLineHighlight extends JTextPane {

    public JTextPaneLineHighlight() {
    }

    public JTextPaneLineHighlight(final StyledDocument doc) {
        super(doc);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        try {
            Component parent = this.getParent();
            JViewport viewport;
            while (parent != null && !(parent instanceof JViewport)) {
                parent = parent.getParent();
            }
            if (parent == null)
                return;
            viewport = (JViewport) parent;
            Interval interval = UIUtils.getVisibleLines(viewport, this);

            Document document = getStyledDocument();
            Element root = document.getDefaultRootElement();

            for (int i = interval.getStart(); i <= interval.getEnd(); ++i) {
                Element element = root.getElement(i);
                Color color = null;
                for (int childNo = 0; childNo < element.getElementCount(); ++childNo) {
                    Element child = element.getElement(childNo);
                    color = (Color) child.getAttributes().getAttribute(StyleConstants.Background);
                    if (color != null)
                        break;
                }
                if (color != null) {
                    int end = element.getEndOffset() - 1;
                    Rectangle r = modelToView(end);
                    r.x += r.width;
                    r.width = viewport.getWidth() - r.x + viewport.getViewRect().x;
                    Color oldColor = g.getColor();
                    g.setColor(color);
                    g.fillRect(r.x, r.y, r.width, r.height);
                    g.setColor(oldColor);
                }
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
