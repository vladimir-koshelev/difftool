package com.github.vedunz.difftool.diff;

import com.sun.istack.internal.NotNull;

import java.util.Collection;

/**
 * Created by vedun on 23.07.2017.
 */
public interface DiffService {
    void uploadFirstText(@NotNull Collection<String> lines);

    void uploadSecondText(@NotNull Collection<String> lines);

    DiffResult getDiffResult();

    static DiffService createDefaultDiffService() {
        return new MyersDiffService();
    }
}
