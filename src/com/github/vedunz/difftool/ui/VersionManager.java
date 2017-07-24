package com.github.vedunz.difftool.ui;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by vedun on 24.07.2017.
 */
public class VersionManager {

    Lock lock = new ReentrantLock();

    private volatile long version = 0;

    public void textUpdated()
    {
        lock.lock();
        version++;
        lock.unlock();
    }

    public long getVersion() {
        lock.lock();
        long value = version;
        lock.unlock();
        return value;
    }

}
