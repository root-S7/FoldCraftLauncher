package com.tungsten.fcl.util;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.os.Build.VERSION.SDK_INT;

import static com.tungsten.fclcore.util.io.IOUtils.readFullyAsString;
import static com.tungsten.fcllibrary.component.dialog.FCLAlertDialog.AlertLevel.ALERT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.DisplayCutout;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.tungsten.fcl.FCLApplication;
import com.tungsten.fcl.R;
import com.tungsten.fcl.activity.WebActivity;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.Logging;
import com.tungsten.fclcore.util.io.FileUtils;
import com.tungsten.fclcore.util.io.IOUtils;
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Pattern;

@SuppressLint("DiscouragedApi")
public class AndroidUtils {

    public static void openLink(Context context, String link) {
        Uri uri = Uri.parse(link);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        if (componentName != null) {
            context.startActivity(Intent.createChooser(intent, ""));
        } else {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("FCL Clipboard", link);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getString(R.string.open_link_failed), Toast.LENGTH_LONG).show();
        }
    }

    public static void openLinkWithBuiltinWebView(Context context, String link) {
        Intent intent = new Intent(context, WebActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("url", link);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void copyText(Context context, String text) {
        ClipboardManager clip = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText(null, text);
        clip.setPrimaryClip(data);
        Toast.makeText(context, context.getString(R.string.message_copy), Toast.LENGTH_SHORT).show();
    }

    public static void clearWebViewCache(Context context) {
        File cache = context.getDir("webview", 0);
        FileUtils.deleteDirectoryQuietly(cache);
        CookieManager.getInstance().removeAllCookies(null);
    }

    public static String getLocalizedText(Context context, String key, Object... formatArgs) {
        return String.format(getLocalizedText(context, key), formatArgs);
    }

    public static String getLocalizedText(Context context, String key) {
        int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
        if (resId != 0) {
            return context.getString(resId);
        } else {
            return key;
        }
    }

    public static boolean hasStringId(Context context, String key) {
        int resId = context.getResources().getIdentifier(key, "string", context.getPackageName());
        return resId != 0;
    }


    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point.y;
    }

    public static int getScreenWidth(Activity context) {
        SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences("theme", MODE_PRIVATE);
        boolean fullscreen = sharedPreferences.getBoolean("fullscreen", FCLPath.GENERAL_SETTING.getProperty("default-fullscreen", "true").equals("true"));
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        if (fullscreen || SDK_INT < Build.VERSION_CODES.P) {
            return point.x;
        } else {
            return point.x - getSafeInset(context);
        }
    }

    public static int getSafeInset(Activity context) {
        try {
            if (SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                DisplayCutout cutout;
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    cutout = wm.getCurrentWindowMetrics().getWindowInsets().getDisplayCutout();
                } else {
                    cutout = context.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
                }
                int safeInsetLeft = cutout != null ? cutout.getSafeInsetLeft() : 0;
                int safeInsetRight = cutout != null ? cutout.getSafeInsetRight() : 0;
                return Math.max(safeInsetLeft, safeInsetRight);
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    @SuppressWarnings("resource")
    public static String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }

    public static String copyFileToDir(Activity activity, Uri uri, File destDir) {
        String name = new File(uri.getPath()).getName();
        File dest = new File(destDir, name);
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Failed to open content stream");
            }
            try (FileOutputStream outputStream = new FileOutputStream(dest)) {
                IOUtils.copyTo(inputStream, outputStream);
            }
            inputStream.close();
        } catch (Exception e) {

        }
        return dest.getAbsolutePath();
    }

    public static String copyFile(Activity activity, Uri uri, File dest) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IOException("Failed to open content stream");
            }
            try (FileOutputStream outputStream = new FileOutputStream(dest)) {
                IOUtils.copyTo(inputStream, outputStream);
            }
            inputStream.close();
        } catch (Exception e) {

        }
        return dest.getAbsolutePath();
    }

    public static boolean isDocUri(Uri uri) {
        return Objects.equals(uri.getScheme(), ContentResolver.SCHEME_FILE) || Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT);
    }

    public static String getFileName(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return uri.getLastPathSegment();
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        if (columnIndex == -1) return uri.getLastPathSegment();
        String fileName = cursor.getString(columnIndex);
        cursor.close();
        return fileName;
    }

    public static boolean isAdrenoGPU() {
        EGLDisplay eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            Logging.LOG.log(Level.SEVERE, "CheckVendor: Failed to get EGL display");
            return false;
        }

        if (!EGL14.eglInitialize(eglDisplay, null, 0, null, 0)) {
            Logging.LOG.log(Level.SEVERE, "CheckVendor: Failed to initialize EGL");
            return false;
        }

        int[] eglAttributes = new int[]{
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(eglDisplay, eglAttributes, 0, configs, 0, 1, numConfigs, 0) || numConfigs[0] == 0) {
            EGL14.eglTerminate(eglDisplay);
            Logging.LOG.log(Level.SEVERE, "CheckVendor: Failed to choose an EGL config");
            return false;
        }

        int[] contextAttributes = new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };

        EGLContext context = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttributes, 0);
        if (context == EGL14.EGL_NO_CONTEXT) {
            EGL14.eglTerminate(eglDisplay);
            Logging.LOG.log(Level.SEVERE, "CheckVendor: Failed to create EGL context");
            return false;
        }

        if (!EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, context)) {
            EGL14.eglDestroyContext(eglDisplay, context);
            EGL14.eglTerminate(eglDisplay);
            Logging.LOG.log(Level.SEVERE, "CheckVendor: Failed to make EGL context current");
            return false;
        }

        String vendor = GLES20.glGetString(GLES20.GL_VENDOR);
        String renderer = GLES20.glGetString(GLES20.GL_RENDERER);
        boolean isAdreno = (vendor != null && renderer != null &&
                vendor.equalsIgnoreCase("Qualcomm") &&
                renderer.toLowerCase().contains("adreno"));

        // Cleanup
        EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(eglDisplay, context);
        EGL14.eglTerminate(eglDisplay);
        Logging.LOG.log(Level.SEVERE, "CheckVendor: Running on Adreno GPU:" + isAdreno);
        return isAdreno;
    }

    public static boolean isRegexMatch(String string, String pattern) {
        try {
            return Pattern.matches(pattern, string);
        }catch(Exception e) {
            return false;
        }
    }

    /**
     * 从集合中获取第一个非null的元素，如果集合为null、为空或所有元素均为null则返回指定的默认值。
     *
     * @param collection   要查找的集合，可以为 null
     * @param defaultValue 如果未找到有效元素则返回的默认值
     * @param <T>          元素的类型
     * @return 集合中第一个非 null 的元素，或 defaultValue（如果没有）
    **/
    public static <T> T getFirstOrDefault(Collection<T> collection, T defaultValue) {
        if(collection == null) return defaultValue;

        return collection.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(defaultValue);
    }

    public static void showErrorDialog(Activity activity, String errMsg, boolean extraTip) {
        if(activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        new FCLAlertDialog.Builder(activity)
                .setTitle("错误")
                .setAlertLevel(ALERT)
                .setMessage(errMsg + (extraTip ? "\n\n由于该错误是致命性的，点击“确定”按钮后将关闭应用" : ""))
                .setNegativeButton("确定", () -> {
                    Activity cActivity = FCLApplication.getCurrentActivity();
                    if(cActivity != null) cActivity.finishAndRemoveTask();
                    System.exit(-1);
                })
                .setCancelable(false)
                .setPercentageSize(0.6f, -1)
                .create()
                .show();
        System.gc();
    }

    /**
     * 从指定的『JsonObject』中安全地获取字符串字段值。
     * 如果『jsonObject 或『fieldName』为『null』，返回空字符
     * 如果字段不存在、为『JsonNull』、非原始类型，或不是字符串，同样返回空字符
     * 仅当字段是有效的JSON字符串时，才返回对应的字符串值
     *
     * @param jsonObject 要解析的JsonObject
     * @param fieldName  字段名
     * @return           对应字段的字符串值，或空字符
    **/
    public static String getStringValue(JsonObject jsonObject, String fieldName) {
        return Optional.ofNullable(jsonObject)
                .filter(obj -> fieldName != null)
                .map(obj -> obj.get(fieldName))
                .filter(element -> !element.isJsonNull())
                .filter(JsonElement::isJsonPrimitive)
                .map(JsonElement::getAsJsonPrimitive)
                .filter(JsonPrimitive::isString)
                .map(JsonPrimitive::getAsString)
                .orElse("");
    }
}
