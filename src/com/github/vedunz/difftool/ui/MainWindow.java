package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffConsumerList;
import com.github.vedunz.difftool.control.DiffController;
import com.github.vedunz.difftool.control.LineDiffConsumerList;
import com.github.vedunz.difftool.control.LineDiffController;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vedun on 21.07.2017.
 */
public final class MainWindow extends JFrame {

    private final DiffPanel firstDiffPanel = new DiffPanel();
    private final DiffPanel secondDiffPanel = new DiffPanel();

    private final VersionManager versionManager = new VersionManager();
    private final LineDiffConsumerList lineDiffConsumerList = new LineDiffConsumerList();

    private final LineDiffController lineDiffController = new LineDiffController(versionManager, lineDiffConsumerList);
    private final HighlightManager mainWindowHighlightManager = new HighlightManager(firstDiffPanel, secondDiffPanel, lineDiffController);

    private final ScrollManager scrollManager = new ScrollManager(firstDiffPanel, secondDiffPanel);

    private final DiffNavigationManager firstDiffNavigationManager = new DiffNavigationManager(firstDiffPanel, true);
    private final DiffNavigationManager secondDiffNavigationManager = new DiffNavigationManager(secondDiffPanel, false);
    private final DiffConsumerList diffConsumerList = new DiffConsumerList();
    private final DiffController controller = new DiffController(versionManager, diffConsumerList);

    private final DocumentListener documentListener = new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }

        private void processUpdate(DocumentEvent e) {
            versionManager.textUpdated();
            diffConsumerList.update(null);
            if (e.getDocument() == firstDiffPanel.getEditor().getDocument()) {
                List<String> lines = Arrays.asList(firstDiffPanel.getEditor().getText().split("\\r?\\n"));
                controller.uploadFirstText(lines);
            } else {
                List<String> lines = Arrays.asList(secondDiffPanel.getEditor().getText().split("\\r?\\n"));
                controller.uploadSecondText(lines);
            }
            controller.requestDiff();
        }
    };


    public MainWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width * 3 / 4, screenSize.height * 3 / 4);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
        add(firstDiffPanel);
        add(secondDiffPanel);

        lineDiffConsumerList.add(mainWindowHighlightManager);

        diffConsumerList.add(mainWindowHighlightManager);
        diffConsumerList.add(scrollManager);
        diffConsumerList.add(firstDiffNavigationManager);
        diffConsumerList.add(secondDiffNavigationManager);

        firstDiffPanel.getEditor().getStyledDocument().addDocumentListener(documentListener);
        secondDiffPanel.getEditor().getStyledDocument().addDocumentListener(documentListener);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
}
