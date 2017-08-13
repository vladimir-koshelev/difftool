package com.github.vedunz.difftool.diff;

import com.sun.istack.internal.NotNull;

import java.util.List;

/**
 * Created by vedun on 23.07.2017.
 */
public interface DiffService {
    static DiffService createDefaultDiffService() {
        return new GNUDiffService();
    }

    void insertFirstLines(int offset, @NotNull List<String> lines);

    void removeFirstLines(int offset, int length);

    void insertSecondLines(int offset, @NotNull List<String> lines);

    void removeSecondLines(int offset, int lenght);

    void replaceLineFirst(int offset, String newValue);

    void replaceLineSecond(int offset, String newValue);

    DiffResult getDiffResult();

    String getFirstText();

    String getSecondText();
}
