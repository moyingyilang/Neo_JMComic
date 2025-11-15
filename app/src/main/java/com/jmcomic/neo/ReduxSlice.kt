package com.yourpackage.neojmcomic.utils.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 切片配置数据类 */
data class SliceConfig<S : ReduxState, A : ReduxAction>(
    val name: String,
    val initialState: S,
    val reducers: Map<String, Reducer<S, A>>,
    val extraReducers: Map<Class<out A>, Reducer<S, A>> = emptyMap()
)

/** 切片类（整合createSlice+createReducer） */
class ReduxSlice<S : ReduxState, A : ReduxAction>(
    private val config: SliceConfig<S, A>
) {
    val name = config.name
    val initialState = config.initialState

    /** 构建切片Reducer */
    fun buildReducer(): Reducer<S, A> {
        return { state, action ->
            // 优先匹配extraReducers（异步Action）
            config.extraReducers[action::class.java]?.invoke(state, action) 
                ?: // 匹配普通reducers
                config.reducers[action::class.simpleName]?.invoke(state, action)
                ?: state // 无匹配返回原状态
        }
    }

    /** 创建同步Action */
    inline fun <reified T : A> createAction(): T where T : ReduxAction, T : Enum<T> {
        return enumValues<T>().first()
    }

    /** 创建异步Thunk（整合createAsyncThunk+AsyncReducer） */
    fun <Params, Result> createAsyncThunk(
        actionType: Class<out A>,
        payloadCreator: suspend (params: Params) -> Result,
        onPending: Reducer<S, A>? = null,
        onFulfilled: (S, Result) -> S,
        onRejected: (S, Throwable) -> S
    ): (params: Params) -> Unit {
        return { params ->
            CoroutineScope(Dispatchers.IO).launch {
                // 分发Pending Action
                onPending?.let { reducer ->
                    ReduxStore.getInstance().dispatch(object : A {}).also {
                        val currentState = ReduxStore.getInstance().state.value as Map<String, ReduxState>
                        val sliceState = currentState[name] as S
                        val newState = reducer(sliceState, it)
                        (currentState as MutableMap<String, ReduxState>)[name] = newState
                    }
                }

                try {
                    // 执行异步逻辑
                    val result = payloadCreator(params)
                    // 分发Fulfilled Action
                    ReduxStore.getInstance().dispatch(object : A {}).also {
                        val currentState = ReduxStore.getInstance().state.value as Map<String, ReduxState>
                        val sliceState = currentState[name] as S
                        val newState = onFulfilled(sliceState, result)
                        (currentState as MutableMap<String, ReduxState>)[name] = newState
                    }
                } catch (e: Throwable) {
                    // 分发Rejected Action
                    ReduxStore.getInstance().dispatch(object : A {}).also {
                        val currentState = ReduxStore.getInstance().state.value as Map<String, ReduxState>
                        val sliceState = currentState[name] as S
                        val newState = onRejected(sliceState, e)
                        (currentState as MutableMap<String, ReduxState>)[name] = newState
                    }
                }
            }
        }
    }
}

/** 快速创建切片的构建器 */
class SliceBuilder<S : ReduxState, A : ReduxAction>(
    private val name: String,
    private val initialState: S
) {
    private val reducers = mutableMapOf<String, Reducer<S, A>>()
    private val extraReducers = mutableMapOf<Class<out A>, Reducer<S, A>>()

    fun addCase(actionClass: Class<out A>, reducer: Reducer<S, A>): SliceBuilder<S, A> {
        extraReducers[actionClass] = reducer
        return this
    }

    fun addReducer(actionName: String, reducer: Reducer<S, A>): SliceBuilder<S, A> {
        reducers[actionName] = reducer
        return this
    }

    fun build(): ReduxSlice<S, A> {
        return ReduxSlice(
            SliceConfig(
                name = name,
                initialState = initialState,
                reducers = reducers,
                extraReducers = extraReducers
            )
        )
    }
}
