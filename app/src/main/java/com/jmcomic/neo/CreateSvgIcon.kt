package com.yourpackage.neojmcomic.ui.components.icons

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.util.AttributeSet
import com.caverock.androidsvg.SVG
import com.yourpackage.neojmcomic.ui.components.icons.SvgIcon

/**
 * SVG图标生成工具，包装SvgIcon组件统一图标创建逻辑
 * @param svgPath SVG路径字符串
 * @param displayName 图标名称（用于测试标识）
 * @return 封装后的SvgIcon组件
 */
fun createSvgIcon(
    svgPath: String,
    displayName: String
): Class<SvgIcon> {
    return object : SvgIcon(context) {
        init {
            // 解析SVG路径并设置到SvgIcon
            val svg = SVG.getFromString(svgPath)
            setPictureDrawable(PictureDrawable(svg.renderToPicture()))
            contentDescription = displayName
            tag = "svg_icon_$displayName"
        }
    }.javaClass
}

/**
 * 预定义常用图标组件（直接映射上传的图标文件）
 */
// AccountCircle图标
class AccountCircleIcon @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SvgIcon(context, attrs) {
    init {
        val svgPath = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2m0 4c1.93 0 3.5 1.57 3.5 3.5S13.93 13 12 13s-3.5-1.57-3.5-3.5S10.07 6 12 6m0 14c-2.03 0-4.43-.82-6.14-2.88C7.55 15.8 9.68 15 12 15s4.45.8 6.14 2.12C16.43 19.18 14.03 20 12 20"
        val svg = SVG.getFromString(svgPath)
        setPictureDrawable(PictureDrawable(svg.renderToPicture()))
        contentDescription = "账户头像"
        displayName = "AccountCircle"
    }
}

// Check图标
class CheckIcon @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SvgIcon(context, attrs) {
    init {
        val svgPath = "M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"
        val svg = SVG.getFromString(svgPath)
        setPictureDrawable(PictureDrawable(svg.renderToPicture()))
        contentDescription = "勾选"
        displayName = "Check"
    }
}

// Close图标
class CloseIcon @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SvgIcon(context, attrs) {
    init {
        val svgPath = "M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
        val svg = SVG.getFromString(svgPath)
        setPictureDrawable(PictureDrawable(svg.renderToPicture()))
        contentDescription = "关闭"
        displayName = "Close"
    }
}

// Search图标
class SearchIcon @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SvgIcon(context, attrs) {
    init {
        val svgPath = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14"
        val svg = SVG.getFromString(svgPath)
        setPictureDrawable(PictureDrawable(svg.renderToPicture()))
        contentDescription = "搜索"
        displayName = "Search"
    }
}
