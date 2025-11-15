package com.yourpackage.neojmcomic.config

/**
 * 应用基础配置类
 */
object AppConfig {
    /** 是否禁用核心功能（默认false） */
    var disabled: Boolean = false
        private set

    /** 更新禁用状态 */
    fun updateDisabledState(isDisabled: Boolean) {
        disabled = isDisabled
    }

    /** 重置配置为默认值 */
    fun reset() {
        disabled = false
    }
}
