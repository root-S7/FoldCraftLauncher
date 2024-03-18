package com.tungsten.fcl.util;

import android.content.Context;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadTools {

    // .minecraft/version
    public static String readAssetsTxt(Context context, String fileName){
        StringBuilder content = new StringBuilder();

        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content.toString();
    }

    // /data/user/0/com.tungsten.fcl/files/version
    public static String readFileTxt(String path){
        StringBuilder content = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader(path))){
            String line;
            while ((line = reader.readLine()) != null) content.append(line).append("\n");
        }catch (IOException e){
            e.printStackTrace();
        }

        return content.toString();
    }

    public static String convertToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        while((line = bufferedReader.readLine()) != null) stringBuilder.append(line).append("\n");

        bufferedReader.close();
        return stringBuilder.toString();
    }
}
