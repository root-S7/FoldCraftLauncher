package com.tungsten.fcl.util;

import android.os.Build;

import com.tungsten.fcl.BuildConfig;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class DeviceConfigUtils {
    public static final LinkedHashMap<String, String> storageInfo = new LinkedHashMap<>();

    private void addDeviceRecord() {
        storageInfo.put("Device", Build.MANUFACTURER + "(" + Build.PRODUCT + ")");
        storageInfo.put("Android", String.valueOf(Build.VERSION.RELEASE));
        storageInfo.put("SDK", String.valueOf(Build.VERSION.SDK_INT));
        storageInfo.put("CPU", getSocInfo());
        storageInfo.put("Launcher", "FCL_" + BuildConfig.VERSION_NAME);
    }

    public static String toText(){
        if(storageInfo.isEmpty()) new DeviceConfigUtils().addDeviceRecord();

        return storageInfo.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private String getSocInfo() {
        try {
            Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method getMethod = systemPropertiesClass.getMethod("get", String.class, String.class);
            return (String) getMethod.invoke(null, "ro.soc.model", Build.HARDWARE);
        }catch(Exception e) {
            return Build.HARDWARE;
        }
    }
}
