package com.tungsten.fcl.setting.rule.core;

import static com.tungsten.fcl.util.AndroidUtils.PLACEHOLDER_PATTERN;
import static com.tungsten.fcl.util.RuleCheckState.NO_CHANGE;
import static com.tungsten.fcl.util.RuleCheckState.UNKNOWN;
import static com.tungsten.fclcore.util.gson.JsonUtils.GSON;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.tungsten.fcl.setting.VersionSetting;
import com.tungsten.fcl.util.RuleCheckState;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

/**
 * “launcher_rules.json”文件内所有规则的基类，所有规则必须继承于它才能识别
 * 注意，自新版本起不在有GameRulesManager，VersionRule等类了，使用Gson后将直接创建为“LinkedHashMap<String, Set<LaunchRule>>”对象
 * 只需要通过getVersionRules方法获取当前版本的启动规则，最后直接调用检测方法即可
 */
public abstract class LaunchRule {
    @SerializedName("tip")
    private final String tip;
    public abstract boolean canDetectRule();
    protected abstract void initPlaceholders(@NonNull VersionSetting setting);

    public LaunchRule(String tip) {
        this.tip = tip;
    }

    @CallSuper
    public RuleCheckState setRule(@NonNull VersionSetting setting) {
        if(!canDetectRule()) return NO_CHANGE;
        else {
            initPlaceholders(setting);
            return UNKNOWN;
        }
    }

    /**
     * 获取下载链接，默认返回 null。
     * 需要提供下载链接的子类（如 GLRender 等）自行重写该方法即可。
     */
    @Nullable
    public URL getDownloadURL() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @NonNull @Override
    public String toString() {
        return GSON.toJson(this);
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