package com.yourpackage.neojmcomic.utils.redux.business

import com.yourpackage.neojmcomic.utils.redux.ReduxAction
import com.yourpackage.neojmcomic.utils.redux.ReduxState
import com.yourpackage.neojmcomic.utils.redux.ReduxSlice
import com.yourpackage.neojmcomic.utils.redux.SliceBuilder

// region 基础业务状态定义
data class BlogsState(
    val blogsList: BlogsList = BlogsList(emptyList(), 0),
    val blogsInfo: Map<String, Any> = emptyMap(),
    val gamesInfo: Map<String, Any> = emptyMap(),
    val isBlogLoading: Boolean = true,
    val isLoadMore: Boolean = false,
    val isRefreshing: Boolean = false
) : ReduxState

data class BlogsList(val list: List<Any>, val total: Int)

data class CategoriesState(
    val categoriesList: Map<String, Any> = emptyMap(),
    val cateFilterList: CateFilterList = CateFilterList(emptyList(), emptyList(), 0, ""),
    val isLoading: Boolean = true,
    val isLoadMore: Boolean = false,
    val isRefreshing: Boolean = false
) : ReduxState

data class CateFilterList(
    val list: List<Any>,
    val tags: List<Any>,
    val total: Int,
    val search_query: String
)

data class MemberState(
    val info: Map<String, Any> = emptyMap(),
    val favoriteList: FavoriteList = FavoriteList(emptyList(), emptyList(), 0, 0),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
) : ReduxState

data class FavoriteList(
    val list: List<Any>,
    val folder_list: List<Any>,
    val total: Int,
    val count: Int
)
// endregion

// region 业务Action定义
sealed class BusinessAction : ReduxAction {
    // Blogs相关Action
    object LoadBlogsList : BusinessAction()
    data class GetBlogsList(val list: List<Any>, val total: Int) : BusinessAction()
    data class ClearBlogState(val key: String) : BusinessAction()

    // Categories相关Action
    object LoadCategoriesList : BusinessAction()
    data class GetCategoriesFilterList(val content: List<Any>, val tags: List<Any>, val total: Int, val search_query: String) : BusinessAction()

    // Member相关Action
    object LoadMemberList : BusinessAction()
    data class GetFavoriteList(val list: List<Any>, val folder_list: List<Any>, val total: Int, val count: Int) : BusinessAction()
}
// endregion

// region 业务切片创建
// Blogs切片
val blogsSlice: ReduxSlice<BlogsState, BusinessAction> = SliceBuilder<BlogsState, BusinessAction>(
    name = "blogs",
    initialState = BlogsState()
)
    .addCase(BusinessAction.LoadBlogsList::class.java) { state, _ ->
        state.copy(isBlogLoading = true, isRefreshing = true)
    }
    .addCase(BusinessAction.GetBlogsList::class.java) { state, action ->
        action as BusinessAction.GetBlogsList
        state.copy(
            blogsList = BlogsList(action.list, action.total),
            isBlogLoading = false,
            isLoadMore = false,
            isRefreshing = false
        )
    }
    .addCase(BusinessAction.ClearBlogState::class.java) { state, action ->
        action as BusinessAction.ClearBlogState
        when (action.key) {
            "blogsList" -> state.copy(blogsList = BlogsList(emptyList(), 0))
            "blogsInfo" -> state.copy(blogsInfo = emptyMap())
            else -> state
        }
    }
    .build()

// Categories切片
val categoriesSlice: ReduxSlice<CategoriesState, BusinessAction> = SliceBuilder<CategoriesState, BusinessAction>(
    name = "categories",
    initialState = CategoriesState()
)
    .addCase(BusinessAction.LoadCategoriesList::class.java) { state, _ ->
        state.copy(isLoading = true, isRefreshing = true)
    }
    .addCase(BusinessAction.GetCategoriesFilterList::class.java) { state, action ->
        action as BusinessAction.GetCategoriesFilterList
        state.copy(
            cateFilterList = CateFilterList(
                list = action.content,
                tags = action.tags,
                total = action.total,
                search_query = action.search_query
            ),
            isLoading = false,
            isRefreshing = false
        )
    }
    .build()

// Member切片
val memberSlice: ReduxSlice<MemberState, BusinessAction> = SliceBuilder<MemberState, BusinessAction>(
    name = "member",
    initialState = MemberState()
)
    .addCase(BusinessAction.LoadMemberList::class.java) { state, _ ->
        state.copy(isLoading = true, isRefreshing = true)
    }
    .addCase(BusinessAction.GetFavoriteList::class.java) { state, action ->
        action as BusinessAction.GetFavoriteList
        state.copy(
            favoriteList = FavoriteList(
                list = action.list,
                folder_list = action.folder_list,
                total = action.total,
                count = action.count
            ),
            isLoading = false,
            isRefreshing = false
        )
    }
    .build()

// 合并所有业务切片Reducer
val businessReducers = mapOf(
    "blogs" to blogsSlice.buildReducer(),
    "categories" to categoriesSlice.buildReducer(),
    "member" to memberSlice.buildReducer()
)

// 全局初始状态
val initialGlobalState = mapOf(
    "blogs" to blogsSlice.initialState,
    "categories" to categoriesSlice.initialState,
    "member" to memberSlice.initialState
)
