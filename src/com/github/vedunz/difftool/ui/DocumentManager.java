package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffController;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DocumentManager {

    private final boolean CONSISTENCY_CHECK = false;

    private final ExecutorService executorConsService = (CONSISTENCY_CHECK) ? Executors.newSingleThreadExecutor(): null;

    private final int LINE_SPEPARATOR_LENGTH = System.lineSeparator().length();

    private final AbstractDocument firstDocument;
    private final AbstractDocument secondDocument;
    private final DiffController controller;
    private final VersionManager versionManager;

    private final DocumentFilter documentFilter = new DocumentFilter() {
        @Override
        public void remove(final FilterBypass fb, final int offset, final int length) throws BadLocationException {
            processRemove(fb.getDocument(), offset, length);
            super.remove(fb, offset, length);
        }

        @Override
        public void replace(final FilterBypass fb, final int offset, final int length,
                            final String text, final AttributeSet attrs) throws BadLocationException {
            AttributeSet newAttrs = keepAttributeSet(fb, offset, attrs);
            processRemove(fb.getDocument(), offset, length);
            super.replace(fb, offset, length, text, newAttrs);
        }

        @Override
        public void insertString(final FilterBypass fb, final int offset, final String string, final AttributeSet attr)
                throws BadLocationException {
            AttributeSet newAttrs = keepAttributeSet(fb, offset, attr);
            super.insertString(fb, offset, string, newAttrs);
        }

        private AttributeSet keepAttributeSet(final FilterBypass fb, final int offset, final AttributeSet attrs) {
            Element root = fb.getDocument().getDefaultRootElement();
            if (offset > 0) {
                Element prev = root.getElement(root.getElementIndex(offset - 1));
                while (prev.getElementCount() != 0) {
                    prev = prev.getElement(prev.getElementCount() - 1);
                }
                Color background = StyleConstants.getBackground(prev.getAttributes());
                if (background != null && !background.equals(Color.BLACK)) {
                    if (attrs == null) {
                        SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
                        StyleConstants.setBackground(simpleAttributeSet, background);
                        return simpleAttributeSet;
                    }

                    MutableAttributeSet mattrs = (MutableAttributeSet) attrs;
                    StyleConstants.setBackground(mattrs, background);
                }
            }
            return attrs;
        }

        private void processRemove(final Document document, final int offset, final int length) throws BadLocationException {
            Element root = document.getDefaultRootElement();
            int start = root.getElementIndex(offset);
            int end = root.getElementIndex(offset + length - 1);
            Element startElement = root.getElement(start);
            Element endElement = root.getElement(end);
            if (start == end) {
                if (startElement.getEndOffset() - startElement.getStartOffset() == length)
                    removeLines(document, start, start);
                else {
                    String newLine = document.getText(startElement.getStartOffset(),
                            offset - startElement.getStartOffset()) +
                            document.getText(offset + length,
                                    startElement.getEndOffset() - offset - length - LINE_SPEPARATOR_LENGTH);

                    replaceLine(document, start, newLine);
                }
            } else {
                removeLines(document, start, end);
                String line = "";
                if (offset !=  startElement.getStartOffset()) {
                    line = document.getText(startElement.getStartOffset(), offset - startElement.getStartOffset());
                }
                if (offset + length != endElement.getEndOffset()) {
                    line += document.getText(offset + length,
                            endElement.getEndOffset() - (offset + length) - LINE_SPEPARATOR_LENGTH);
                    insertLines(document, start, Collections.singletonList(line));
                } else if (!line.equals("")) {
                    if (end + 1 < root.getElementCount()) {
                        final Element endNext = root.getElement(end + 1);
                        line += getText(document, endNext);
                        replaceLine(document, start, line);
                    } else
                        insertLines(document, start, Collections.singletonList(line));
                }
            }
        }
    };

    private final DocumentListener documentListener = new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent e) {
            try {
                processInsert(e);
                checkConsistency();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
            versionManager.textUpdated();
            controller.invalidate();
            controller.requestDiff();

        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            try {
                checkConsistency();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
            versionManager.textUpdated();
            controller.invalidate();
            controller.requestDiff();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {

        }

        private void processInsert(DocumentEvent e) throws BadLocationException {

            final Document document = e.getDocument();
            Element root = document.getDefaultRootElement();
            int start = root.getElementIndex(e.getOffset());
            int end = root.getElementIndex(e.getOffset() + e.getLength() - 1);
            Element startElement = root.getElement(start);
            Element endElement = root.getElement(end);
            if (start == end) {
                String newValue = getText(document, startElement);
                replaceLine(document, start, newValue);
                if (e.getOffset() + e.getLength() == startElement.getEndOffset()) {
                    Element startNext = root.getElement(start + 1);

                    insertLines(document, start + 1, Collections.singletonList(
                            getText(document, startNext)
                    ));
                }
            } else {
                removeLines(document, start, start);

                List<String> lines = new ArrayList<>();

                for (int line = start; line <= end; ++line) {
                    lines.add(getText(document, root.getElement(line)));
                }

               if (e.getOffset() + e.getLength() == endElement.getEndOffset() && end + 1 < root.getElementCount()) {
                    Element endNext = root.getElement(end + 1);
                    lines.add(getText(document, endNext));
                }

                insertLines(document, start, lines);
            }
        }
    };

    private void checkConsistency() throws BadLocationException {
        if (!CONSISTENCY_CHECK)
            return;
        String firstText = firstDocument.getText(0, firstDocument.getLength());
        String secondText = secondDocument.getText(0, secondDocument.getLength());
        Future<String> firstTextService = controller.getFirstText();
        Future<String> secondTextService = controller.getSecondText();
        executorConsService.submit(() -> {
            try {
                String firstTextServiceRes = firstTextService.get();
                String secondTextServiceRes = secondTextService.get();
                if (!firstText.equals(firstTextServiceRes)) {
                    System.err.println("First text mismatch");
                    System.err.println(firstText);
                    System.err.println(firstTextServiceRes);
                }

                if (!secondText.equals(secondTextService.get())) {
                    System.err.println("Second text mismatch");
                    System.err.println(secondText);
                    System.err.println(secondTextServiceRes);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }


    public DocumentManager(AbstractDocument firstDocument, AbstractDocument secondDocument,
                           DiffController diffController, VersionManager versionManager) {
        this.firstDocument = firstDocument;
        this.secondDocument = secondDocument;
        this.controller = diffController;
        this.versionManager = versionManager;
        firstDocument.setDocumentFilter(documentFilter);
        secondDocument.setDocumentFilter(documentFilter);
        firstDocument.addDocumentListener(documentListener);
        secondDocument.addDocumentListener(documentListener);
    }


    private String getText(final Document document, final Element startNext) throws BadLocationException {
        return document.getText(startNext.getStartOffset(), startNext.getEndOffset() - startNext.getStartOffset() - LINE_SPEPARATOR_LENGTH);
    }

    private void insertLines(final Document document, final int offset, final List<String> lines) {
        if (document == firstDocument) {
            controller.insertLinesFirst(offset, lines);
        } else {
            controller.insertLinesSecond(offset, lines);
        }
    }

    private void removeLines(final Document document, final int start, final int end) {
        if (document == firstDocument) {
            controller.removeLinesFirst(start, end);
        } else {
            controller.removeLinesSecond(start, end);
        }
    }

    private void replaceLine(final Document document, final int index, final String newLine) {
        if (document == firstDocument) {
            controller.replaceLineFirst(index, newLine);
        } else
            controller.replaceLineSecond(index, newLine);
    }
}
