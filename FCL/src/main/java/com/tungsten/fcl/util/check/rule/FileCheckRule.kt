package com.tungsten.fcl.util.check.rule

/**
 *  文件格式有效性检测接口
 *  不止文件格式检测，你也可以指定检测后的文件用什么
**/
fun interface FileCheckRule {
    fun check(assPath: String): Boolean
}