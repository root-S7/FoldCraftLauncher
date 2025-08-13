package com.tungsten.fcl.util;

import static android.os.Build.*;
import static android.os.Build.VERSION.*;
import static com.tungsten.fcl.BuildConfig.*;
import static com.tungsten.fclauncher.FCLauncher.*;
import static java.lang.String.*;

import androidx.annotation.*;

import java.util.Map;
import java.util.stream.*;

public class DeviceInfo {
    public static Map<String, String> DEVICE_DATA;

    static {
        DEVICE_DATA = Map.of(
                "Device", MANUFACTURER + "(" + PRODUCT + ")",
                "Android-Version", RELEASE,
                "Android-SDK", valueOf(SDK_INT),
                "CPU", getSocName(),
                "FCL-Version", VERSION_NAME,
                "ROM-Version", DISPLAY
        );
    }

    public static String toText(){
        return DEVICE_DATA.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    @NonNull @Override
    public String toString() {
        return toText();
    }
}
