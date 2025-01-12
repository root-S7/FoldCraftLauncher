package com.tungsten.fcl.util;

import android.os.Build;

import com.tungsten.fcl.BuildConfig;
import com.tungsten.fclauncher.FCLauncher;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class DeviceConfigUtils {
    public static final LinkedHashMap<String, String> Device_INFO = new LinkedHashMap<>();

    private void addDeviceRecord() {
        Device_INFO.put("Device", Build.MANUFACTURER + "(" + Build.PRODUCT + ")");
        Device_INFO.put("Android", String.valueOf(Build.VERSION.RELEASE));
        Device_INFO.put("SDK", String.valueOf(Build.VERSION.SDK_INT));
        Device_INFO.put("CPU", FCLauncher.getSocName());
        Device_INFO.put("Launcher", "FCL_" + BuildConfig.VERSION_NAME);
    }

    public static String toText(){
        if(Device_INFO.isEmpty()) new DeviceConfigUtils().addDeviceRecord();

        return Device_INFO.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }
}
