package com.tungsten.fcl.setting.rules.extend;

import static com.mio.manager.RendererManager.RENDERER_GL4ES;
import static com.mio.manager.RendererManager.getRendererOrNull;
import static com.tungsten.fcl.util.AndroidUtils.getFirstOrDefault;
import static com.tungsten.fcl.util.RuleCheckState.*;
import static com.tungsten.fclauncher.utils.FCLPath.CONTEXT;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.mio.data.Renderer;
import com.mio.manager.RendererManager;
import com.tungsten.fcl.setting.VersionSetting;
import com.tungsten.fcl.util.RuleCheckState;

import java.net.URL;
import java.util.*;

public class RendererRule extends RuleBase {
    @SerializedName("useRenderer")
    private final LinkedHashSet<Renderer> useRenderer;
    @SerializedName("downloadURL")
    private final URL downloadURL;
    @SerializedName("forceChange")
    private final boolean fChange;

    private transient String requiredRenderer, setRenderer;
    private static final Renderer D_RENDERER = RENDERER_GL4ES;

    public RendererRule() {
        super(null);
        this.useRenderer = null;
        this.downloadURL = null;
        this.fChange = false;
    }

    public RendererRule(LinkedHashSet<Renderer> useRenderer, URL downloadURL, String tip, boolean fChange) {
        super(tip);
        this.useRenderer = useRenderer;
        this.downloadURL = downloadURL;
        this.fChange = fChange;
    }

    public LinkedHashSet<Renderer> getUseRenderer() {
        return useRenderer;
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    public boolean isChange() {
        return fChange;
    }

    @Override
    public boolean canDetectRule() {
        return useRenderer != null && !useRenderer.isEmpty();
    }

    @Override
    public RuleCheckState setRule(@NonNull VersionSetting setting) {
        super.setRule(setting);
        boolean shouldContinue = Optional.of(setting)
                .filter(s -> canDetectRule())
                .map(s -> { RendererManager.INSTANCE.refresh(CONTEXT); return s; })
                .filter(s -> fChange || !isCurrentRendererValid(s))
                .isPresent();
        if(!shouldContinue) return NO_CHANGE;

        return Objects.requireNonNull(useRenderer)
                .stream()
                .filter(Objects::nonNull)
                .filter(renderer -> getRendererOrNull(renderer.getId()) != null)
                .findFirst()
                .map(renderer -> {
                    setting.setRenderer(renderer.getId());
                    return SUCCESS;
                })
                .orElse(UNKNOWN);
    }

    private boolean isCurrentRendererValid(VersionSetting setting) {
        return getRendererOrNull(setting.getRenderer()) != null &&
                useRenderer.stream().anyMatch(r -> r != null && r.isEqual(setting.getRenderer()));
    }

    @Override
    protected void initPlaceholders(@NonNull VersionSetting setting) {
        this.setRenderer = RendererManager.getRenderer(setting.getRenderer()).getName();

        Renderer needRenderer = getFirstOrDefault(useRenderer, D_RENDERER);
        this.requiredRenderer = needRenderer != null ? needRenderer.getName() : D_RENDERER.getName();
    }
}