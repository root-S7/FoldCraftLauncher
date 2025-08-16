package com.tungsten.fcl.util.gson;

import static com.tungsten.fcl.util.AndroidUtils.getStringValue;

import com.mio.data.Renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.*;
import com.tungsten.fclcore.util.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

public class CustomRendererSetAdapter implements JsonSerializer<Set<Renderer>>, JsonDeserializer<Set<Renderer>> {
    private final String libDir;

    public CustomRendererSetAdapter(String libDir) {
        this.libDir = libDir;
    }

    @Override
    public JsonElement serialize(Set<Renderer> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();
        for (Renderer renderer : src) {
            array.add(renderer.getDes());
        }
        return array;
    }

    @Override
    public Set<Renderer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Set<Renderer> rendererSet = new HashSet<>();
        if(!json.isJsonObject()) return rendererSet;

        JsonObject jsonObject = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String packageName = entry.getKey();
            JsonElement rendererElement = entry.getValue();

            if(!rendererElement.isJsonObject()) continue;
            JsonObject rendererJson = rendererElement.getAsJsonObject();
            String des = getStringValue(rendererJson, "des");
            String renderer = getStringValue(rendererJson, "renderer");
            String boatString = getStringValue(rendererJson, "boatEnv");
            String pojavString = getStringValue(rendererJson, "pojavEnv");
            String minMCVer = getStringValue(rendererJson, "minMCVer");
            String maxMCVer = getStringValue(rendererJson, "maxMCVer");
            boolean success = StringUtils.allStringsValid(packageName, des, renderer);
            if(!success || (boatString.trim().isEmpty() && pojavString.trim().isEmpty())) continue;
            
            String[] renderArr = renderer.split(":");
            Renderer rendererObj = new Renderer(
                    renderArr[0].trim(), des.trim(), renderArr[1].trim(), renderArr[2].trim(), libDir,
                    List.of(boatString.split(":")), List.of(pojavString.split(":")),
                    packageName.trim(), minMCVer.trim(), maxMCVer.trim()
            );
            rendererSet.add(rendererObj);
        }

        return rendererSet;
    }
}