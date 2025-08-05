package com.tungsten.fcl.setting.rules.extend;

import static com.tungsten.fcl.util.RuleCheckState.*;

import androidx.annotation.NonNull;

import com.google.gson.annotations.*;
import com.mio.JavaManager;
import com.tungsten.fcl.setting.VersionSetting;
import com.tungsten.fcl.util.RuleCheckState;
import com.tungsten.fcl.util.gson.RuleJavaSetAdapter;
import com.tungsten.fclcore.game.JavaVersion;

import java.net.URL;
import java.util.*;

public class JavaRule extends RuleBase {
    @SerializedName("useJava") @JsonAdapter(RuleJavaSetAdapter.class)
    private final LinkedHashSet<JavaVersion> requiredJava;
    @SerializedName("downloadURL")
    private final URL downloadURL;
    private transient String useJava;

    public JavaRule() {
        super(null);
        this.requiredJava = null;
        this.downloadURL = null;
    }

    public JavaRule(LinkedHashSet<JavaVersion> useJava, URL downloadURL, String tip) {
        super(tip);
        this.requiredJava = useJava;
        this.downloadURL = downloadURL;
    }

    public LinkedHashSet<JavaVersion> getRequiredJava() {
        return requiredJava;
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    @Override
    public boolean canDetectRule() {
        return requiredJava != null && !requiredJava.isEmpty();
    }

    @Override
    public RuleCheckState setRule(@NonNull VersionSetting setting) {
        super.setRule(setting);
        if(!canDetectRule()) return NO_CHANGE;

        return Objects.requireNonNull(requiredJava)
                .stream()
                .map(java -> {
                    if (java.isEqual(setting.getJava())) return NO_CHANGE;
                    else if (JavaManager.getJava(java.getName()) != null) {
                        setting.setJava(java.getName());
                        return SUCCESS;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(UNKNOWN);
    }

    @Override
    protected void initPlaceholders(@NonNull VersionSetting setting) {
        useJava = setting.getJava();
    }
}
