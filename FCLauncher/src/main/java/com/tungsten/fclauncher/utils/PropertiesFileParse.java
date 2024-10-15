package com.tungsten.fclauncher.utils;

import android.content.Context;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PropertiesFileParse {

    private final Properties properties;
    private final InputStream in;

    public PropertiesFileParse(String propertiesFileName, Context context) {
        properties = new Properties();
        try {
            in = context.getAssets().open(propertiesFileName);
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PropertiesFileParse(String propertiesFile) {
        properties = new Properties();
        try {
            in = new BufferedInputStream(Files.newInputStream(Paths.get(propertiesFile)));
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Properties getProperties() {
        return properties;
    }

    public Map<String,String> propertiesToMap(){
        return new HashMap<String, String>((Map) properties);
    }
}