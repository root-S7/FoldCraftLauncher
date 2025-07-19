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
    private transient String requiredRenderer, setRenderer;
    private static final Renderer D_RENDERER = RENDERER_GL4ES;

    public RendererRule() {
        super(null);
        this.useRenderer = new LinkedHashSet<>();
        this.downloadURL = null;
    }

    public RendererRule(LinkedHashSet<Renderer> useRenderer, LinkedHashSet<String> customRenderer, URL downloadURL, String tip) {
        super(tip);
        this.useRenderer = useRenderer;
        this.downloadURL = downloadURL;
    }

    public LinkedHashSet<Renderer> getUseRenderer() {
        return useRenderer;
    }

    public URL getDownloadURL() {
        return downloadURL;
    }

    @Override
    public boolean canDetectRule() {
        return useRenderer != null && !useRenderer.isEmpty();
    }

    @Override
    public RuleCheckState setRule(@NonNull VersionSetting setting) {
        super.setRule(setting);
        if(!canDetectRule()) return NO_CHANGE;
        RendererManager.INSTANCE.refresh(CONTEXT);

        for(Renderer renderer : useRenderer) {
            if(renderer == null) continue;

            if(getRendererOrNull(renderer.getId()) != null) {
                setting.setRenderer(renderer.getId());
                return SUCCESS;
            }
        }
        return UNKNOWN;
    }

    @Override
    protected void initPlaceholders(@NonNull VersionSetting setting) {
        this.setRenderer = RendererManager.getRenderer(setting.getRenderer()).getName();

        Renderer needRenderer = getFirstOrDefault(useRenderer, D_RENDERER);
        this.requiredRenderer = needRenderer != null ? needRenderer.getName() : D_RENDERER.getName();
    }
}