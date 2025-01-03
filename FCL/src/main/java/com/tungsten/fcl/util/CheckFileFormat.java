package com.tungsten.fcl.util;

import com.tungsten.fcl.setting.ConfigHolder;
import com.tungsten.fclauncher.utils.FCLPath;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class CheckFileFormat {

    /**
     * 检查文件格式是否正确
    **/
    public static Set<FileInfo> defaultCheckFiles = Set.of(
            new FileInfo(FCLPath.ASSETS_CONFIG_JSON, ConfigHolder.CONFIG_PATH),
            new FileInfo(FCLPath.ASSETS_MENU_SETTING_JSON, Paths.get(FCLPath.FILES_DIR + "/menu_setting.json")),
            new FileInfo(FCLPath.ASSETS_AUTH_INJECTOR_SERVER_JSON, null),
            new FileInfo(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/lt.png", Paths.get(FCLPath.LT_BACKGROUND_PATH)),
            new FileInfo(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/dk.png", Paths.get(FCLPath.DK_BACKGROUND_PATH)),
            new FileInfo(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/cursor.png", Paths.get(FCLPath.FILES_DIR + "/cursor.png")),
            new FileInfo(FCLPath.ASSETS_SETTING_LAUNCHER_PICTURES + "/menu_icon.png", Paths.get(FCLPath.FILES_DIR + "/menu_icon.png"))
    );


    public static void checkInternalFiles() {

    }

    public static class FileInfo {
        private final String internalPath;
        private Path externalPath;

        public FileInfo(String internalPath, Path externalPath) {
            this.internalPath = internalPath;
            this.externalPath = externalPath;
        }

        public String getInternalPath() {
            return internalPath;
        }

        public Path getExternalPath() {
            return externalPath;
        }

        public void setExternalPath(Path externalPath) {
            this.externalPath = externalPath;
        }
    }
}
