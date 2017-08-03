package com.github.vedunz.difftool.ui;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by vedun on 24.07.2017.
 */
public class VersionManager {

    private final AtomicLong version = new AtomicLong(0);

    public void textUpdated() {
        version.incrementAndGet();
    }

    public long getVersion() {
        return version.get();
    }

}
