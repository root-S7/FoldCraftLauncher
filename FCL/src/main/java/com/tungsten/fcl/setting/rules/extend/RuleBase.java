package com.tungsten.fcl.setting.rules.extend;

import static com.tungsten.fcl.util.RuleCheckState.UNKNOWN;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.tungsten.fcl.setting.VersionSetting;
import com.tungsten.fcl.util.RuleCheckState;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.*;

public abstract class RuleBase {
    @SerializedName("tip")
    private final String tip;
    protected static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([a-zA-Z_][a-zA-Z0-9_]*)\\}");
    public abstract boolean canDetectRule();
    protected abstract void initPlaceholders(@NonNull VersionSetting setting);

    public RuleBase(String tip) {
        this.tip = tip;
    }

    @CallSuper
    public RuleCheckState setRule(@NonNull VersionSetting setting) {
        initPlaceholders(setting);
        return UNKNOWN;
    }

    public Map<String, String> getProperties(@NonNull String... propertyNames) {
        Set<String> nameSet = new HashSet<>(Arrays.asList(propertyNames));
        Map<String, String> result = new HashMap<>();

        Class<?> clazz = this.getClass();

        for(String name : nameSet) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                Object value = field.get(this);
                if(value != null) result.put(name, value.toString());
            }catch(Exception e) {
                result.put(name, "");
            }
        }

        return result;
    }

    /**
     * 解析内容中的占位符并替换为实际值
     * @param content 包含占位符的内容
     * @return 替换后的内容
    **/
    public String parseContent(String content) {
        if(content == null || content.isEmpty()) return "";
        Set<String> placeholders = getContentPlaceholders(content);

        if(placeholders.isEmpty()) return content;

        Map<String, String> propertyValues = getProperties(placeholders.toArray(new String[0]));
        return replacePlaceholders(content, propertyValues);
    }

    /**
     * 提取内容中的所有占位符
     * @param content 内容
     * @return 占位符集合（去重），只包含符合Java标识符命名规则的占位符
    **/
    protected static Set<String> getContentPlaceholders(@NonNull String content) {
        Set<String> placeholders = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);

        int count = 0;
        while(matcher.find() && count < 22) {
            placeholders.add(matcher.group(1));
            count++;
        }

        return placeholders;
    }

    /**
     * 替换内容中的占位符
     * @param content 原始内容
     * @param propertyValues 属性值映射
     * @return 替换后的内容
    **/
    protected static String replacePlaceholders(@NonNull String content, @NonNull Map<String, String> propertyValues) {
        return propertyValues.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .reduce(content, (result, entry) -> result.replace("${" + entry.getKey() + "}", entry.getValue()), (r1, r2) -> r1);
    }

    public String getTip() {
        return parseContent(tip == null ? "" : tip);
    }
}