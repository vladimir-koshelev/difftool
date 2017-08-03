package com.github.vedunz.difftool.ui;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by vedun on 24.07.2017.
 */
public class VersionManager {

    private final AtomicLong version = new AtomicLong(0);

    private final AtomicLong firstVersion = new AtomicLong(0);
    private final AtomicLong secondVersion = new AtomicLong(0);

    public long getVersion() {
        return version.get();
    }

    public void firstTextUpdated() { firstVersion.incrementAndGet(); textUpdated();}
    public void secondTextUpdated() { secondVersion.incrementAndGet(); textUpdated();}

    public long getFirstVersion() {return firstVersion.get();}
    public long getSecondVersion() {return secondVersion.get();}

    private void textUpdated() {
        version.incrementAndGet();
    }
}
