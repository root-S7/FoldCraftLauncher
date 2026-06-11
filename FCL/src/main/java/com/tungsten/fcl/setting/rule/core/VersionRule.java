package com.tungsten.fcl.setting.rule.core;

import static com.mio.manager.GameRulesManager.*;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.tungsten.fcl.setting.rule.JavaRule;
import com.tungsten.fcl.setting.rule.MemoryRule;
import com.tungsten.fcl.setting.rule.GlRendererRule;

public record VersionRule(
        @SerializedName("memory") MemoryRule memory,
        @SerializedName("renderer") GlRendererRule glRenderer,
        @SerializedName("java") JavaRule java
) {

    public VersionRule() {
        this(null, null, null);
    }

    @Override
    public MemoryRule memory() {
        return memory;
    }

    @Override
    public GlRendererRule glRenderer() {
        return glRenderer;
    }

    @Override
    public JavaRule java() {
        return java;
    }

    @NonNull
    @Override
    public String toString() {
        return getGSON().toJson(this);
    }
}
