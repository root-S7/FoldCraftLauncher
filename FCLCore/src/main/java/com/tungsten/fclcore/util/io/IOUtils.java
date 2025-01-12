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
package com.tungsten.fclcore.util.io;

import android.content.Context;
import android.content.res.AssetManager;

import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.util.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

/**
 * This utility class consists of some util methods operating on InputStream/OutputStream.
 */
public final class IOUtils {

    private IOUtils() {
    }

    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    /**
     * Read all bytes to a buffer from given input stream. The stream will not be closed.
     *
     * @param stream the InputStream being read.
     * @return all bytes read from the stream
     * @throws IOException if an I/O error occurs.
     */
    public static byte[] readFullyWithoutClosing(InputStream stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(Math.max(stream.available(), 32));
        copyTo(stream, result);
        return result.toByteArray();
    }

    public static String readFullyAsStringWithClosing(InputStream stream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(Math.max(stream.available(), 32));
        copyTo(stream, result);
        return result.toString("UTF-8");
    }

    /**
     * Read all bytes to a buffer from given input stream, and close the input stream finally.
     *
     * @param stream the InputStream being read, closed finally.
     * @return all bytes read from the stream
     * @throws IOException if an I/O error occurs.
     */
    public static ByteArrayOutputStream readFully(InputStream stream) throws IOException {
        try (InputStream is = stream) {
            ByteArrayOutputStream result = new ByteArrayOutputStream(Math.max(is.available(), 32));
            copyTo(is, result);
            return result;
        }
    }

    public static byte[] readFullyAsByteArray(InputStream stream) throws IOException {
        return readFully(stream).toByteArray();
    }

    public static String readFullyAsString(InputStream stream) throws IOException {
        return readFully(stream).toString("UTF-8");
    }

    public static void copyTo(InputStream src, OutputStream dest) throws IOException {
        copyTo(src, dest, new byte[DEFAULT_BUFFER_SIZE]);
    }

    public static void copyTo(InputStream src, OutputStream dest, byte[] buf) throws IOException {
        while (true) {
            int len = src.read(buf);
            if (len == -1)
                break;
            dest.write(buf, 0, len);
        }
    }

    public static InputStream wrapFromGZip(InputStream inputStream) throws IOException {
        return new GZIPInputStream(inputStream);
    }

    public static void copyAssets(String assetFileName, String targetPath) throws IOException {
        AssetManager assetManager = FCLPath.CONTEXT.getAssets();
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            // 打开 assets 文件输入流
            inputStream = assetManager.open(assetFileName);

            // 创建目标文件输出流
            File outFile = new File(targetPath);
            File parentDir = outFile.getParentFile();
            if(parentDir != null && !parentDir.exists()) parentDir.mkdirs(); // 确保父目录存在

            fileOutputStream = new FileOutputStream(outFile);

            // 调用 copyTo 方法进行文件拷贝
            copyTo(inputStream, fileOutputStream, new byte[DEFAULT_BUFFER_SIZE]);
        }finally {
            if(inputStream != null) inputStream.close();
            if(fileOutputStream != null) fileOutputStream.close();
        }
    }

    public static String calculateSHA256(InputStream inputStream) throws IOException {
        // 创建 SHA-256 MessageDigest 实例
        MessageDigest digest = DigestUtils.getDigest("SHA-256");

        byte[] buffer = new byte[8192];  // 缓冲区大小，可以根据需要调整
        int bytesRead;

        // 逐块读取 InputStream 数据并更新 MessageDigest
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        // 获取计算出的 SHA-256 哈希字节数组
        byte[] hashBytes = digest.digest();

        // 将字节数组转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));  // 每个字节转换为两位的十六进制数
        }

        return hexString.toString();  // 返回最终的 SHA-256 哈希值
    }

    public static String calculateSHA256(Path path) throws IOException {
        return calculateSHA256(Files.newInputStream(path));
    }
}
