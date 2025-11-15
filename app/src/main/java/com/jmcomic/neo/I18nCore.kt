package com.yourpackage.neojmcomic.utils.i18n

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.yourpackage.neojmcomic.utils.persistence.PersistenceStorage
import java.util.Locale

/** 国际化核心工具（适配Android Resources，支持多语言切换） */
object I18nCore {
    private const val KEY_CURRENT_LOCALE = "current_locale"
    private var currentLocale: Locale = Locale.getDefault()

    /** 初始化（从缓存恢复语言设置） */
    fun init(context: Context) {
        val savedLocale = PersistenceStorage.getString(KEY_CURRENT_LOCALE)
        if (savedLocale.isNullOrEmpty()) return
        currentLocale = Locale.forLanguageTag(savedLocale)
        updateResources(context, currentLocale)
    }

    /** 切换语言（支持zh、en、ja等） */
    fun switchLanguage(context: Context, language: String) {
        currentLocale = Locale(language)
        PersistenceStorage.putString(KEY_CURRENT_LOCALE, currentLocale.toLanguageTag())
        updateResources(context, currentLocale)
    }

    /** 获取当前语言的字符串资源 */
    fun getString(context: Context, resId: Int): String {
        return try {
            context.resources.getString(resId)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    /** 获取当前语言的带参数字符串资源 */
    fun getString(context: Context, resId: Int, vararg formatArgs: Any): String {
        return try {
            context.resources.getString(resId, *formatArgs)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    /** 更新资源配置 */
    private fun updateResources(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            config.locale = locale
        }
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /** 获取当前语言标签（如zh-CN、en-US） */
    fun getCurrentLanguageTag(): String = currentLocale.toLanguageTag()
}
