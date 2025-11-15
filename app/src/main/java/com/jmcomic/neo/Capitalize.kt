package com.yourpackage.neojmcomic.utils

/**
 * 字符串首字母大写（仅处理第一个单词，非完整text-transform: capitalize效果）
 * @param string 待处理字符串
 * @throws IllegalArgumentException 当输入非字符串时抛出异常
 * @return 首字母大写后的字符串
 */
fun capitalize(string: String?): String {
    if (string == null || string.isEmpty()) return ""
    if (string !is String) {
        throw IllegalArgumentException("MUI: `capitalize(string)` expects a string argument.")
    }
    return string.replaceFirstChar { it.uppercaseChar() } + string.substring(1)
}
