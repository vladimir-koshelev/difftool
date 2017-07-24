package com.github.vedunz.difftool.ui.util;

import com.github.vedunz.difftool.diff.Interval;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import java.awt.*;

/**
 * Created by vedun on 25.07.2017.
 */
public class UIUtils {

    public static Interval getVisibleLines(JViewport viewport, JTextPane jTextPane) throws BadLocationException {
      Rectangle viewRect = viewport.getViewRect();

      Point p = viewRect.getLocation();
      int startIndex = jTextPane.viewToModel(p);

      p.x += viewRect.width;
      p.y += viewRect.height;
      int endIndex = jTextPane.viewToModel(p);

      Element element = jTextPane.getDocument().getDefaultRootElement();

      return new Interval(element.getElementIndex(startIndex), element.getElementIndex(endIndex));
    }

    public static void showLine(JViewport viewport, JTextPane jTextPane, int line) throws BadLocationException {
        Element element = jTextPane.getDocument().getDefaultRootElement();
        int startOffset = element.getElement(line).getStartOffset();
        Rectangle rectangle = jTextPane.modelToView(startOffset);
        if (viewport.getViewPosition().getY() != (int) rectangle.getY()) {
            viewport.setViewPosition(new Point(0, (int) rectangle.getY()));
        }
    }
}
