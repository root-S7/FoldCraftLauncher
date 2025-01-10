package com.tungsten.fcl.setting;

import java.util.TreeMap;

public class ConfigAsFake {

    private String last = "";
    private boolean autoDownloadThreads = true;
    private int downloadThreads = 64;
    private String downloadType = DownloadProviders.DEFAULT_RAW_PROVIDER_ID;
    private boolean autoChooseDownloadType;
    private String versionListSource = "balanced";
    private TreeMap<String, Profile> configurations = new TreeMap<>();
    private int _version;

    public ConfigAsFake(String last, boolean autoDownloadThreads, int downloadThreads, String downloadType, boolean autoChooseDownloadType, String versionListSource, TreeMap<String, Profile> configurations, int _version) {
        this.last = last;
        this.autoDownloadThreads = autoDownloadThreads;
        this.downloadThreads = downloadThreads;
        this.downloadType = downloadType;
        this.autoChooseDownloadType = autoChooseDownloadType;
        this.versionListSource = versionListSource;
        this.configurations = configurations;
        this._version = _version;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public boolean isAutoDownloadThreads() {
        return autoDownloadThreads;
    }

    public void setAutoDownloadThreads(boolean autoDownloadThreads) {
        this.autoDownloadThreads = autoDownloadThreads;
    }

    public int getDownloadThreads() {
        return downloadThreads;
    }

    public void setDownloadThreads(int downloadThreads) {
        this.downloadThreads = downloadThreads;
    }

    public String getDownloadType() {
        return downloadType;
    }

    public void setDownloadType(String downloadType) {
        this.downloadType = downloadType;
    }

    public boolean isAutoChooseDownloadType() {
        return autoChooseDownloadType;
    }

    public void setAutoChooseDownloadType(boolean autoChooseDownloadType) {
        this.autoChooseDownloadType = autoChooseDownloadType;
    }

    public String getVersionListSource() {
        return versionListSource;
    }

    public void setVersionListSource(String versionListSource) {
        this.versionListSource = versionListSource;
    }

    public TreeMap<String, Profile> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(TreeMap<String, Profile> configurations) {
        this.configurations = configurations;
    }

    public int get_version() {
        return _version;
    }

    public void set_version(int _version) {
        this._version = _version;
    }
}
