package com.tungsten.fcl.setting.rules;

import static com.tungsten.fcl.util.AndroidUtils.isRegexMatch;
import static com.tungsten.fclauncher.utils.FCLPath.ASSETS_LAUNCHER_RULES;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.mio.data.Renderer;
import com.tungsten.fcl.setting.rules.extend.VersionRule;
import com.tungsten.fcl.util.gson.RuleJavaSetAdapter;
import com.tungsten.fcl.util.gson.RuleRendererSetAdapter;
import com.tungsten.fclcore.game.JavaVersion;
import com.tungsten.fclcore.util.gson.URLTypeAdapter;
import com.tungsten.fclcore.util.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class LauncherRules {
    @SerializedName("launcherRules")
    private final LinkedHashMap<String, VersionRule> launcherRules = new LinkedHashMap<>();
    public final static Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(URL.class, new URLTypeAdapter())
            .registerTypeAdapter(new TypeToken<LinkedHashSet<Renderer>>(){}.getType(), new RuleRendererSetAdapter())
            .registerTypeAdapter(new TypeToken<LinkedHashSet<JavaVersion>>(){}.getType(), new RuleJavaSetAdapter())
            .create();
    public Map<String, VersionRule> getLauncherRules() {
        return launcherRules;
    }

    public VersionRule getVersionRule(String version) {
        return launcherRules.getOrDefault(version, launcherRules.entrySet()
                        .stream()
                        .filter(entry -> isRegexMatch(version, entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(new VersionRule())
        );
    }

    public LinkedHashSet<VersionRule> getVersionRules(String version) {
        return launcherRules.entrySet()
                .stream()
                .filter(entry -> version.equals(entry.getKey()) || isRegexMatch(version, entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static LauncherRules fromJson(String jsonString) {
        try {
            return GSON.fromJson(jsonString, LauncherRules.class);
        }catch(Exception ex) {
            return new LauncherRules();
        }
    }

    public static LauncherRules fromJson(Context context) {
        try(InputStream open = context.getAssets().open(ASSETS_LAUNCHER_RULES)) {
            return LauncherRules.fromJson(IOUtils.readFullyAsString(open));
        }catch(Exception ex) {
            return new LauncherRules();
        }
    }

    @NonNull @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
