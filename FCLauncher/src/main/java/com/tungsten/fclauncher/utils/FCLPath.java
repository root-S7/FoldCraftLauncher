package com.tungsten.fclauncher.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class FCLPath {

    public static Context CONTEXT;

    public static String NATIVE_LIB_DIR;

    public static String LOG_DIR;
    public static String CACHE_DIR;

    public static String RUNTIME_DIR;
    public static String JAVA_8_PATH;
    public static String JAVA_11_PATH;
    public static String JAVA_17_PATH;
    public static String JAVA_21_PATH;
    public static String JAVA_PATH;
    public static String JNA_PATH;
    public static String LWJGL_DIR;
    public static String CACIOCAVALLO_8_DIR;
    public static String CACIOCAVALLO_11_DIR;
    public static String CACIOCAVALLO_17_DIR;

    public static String CONFIG_DIR;

    public static Properties GENERAL_SETTING = new Properties();

    public static String FILES_DIR;
    public static String PLUGIN_DIR;
    public static String BACKGROUND_DIR;
    public static String CONTROLLER_DIR;

    public static String PRIVATE_COMMON_DIR;
    public static String SHARED_COMMON_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FCL/.minecraft";

    public static String AUTHLIB_INJECTOR_PATH;
    public static String LIB_PATCHER_PATH;
    public static String MIO_LAUNCH_WRAPPER;
    public static String LT_BACKGROUND_PATH;
    public static String DK_BACKGROUND_PATH;

    public static String ASSETS_AUTHLIB_INJECTOR_JAR = "game/authlib-injector.jar";
    public static String ASSETS_GENERAL_SETTING_PROPERTIES = "app_config/general_setting.properties";
    public static String ASSETS_CONFIG_JSON = "app_config/config.json";
    public static String ASSETS_MENU_SETTING_JSON = "app_config/menu_setting.json";
    public static String ASSETS_AUTH_INJECTOR_SERVER_JSON = "app_config/authlib_injector_server.json";
    public static String ASSETS_DEFAULT_CONTROLLER = "controllers/00000000.json";

    public static String ASSETS_SETTING_LAUNCHER_PICTURES = "app_config/settings_launcher_pictures";

    public static void loadPaths(Context context) {
        CONTEXT = context;

        try {
            GENERAL_SETTING.load(context.getAssets().open("app_config/general_setting.properties"));
        }catch(Exception e) {
            GENERAL_SETTING = new Properties();
        }

        NATIVE_LIB_DIR = context.getApplicationInfo().nativeLibraryDir;

        LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FCL/log";
        CACHE_DIR = context.getCacheDir() + "/fclauncher";

        RUNTIME_DIR = context.getDir("runtime", 0).getAbsolutePath();
        JAVA_PATH = RUNTIME_DIR + "/java";
        JAVA_8_PATH = RUNTIME_DIR + "/java/jre8";
        JAVA_11_PATH = RUNTIME_DIR + "/java/jre11";
        JAVA_17_PATH = RUNTIME_DIR + "/java/jre17";
        JAVA_21_PATH = RUNTIME_DIR + "/java/jre21";
        JNA_PATH = RUNTIME_DIR + "/jna";
        LWJGL_DIR = RUNTIME_DIR + "/lwjgl";
        CACIOCAVALLO_8_DIR = RUNTIME_DIR + "/caciocavallo";
        CACIOCAVALLO_11_DIR = RUNTIME_DIR + "/caciocavallo11";
        CACIOCAVALLO_17_DIR = RUNTIME_DIR + "/caciocavallo17";

        CONFIG_DIR = context.getDir("config", 0).getAbsolutePath();

        FILES_DIR = context.getFilesDir().getAbsolutePath();
        PLUGIN_DIR = FILES_DIR + "/plugins";
        BACKGROUND_DIR = FILES_DIR + "/background";
        CONTROLLER_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + GENERAL_SETTING.getProperty("controller-dir", "FCL-Server") + "/control";

        PRIVATE_COMMON_DIR = context.getExternalFilesDir(".minecraft").getAbsolutePath();

        AUTHLIB_INJECTOR_PATH = PLUGIN_DIR + "/authlib-injector.jar";
        LIB_PATCHER_PATH = PLUGIN_DIR + "/MioLibPatcher.jar";
        MIO_LAUNCH_WRAPPER = PLUGIN_DIR + "/MioLaunchWrapper.jar";
        LT_BACKGROUND_PATH = BACKGROUND_DIR + "/lt.png";
        DK_BACKGROUND_PATH = BACKGROUND_DIR + "/dk.png";

        init(LOG_DIR);
        init(CACHE_DIR);
        init(RUNTIME_DIR);
        init(JAVA_8_PATH);
        init(JAVA_11_PATH);
        init(JAVA_17_PATH);
        init(JAVA_21_PATH);
        init(LWJGL_DIR);
        init(CACIOCAVALLO_8_DIR);
        init(CACIOCAVALLO_11_DIR);
        init(CACIOCAVALLO_17_DIR);
        init(CONFIG_DIR);
        init(FILES_DIR);
        init(PLUGIN_DIR);
        init(BACKGROUND_DIR);
        init(CONTROLLER_DIR);
        init(PRIVATE_COMMON_DIR);
        init(SHARED_COMMON_DIR);
    }

    private static boolean init(String path) {
        if (!new File(path).exists()) {
            return new File(path).mkdirs();
        }
        return true;
    }

}
