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

    public static String parseFile() throws IOException {
        return IOUtils.readFullyAsString(
                ParseAuthlibInjectorServerUtils.class.getResourceAsStream(
                        "/assets/" + FCLPath.ASSETS_AUTH_INJECTOR_SERVER_JSON
                )
        );
    }
}