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
import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vedun on 21.07.2017.
 */
public final class MainWindow extends JFrame {

    private JTextPane firstEditor = new JTextPane(new DefaultStyledDocument());
    private JTextPane secondEditor = new JTextPane(new DefaultStyledDocument());

    private JPanel firstPanel = new JPanel(new BorderLayout());
    private JPanel secondPanel = new JPanel(new BorderLayout());
    {
        firstPanel.add(firstEditor, BorderLayout.CENTER);
        secondPanel.add(secondEditor, BorderLayout.CENTER);
    }

    private JScrollPane firstScrollPane = new JScrollPane(firstPanel);
    private JScrollPane secondScrollPane = new JScrollPane(secondPanel);
    {
        firstScrollPane.getVerticalScrollBar().setUnitIncrement(32);
        secondScrollPane.getVerticalScrollBar().setUnitIncrement(32);
    }

    private JButton firstOpenButton = new JButton();
    private JButton secondOpenButton = new JButton();
    {
        try {
            Image img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("images/document-open.png"));
            firstOpenButton.setToolTipText("Open first file");
            firstOpenButton.setIcon(new ImageIcon(img));
            secondOpenButton.setToolTipText("Open second file");
            secondOpenButton.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JButton firstNextDiffButton = new JButton();
    private JButton secondNextDiffButton = new JButton();

    {
        try {
            Image img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("images/go-next.png"));
            firstNextDiffButton.setToolTipText("Next diff");
            firstNextDiffButton.setIcon(new ImageIcon(img));
            secondNextDiffButton.setToolTipText("Next diff");
            secondNextDiffButton.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JButton firstPrevDiffButton = new JButton();
    private JButton secondPrevDiffButton = new JButton();

    {
        try {
            Image img = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource("images/go-previous.png"));
            firstPrevDiffButton.setIcon(new ImageIcon(img));
            firstPrevDiffButton.setToolTipText("Previous diff");
            secondPrevDiffButton.setIcon(new ImageIcon(img));
            secondPrevDiffButton.setToolTipText("Previous diff");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class IgnoreChangeUndoManager extends UndoManager  {
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            AbstractDocument.DefaultDocumentEvent event =
                    (AbstractDocument.DefaultDocumentEvent)e.getEdit();

            if  (!event.getType().equals(DocumentEvent.EventType.CHANGE))
                super.undoableEditHappened(e);
        }
    };

    private UndoManager firstUndoManager = new IgnoreChangeUndoManager();
    private UndoManager secondUndoManager = new IgnoreChangeUndoManager();

    {
        firstEditor.getDocument().addUndoableEditListener(firstUndoManager);
        secondEditor.getDocument().addUndoableEditListener(secondUndoManager);

        Action firstUndoAnction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstUndoManager.canUndo())
                    firstUndoManager.undo();
            }
        };
        Action secondUndoAnction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secondUndoManager.canUndo())
                    secondUndoManager.undo();
            }
        };
        Action firstRedoAnction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (firstUndoManager.canRedo())
                    firstUndoManager.redo();
            }
        };
        Action secondRedoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (secondUndoManager.canRedo())
                    firstUndoManager.redo();
            }
        };

        String keyStrokeAndKeyUndo = "control Z";
        String keyStrokeAndKeyRedo = "control Y";
        KeyStroke keyStrokeUndo = KeyStroke.getKeyStroke(keyStrokeAndKeyUndo);
        firstEditor.getInputMap().put(keyStrokeUndo, keyStrokeAndKeyUndo);
        firstEditor.getActionMap().put(keyStrokeAndKeyUndo, firstUndoAnction);
        secondEditor.getInputMap().put(keyStrokeUndo, keyStrokeAndKeyUndo);
        secondEditor.getActionMap().put(keyStrokeAndKeyUndo, secondUndoAnction);

        KeyStroke keyStrokeRedo = KeyStroke.getKeyStroke(keyStrokeAndKeyRedo);
        firstEditor.getInputMap().put(keyStrokeRedo, keyStrokeAndKeyRedo);
        firstEditor.getActionMap().put(keyStrokeAndKeyRedo, firstRedoAnction);
        secondEditor.getInputMap().put(keyStrokeRedo, keyStrokeAndKeyRedo);
        secondEditor.getActionMap().put(keyStrokeAndKeyRedo, secondRedoAction);
    }


    private JTextArea firstFileName = new JTextArea(1, 20);
    private JTextArea secondFileName = new JTextArea(1, 20);
    private VersionManager versionManager = new VersionManager();

    private LineDiffConsumerList lineDiffConsumerList = new LineDiffConsumerList();
    private LineDiffController lineDiffController = new LineDiffController(versionManager, lineDiffConsumerList);
    private HighlightManager mainWindowHighlightManager = new HighlightManager(firstEditor, secondEditor,
            firstScrollPane, secondScrollPane, lineDiffController);
    {
        lineDiffConsumerList.add(mainWindowHighlightManager);
    }

    private ScrollManager scrollManager = new ScrollManager(firstEditor, secondEditor, firstScrollPane, secondScrollPane);
    private DiffNavigationManager firstDiffNavigationManager = new DiffNavigationManager(firstScrollPane, firstEditor,
            firstPrevDiffButton, firstNextDiffButton, true);

    private DiffNavigationManager secondDiffNavigationManager = new DiffNavigationManager(secondScrollPane, secondEditor,
            secondPrevDiffButton, secondNextDiffButton, false);

    private DiffConsumerList diffConsumerList = new DiffConsumerList();
    {
        diffConsumerList.add(mainWindowHighlightManager);
        diffConsumerList.add(scrollManager);
        diffConsumerList.add(firstDiffNavigationManager);
        diffConsumerList.add(secondDiffNavigationManager);
    }



    private DiffController controller = new DiffController(versionManager, diffConsumerList);
    private final DocumentListener documentListener = new DocumentListener() {

        private void processUpdate(DocumentEvent e) {
            versionManager.textUpdated();
            diffConsumerList.update(null);
            if (e.getDocument() == firstEditor.getDocument()) {
                List<String> lines = Arrays.asList(firstEditor.getText().split("\\r?\\n"));
                controller.uploadFirstText(lines);
            } else {
                List<String> lines = Arrays.asList(secondEditor.getText().split("\\r?\\n"));
                controller.uploadSecondText(lines);
            }
            controller.requestDiff();
        }


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
    };

    private static String readAllLines(BufferedReader buffIn) throws IOException {
        StringBuilder allLines = new StringBuilder();
        String line;
        while( (line = buffIn.readLine()) != null) {
            allLines.append(line + System.lineSeparator());
        }
        return allLines.toString();
    }

    public class FileOpenActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            try {
                int result = fileChooser.showOpenDialog(MainWindow.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    FileReader reader = new FileReader(selectedFile);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    if (e.getSource() == firstOpenButton) {
                        firstEditor.setText(readAllLines(bufferedReader));
                        firstFileName.setText(selectedFile.getAbsolutePath());
                    } else {
                        secondEditor.setText(readAllLines(bufferedReader));
                        secondFileName.setText(selectedFile.getAbsolutePath());
                    }
                }
            }  catch (IOException ioe) {
                JOptionPane.showMessageDialog(MainWindow.this,
                        ioe.toString(),
                        "Cannot open file",
                        JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    public MainWindow() {
        FileOpenActionListener fileOpenActionListener = new FileOpenActionListener();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width * 3 / 4, screenSize.height * 3 / 4);
        setLayout(new GridBagLayout());

        firstOpenButton.addActionListener(fileOpenActionListener);
        secondOpenButton.addActionListener(fileOpenActionListener);

        createFilePanel(firstOpenButton, firstPrevDiffButton, firstNextDiffButton, firstFileName, this, firstScrollPane, 0);
        createFilePanel(secondOpenButton, secondPrevDiffButton, secondNextDiffButton, secondFileName, this, secondScrollPane, 4);

        firstEditor.getStyledDocument().addDocumentListener(documentListener);
        secondEditor.getStyledDocument().addDocumentListener(documentListener);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private static void createFilePanel(JButton button, JButton prev, JButton next, JTextArea textArea, Container container, JScrollPane scrollPane,
                                        int offset) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = offset;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;

        container.add(button, gbc);
        gbc.gridx++;
        container.add(prev, gbc);
        gbc.gridx++;
        container.add(next, gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        container.add(textArea, gbc);

        gbc.gridy = 1;
        gbc.gridx = offset;
        gbc.weighty = 1;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.BOTH;

        container.add(scrollPane, gbc);
    }

}
