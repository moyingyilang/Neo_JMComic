package com.yourpackage.neojmcomic.utils

/**
 * 比较两个数组是否相等（长度一致且元素逐一匹配）
 * @param array1 第一个数组
 * @param array2 第二个数组
 * @param itemComparer 自定义元素比较逻辑，默认使用 === 比较
 * @return 数组是否相等
 */
fun <T> areArraysEqual(
    array1: Array<T>,
    array2: Array<T>,
    itemComparer: (a: T, b: T) -> Boolean = { a, b -> a === b }
): Boolean {
    if (array1.size != array2.size) return false
    return array1.forEachIndexed { index, value ->
        if (!itemComparer(value, array2[index])) return@areArraysEqual false
    }
    return true
}

/** 针对List的重载方法 */
fun <T> areListsEqual(
    list1: List<T>,
    list2: List<T>,
    itemComparer: (a: T, b: T) -> Boolean = { a, b -> a === b }
): Boolean {
    if (list1.size != list2.size) return false
    return list1.forEachIndexed { index, value ->
        if (!itemComparer(value, list2[index])) return@areListsEqual false
    }
    return true
}
