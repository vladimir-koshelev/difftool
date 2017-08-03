package com.github.vedunz.difftool.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DiffPanel extends JPanel {
    private final JTextPane editor = new JTextPane(new DefaultStyledDocument());
    private final JPanel editorPanel = new JPanel(new BorderLayout());
    private final JScrollPane scrollPane = new JScrollPane(editorPanel);
    private final JButton openButton = new JButton();
    private final JButton nextDiffButton = new JButton();
    private final JButton prevDiffButton = new JButton();
    private final UndoManager undoManager = new IgnoreChangeUndoManager();
    private final JTextArea fileName = new JTextArea(1, 20);
    private final LinePanel linePanel;


    public DiffPanel() {
        fileName.setEditable(false);
        setLayout(new GridBagLayout());
        linePanel = new LinePanel(editor, scrollPane.getViewport());
        loadButtonImages();
        addOpenFileDialog();
        addUndoRedoActions();
        setupLayout();
        scrollPane.getVerticalScrollBar().setUnitIncrement(32);
    }

    public JTextPane getEditor() {
        return editor;
    }

    public JPanel getEditorPanel() {
        return editorPanel;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JButton getOpenButton() {
        return openButton;
    }

    public JButton getNextDiffButton() {
        return nextDiffButton;
    }

    public JButton getPrevDiffButton() {
        return prevDiffButton;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public JTextArea getFileName() {
        return fileName;
    }

    public LinePanel getLinePanel() { return linePanel; }

    private static String readAllLines(BufferedReader buffIn) throws IOException {
        StringBuilder allLines = new StringBuilder();
        String line;
        while ((line = buffIn.readLine()) != null) {
            allLines.append(line + System.lineSeparator());
        }
        return allLines.toString();
    }

    private void addUndoRedoActions() {
        editor.getDocument().addUndoableEditListener(undoManager);
        Action undoAnction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo())
                    undoManager.undo();
            }
        };
        Action redoAnction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo())
                    undoManager.redo();
            }
        };
        String keyStrokeAndKeyUndo = "control Z";
        String keyStrokeAndKeyRedo = "control Y";
        KeyStroke keyStrokeUndo = KeyStroke.getKeyStroke(keyStrokeAndKeyUndo);
        editor.getInputMap().put(keyStrokeUndo, keyStrokeAndKeyUndo);
        editor.getActionMap().put(keyStrokeAndKeyUndo, undoAnction);

        KeyStroke keyStrokeRedo = KeyStroke.getKeyStroke(keyStrokeAndKeyRedo);
        editor.getInputMap().put(keyStrokeRedo, keyStrokeAndKeyRedo);
        editor.getActionMap().put(keyStrokeAndKeyRedo, redoAnction);
    }

    private void addOpenFileDialog() {
        openButton.addActionListener( (e) -> {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            try {
                int result = fileChooser.showOpenDialog(DiffPanel.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    FileReader reader = new FileReader(selectedFile);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    editor.setText(readAllLines(bufferedReader));
                    fileName.setText(selectedFile.getAbsolutePath());

                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(DiffPanel.this,
                        ioe.toString(),
                        "Cannot open file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        );
    }

    private void loadButtonImages() {
        try {
            Image img = ImageIO.read(ClassLoader.getSystemResourceAsStream("images/document-open.png"));
            openButton.setToolTipText("Open first file");
            openButton.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Image img = ImageIO.read(ClassLoader.getSystemResourceAsStream("images/go-next.png"));
            nextDiffButton.setToolTipText("Next diff");
            nextDiffButton.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Image img = ImageIO.read(ClassLoader.getSystemResourceAsStream("images/go-previous.png"));
            prevDiffButton.setIcon(new ImageIcon(img));
            prevDiffButton.setToolTipText("Previous diff");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupLayout() {

        editorPanel.add(editor, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;

        add(openButton, gbc);
        gbc.gridx += 2;
        gbc.gridwidth = 1;
        add(prevDiffButton, gbc);
        gbc.gridx++;
        add(nextDiffButton, gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

       /* JScrollPane panelForFileName = new JScrollPane(fileName);
        panelForFileName.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        panelForFileName.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);*/
        add(fileName, gbc);

        JPanel tempEditorPanel = new JPanel(new BorderLayout());
        JPanel editorPanel = new JPanel();
        tempEditorPanel.add(editorPanel, BorderLayout.CENTER);
        editorPanel.setLayout(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;

        scrollPane.setRowHeaderView(linePanel);
        final JViewport parent = (JViewport) linePanel.getParent();
        parent.addChangeListener(new ChangeListener() {
            private  boolean isChanged = false;
            @Override
            public void stateChanged(final ChangeEvent e) {
                try {
                    if (!isChanged) {
                        isChanged = true;
                        parent.setViewPosition(new Point(0,0));
                    }
                } finally {
                    isChanged = false;
                }

            }
        });

        editorPanel.add(scrollPane, gbc);


        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 6;
        gbc.fill = GridBagConstraints.BOTH;

        add(tempEditorPanel, gbc);



    }

    private class IgnoreChangeUndoManager extends UndoManager {
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            AbstractDocument.DefaultDocumentEvent event =
                    (AbstractDocument.DefaultDocumentEvent) e.getEdit();

            if (!event.getType().equals(DocumentEvent.EventType.CHANGE))
                super.undoableEditHappened(e);
        }
    }
}
