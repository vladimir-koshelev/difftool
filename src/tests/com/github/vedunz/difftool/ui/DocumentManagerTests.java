package com.github.vedunz.difftool.ui;

import com.github.vedunz.difftool.control.DiffController;
import org.junit.jupiter.api.Test;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class DocumentManagerTests {

    private AbstractDocument currentDocument;
    private AbstractDocument firstDocument;
    private AbstractDocument secondDocument;

    private VersionManager versionManager;
    private DiffController diffController;
    private DocumentManager documentManager;

    @Test
    public void testInsertChar() {
        try {
            createEnv();
            currentDocument.insertString(0,"a", null);
            checkStringEquality();
        }
        catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testInsert() {
        try
        {
            createDefaultEnv();
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveLine() {
        try {
            createDefaultEnv();
            removeLine(1, 0 ,0);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveLineAfterFirstLine() {
        try {
            createDefaultEnv();
            removeLine(0, 0, 1);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveLineAfter() {
        try {
            createDefaultEnv();
            removeLine(1, 0, 1);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveLineBefore() {
        try {
            createDefaultEnv();
            removeLine(1, 1, 0);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveLineBeforeAndAfter() {
        try {
            createDefaultEnv();
            removeLine(1, 1, 1);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveTwoLinesFromStart() {
        try {
            createDefaultEnv();
            removeLine(0, 0, 4);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }
    @Test
    public void testRemoveTwoLinesFromMiddle() {
        try {
            createDefaultEnv();
            removeLine(1, 0, 3);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveSymbol() {
        try {
            createDefaultEnv();
            removeLine(1, 0, -3);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveAtStart() {
        try {
            createDefaultEnv();
            removeLine(0, 0, 1);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testRemoveAtEnd() {
        try {
            createDefaultEnv();
            removeLine(2, 1, -1);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }


    @Test
    public void testInsertText() {
        try {
            createDefaultEnv();
            insertLine("dddd", 0, 0);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testInsertTextWithOffset() {
        try {
            createDefaultEnv();
            insertLine("dddd", 0, 2);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }


    @Test
    public void testInsertTextOffsetAtTheEnd() {
        try {
            createDefaultEnv();
            insertLine("dddd", 0, 3);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }


    @Test
    public void testInsertLine() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator(), 0, 0);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }
    @Test
    public void testInsertLineWithOffset() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator(), 0, 2);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }
    @Test
    public void testInsertLineWithOffsetAtTheEnd() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator(), 0, 3);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testInsertLines() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator() + "eeee" + System.lineSeparator(), 0, 0);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }
    @Test
    public void testInsertLinesWithOffset() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator() + "eeee" + System.lineSeparator(), 0, 2);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }
    @Test
    public void testInsertLinesWithOffsetAtTheEnd() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator() + "eeee" + System.lineSeparator(), 0, 3);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    @Test
    public void testInsertLinesWithoutLS() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator() + "eeee", 0, 0);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }
    @Test
    public void testInsertLinesWithOffsetWithoutLS() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator() + "eeee", 0, 2);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }
    @Test
    public void testInsertLinesWithOffsetAtTheEndWithoutLS() {
        try {
            createDefaultEnv();
            insertLine("dddd" + System.lineSeparator() + "eeee", 0, 3);
            checkStringEquality();
        } catch (BadLocationException | InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    private void checkStringEquality() throws InterruptedException, ExecutionException, BadLocationException {
        if (currentDocument == firstDocument)
            checkFirstStringEquality();
        else
            checkSecondStringEquality();
    }

    private void checkFirstStringEquality() throws InterruptedException, ExecutionException, BadLocationException {
        String text = diffController.getFirstText().get();
        String documentText = currentDocument.getText(0, currentDocument.getLength());
        assertTrue(documentText.equals(text),
                () -> "Text:" +System.lineSeparator() + text + "DocText:" + System.lineSeparator() + documentText);
    }

    private void checkSecondStringEquality() throws InterruptedException, ExecutionException, BadLocationException {
        String text = diffController.getSecondText().get();
        String documentText = secondDocument.getText(0, secondDocument.getLength());
        assertTrue(documentText.equals(text),
                () -> "Text:" +System.lineSeparator() + text + "DocText:" + System.lineSeparator() + documentText);
    }

    private void createDefaultEnv() throws BadLocationException {
        createEnv();
        String str = "aaa" + System.lineSeparator() + "bbb" + System.lineSeparator() + "ccc";
        currentDocument.insertString(0, str, null);
    }

    private void createEnv() {
        firstDocument = new DefaultStyledDocument();
        secondDocument = new DefaultStyledDocument();
        currentDocument = secondDocument;
        versionManager = new VersionManager();
        diffController = new DiffController(versionManager);
        documentManager = new DocumentManager(firstDocument, secondDocument, diffController, versionManager);
    }

    private void insertLine(final String text, final int idx, final int offset) throws BadLocationException {
        Element defaultRoot = currentDocument.getDefaultRootElement();
        final Element element = defaultRoot.getElement(idx);
        currentDocument.insertString(element.getStartOffset() + offset, text, null);
    }

    private void removeLine(int idx, int lengthBefore, int lengthAfter) throws BadLocationException {
        Element defaultRoot = currentDocument.getDefaultRootElement();
        final Element element = defaultRoot.getElement(idx);
        currentDocument.remove(element.getStartOffset() - lengthBefore,
            element.getEndOffset() - element.getStartOffset() + lengthAfter + lengthBefore);
    }
}
