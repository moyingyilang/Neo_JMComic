package com.yourpackage.neojmcomic.utils.i18n

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import com.yourpackage.neojmcomic.utils.persistence.PersistenceStorage
import java.util.Locale

/** 国际化管理类（整合语言检测、资源加载、语言切换） */
object I18nManager {
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_LANGUAGE_COOKIE = "i18next"
    private const val KEY_LANGUAGE_LOCAL_STORAGE = "i18nextLng"
    private var currentLocale: Locale = Locale.getDefault()

    /** 初始化（优先从缓存/系统检测语言） */
    fun init(context: Context) {
        // 1. 检测语言优先级：缓存 > Cookie > LocalStorage > 系统语言 > 默认中文
        val cachedLang = PersistenceStorage.getString(KEY_LANGUAGE)
        if (!TextUtils.isEmpty(cachedLang)) {
            currentLocale = Locale(cachedLang)
        } else {
            val cookieLang = getCookieValue(context, KEY_LANGUAGE_COOKIE)
            if (!TextUtils.isEmpty(cookieLang)) {
                currentLocale = Locale(cookieLang)
            } else {
                val localStorageLang = PersistenceStorage.getString(KEY_LANGUAGE_LOCAL_STORAGE)
                if (!TextUtils.isEmpty(localStorageLang)) {
                    currentLocale = Locale(localStorageLang)
                } else {
                    currentLocale = getSystemLocale()
                }
            }
        }
        updateResources(context, currentLocale)
        // 缓存当前语言
        PersistenceStorage.putString(KEY_LANGUAGE, currentLocale.language)
        PersistenceStorage.putString(KEY_LANGUAGE_LOCAL_STORAGE, currentLocale.language)
        setCookieValue(context, KEY_LANGUAGE_COOKIE, currentLocale.language, 30 * 24 * 60) // 缓存30天
    }

    /** 切换语言（支持zh、en、ja、ko等） */
    fun switchLanguage(context: Context, language: String): Context {
        currentLocale = Locale(language)
        // 持久化存储
        PersistenceStorage.putString(KEY_LANGUAGE, language)
        PersistenceStorage.putString(KEY_LANGUAGE_LOCAL_STORAGE, language)
        setCookieValue(context, KEY_LANGUAGE_COOKIE, language, 30 * 24 * 60)
        // 更新资源
        return updateResources(context, currentLocale)
    }

    /** 获取当前语言的字符串资源 */
    fun getString(context: Context, resId: Int): String {
        return try {
            context.resources.getString(resId)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    /** 获取带参数的字符串资源 */
    fun getString(context: Context, resId: Int, vararg formatArgs: Any): String {
        return try {
            context.resources.getString(resId, *formatArgs)
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    /** 获取当前语言标签（如zh、en、ja） */
    fun getCurrentLanguage(): String = currentLocale.language

    /** 检测系统语言 */
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale
        }
    }

    /** 更新资源配置 */
    private fun updateResources(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val resources = context.resources
        val config = Configuration(resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics)
            return context
        }
    }

    /** 从Cookie获取值（模拟前端cookie存储） */
    private fun getCookieValue(context: Context, key: String): String? {
        val cookieStr = PersistenceStorage.getString("app_cookies") ?: ""
        val cookies = cookieStr.split("; ")
        for (cookie in cookies) {
            val parts = cookie.split("=")
            if (parts.size == 2 && parts[0] == key) {
                return parts[1]
            }
        }
        return null
    }

    /** 设置Cookie值 */
    private fun setCookieValue(context: Context, key: String, value: String, minutes: Int) {
        val cookieStr = "$key=$value; Max-Age=${minutes * 60}; Path=/"
        PersistenceStorage.putString("app_cookies", cookieStr)
    }
}
