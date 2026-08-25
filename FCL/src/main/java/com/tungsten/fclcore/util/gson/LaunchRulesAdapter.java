package com.tungsten.fclcore.util.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.*;
import com.mio.data.Renderer;
import com.tungsten.fcl.setting.rule.core.LaunchRule;
import com.tungsten.fcl.setting.rule.launch.*;
import com.tungsten.fclcore.game.JavaVersion;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class LaunchRulesAdapter extends TypeAdapter<LinkedHashMap<String, Set<LaunchRule>>> {

    private final Gson RULE_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(URL.class, new URLTypeAdapter())
            .registerTypeAdapter(new TypeToken<LinkedHashSet<Renderer>>() {}.getType(), new RuleRendererSetAdapter())
            .registerTypeAdapter(new TypeToken<LinkedHashSet<JavaVersion>>() {}.getType(), new RuleJavaSetAdapter())
            .create();

    @Override
    public void write(JsonWriter out, LinkedHashMap<String, Set<LaunchRule>> value) throws IOException {}

    @Override
    public LinkedHashMap<String, Set<LaunchRule>> read(JsonReader in) throws IOException {
        LinkedHashMap<String, Set<LaunchRule>> result = new LinkedHashMap<>();
        JsonObject root = JsonParser.parseReader(in)
                .getAsJsonObject();

        for(var versionEntry : root.entrySet()) {
            Set<LaunchRule> rules = new HashSet<>();
            JsonObject ruleObject = versionEntry.getValue()
                    .getAsJsonObject();

            for(var ruleEntry : ruleObject.entrySet()) {
                LaunchRule rule = switch(ruleEntry.getKey()) {
                    case "memory" -> RULE_GSON.fromJson(ruleEntry.getValue(), Memory.class);
                    case "renderer" -> RULE_GSON.fromJson(ruleEntry.getValue(), GLRender.class);
                    case "java" -> RULE_GSON.fromJson(ruleEntry.getValue(), Java.class);
                    default -> null;
                };

                if(rule != null) rules.add(rule);
            }
            result.put(versionEntry.getKey(), rules);
        }
        return result;
    }
}