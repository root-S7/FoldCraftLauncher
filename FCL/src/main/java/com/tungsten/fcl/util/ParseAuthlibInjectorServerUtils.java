package com.tungsten.fcl.util;

import com.google.gson.*;
import com.tungsten.fcl.setting.Config;
import com.tungsten.fcl.setting.ConfigHolder;
import com.tungsten.fclauncher.utils.FCLPath;
import com.tungsten.fclcore.auth.authlibinjector.AuthlibInjectorServer;
import com.tungsten.fclcore.util.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;

public class ParseAuthlibInjectorServerUtils {


    /**
     * 将APK中的“authlib_injector_server.json”文件中所有地址解析后并转存
     * @param config 提供一个需要修改的Config对象
    **/
    public static void parseUrlAndWriteToFile(Config config) {

        try(StringReader jsonReader = new StringReader(parseFile())) {
            JsonObject authlibInjectorServerObject = JsonParser.parseReader(jsonReader).getAsJsonObject();
            JsonArray asJsonArray = authlibInjectorServerObject.getAsJsonArray("server-address");

            for(JsonElement data : asJsonArray) {
                try {
                    config.getAuthlibInjectorServers().add(AuthlibInjectorServer.locateServer(data.getAsString()));
                }catch(IOException ignored) {}
            }

            ConfigHolder.writeToConfig(config.toJson());
        }catch(Exception ignored) { }
    }

    /**
     * 读取“authlib_injector_server.json”内容
     * @return 返回内容
     * @throws IOException 读取失败异常
    **/
    public static String parseFile() throws IOException {
        return IOUtils.readFullyAsString(
                ParseAuthlibInjectorServerUtils.class.getResourceAsStream(
                        "/assets/" + FCLPath.ASSETS_AUTH_INJECTOR_SERVER_JSON
                )
        );
    }
}