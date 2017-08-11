package com.github.vedunz.difftool.ui;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by vedun on 24.07.2017.
 */
public class VersionManager {

    private final AtomicLong version = new AtomicLong(0);

    public long getVersion() {
        return version.get();
    }

    public void textUpdated() {
        version.incrementAndGet();
    }
}
