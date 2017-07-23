package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffController;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
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

    private JScrollPane firstScrollPane = new JScrollPane(firstEditor);
    private JScrollPane secondScrollPane = new JScrollPane(secondEditor);

    private JButton firstOpenButton = new JButton("Open first file");
    private JButton secondOpenButton = new JButton("Open second file");

    private JTextArea firstFileName = new JTextArea(1, 20);
    private JTextArea secondFileName = new JTextArea(1, 20);

    private MainWindowHighlightManager mainWindowHighlightManager = new MainWindowHighlightManager(this);
    private ScrollManager scrollManager = new ScrollManager(firstEditor, secondEditor, firstScrollPane, secondScrollPane);
    private DiffController controller = new DiffController(mainWindowHighlightManager, scrollManager);
    private final DocumentListener documentListener;

    public JTextPane getFirstEditor() {
        return firstEditor;
    }

    public JTextPane getSecondEditor() {
        return secondEditor;
    }

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

        createFilePanel(firstOpenButton, firstFileName, this, firstScrollPane, 0);
        createFilePanel(secondOpenButton, secondFileName, this, secondScrollPane, 2);

        documentListener = new DocumentListener() {

            private void processUpdate(DocumentEvent e) {
                mainWindowHighlightManager.textUpdated();
                if (e.getDocument() == firstEditor.getDocument()) {
                    List<String> lines = Arrays.asList(firstEditor.getText().split("\\r?\\n"));
                    controller.uploadFirstText(lines);
                } else {
                    List<String> lines = Arrays.asList(secondEditor.getText().split("\\r?\\n"));
                    controller.uploadSecondText(lines);
                }
                controller.getDiff();
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

        firstEditor.getStyledDocument().addDocumentListener(documentListener);
        secondEditor.getStyledDocument().addDocumentListener(documentListener);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private static void createFilePanel(JButton button, JTextArea textArea, Container container, JScrollPane scrollPane,
                                        int offset) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = offset;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.ipadx = 10;
        gbc.ipady = 10;
        gbc.fill = GridBagConstraints.NONE;

        container.add(button, gbc);

        gbc.gridx = offset + 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        container.add(textArea, gbc);

        gbc.gridy = 1;
        gbc.gridx = offset;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;

        container.add(scrollPane, gbc);
    }

}
