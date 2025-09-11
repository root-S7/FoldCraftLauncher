/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2020  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tungsten.fcl.setting;

import static com.tungsten.fcl.setting.Config.*;
import static com.tungsten.fclauncher.utils.FCLPath.*;
import static com.tungsten.fclcore.util.Logging.LOG;
import static com.tungsten.fclcore.util.io.FileUtils.*;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.tungsten.fcl.R;
import com.tungsten.fclcore.fakefx.beans.property.MapProperty;
import com.tungsten.fclcore.util.InvocationDispatcher;
import com.tungsten.fclcore.util.Lang;
import com.tungsten.fclcore.util.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.util.Optional;
import java.util.logging.Level;

public final class ConfigHolder {

    private ConfigHolder() {
    }

    public static final Path CONFIG_PATH = new File(FILES_DIR + "/config.json").toPath();

    private static Config configInstance;
    private static boolean newlyCreated;

    public static boolean isInit() {
        return configInstance != null;
    }

    public static Config config() {
        if (configInstance == null) {
            throw new IllegalStateException("config.json文件只会在进入启动器主界面时候才会解析，你在主界面之前读取该配置文件属于非法操作！");
        }
        return configInstance;
    }

    public static boolean isNewlyCreated() {
        return newlyCreated;
    }

    public synchronized static void init() throws IOException {
        if (configInstance != null) {
            return;
        }

        configInstance = loadConfig();
        configInstance.addListener(source -> markConfigDirty());

        Settings.init();

        if (newlyCreated) {
            saveConfigSync();
        }
    }

    /**
     * 临时初始化外部的config.json文件并返回
     * 注意，会先从configInstance获取
     * 如果无法获取则通过CONFIG_PATH路径获取，若仍然无法获取则返回null
     * 同时，若外部文件格式有异常则会尝试删除！
    **/
    public synchronized static Config initTempConfig() {
        if(configInstance != null) return validateProfile(configInstance);

        try {
            return fromJson(readText(CONFIG_PATH));
        }catch(Exception ex) {
            if(!(ex instanceof FileNotFoundException)) {
                FileUtils.forceDelete();
            }
        }
        return null;
    }

    private static Config loadConfig() throws IOException {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String content = readText(CONFIG_PATH);
                Config deserialized = Config.fromJson(content);
                if (deserialized == null) {
                    LOG.info("Config is empty");
                } else {
                    return deserialized;
                }
            } catch (JsonParseException e) {
                LOG.log(Level.WARNING, "Malformed config.", e);
            }
        }

        LOG.info("Creating an empty config");
        newlyCreated = true;
        return new Config();
    }

    private static final InvocationDispatcher<String> configWriter = InvocationDispatcher.runOn(Lang::thread, content -> {
        try {
            writeToConfig(content);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to save config", e);
        }
    });

    private static void writeToConfig(String content) throws IOException {
        LOG.info("Saving config");
        synchronized (CONFIG_PATH) {
            FileUtils.saveSafely(CONFIG_PATH, content);
        }
    }

    public static void writeToConfig(@NonNull Config config) throws IOException {
        LOG.info("Saving config");
        synchronized (CONFIG_PATH) {
            FileUtils.saveSafely(CONFIG_PATH, config.toJson());
        }
    }

    static void markConfigDirty() {
        configWriter.accept(configInstance.toJson());
    }

    private static void saveConfigSync() throws IOException {
        writeToConfig(configInstance.toJson());
    }

    /**
     * 校验传入的 config 中 selectedProfile 是否有效：
     * - 名称不为空且存在于 configurations 中
     * - 对应 Profile 的 gameDir 路径存在且读写权限有效
     * 如果无效，则删除该配置，同时清空 selectedProfile，
     * 然后添加一个新的默认私有目录配置，并设置为 selectedProfile。
    **/
    public static Config validateProfile(Config config) {
        if(config == null) config = new Config();
        String selected = config.getSelectedProfile();
        MapProperty<String, Profile> configurations = config.getConfigurations();

        if (!validateSelectedPath(config)) {
            if(selected != null) configurations.remove(selected);


            String privateName = CONTEXT.getString(R.string.profile_private);
            Profile privateProfile = new Profile("global", new File(PRIVATE_COMMON_DIR));
            configurations.put(privateName, privateProfile);
            config.setSelectedProfile(privateName);
        }

        return config;
    }

    /**
     * 获取 config 中选中配置的 gameDir 路径。
     * - 若 selectedProfile 有效（存在且目录有读写权限），返回其路径；
     * - 否则返回默认路径 FCLPath.PRIVATE_COMMON_DIR。
    **/
    public static File getSelectedPath(Config config) {
        if(config == null) config = new Config();
        String selected = config.getSelectedProfile();
        MapProperty<String, Profile> configurations = config.getConfigurations();

        return Optional.ofNullable(selected)
                .filter(s -> !s.isBlank())
                .filter(configurations::containsKey)
                .map(configurations::get)
                .map(Profile::getGameDir)
                .filter(gameDir -> checkPermission(gameDir.getAbsolutePath()))
                .orElse(new File(PRIVATE_COMMON_DIR));
    }

    /**
     * 校验传入的路径是否有效：
     * - 不为空
     * - 对应目录存在
     * - 拥有读写权限
    **/
    public static boolean validateSelectedPath(@NonNull Config config) {
        String selected = config.getSelectedProfile();
        MapProperty<String, Profile> configurations = config.getConfigurations();

        return Optional.ofNullable(selected)
                .filter(s -> !s.isBlank())
                .filter(configurations::containsKey)
                .map(configurations::get)
                .map(Profile::getGameDir)
                .map(File::getPath)
                .map(FileUtils::checkPermission)
                .orElse(false);
    }
}
