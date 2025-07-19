package com.tungsten.fcl.setting.rules.extend;

import static com.tungsten.fcl.util.RuleCheckState.SUCCESS;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.tungsten.fcl.setting.VersionSetting;
import com.tungsten.fcl.util.RuleCheckState;

import java.net.URL;
import java.util.LinkedHashSet;

public class JavaRule extends RuleBase {
    @SerializedName("useJava")
    private final LinkedHashSet<String> useJava;
    @SerializedName("downloadURL")
    private final URL downloadURL;

    public JavaRule() {
        super(null);
        this.useJava = new LinkedHashSet<>();
        this.downloadURL = null;
    }

    public JavaRule(LinkedHashSet<String> useJava, URL downloadURL, String tip) {
        super(tip);
        this.useJava = useJava;
        this.downloadURL = downloadURL;
    }

    public LinkedHashSet<String> getUseJava() {
        return useJava;
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    @Override
    public boolean canDetectRule() {
        return useJava != null && !useJava.isEmpty();
    }

    @Override
    public RuleCheckState setRule(@NonNull VersionSetting setting) {
        super.setRule(setting);
        if(canDetectRule()) {

        }

        return SUCCESS;
    }

    @Override
    protected void initPlaceholders(@NonNull VersionSetting setting) {

    }
}
