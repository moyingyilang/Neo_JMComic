package com.yourpackage.neojmcomic.utils.redux

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 全局State基类 */
interface ReduxState

/** Action基类 */
interface ReduxAction

/** Reducer接口 */
typealias Reducer<S extends ReduxState, A extends ReduxAction> = (state: S, action: A) -> S

/** 合并多个Reducer */
fun <S extends ReduxState, A extends ReduxAction> combineReducers(
    reducers: Map<String, Reducer<out ReduxState, A>>,
    initialState: S
): Reducer<S, A> {
    return { state, action ->
        var newState = state
        reducers.forEach { (key, reducer) ->
            val sliceState = (state as Map<String, ReduxState>)[key] ?: return@forEach
            val newSliceState = reducer(sliceState, action)
            (newState as MutableMap<String, ReduxState>)[key] = newSliceState
        }
        newState
    }
}

/** Redux Store核心类（整合configureStore+rootReducer） */
class ReduxStore private constructor(
    private val rootReducer: Reducer<ReduxState, ReduxAction>,
    initialState: ReduxState
) {
    private val _state = MutableLiveData<ReduxState>(initialState)
    val state: LiveData<ReduxState> = _state

    // 单例模式
    companion object {
        @Volatile
        private var instance: ReduxStore? = null

        fun init(
            context: Context,
            rootReducer: Reducer<ReduxState, ReduxAction>,
            initialState: ReduxState
        ): ReduxStore {
            if (instance == null) {
                synchronized(this) {
                    instance = ReduxStore(rootReducer, initialState)
                }
            }
            return instance!!
        }

        fun getInstance(): ReduxStore = instance
            ?: throw IllegalStateException("ReduxStore not initialized")
    }

    /** 分发Action */
    fun dispatch(action: ReduxAction) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentState = _state.value ?: return@launch
            val newState = rootReducer(currentState, action)
            withContext(Dispatchers.Main) {
                _state.value = newState
            }
        }
    }

    /** 订阅状态变化 */
    fun <T> subscribe(
        owner: LifecycleOwner,
        selector: (ReduxState) -> T,
        observer: (T) -> Unit
    ) {
        var lastValue = selector(_state.value ?: return)
        _state.observe(owner) { newState ->
            val newValue = selector(newState)
            if (newValue != lastValue) {
                lastValue = newValue
                observer(newValue)
            }
        }
    }
}
