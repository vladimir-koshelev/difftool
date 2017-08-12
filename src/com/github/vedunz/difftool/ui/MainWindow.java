package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffController;
import com.github.vedunz.difftool.control.LineDiffController;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vedun on 21.07.2017.
 */
public final class MainWindow extends JFrame {

    private final DiffPanel firstDiffPanel = new DiffPanel(true);
    private final DiffPanel secondDiffPanel = new DiffPanel(false);

    private final VersionManager versionManager = new VersionManager();

    private final LineDiffController lineDiffController = new LineDiffController(versionManager);
    private final HighlightManager mainWindowHighlightManager = new HighlightManager(firstDiffPanel, secondDiffPanel, lineDiffController);

    private final ScrollManager scrollManager = new ScrollManager(firstDiffPanel, secondDiffPanel);

    private final DiffNavigationManager firstDiffNavigationManager = new DiffNavigationManager(firstDiffPanel, true);
    private final DiffNavigationManager secondDiffNavigationManager = new DiffNavigationManager(secondDiffPanel, false);

    private final DiffController controller = new DiffController(versionManager);

    private final DocumentManager documentManager =
            new DocumentManager((AbstractDocument) firstDiffPanel.getEditor().getDocument(),
                    (AbstractDocument) secondDiffPanel.getEditor().getDocument(), controller, versionManager);

    public MainWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width * 3 / 4, screenSize.height * 3 / 4);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
        add(firstDiffPanel);
        add(secondDiffPanel);

        lineDiffController.addLineDiffConsumer(mainWindowHighlightManager);

        controller.addDiffConsumer(mainWindowHighlightManager);
        controller.addDiffConsumer(scrollManager);
        controller.addDiffConsumer(firstDiffNavigationManager);
        controller.addDiffConsumer(secondDiffNavigationManager);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
