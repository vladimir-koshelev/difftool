package com.github.vedunz.difftool.diff;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by vedun on 22.07.2017.
 */
public class NaiveDiffService extends AbstractDiffService {

    @Override
    public DiffResult getDiffResult() {
        int n = firstText.size();
        int m = secondText.size();
        int[][] lcs = new int[n + 1][m + 1];
        LCSParent[][] lcsParent = new LCSParent[n + 1][m + 1];

        for (int i = 1; i <= n; ++i) {
            for (int j = 1; j <= m; ++j) {
                if (firstText.get(i - 1).equals(secondText.get(j - 1))) {
                    lcsParent[i][j] = LCSParent.BOTH;
                    lcs[i][j] = lcs[i - 1][j - 1] + 1;
                } else {
                    if (isFirstGreater(lcs[i - 1][j], lcs[i][j - 1], lcsParent[i - 1][j])) {
                        lcs[i][j] = lcs[i - 1][j];
                        lcsParent[i][j] = LCSParent.FIRST;
                    } else {
                        lcs[i][j] = lcs[i][j - 1];
                        lcsParent[i][j] = LCSParent.SECOND;
                    }
                }
            }
        }

        List<DiffInterval> diffIntervals = new ArrayList<>();
        int i = n, j = m;
        MutableDiffInterval curDiffInterval = null;
        while (i > 0 && j > 0) {
            if (lcsParent[i][j] == LCSParent.BOTH) {
                if (curDiffInterval == null)
                    curDiffInterval = new MutableDiffInterval(i - 1, j - 1);
                else
                    curDiffInterval.expand();
                i--;
                j--;
            } else {
                if (curDiffInterval != null) {
                    curDiffInterval.reverse();
                    diffIntervals.add(curDiffInterval);
                    curDiffInterval = null;
                }
                if (lcsParent[i][j] == LCSParent.FIRST) {
                    i = i - 1;
                } else {
                    j = j - 1;
                }
            }
        }
        if (curDiffInterval != null) {
            curDiffInterval.reverse();
            diffIntervals.add(curDiffInterval);
        }
        Collections.reverse(diffIntervals);
        return new DiffResult(diffIntervals, n, m);
    }

    private static boolean isFirstGreater(int firstLCSLength, int secondLCSLength,
                                          LCSParent firstLCSParent) {
        if (firstLCSLength > secondLCSLength)
            return true;
        if (firstLCSLength < secondLCSLength)
            return false;
        return firstLCSParent == LCSParent.BOTH;
    }

    enum LCSParent {
        FIRST,
        SECOND,
        BOTH
    }
}
