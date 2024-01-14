package com.tungsten.fcl.util;

import android.content.Context;
import java.io.*;
import java.util.*;

public class PropertiesFileParse {
    private Properties properties;
    private InputStream in;
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
            in = new BufferedInputStream(new FileInputStream(propertiesFile));
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