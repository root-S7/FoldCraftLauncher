package com.tungsten.fcl.util;

import static com.tungsten.fcl.util.AndroidUtils.*;
import static com.tungsten.fclauncher.utils.AssetsPath.*;
import static com.tungsten.fclcore.util.io.IOUtils.*;

import com.google.gson.*;
import com.tungsten.fcl.setting.Config;
import com.tungsten.fclcore.auth.authlibinjector.AuthlibInjectorServer;

import java.io.IOException;
import java.io.StringReader;

public class ParseAuthlibInjectorServerUtils {

    /**
     * 将APK中的“authlib_injector_server.json”文件中所有地址解析后并转存
     * @param config 提供一个需要修改的Config对象
    **/
    public static void parseUrlToConfig(Config config) {

        try(StringReader jsonReader = new StringReader(readFullyAsString(openAssets(null, AUTH_SERVER)))) {
            JsonObject authObject = JsonParser.parseReader(jsonReader).getAsJsonObject();
            JsonArray asJsonArray = authObject.getAsJsonArray("server-address");

            for(JsonElement data : asJsonArray) {
                try {
                    config.getAuthlibInjectorServers().add(AuthlibInjectorServer.locateServer(data.getAsString()));
                }catch(IOException ignored) {}
            }
        }catch(Exception ignored) { }
    }
}