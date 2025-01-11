package com.tungsten.fcl.util;

import android.util.Log;

import com.google.gson.JsonParseException;
import com.tungsten.fcl.R;
import com.tungsten.fcl.setting.Config;
import com.tungsten.fcl.setting.ConfigHolder;
import com.tungsten.fcl.setting.Profile;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.fakefx.beans.property.MapProperty;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fclcore.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConfigUtils {
    // 定义选中哪个名称（具有强制性），当“config.json”文件内设置的选中名称找不到时会替换成该名称
    public final static String DEFINE_DEFAULT_SELECT_PROFILE = FCLPath.CONTEXT.getString(R.string.profile_private);

    /**
     * 返回一个没有没有问题的Config对象
     *      * 依次查找顺序为：外存的Json文件 ——> APK内部的Json文件（需要checkAppInternalFile为真） ——> 你提供的Config对象
     *      * 注意：若外存中的“config.json”文件格式存在问题则会删除它
     *
     * @param checkAppInternalFile 是否允许从APK内部的Json文件解析
     * @param provideBackupConfig 你所提供的一个没有问题的Config对象（一般情况是通过new出来的）
     * @return 返回一个没有问题的Config
     **/
    public static Config getNoProblemConfig(boolean checkAppInternalFile, Config provideBackupConfig) {
        Config config;

        try {
            config = Config.fromJson(FileUtils.readText(ConfigHolder.CONFIG_PATH));
        }catch(JsonParseException | IOException f) {
            if(!checkAppInternalFile) config = provideBackupConfig;
            else {
                ConfigHolder.CONFIG_PATH.toFile().delete();

                config = fromAppInternalGetConfig(provideBackupConfig);
            }
        }

        return config;
    }

    /**
     * 尝试从APK内部的“config.json”文件解析，若内部的“config.json”文件也存在格式问题则返回为“provideBackupConfig”
     * @param provideBackupConfig 你所提供的一个没有问题的Config对象（一般情况是通过new出来的）
     * @return 返回一个没有问题的Config
    **/
    public static Config fromAppInternalGetConfig(Config provideBackupConfig) {
        Config config;

        try(InputStream resourceAsStream = ConfigHolder.class.getResourceAsStream("/assets/" + FCLPath.ASSETS_CONFIG_JSON)) {
            config = Config.fromJson(IOUtils.readFullyAsString(resourceAsStream));
        }catch(JsonParseException | IOException e) {
            config = provideBackupConfig;
        }

        return config;
    }

    /**
     * 检测“config.json”文件的“configurations”键值对格式是否正确
     *  规则1：选中名称合法且有对应路径信息，则会尝试判断该路径是否拥有能正常访问，若不能访问则根据选中名称修改路径并返回新的Config对象
     *  规则2：选中的名称不合法（或选中的名称不包含该配置文件），则通过“artificiallyDefinedDefaultSelectedProfile”名称设置其选中的名称和对应的配置文件
     *
     * @param config 需要检测的配置文件
     * @return 修改后的配置文件
    **/
    public static Config setConfigConfigurations(Config config) {
        String selectedProfile = config.getSelectedProfile();
        MapProperty<String, Profile> configurations = config.getConfigurations();
        if (selectedProfile != null && !selectedProfile.isBlank() && !selectedProfile.isEmpty() && configurations.containsKey(selectedProfile)) {
            String absolutePath = configurations.get(selectedProfile).getGameDir().getAbsolutePath();

            if (FileUtils.checkFileOrDirectoryPermission(absolutePath)) return config;
            else { // 若选中的名称是“R.string.profile_private”有关的则设置为私有路径，否则都为公有路径
                Profile profile = configurations.get(selectedProfile);
                profile.setGameDir(
                        new File(selectedProfile.equals(FCLPath.CONTEXT.getString(R.string.profile_private)) ? FCLPath.PRIVATE_COMMON_DIR : FCLPath.SHARED_COMMON_DIR)
                );
            }
        } else {
            config.setSelectedProfile(DEFINE_DEFAULT_SELECT_PROFILE);
            configurations.put(DEFINE_DEFAULT_SELECT_PROFILE, new Profile("global", DEFINE_DEFAULT_SELECT_PROFILE.equals(FCLPath.PRIVATE_COMMON_DIR) ? new File(FCLPath.PRIVATE_COMMON_DIR) : new File(FCLPath.SHARED_COMMON_DIR)));
        }

        return config;
    }

    // 使用 ExecutorService 执行异步任务
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    /**
     * 获取当前选中的游戏目录
     * 首先从外存中读取“config.json”文件并读取选中的游戏目录
     * 若外存中“config.json”文件格式有问题则从APK内部自带的json中读取
     * 若APK内部的json文件也有问题则尝试new一个新的
     *     注意，只要外存中的“config.json”文件格式存在问题，则会删除该文件（getNoProblemConfig方法导致的）
     *
     * @return 前选中的游戏目录
    **/
    public static String getGameDirectory() throws ExecutionException, InterruptedException {
        Future<String> future = executorService.submit(ConfigUtils::tryExternalFileGetGameDir);

        return future.get();
    }


    protected static String tryExternalFileGetGameDir() {
        Config config;

        try {
            config = ConfigHolder.config();
        } catch (IllegalStateException e) {
            try {
                config = Config.fromJson(FileUtils.readText(ConfigHolder.CONFIG_PATH));
            } catch (JsonParseException | IOException f) {
                config = getNoProblemConfig(true, new Config());
            }
        }

        return findDir(config);
    }

    protected static String findDir(Config config) {
        Config configChange = setConfigConfigurations(config);

        //writeToConfig(config.toJson());
        String absolutePath = configChange.getConfigurations().get(configChange.getSelectedProfile()).getGameDir().getAbsolutePath();
        Log.d("事件", absolutePath);
        return absolutePath;
    }
}
