package com.github.vedunz.difftool.diff;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by vedun on 22.07.2017.
 */
class DiffServiceTests {

    private static final ArrayList<String> PATTERN1 = new ArrayList<>();
    private static final ArrayList<String> PATTERN2 = new ArrayList<>();

    static {
        PATTERN1.add("abacaba");
        PATTERN1.add("aba");
        PATTERN1.add("caba");

        PATTERN2.add("caba");
        PATTERN2.add("aba");
        PATTERN2.add("abacaba");
    }

    @Test
    void naiveTest() {
        ArrayList<String> firstText = new ArrayList<>();
        firstText.addAll(PATTERN1);
        firstText.addAll(PATTERN2);
        firstText.addAll(PATTERN1);
        ArrayList<String> secondText = new ArrayList<>();
        secondText.addAll(PATTERN2);
        secondText.addAll(PATTERN1);
        secondText.addAll(PATTERN2);
        DiffService diffService = new NaiveDiffService();
        diffService.insertFirstLines(0, firstText);
        diffService.insertSecondLines(0, secondText);
        DiffResult result = diffService.getDiffResult();
        int totalLength = 0;
        for (DiffInterval interval : result.getIntervals()) {
            totalLength += interval.getLength();
        }
        assertTrue(totalLength == 6);
    }

    @Test
    void myaersTest() {
        ArrayList<String> firstText = new ArrayList<>();
        firstText.addAll(PATTERN1);
        firstText.addAll(PATTERN2);
        firstText.addAll(PATTERN1);
        ArrayList<String> secondText = new ArrayList<>();
        secondText.addAll(PATTERN2);
        secondText.addAll(PATTERN1);
        secondText.addAll(PATTERN2);
        DiffService diffService = new MyersDiffService();
        diffService.insertFirstLines(0, firstText);
        diffService.insertSecondLines(0, secondText);
        DiffResult result = diffService.getDiffResult();
        int totalLength = 0;
        for (DiffInterval interval : result.getIntervals()) {
            totalLength += interval.getLength();
        }
        assertTrue(totalLength == 6);
    }

    @Test
    void gnuDiffTest() {
        ArrayList<String> firstText = new ArrayList<>();
        firstText.addAll(PATTERN1);
        firstText.addAll(PATTERN2);
        firstText.addAll(PATTERN1);
        ArrayList<String> secondText = new ArrayList<>();
        secondText.addAll(PATTERN2);
        secondText.addAll(PATTERN1);
        secondText.addAll(PATTERN2);
        DiffService diffService = new GNUDiffService();
        diffService.insertFirstLines(0, firstText);
        diffService.insertSecondLines(0, secondText);
        DiffResult result = diffService.getDiffResult();
        int totalLength = 0;
        for (DiffInterval interval : result.getIntervals()) {
            totalLength += interval.getLength();
        }
        assertTrue(totalLength == 6);
    }
}