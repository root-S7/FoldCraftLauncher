package com.tungsten.fcl.util.check.rule

import android.graphics.BitmapFactory
import android.util.Xml
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.tungsten.fcl.util.AndroidUtils.openAssets
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.FileNotFoundException

object JsonRule : FileCheckRule {
    override fun check(assPath: String): Boolean {
        return try {
            val result = openAssets(null, assPath).use { input ->
                input.bufferedReader().use {
                    GsonBuilder()
                        .setPrettyPrinting()
                        .create()
                        .fromJson(it, JsonElement::class.java)
                }
            }
            result ?: throw JsonParseException("")

            true
        }catch(ex: FileNotFoundException) {
            throw ex
        }catch(_: JsonParseException) {
            throw JsonParseException("文件『$assPath』解析错误，请检查是否为有效的Json格式！")
        }catch(_: Exception) {
            false
        }
    }
}

object ImageRule : FileCheckRule {
    private const val MAX_SIZE_BYTES = (1.11 * 1024 * 1024).toInt()
    private const val MAX_WIDTH = 2560
    private const val MAX_HEIGHT = 1440

    override fun check(assPath: String): Boolean = run {
        openAssets(null, assPath).use { input ->
            if(input.available() > MAX_SIZE_BYTES) throw IllegalArgumentException("『$assPath』图片大小超过1.11MB")

            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(input, null, opts)
            val w = opts.outWidth
            val h = opts.outHeight
            if(w <= 0 || h <= 0) throw IllegalArgumentException("文件『$assPath』不是有效的图片")
            if(w > MAX_WIDTH || h > MAX_HEIGHT) throw IllegalArgumentException("『$assPath』图片分辨率过大（最大 ${MAX_WIDTH}×${MAX_HEIGHT}）")

            true
        }
    }
}

object BaseRule : FileCheckRule {
    override fun check(assPath: String): Boolean {
        return try {
            openAssets(null, assPath).use { _ -> true }
        }catch(ex: FileNotFoundException) {
            throw ex
        }catch(_: Exception) {
            false
        }
    }
}

object SharedPreferencesRule : FileCheckRule {
    override fun check(assPath: String): Boolean {
        return try {
            openAssets(null, assPath).use { input ->
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(input, null)
                var eventType = parser.eventType
                var foundMapTag = false
                var mapDepth = 0

                while(eventType != XmlPullParser.END_DOCUMENT) {
                    when(eventType) {
                        XmlPullParser.START_TAG -> {
                            if(parser.name == "map") {
                                if(foundMapTag) throw XmlPullParserException("文档中存在多个map标签，只能有一个根map节点")
                                foundMapTag = true
                                mapDepth++
                                validateMapContent(parser)
                            }else throw XmlPullParserException("根节点只能是map，发现非法标签: <${parser.name}>")
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
                        "string", "set" -> {}
                        "int", "long", "float", "boolean" -> {
                            val v = parser.getAttributeValue(null, "value") ?: throw XmlPullParserException("<$tagName> 缺少 'value' 属性")
                            when(tagName) {
                                "int" -> v.toIntOrNull() ?: throw XmlPullParserException("无效的 int: $v")
                                "long" -> v.toLongOrNull() ?: throw XmlPullParserException("无效的 long: $v")
                                "float" -> v.toFloatOrNull() ?: throw XmlPullParserException("无效的 float: $v")
                                "boolean" -> if (v != "true" && v != "false") throw XmlPullParserException("无效的 boolean: $v")
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
}