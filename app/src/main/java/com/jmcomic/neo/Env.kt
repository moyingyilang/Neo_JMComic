package com.yourpackage.neojmcomic.constants

/**
 * Immer相关符号常量（用于不可变数据处理）
 */
object ImmerConstants {
    /** 用于替换draft为undefined的标记 */
    val NOTHING: Symbol = Symbol("immer-nothing")

    /** 标记类实例可被Immer draft处理 */
    val DRAFTABLE: Symbol = Symbol("immer-draftable")

    /** 存储draft状态的标记 */
    val DRAFT_STATE: Symbol = Symbol("immer-state")
}
