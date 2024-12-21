package com.tungsten.fclcore.util.platform;

import android.app.ActivityManager;
import android.content.Context;

import com.tungsten.fclauncher.utils.Architecture;

public class MemoryUtils {

    public static int getTotalDeviceMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        return (int) (memInfo.totalMem / 1048576L);
    }

    public static int getUsedDeviceMemory(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return (int) ((memInfo.totalMem - memInfo.availMem) / 1048576L);
    }

    public static int getFreeDeviceMemory(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        return (int) (memInfo.availMem / 1048576L);
    }

    public static int findBestRAMAllocation(Context context) {
        int totalDeviceMemory = getTotalDeviceMemory(context);
        if (totalDeviceMemory <= 1024) {
            return 512;
        } else if (totalDeviceMemory <= 3072) {
            return 1024;
        } else if (totalDeviceMemory <= 4096) {
            return Architecture.is32BitsDevice() ? 1024 : 1536;
        } else if (totalDeviceMemory <= 7168) {
            return Architecture.is32BitsDevice() ? 1024 : 2560;
        } else if (totalDeviceMemory <= 10240) {
            return Architecture.is32BitsDevice() ? 1024 : 3072;
        } else if (totalDeviceMemory <= 13312) {
            return Architecture.is32BitsDevice() ? 1024 : 4096;
        } else {
            return Architecture.is32BitsDevice() ? 1024 : 5120;
        }
    }

}