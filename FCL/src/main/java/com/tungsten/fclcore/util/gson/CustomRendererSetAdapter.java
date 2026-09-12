package com.tungsten.fclcore.util.gson;

import static com.mio.plugin.RendererPlugin.parseAndCollect;
import static com.mio.util.CustomRendererLoaderKt.buildApplicationInfo;
import static com.mio.util.CustomRendererLoaderKt.buildBundle;
import static com.mio.util.CustomRendererLoaderKt.checkRendererSo;
import static com.tungsten.fclcore.util.StringUtils.getStringValue;

import com.mio.data.Renderer;

import java.util.*;

import com.google.gson.*;
import com.mio.plugin.PluginManager;

import java.lang.reflect.Type;

public class CustomRendererSetAdapter implements JsonSerializer<Set<Renderer>>, JsonDeserializer<Set<Renderer>> {

    @Override
    public JsonElement serialize(Set<Renderer> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (Renderer renderer : src) {
            array.add(renderer.getDes());
        }
        return array;
    }

    @Override
    public Set<Renderer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if(!json.isJsonObject()) return Collections.emptySet();

        var fakeApps = new ArrayList<PluginManager.PluginApp>();
        var jsonObject = json.getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if(!entry.getValue().isJsonObject()) continue;
            var appJson = entry.getValue().getAsJsonObject();

            var label = getStringValue(appJson, "label");
            if(label == null || label.trim().isEmpty()) label = entry.getKey();

            var versionName = getStringValue(appJson, "versionName");
            if(versionName == null) versionName = "1.0";

            var metaDataJson = appJson.getAsJsonObject("meta-data");
            if(metaDataJson == null) continue;

            PluginManager.PluginApp fakeApp = new PluginManager.PluginApp(entry.getKey(), label, versionName, null, Set.of(PluginManager.PluginType.RENDERER), buildApplicationInfo(entry.getKey(), buildBundle(metaDataJson)), System.currentTimeMillis());
            fakeApps.add(fakeApp);
        }

        var renderers = parseAndCollect(fakeApps);
        renderers.removeIf(r -> !checkRendererSo(r));
        return renderers;
    }
}