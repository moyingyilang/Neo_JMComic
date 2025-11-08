// MainActivity.kt
// 在文件顶部添加导入
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.yourpackage.databinding.ActivityMainBinding
import com.yourpackage.viewmodel.MainViewModel
import com.yourpackage.viewmodel.GlobalConfigViewModel
import com.yourpackage.manager.AuthManager
import com.yourpackage.manager.HostManager
import com.yourpackage.manager.HotUpdateManager
import com.yourpackage.manager.SecurityManager
import com.yourpackage.util.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

// 添加伴生对象定义常量
companion object {
    private const val TAG = "MainActivity"
}
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var backButtonHandler: SecurityManager.BackButtonHandler
    
    private val mainViewModel: MainViewModel by viewModels()
    private val globalConfigViewModel: GlobalConfigViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 安全防护
        SecurityManager.enableDevToolsBlocker(this)
        backButtonHandler = SecurityManager.BackButtonHandler(this)
        
        setupNavigation()
        setupObservers()
        initializeApp()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // 设置底部导航
        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.mainFragment)
                    true
                }
                R.id.navigation_library -> {
                    navController.navigate(R.id.libraryFragment)
                    true
                }
                R.id.navigation_member -> {
                    navController.navigate(R.id.memberFragment)
                    true
                }
                else -> false
            }
        }
        
        // 监听导航变化，更新底部导航选中状态
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mainFragment -> binding.bottomNav.selectedItemId = R.id.navigation_home
                R.id.libraryFragment -> binding.bottomNav.selectedItemId = R.id.navigation_library
                R.id.memberFragment -> binding.bottomNav.selectedItemId = R.id.navigation_member
            }
        }
    }
    
    private fun setupObservers() {
        globalConfigViewModel.config.observe(this) { config ->
            // 更新UI based on global config
            updateTheme(config.darkMode)
            updateLanguage(config.language)
        }
        
        mainViewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> hideLoading()
                is UiState.Error -> showError(state.message)
                else -> {}
            }
        }
    }
    
    private fun initializeApp() {
        // 检查认证状态
        if (AuthManager.checkAuthExpiry()) {
            AuthManager.clearAuth()
        } else {
            // 自动登录逻辑
            autoLogin()
        }
        
        // 初始化主机
        initializeHost()
        
        // 检查热更新
        checkHotUpdate()
        
        // 加载主页数据
        mainViewModel.loadInitialData()
    }
    
    private fun autoLogin() {
        val memberInfo = AuthManager.getMemberInfo()
        if (memberInfo != null) {
            globalConfigViewModel.updateConfig { config ->
                config.isLoggedIn = true
                config.memberInfo = memberInfo
            }
        }
    }
    
    private fun initializeHost() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hostData = HostManager.fetchHost()
                globalConfigViewModel.updateHost(hostData)
            } catch (e: Exception) {
                Log.e("MainActivity", "Host initialization failed", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "网络连接失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun checkHotUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            HotUpdateManager.initHotUpdate(this@MainActivity)
        }
    }
    
    override fun onBackPressed() {
        if (!backButtonHandler.handleBackPress()) {
            super.onBackPressed()
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun updateTheme(isDarkMode: Boolean) {
        // 实现主题切换
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun updateLanguage(language: String) {
        // 实现语言切换
        val resources = resources
        val configuration = resources.configuration
        val locale = when (language) {
            "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            "zh-TW" -> Locale.TRADITIONAL_CHINESE
            else -> Locale.getDefault()
        }
        
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}