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
package com.tungsten.fclcore.mod.multimc;

import com.tungsten.fclcore.mod.ModpackManifest;
import com.tungsten.fclcore.mod.ModpackProvider;
import com.tungsten.fclcore.util.Lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

public final class MultiMCInstanceConfiguration implements ModpackManifest {

    private final String instanceType; // InstanceType
    private final String name; // name
    private final String gameVersion; // IntendedVersion
    private final Integer permGen; // PermGen
    private final String wrapperCommand; // WrapperCommand
    private final String preLaunchCommand; // PreLaunchCommand
    private final String postExitCommand; // PostExitCommand
    private final String notes; // notes
    private final String javaPath; // JavaPath
    private final String jvmArgs; // JvmArgs
    private final boolean fullscreen; // LaunchMaximized
    private final Integer width; // MinecraftWinWidth
    private final Integer height; // MinecraftWinHeight
    private final Integer maxMemory; // MaxMemAlloc
    private final Integer minMemory; // MinMemAlloc
    private final boolean showConsole; // ShowConsole
    private final boolean showConsoleOnError; // ShowConsoleOnError
    private final boolean autoCloseConsole; // AutoCloseConsole
    private final boolean overrideMemory; // OverrideMemory
    private final boolean overrideJavaLocation; // OverrideJavaLocation
    private final boolean overrideJavaArgs; // OverrideJavaArgs
    private final boolean overrideConsole; // OverrideConsole
    private final boolean overrideCommands; // OverrideCommands
    private final boolean overrideWindow; // OverrideWindow
    private final String iconKey;

    private final MultiMCManifest mmcPack;

    MultiMCInstanceConfiguration(String defaultName, InputStream contentStream, MultiMCManifest mmcPack) throws IOException {
        // instance.cfg is in .ini format. As it's like the .properties format, we use Properties to parse thie file.
        // Maybe this will provide some problems (and it does, such as https://github.com/HMCL-dev/HMCL/issues/2991), but this workaround works, doesn't it?
        Properties p = new Properties();
        p.load(new InputStreamReader(contentStream, StandardCharsets.UTF_8));

        this.mmcPack = mmcPack;

        instanceType = readValue(p, "InstanceType");
        autoCloseConsole = Boolean.parseBoolean(readValue(p, "AutoCloseConsole"));
        gameVersion = mmcPack != null ? mmcPack.getComponents().stream().filter(e -> "net.minecraft".equals(e.getUid())).findAny()
                .orElseThrow(() -> new IOException("Malformed mmc-pack.json")).getVersion() : readValue(p, "IntendedVersion");
        javaPath = readValue(p, "JavaPath");
        jvmArgs = readValue(p, "JvmArgs");
        fullscreen = Boolean.parseBoolean(readValue(p, "LaunchMaximized"));
        maxMemory = Lang.toIntOrNull(readValue(p, "MaxMemAlloc"));
        minMemory = Lang.toIntOrNull(readValue(p, "MinMemAlloc"));
        height = Lang.toIntOrNull(readValue(p, "MinecraftWinHeight"));
        width = Lang.toIntOrNull(readValue(p, "MinecraftWinWidth"));
        overrideCommands = Boolean.parseBoolean(readValue(p, "OverrideCommands"));
        overrideConsole = Boolean.parseBoolean(readValue(p, "OverrideConsole"));
        overrideJavaArgs = Boolean.parseBoolean(readValue(p, "OverrideJavaArgs"));
        overrideJavaLocation = Boolean.parseBoolean(readValue(p, "OverrideJavaLocation"));
        overrideMemory = Boolean.parseBoolean(readValue(p, "OverrideMemory"));
        overrideWindow = Boolean.parseBoolean(readValue(p, "OverrideWindow"));
        permGen = Lang.toIntOrNull(readValue(p, "PermGen"));
        postExitCommand = readValue(p, "PostExitCommand");
        preLaunchCommand = readValue(p, "PreLaunchCommand");
        showConsole = Boolean.parseBoolean(readValue(p, "ShowConsole"));
        showConsoleOnError = Boolean.parseBoolean(readValue(p, "ShowConsoleOnError"));
        wrapperCommand = readValue(p, "WrapperCommand");
        name = defaultName;
        notes = Optional.ofNullable(readValue(p, "notes")).orElse("");
        iconKey = readValue(p, "iconKey");
    }

    /**
     * A workaround for <a href="https://github.com/HMCL-dev/HMCL/issues/2991">...</a>
     */
    private String readValue(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null) {
            return null;
        }
        int l = value.length();
        if (l >= 2 && value.charAt(0) == '"' && value.charAt(l - 1) == ':') {
            return value.substring(0, l - 1);
        }
        return value;
    }

    public MultiMCInstanceConfiguration(String instanceType, String name, String gameVersion, Integer permGen, String wrapperCommand, String preLaunchCommand, String postExitCommand, String notes, String javaPath, String jvmArgs, boolean fullscreen, Integer width, Integer height, Integer maxMemory, Integer minMemory, boolean showConsole, boolean showConsoleOnError, boolean autoCloseConsole, boolean overrideMemory, boolean overrideJavaLocation, boolean overrideJavaArgs, boolean overrideConsole, boolean overrideCommands, boolean overrideWindow, String iconKey) {
        this.instanceType = instanceType;
        this.name = name;
        this.gameVersion = gameVersion;
        this.permGen = permGen;
        this.wrapperCommand = wrapperCommand;
        this.preLaunchCommand = preLaunchCommand;
        this.postExitCommand = postExitCommand;
        this.notes = notes;
        this.javaPath = javaPath;
        this.jvmArgs = jvmArgs;
        this.fullscreen = fullscreen;
        this.width = width;
        this.height = height;
        this.maxMemory = maxMemory;
        this.minMemory = minMemory;
        this.showConsole = showConsole;
        this.showConsoleOnError = showConsoleOnError;
        this.autoCloseConsole = autoCloseConsole;
        this.overrideMemory = overrideMemory;
        this.overrideJavaLocation = overrideJavaLocation;
        this.overrideJavaArgs = overrideJavaArgs;
        this.overrideConsole = overrideConsole;
        this.overrideCommands = overrideCommands;
        this.overrideWindow = overrideWindow;
        this.mmcPack = null;
        this.iconKey = iconKey;
    }

    public String getInstanceType() {
        return instanceType;
    }

    /**
     * The instance's name.
     */
    public String getName() {
        return name;
    }

    /**
     * The game version of the instance.
     */
    public String getGameVersion() {
        return gameVersion;
    }

    /**
     * The permanent generation size of JVM.
     * Nullable.
     */
    public Integer getPermGen() {
        return permGen;
    }

    /**
     * The command to launch JVM.
     */
    public String getWrapperCommand() {
        return wrapperCommand;
    }

    /**
     * The command that will be executed before game launches.
     */
    public String getPreLaunchCommand() {
        return preLaunchCommand;
    }

    /**
     * The command that will be executed after game exits.
     */
    public String getPostExitCommand() {
        return postExitCommand;
    }

    /**
     * The description of the instance.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * JVM installation location.
     */
    public String getJavaPath() {
        return javaPath;
    }

    /**
     * The JVM arguments
     */
    public String getJvmArgs() {
        return jvmArgs;
    }

    /**
     * True if Minecraft will start in fullscreen mode.
     */
    public boolean isFullscreen() {
        return fullscreen;
    }

    /**
     * The initial width of the game window.
     * Nullable.
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * The initial height of the game window.
     * Nullable.
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * The maximum memory that JVM can allocate.
     * Nullable.
     */
    public Integer getMaxMemory() {
        return maxMemory;
    }

    /**
     * The minimum memory that JVM can allocate.
     * Nullable.
     */
    public Integer getMinMemory() {
        return minMemory;
    }

    /**
     * True if show the console window when game launches.
     */
    public boolean isShowConsole() {
        return showConsole;
    }

    /**
     * True if show the console window when game crashes.
     */
    public boolean isShowConsoleOnError() {
        return showConsoleOnError;
    }

    /**
     * True if closes the console window when game stops.
     */
    public boolean isAutoCloseConsole() {
        return autoCloseConsole;
    }

    /**
     * True if {@link #getMaxMemory}, {@link #getMinMemory}, {@link #getPermGen} will come info force.
     */
    public boolean isOverrideMemory() {
        return overrideMemory;
    }

    /**
     * True if {@link #getJavaPath} will come info force.
     */
    public boolean isOverrideJavaLocation() {
        return overrideJavaLocation;
    }

    /**
     * True if {@link #getJvmArgs} will come info force.
     */
    public boolean isOverrideJavaArgs() {
        return overrideJavaArgs;
    }

    /**
     * True if {@link #isShowConsole}, {@link #isShowConsoleOnError}, {@link #isAutoCloseConsole} will come into force.
     */
    public boolean isOverrideConsole() {
        return overrideConsole;
    }

    /**
     * True if {@link #getPreLaunchCommand}, {@link #getPostExitCommand}, {@link #getWrapperCommand} will come into force.
     */
    public boolean isOverrideCommands() {
        return overrideCommands;
    }

    /**
     * True if {@link #getHeight}, {@link #getWidth}, {@link #isFullscreen} will come into force.
     */
    public boolean isOverrideWindow() {
        return overrideWindow;
    }

    public String getIconKey() {
        return iconKey;
    }

    public Properties toProperties() {
        Properties p = new Properties();
        if (instanceType != null) p.setProperty("InstanceType", instanceType);
        p.setProperty("AutoCloseConsole", Boolean.toString(autoCloseConsole));
        if (gameVersion != null) p.setProperty("IntendedVersion", gameVersion);
        if (javaPath != null) p.setProperty("JavaPath", javaPath);
        if (jvmArgs != null) p.setProperty("JvmArgs", jvmArgs);
        p.setProperty("LaunchMaximized", Boolean.toString(fullscreen));
        if (maxMemory != null) p.setProperty("MaxMemAlloc", Integer.toString(maxMemory));
        if (minMemory != null) p.setProperty("MinMemAlloc", Integer.toString(minMemory));
        if (height != null) p.setProperty("MinecraftWinHeight", Integer.toString(height));
        if (width != null) p.setProperty("MinecraftWinWidth", Integer.toString(width));
        p.setProperty("OverrideCommands", Boolean.toString(overrideCommands));
        p.setProperty("OverrideConsole", Boolean.toString(overrideConsole));
        p.setProperty("OverrideJavaArgs", Boolean.toString(overrideJavaArgs));
        p.setProperty("OverrideJavaLocation", Boolean.toString(overrideJavaLocation));
        p.setProperty("OverrideMemory", Boolean.toString(overrideMemory));
        p.setProperty("OverrideWindow", Boolean.toString(overrideWindow));
        if (permGen != null) p.setProperty("PermGen", Integer.toString(permGen));
        if (postExitCommand != null) p.setProperty("PostExitCommand", postExitCommand);
        if (preLaunchCommand != null) p.setProperty("PreLaunchCommand", preLaunchCommand);
        p.setProperty("ShowConsole", Boolean.toString(showConsole));
        p.setProperty("ShowConsoleOnError", Boolean.toString(showConsoleOnError));
        if (wrapperCommand != null) p.setProperty("WrapperCommand", wrapperCommand);
        if (name != null) p.setProperty("name", name);
        if (notes != null) p.setProperty("notes", notes);
        if (iconKey != null) p.setProperty("iconKey", iconKey);
        return p;
    }

    public MultiMCManifest getMmcPack() {
        return mmcPack;
    }

    @Override
    public ModpackProvider getProvider() {
        return MultiMCModpackProvider.INSTANCE;
    }

}
