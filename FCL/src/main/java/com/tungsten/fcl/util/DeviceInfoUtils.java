package com.tungsten.fcl.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.jaredrummler.android.device.DeviceName;
import com.tungsten.fcl.BuildConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeviceInfoUtils{
    private LinkedHashMap<String, String> map;
    private Context context;

    public DeviceInfoUtils(Context context){
        map = new LinkedHashMap<>();
        this.context = context;

        try{
            readDeviceInfo();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void readDeviceInfo() throws IOException {
        map.put("Device-Name", DeviceName.getDeviceName());

        map.put("Android-Version", "Android " + Build.VERSION.RELEASE);

        map.put("Launcher-Version", BuildConfig.VERSION_NAME);

        map.put("SOC-Information", Build.HARDWARE);

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
        map.put("Device-RAM-Size", ((memoryInfo.totalMem + 1073741824 - 1) / 1073741824) + "GB");

        map.put("Device-Arch", System.getProperty("os.arch") + "(" + Build.SUPPORTED_ABIS[0] + ")");

        map.put("Build-Version", Build.VERSION.INCREMENTAL);
    }

    @NonNull @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, String> entry : map.entrySet()) stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");

        return stringBuilder.deleteCharAt(stringBuilder.length() - 2).toString();
    }
}