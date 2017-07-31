package com.github.vedunz.difftool.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vedun on 26.07.2017.
 */
public class MyersLineDiffService extends MyersDiffServiceCommon implements LineDiffService {
    private final List<Character> firstChars = new ArrayList<>();
    private final List<Character> secondChars = new ArrayList<>();

    @Override
    public DiffResult getDiffResult(String firstLine, String secondLine) {
        firstChars.clear();
        secondChars.clear();

        for (char c : firstLine.toCharArray()) {
            firstChars.add(c);
        }
        for (char c : secondLine.toCharArray()) {
            secondChars.add(c);
        }
        return getDiffResult(firstChars, secondChars);
    }
}
