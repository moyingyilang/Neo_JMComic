package com.yourpackage.neojmcomic.utils.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** Compose专用国际化工具 */
object I18nComposeHelper {
    /** Compose中获取字符串资源 */
    @Composable
    fun getString(resId: Int): String {
        val context = LocalContext.current
        return I18nManager.getString(context, resId)
    }

    /** Compose中获取带参数字符串资源 */
    @Composable
    fun getString(resId: Int, vararg formatArgs: Any): String {
        val context = LocalContext.current
        return I18nManager.getString(context, resId, *formatArgs)
    }

    /** Compose中切换语言并重建界面 */
    @Composable
    fun switchLanguage(language: String, onLanguageChanged: () -> Unit) {
        val context = LocalContext.current
        I18nManager.switchLanguage(context, language)
        onLanguageChanged()
    }
}
