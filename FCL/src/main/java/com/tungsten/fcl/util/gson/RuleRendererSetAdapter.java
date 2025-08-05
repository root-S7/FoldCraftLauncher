package com.tungsten.fcl.util.gson;

import static com.mio.manager.RendererManager.*;
import static com.tungsten.fcl.util.AndroidUtils.*;

import static java.util.stream.StreamSupport.*;

import com.google.gson.*;
import com.mio.data.Renderer;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.*;

public class RuleRendererSetAdapter implements JsonSerializer<LinkedHashSet<Renderer>>, JsonDeserializer<LinkedHashSet<Renderer>> {

    @Override
    public JsonElement serialize(LinkedHashSet<Renderer> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray array = new JsonArray();

        if(src != null) {
            src.stream()
                .map(Renderer::getId)
                .forEach(array::add);
        }

        return array;
    }

    @Override
    public LinkedHashSet<Renderer> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(json == null || !json.isJsonArray()) return new LinkedHashSet<>();

        return stream(json.getAsJsonArray().spliterator(), false)
                .map(this::parseElement)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Renderer parseElement(JsonElement element) {
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) return getRenderer(element.getAsString());
        else if(element.isJsonObject()) return parseRendererObject(element.getAsJsonObject());

        return null;
    }

    /**
     * 解析形如『{"packageName": "ren.test.com", "name": "Renderer name"}』格式的渲染器对象
     * 逻辑流程：
     *   1.从『JsonObject』中安全地提取『name』和『packageName』字符串字段。
     *   2.验证这两个字段非空且符合格式要求。
     *   3.若验证通过，返回一个新的『Renderer』实例，否则返回『null』。
     *
     * @param obj JSON对象，期望含有有效的『name』和『packageName』字段
     * @return 合法的『Renderer』对象或『null』
    **/
    private Renderer parseRendererObject(JsonObject obj) {
        String name = tryGetString(obj, "name");
        String packageName = tryGetString(obj, "packageName");

        return Optional.ofNullable(obj)
                .filter(o -> name != null && !name.trim().isEmpty())
                .filter(o -> packageName != null && !packageName.trim().isEmpty())
                .filter(this::isValidCustomJson)
                .map(o -> new Renderer(name, "", "", "", "", null, null, packageName, "", ""))
                .orElse(null);
    }


    /**
     * 校验传入的『JsonObject』中『packageName』字段是否满足包名格式的正则表达式要求
     * 若字段不存在或格式错误，返回『false』
     *
     * @param obj 期望包含『packageName』段的『JsonObject』
     * @return 如果包名格式合法，返回『true』，否则『false』
    **/
    private boolean isValidCustomJson(JsonObject obj) {
        try {
            String pkg = obj.get("packageName").getAsString();
            return isRegexMatch(pkg, "^[a-zA-Z][a-zA-Z0-9_]*(\\.([a-zA-Z][a-zA-Z0-9_]*))+$");
        }catch(Exception ex) {
            return false;
        }
    }

    /**
     * 从『JsonObject』安全提取指定『key』对应的字符串值
     * 如果『key』不存在、对应值不是字符串，或发生异常，则返回『null』
     *
     * @param obj  JSON对象
     * @param key  期望获取的字符串字段名
     * @return key 对应的字符串值，或『null』
    **/
    private String tryGetString(JsonObject obj, String key) {
        try {
            JsonElement element = obj.get(key);
            if(element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return element.getAsString();
            }
        }catch(Exception ignored) {}
        return null;
    }
}