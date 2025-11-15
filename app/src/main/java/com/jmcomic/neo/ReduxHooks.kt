package com.yourpackage.neojmcomic.utils.redux

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** ViewModel基类（整合useDispatch+useSelector，绑定生命周期） */
open class ReduxViewModel : ViewModel() {
    private val store = ReduxStore.getInstance()

    /** 分发Action */
    fun dispatch(action: ReduxAction) {
        viewModelScope.launch(Dispatchers.IO) {
            store.dispatch(action)
        }
    }

    /** 订阅状态 */
    fun <T> select(
        owner: LifecycleOwner,
        selector: (ReduxState) -> T,
        observer: (T) -> Unit
    ) {
        store.subscribe(owner, selector, observer)
    }

    /** 订阅指定切片状态 */
    fun <S : ReduxState, T> selectSlice(
        owner: LifecycleOwner,
        sliceName: String,
        selector: (S) -> T,
        observer: (T) -> Unit
    ) {
        select(owner, { state ->
            val sliceState = (state as Map<String, ReduxState>)[sliceName] as S
            selector(sliceState)
        }, observer)
    }
}
