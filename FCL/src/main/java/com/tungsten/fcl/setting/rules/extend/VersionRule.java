package com.tungsten.fcl.setting.rules.extend;

import static com.tungsten.fcl.setting.rules.GameRulesManager.*;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class VersionRule {
    @SerializedName("memory")
    private final MemoryRule memory;
    @SerializedName("renderer")
    private final RendererRule renderer;
    @SerializedName("java")
    private final JavaRule java;

    public VersionRule() {
        this.memory = null;
        this.renderer = null;
        this.java = null;
    }

    public VersionRule(MemoryRule memory, RendererRule renderer, JavaRule java) {
        this.memory = memory;
        this.renderer = renderer;
        this.java = java;
    }

    public MemoryRule getMemory() {
        return memory;
    }

    public RendererRule getRenderer() {
        return renderer;
    }

    public JavaRule getJava() {
        return java;
    }

    @NonNull @Override
    public String toString() {
        return getGSON().toJson(this);
    }
}
