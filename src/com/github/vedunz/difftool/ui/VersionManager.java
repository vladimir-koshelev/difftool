package com.github.vedunz.difftool.ui;

/**
 * Created by vedun on 24.07.2017.
 */
public class VersionManager {

    private long version = 0;

    public void textUpdated() {
        version++;
    }

    public long getVersion() {
        return version;
    }

}
