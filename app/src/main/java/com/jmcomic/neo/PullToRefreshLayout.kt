// PullToRefreshLayout.kt
class PullToRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SwipeRefreshLayout(context, attrs, defStyleAttr) {

    private var headHeight = 50.dpToPx()
    private var startDistance = 30.dpToPx()
    private var threshold = 50.dpToPx()
    private var resistance = 0.6f
    
    private var currentState = State.NORMAL
    private var onRefreshListener: (() -> Unit)? = null
    
    enum class State {
        NORMAL, PULLING, CAN_RELEASE, REFRESHING, COMPLETE
    }
    
    init {
        setOnRefreshListener {
            if (currentState == State.CAN_RELEASE) {
                currentState = State.REFRESHING
                onRefreshListener?.invoke()
            }
        }
    }
    
    fun setOnPullRefreshListener(listener: () -> Unit) {
        this.onRefreshListener = listener
    }
    
    fun setRefreshing(refreshing: Boolean) {
        isRefreshing = refreshing
        currentState = if (refreshing) State.REFRESHING else State.COMPLETE
    }
    
    fun renderText(state: State, progress: Int): String {
        return when (state) {
            State.PULLING -> "下拉刷新"
            State.CAN_RELEASE -> "释放立即刷新"
            State.REFRESHING -> "刷新中..."
            State.COMPLETE -> "刷新完成"
            else -> ""
        }
    }
}

// 扩展函数
fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()