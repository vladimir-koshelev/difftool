package com.github.vedunz.difftool.diff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by vedun on 26.07.2017.
 */
public class DiffResultTests {

    @Test
    public void TestIntervalOperations() {
        Interval interval = new Interval(10, 20);
        assertTrue(interval.isLineAfter(21));
        assertTrue(interval.isLineBefore(9));
        assertFalse(interval.isLineAfter(20));
        assertFalse(interval.isLineBefore(10));
        assertTrue(interval.isLineInside(20));
        assertTrue(interval.isLineInside(10));
    }

    @Test
    public void TestDiffIntervalOperations() {
        DiffInterval diffInterval = new MutableDiffInterval(10, 11);
        assertTrue(diffInterval.getInterval(true).equals(diffInterval.getFirstInterval()));
        assertTrue(diffInterval.getInterval(false).equals(diffInterval.getSecondInterval()));
        assertTrue(diffInterval.getFirstInterval().equals(new Interval(10, 10)));
        assertTrue(diffInterval.getSecondInterval().equals(new Interval(11, 11)));
    }


}
