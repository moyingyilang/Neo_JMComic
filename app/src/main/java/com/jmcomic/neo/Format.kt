package com.yourpackage.neojmcomic.utils

/** 小型单词列表（标题格式化时小写处理） */
private val smallWords = Regex("^(a|an|and|as|at|but|by|en|for|if|in|nor|of|on|or|per|the|to|vs?\\.?|via)$", RegexOption.IGNORE_CASE)

/** 邮箱地址匹配正则（简化版） */
private val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")

/** 邮箱脱敏提示文本 */
private const val REDACTED_EMAIL = "REDACTED (Potential Email Address)"

/**
 * 文本格式化工具
 * @param s 待格式化文本
 * @param titleCase 是否转为标题格式（首字母大写）
 * @param redactingEmail 是否脱敏邮箱地址
 * @return 格式化后的文本
 */
fun format(
    s: String?,
    titleCase: Boolean = true,
    redactingEmail: Boolean = true
): String {
    val str = s ?: ""
    var result = str.trim()

    // 邮箱脱敏
    if (redactingEmail && emailRegex.matches(result)) {
        println("This arg looks like an email address, redacting.")
        return REDACTED_EMAIL
    }

    // 标题格式化
    if (titleCase && result.isNotEmpty()) {
        result = toTitleCase(result)
    }

    return result
}

/**
 * 转为标题格式（处理小型单词小写）
 */
private fun toTitleCase(string: String): String {
    return string.replace(Regex("[A-Za-z0-9\u00C0-\u00FF]+[^\s-]*")) { match ->
        val word = match.value
        // 小型单词且非句首/冒号后/连字符后，保持小写
        if (word.matches(smallWords) && !isImportantWord(match.range.first, string)) {
            word.lowercase()
        } else {
            word.replaceFirstChar { it.uppercaseChar() }
        }
    }
}

/**
 * 判断单词是否需要大写（句首、冒号后、连字符后）
 */
private fun isImportantWord(startIndex: Int, string: String): Boolean {
    if (startIndex == 0) return true
    // 冒号后单词大写
    if (startIndex >= 2 && string[startIndex - 2] == ':') return true
    // 连字符后单词大写（排除连续连字符）
    if (string[startIndex - 1] == '-' && (startIndex < 2 || string[startIndex - 2] != '-')) return true
    return false
}
