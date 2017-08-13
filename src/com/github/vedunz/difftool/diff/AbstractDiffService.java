package com.github.vedunz.difftool.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractDiffService implements DiffService {

    protected final List<String> firstText = new ArrayList<>();
    protected final List<String> secondText = new ArrayList<>();


    @Override
    public void insertFirstLines(final int offset, final List<String> lines) {
        firstText.addAll(offset, lines);
    }

    @Override
    public void removeFirstLines(final int offset, final int length) {
        firstText.subList(offset, offset + length).clear();
    }

    @Override
    public void insertSecondLines(final int offset, final List<String> lines) {
        secondText.addAll(offset, lines);
    }

    @Override
    public void removeSecondLines(final int offset, final int lenght) {
        secondText.subList(offset, offset + lenght).clear();
    }

    @Override
    public void replaceLineFirst(final int offset, final String newValue) {
        if (offset < firstText.size())
            firstText.set(offset, newValue);
        else
            firstText.add(newValue);
    }

    @Override
    public void replaceLineSecond(final int offset, final String newValue) {
        if (offset < secondText.size())
            secondText.set(offset, newValue);
        else
            secondText.add(newValue);
    }

    @Override
    public List<String> getFirstTextLines() {
        return firstText;
    }

    @Override
    public List<String> getSecondTextLines() {
        return secondText;
    }

    @Override
    public String getFirstText() {
        return list2String(firstText);
    }

    @Override
    public String getSecondText() {
        return list2String(secondText);
    }

    private String list2String(final List<String> text) {
        StringBuilder stringBuilder = new StringBuilder();
        if (text.isEmpty())
            return "";
        stringBuilder.append(text.get(0));
        for (int i = 1; i < text.size(); ++i) {
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(text.get(i));
        }
        return stringBuilder.toString();
    }
}
