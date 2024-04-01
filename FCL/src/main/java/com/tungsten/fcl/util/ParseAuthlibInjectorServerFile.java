package com.tungsten.fcl.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.google.gson.*;
import com.tungsten.fcl.fragment.RuntimeFragment;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.auth.authlibinjector.AuthlibInjectorServer;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ParseAuthlibInjectorServerFile {
    private final Context context;
    private final String authlibInjectorServerFileName;

    public ParseAuthlibInjectorServerFile(Activity activity,String authlibInjectorServerFileName) {
        this.context = activity;
        this.authlibInjectorServerFileName = authlibInjectorServerFileName;
    }

    public ParseAuthlibInjectorServerFile(Context context,String authlibInjectorServerFileName) {
        this.context = context;
        this.authlibInjectorServerFileName = authlibInjectorServerFileName;
    }

    public ParseAuthlibInjectorServerFile(RuntimeFragment runtimeFragment, String authlibInjectorServerFileName) {
        this.context = runtimeFragment.getContext();
        this.authlibInjectorServerFileName = authlibInjectorServerFileName;
    }

    /**
     * 控制台输出文本内容
    **/
    public void printFileContent(){
        try {
            InputStream open = context.getAssets().open(authlibInjectorServerFileName);
            int size;
            byte[] buffer = new byte[1024];
            while ((size = open.read(buffer)) != -1) System.out.println(new String(buffer, 0, size));
        }catch (IOException ignored) {
            System.out.append("无法读取数据");
        }
    }

    /**
     * 读取文本流
     * @return 返回一个流
    **/
    private Reader fileToReader() {
        try {
            return new BufferedReader(new InputStreamReader(context.getAssets().open(authlibInjectorServerFileName), StandardCharsets.UTF_8));
        }catch (IOException e) {
            return new StringReader("");
        }
    }

    /**
     * 解析JSON文件"authlib-injector-server.json"
     * @return 返回一个JsonObject对象
    **/
    private JsonObject parseJsonFileToObject(){
        Reader reader = fileToReader();
        try {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            return jsonElement.getAsJsonObject();
        }catch(JsonSyntaxException e) {
            Log.d("事件", "JSON 解析失败：" + e.getMessage());
            return new JsonObject();
        }
    }

    private JsonArray parseJsonFileToArray(){
        Reader reader = fileToReader();
        try {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            return jsonElement.getAsJsonArray();
        }catch(JsonSyntaxException e) {
            Log.d("事件", "JSON 解析失败：" + e.getMessage());
            return new JsonArray();
        }
    }

    /**
     * 解析JSON文件"authlib-injector-server.json"中的"server-address"数组内数据并转换皮肤站view
    **/
    public void parseFileAndConvert(){
        JsonArray asJsonArray = parseJsonFileToObject().getAsJsonArray("server-address");

        if(asJsonArray != null){
            JsonArray test = new JsonArray();
            for(JsonElement element : asJsonArray) {
                try{
                    test.add(authlibInjectorServerToJsonObject(AuthlibInjectorServer.locateServer(element.getAsString())));
                }catch(IOException e) {
                    Log.d("事件", e.toString());
                }
            }
            addToFile(test);
        }
    }

    /**
     * 将view内容加到config.json文件中
     * @param jsonArray 对象数组
    **/
    private void addToFile(JsonArray jsonArray) {
        File file = new File(FCLPath.FILES_DIR + "/config.json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("authlibInjectorServers",jsonArray);

        if(file.exists()) {
            try {
                JsonParser parser = new JsonParser();
                jsonObject = parser.parse(new FileReader(file)).getAsJsonObject();
                jsonObject.add("authlibInjectorServers", jsonArray);
            }catch(IOException | IllegalStateException | JsonIOException | JsonSyntaxException e) {
                RuntimeUtils.delete(FCLPath.FILES_DIR + "/config.json");
                addToFile(jsonArray);
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(file);

            outputStream.write(jsonObject.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 把"AuthlibInjectorServer"对象转成"JsonObject"
     * @param authlibInjectorServer 通过网络解析的皮肤站
     * @return 一个JsonObject对象
    **/
    public JsonObject authlibInjectorServerToJsonObject(AuthlibInjectorServer authlibInjectorServer){
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("metadataResponse", String.valueOf(authlibInjectorServer.getMetadataResponse()));
        jsonObject.addProperty("metadataTimestamp", authlibInjectorServer.getMetadataTimestamp());
        jsonObject.addProperty("url", authlibInjectorServer.getUrl());

        return jsonObject;
    }
}