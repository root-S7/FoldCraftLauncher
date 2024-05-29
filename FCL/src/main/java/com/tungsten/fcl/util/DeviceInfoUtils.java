package com.tungsten.fcl.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
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
        map.put("Device-Name", Build.MANUFACTURER.substring(0, 1).toUpperCase() + Build.MANUFACTURER.substring(1) + "(" + Build.MODEL + ")");

        map.put("Android-Version", "Android " + Build.VERSION.RELEASE);

        map.put("Launcher-Version", BuildConfig.VERSION_NAME);

        try{
            InputStream inputStream = Runtime.getRuntime().exec("cat /proc/cpuinfo").getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = bufferedReader.readLine()) != null)
                if(line.contains("Hardware")){
                    map.put("SOC-Information", line.split(":")[1].trim());
                    break;
                }
            bufferedReader.close();
            inputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }

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