package com.tungsten.fcl.game.check.rule

import android.graphics.BitmapFactory
import android.util.Xml
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.tungsten.fcl.setting.Config.fromJson
import com.tungsten.fcl.setting.ConfigHolder.validateSelectedPath
import com.tungsten.fcl.util.AndroidUtils.openAssets
import com.tungsten.fclcore.util.gson.JsonUtils.GSON_SIMPLE
import com.tungsten.fclcore.util.io.IOUtils.readFullyAsString
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.FileNotFoundException
import javax.xml.parsers.ParserConfigurationException

typealias FileCheckRule = (String) -> Boolean

val JsonRule: FileCheckRule = { assPath ->
    try {
        val element = openAssets(null, assPath).use { input ->
            GSON_SIMPLE.fromJson(input.reader(), JsonElement::class.java)
        }
        element != null || throw JsonParseException("文件『$assPath』解析错误，请检查是否为有效的Json格式！")
    }catch(ex: Exception) {
        when(ex) {
            is FileNotFoundException, is JsonParseException -> throw ex
            else -> false
        }
    }
}

val ImageRule: FileCheckRule = { assPath ->
    openAssets(null, assPath).use { input ->
        if(input.available() > (1.11 * 1024 * 1024).toInt()) throw IllegalArgumentException("『$assPath』图片大小超过1.11MB")

        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }.let {
            BitmapFactory.decodeStream(input, null, it)
            if(it.outWidth <= 0 || it.outHeight <= 0) throw IllegalArgumentException("文件『$assPath』不是有效的图片")
            if(it.outWidth > 2560 || it.outHeight > 1440) throw IllegalArgumentException("『$assPath』图片分辨率过大（最大 2560×1440）")
        }
        true
    }
}

val BaseRule: FileCheckRule = { assPath ->
    runCatching {
        openAssets(null, assPath).use { true }
    }.getOrElse { ex ->
        if(ex is FileNotFoundException) throw ex else false
    }
}

val SharedPreferencesRule: FileCheckRule = { assPath ->
    try {
        openAssets(null, assPath).use { input ->
            val parser = Xml.newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                setInput(input, null)
            }
            var eventType = parser.eventType
            var foundMapTag = false
            var mapDepth = 0

            while(eventType != XmlPullParser.END_DOCUMENT) {
                when(eventType) {
                    XmlPullParser.START_TAG -> {
                        if(parser.name != "map") throw XmlPullParserException("根节点只能是map，发现非法标签: <${parser.name}>")
                        if(foundMapTag) throw XmlPullParserException("文档中存在多个map标签，只能有一个根map节点")

                        foundMapTag = true
                        mapDepth++
                        validateMapContent(parser)
                    }
                    XmlPullParser.END_TAG -> if(parser.name == "map") mapDepth--
                    XmlPullParser.TEXT -> {
                        val text = parser.text.trim()
                        if(text.isNotEmpty() && mapDepth == 0) throw XmlPullParserException("根节点外存在非法文本内容: $text")
                    }
                }
                eventType = parser.next()
            }

            if(!foundMapTag) throw XmlPullParserException("未找到map根节点")
            true
        }
    }catch(ex: FileNotFoundException) {
        throw ex
    }catch(ex: XmlPullParserException) {
        throw XmlPullParserException("文件『$assPath』解析错误,请检查是否为有效的SharedPreferences格式! ${ex.message}")
    }catch(_: Exception) {
        false
    }
}

private fun validateMapContent(parser: XmlPullParser) {
    var eventType = parser.next()

    while(eventType != XmlPullParser.END_DOCUMENT) {
        when(eventType) {
            XmlPullParser.START_TAG -> {
                val tagName = parser.name
                parser.getAttributeValue(null, "name") ?: throw XmlPullParserException("标签 <$tagName> 缺少 'name' 属性")

                when(tagName) {
                    "string", "set" -> Unit
                    "int", "long", "float", "boolean" -> {
                        val value = parser.getAttributeValue(null, "value")
                            ?: throw XmlPullParserException("<$tagName> 缺少 'value' 属性")

                        when(tagName) {
                            "int" -> value.toIntOrNull() ?: throw XmlPullParserException("无效的 int: $value")
                            "long" -> value.toLongOrNull() ?: throw XmlPullParserException("无效的 long: $value")
                            "float" -> value.toFloatOrNull() ?: throw XmlPullParserException("无效的 float: $value")
                            "boolean" -> if(value != "true" && value != "false") throw XmlPullParserException("无效的 boolean: $value")
                        }
                    }
                    else -> throw XmlPullParserException("不支持的标签: <$tagName>")
                }
            }
            XmlPullParser.END_TAG -> if(parser.name == "map") return
        }
        eventType = parser.next()
    }
}

fun typeJsonRule(clazz: Class<*>, gson: Gson = GSON_SIMPLE): FileCheckRule = {
    openAssets(null, it).use { input ->
        input.bufferedReader().use { reader -> gson.fromJson(reader, clazz) != null }
    }
}

fun configRule(): FileCheckRule = {
    val config = fromJson(readFullyAsString(openAssets(null, it))) ?: throw ParserConfigurationException("文件『$it』未通过校验，请重新制作！")
    if(!validateSelectedPath(config)) throw IllegalArgumentException("选择的路径无效或不可访问，请重新选择路径！")

    true
}