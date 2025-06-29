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

import static com.tungsten.fclcore.util.Logging.LOG;
import static com.tungsten.fclcore.util.io.FileUtils.checkPermission;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.tungsten.fcl.R;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.fakefx.beans.property.MapProperty;
import com.tungsten.fclcore.util.InvocationDispatcher;
import com.tungsten.fclcore.util.Lang;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fclcore.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;
import java.util.logging.Level;

public final class ConfigHolder {

    private ConfigHolder() {
    }

    public static final Path CONFIG_PATH = new File(FCLPath.FILES_DIR + "/config.json").toPath();

    private static Config configInstance, innerConfigInstance;
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

    public static Config innerConfig() {
        if (innerConfigInstance == null) {
            throw new IllegalStateException("APK内部的“config.json”文件需要进入到启动器安装步骤才能读取到，跳过该步骤将无效！");
        }
        return innerConfigInstance;
    }

    public static boolean isNewlyCreated() {
        return newlyCreated;
    }

    public synchronized static void init() throws IOException {
        if (configInstance != null) {
            return;
        }

        configInstance = loadConfig(false);
        configInstance.addListener(source -> markConfigDirty());

        Settings.init();

        if (newlyCreated) {
            saveConfigSync();
        }
    }

    /**
     * 临时初始化内外部的config.json文件，使用完毕后需要手动释放
    **/
    public synchronized static void initWithTemp() throws IOException {
        if(configInstance == null) configInstance = loadConfig(false);
        if(innerConfigInstance == null) innerConfigInstance = loadConfig(true);

        validateProfile(innerConfigInstance);
        validateProfile(configInstance);
    }

    /**
     * 读取配置文件
     * @param innerConfig 若为“true”则读取APK内部的配置文件
     * @return 成功读取的配置文件对象
     * @throws IOException 读取错误
    **/
    private static Config loadConfig(boolean innerConfig) throws IOException {
        if(!innerConfig) {
            if (Files.exists(CONFIG_PATH)) {
                try {
                    String content = FileUtils.readText(CONFIG_PATH);
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
        }else {
            try(InputStream is = FCLPath.CONTEXT.getAssets().open(FCLPath.ASSETS_CONFIG_JSON)) {
                String innerString = IOUtils.readFullyAsString(is);
                Config config = Config.fromJson(innerString);

                return config == null ? new Config() : config;
            }catch(Exception e) {
                return new Config();
            }
        }
    }

    private static final InvocationDispatcher<String> configWriter = InvocationDispatcher.runOn(Lang::thread, content -> {
        try {
            writeToConfig(content);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to save config", e);
        }
    });

    public static void writeToConfig(String content) throws IOException {
        LOG.info("Saving config");
        synchronized (CONFIG_PATH) {
            FileUtils.saveSafely(CONFIG_PATH, content);
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
    public static Config validateProfile(@NonNull Config config) {
        String selected = config.getSelectedProfile();
        MapProperty<String, Profile> configurations = config.getConfigurations();

        boolean valid = Optional.ofNullable(selected)
                .filter(s -> !s.isBlank())
                .filter(configurations::containsKey)
                .map(configurations::get)
                .map(Profile::getGameDir)
                .map(File::getPath)
                .map(FileUtils::checkPermission)
                .orElse(false);

        if (!valid) {
            if(selected != null) configurations.remove(selected);


            String privateName = FCLPath.CONTEXT.getString(R.string.profile_private);
            Profile privateProfile = new Profile("global", new File(FCLPath.PRIVATE_COMMON_DIR));
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
    public static File getSelectedPath(@NonNull Config config) {
        String selected = config.getSelectedProfile();
        MapProperty<String, Profile> configurations = config.getConfigurations();

        return Optional.ofNullable(selected)
                .filter(s -> !s.isBlank())
                .filter(configurations::containsKey)
                .map(configurations::get)
                .map(Profile::getGameDir)
                .filter(gameDir -> checkPermission(gameDir.getAbsolutePath()))
                .orElse(new File(FCLPath.PRIVATE_COMMON_DIR));
    }

    /**
     * 将某个Config对象写入到外部文件
     * @param config 提供一个Config（非空）
     * @return 若成功则返回true
    **/
    public static boolean saveConfig(@NonNull Config config) {
        try {
            configInstance = validateProfile(config);
            saveConfigSync();

            configInstance = null;
            return true;
        }catch(Exception e) {
            return false;
        }
    }

    public static void setNull() {
        configInstance = null;
        innerConfigInstance = null;
    }
}
