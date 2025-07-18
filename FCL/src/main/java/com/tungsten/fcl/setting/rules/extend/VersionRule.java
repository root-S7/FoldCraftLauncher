package com.tungsten.fcl.setting.rules.extend;

import static com.tungsten.fcl.setting.rules.LauncherRules.GSON;

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
        this.memory = new MemoryRule();
        this.renderer = new RendererRule();
        this.java = new JavaRule();
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
        return GSON.toJson(this);
    }
}
