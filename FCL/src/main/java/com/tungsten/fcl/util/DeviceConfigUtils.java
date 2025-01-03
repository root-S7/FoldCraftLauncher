package com.tungsten.fcl.util;

import android.os.Build;

import com.tungsten.fcl.BuildConfig;
import com.tungsten.fclauncher.FCLauncher;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class DeviceConfigUtils {
    public static final LinkedHashMap<String, String> storageInfo = new LinkedHashMap<>();

    private void addDeviceRecord() {
        storageInfo.put("Device", Build.MANUFACTURER + "(" + Build.PRODUCT + ")");
        storageInfo.put("Android", String.valueOf(Build.VERSION.RELEASE));
        storageInfo.put("SDK", String.valueOf(Build.VERSION.SDK_INT));
        storageInfo.put("CPU", FCLauncher.getSocName());
        storageInfo.put("Launcher", "FCL_" + BuildConfig.VERSION_NAME);
    }

    public static String toText(){
        if(storageInfo.isEmpty()) new DeviceConfigUtils().addDeviceRecord();

        return storageInfo.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }
}
