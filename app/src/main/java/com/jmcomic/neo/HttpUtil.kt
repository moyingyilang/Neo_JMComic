// network/HttpUtil.kt
object HttpUtil {
    
    private const val MAX_RETRIES = 3
    private const val TIMEOUT_MS = 15000L
    
    private var getRetryCount = 0
    private var postRetryCount = 0
    
    // 获取Token参数
    private fun getTokenParams(): Pair<String, String> {
        val currentTime = System.currentTimeMillis() / 1000
        val version = BuildConfig.VERSION_NAME
        val tokenParam = "$currentTime,$version"
        val token = EncryptionUtil.md5("$currentTime${ApiPaths.TOKEN}")
        
        return Pair(tokenParam, token)
    }
    
    // GET请求
    suspend fun fetchGet(
        url: String,
        params: Map<String, Any> = emptyMap(),
        onSuccess: (ApiResponse<Any>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val fullUrl = buildUrlWithParams(url, params)
                val (tokenParam, token) = getTokenParams()
                val authHeaders = getAuthHeaders()
                
                val response = executeRequestWithTimeout(
                    url = fullUrl,
                    method = "GET",
                    headers = buildHeaders(tokenParam, token, authHeaders)
                )
                
                handleResponse(response, url, onSuccess, onError, ::fetchGet)
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
    
    // POST请求
    suspend fun fetchPost(
        url: String,
        params: Map<String, Any> = emptyMap(),
        onSuccess: (ApiResponse<Any>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val (tokenParam, token) = getTokenParams()
                val authHeaders = getAuthHeaders()
                
                val response = executeRequestWithTimeout(
                    url = url,
                    method = "POST",
                    headers = buildHeaders(tokenParam, token, authHeaders),
                    body = buildFormData(params)
                )
                
                handleResponse(response, url, onSuccess, onError, ::fetchPost)
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
    
    // 构建带参数的URL
    private fun buildUrlWithParams(url: String, params: Map<String, Any>): String {
        if (params.isEmpty()) return url
        
        val urlBuilder = StringBuilder(url)
        urlBuilder.append("?")
        
        params.forEach { (key, value) ->
            urlBuilder.append("$key=${value.toString().urlEncode()}&")
        }
        
        return urlBuilder.removeSuffix("&").toString()
    }
    
    // 构建请求头
    private fun buildHeaders(tokenParam: String, token: String, authHeaders: Map<String, String>): Map<String, String> {
        val headers = mutableMapOf(
            "Tokenparam" to tokenParam,
            "Token" to token
        )
        
        headers.putAll(authHeaders)
        return headers
    }
    
    // 获取认证头信息
    private fun getAuthHeaders(): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        
        val jwtToken = AuthManager.getToken()
        if (jwtToken != null) {
            headers["Authorization"] = "Bearer $jwtToken"
        }
        
        val memberInfo = AuthManager.getMemberInfo()
        if (memberInfo != null) {
            headers["Cookie"] = "AVS=${memberInfo.s}" // 假设memberInfo有s字段
        }
        
        return headers
    }
    
    // 执行带超时的请求
    private suspend fun executeRequestWithTimeout(
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        body: RequestBody? = null
    ): Response {
        return withTimeout(TIMEOUT_MS) {
            val request = buildRequest(url, method, headers, body)
            OkHttpClient().newCall(request).execute()
        }
    }
    
    // 构建请求
    private fun buildRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: RequestBody? = null
    ): Request {
        val requestBuilder = Request.Builder()
            .url(url)
            .apply {
                headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
        
        return when (method) {
            "GET" -> requestBuilder.get().build()
            "POST" -> requestBuilder.post(body ?: FormBody.Builder().build()).build()
            else -> throw IllegalArgumentException("Unsupported method: $method")
        }
    }
    
    // 构建FormData
    private fun buildFormData(params: Map<String, Any>): FormBody {
        val formBuilder = FormBody.Builder()
        params.forEach { (key, value) ->
            formBuilder.add(key, value.toString())
        }
        return formBuilder.build()
    }
    
    // 处理响应
    private suspend fun handleResponse(
        response: Response,
        url: String,
        onSuccess: (ApiResponse<Any>) -> Unit,
        onError: (Throwable) -> Unit,
        retryFunction: suspend (String, Map<String, Any>, (ApiResponse<Any>) -> Unit, (Throwable) -> Unit) -> Unit
    ) {
        if (!response.isSuccessful && response.code != 401) {
            handleErrorResponse(response, url, onError, retryFunction)
            return
        }
        
        try {
            val responseBody = response.body?.string() ?: "{}"
            val apiResponse = tryDecryption(responseBody, url)
            
            withContext(Dispatchers.Main) {
                onSuccess(apiResponse)
            }
            
            // 重置重试计数
            resetRetryCount()
            
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e)
            }
        }
    }
    
    // 处理错误响应
    private suspend fun handleErrorResponse(
        response: Response,
        url: String,
        onError: (Throwable) -> Unit,
        retryFunction: suspend (String, Map<String, Any>, (ApiResponse<Any>) -> Unit, (Throwable) -> Unit) -> Unit
    ) {
        val errorMessage = buildErrorMessage(response.code, url, "GET")
        
        if (shouldRetry(retryFunction)) {
            incrementRetryCount(retryFunction)
            // 重试逻辑
            retryFunction(url, emptyMap(), { /* success */ }, onError)
        } else {
            withContext(Dispatchers.Main) {
                onError(Exception(errorMessage))
                showErrorDialog(errorMessage)
            }
        }
    }
    
    // 尝试解密
    private fun tryDecryption(responseBody: String, url: String): ApiResponse<Any> {
        val responseObj = Gson().fromJson(responseBody, ApiResponse::class.java)
        
        val possibleKeys = listOf(
            listOf(49, 56, 53, 72, 99, 111, 109, 105, 99, 51, 80, 65, 80, 80, 55, 82), // 185Hcomic3PAPP7R
            listOf(49, 56, 99, 111, 109, 105, 99, 65, 80, 80, 67, 111, 110, 116, 101, 110, 116) // 18comicAPPContent
        )
        
        val adKeyUrls = listOf("ad_content_all", "advertise_all")
        val isAdUrl = adKeyUrls.any { url.contains(it) }
        
        for (key in possibleKeys) {
            val content = String(key.map { it.toChar() }.toCharArray())
            val keyToTry = if (isAdUrl) {
                EncryptionUtil.md5(content)
            } else {
                val currentTime = System.currentTimeMillis() / 1000
                EncryptionUtil.md5("$currentTime$content")
            }
            
            try {
                val decryptedData = EncryptionUtil.decryptAES(responseObj.data.toString(), keyToTry)
                responseObj.data = Gson().fromJson(decryptedData, Any::class.java)
                return responseObj
            } catch (e: Exception) {
                // 继续尝试下一个密钥
                continue
            }
        }
        
        // 所有解密尝试都失败
        responseObj.data = ""
        return responseObj
    }
    
    // 构建错误消息
    private fun buildErrorMessage(statusCode: Int, url: String, method: String): String {
        val hostInfo = getApiHostInfo()
        val taipeiTime = FunctionUtils.getTaipeiTimeString()
        val version = BuildConfig.VERSION_NAME
        
        return """
            $method 發生錯誤，請回報管理員
            
            現在時間：$taipeiTime,
            source=${hostInfo.hostName}
            key=${parseUrl(url)}
            
            ＊目前版本為 ${version} 版，最新版本為 $version 版
            
            若仍有問題請截圖到官方Discord群
            https://discord.gg/V74p7HM
            #網站與app問題回報
        """.trimIndent()
    }
    
    // 获取API主机信息
    private fun getApiHostInfo(): ApiHostInfo {
        val apiUrl = GlobalStore.apiUrl
        val host = java.net.URL(apiUrl).host
        
        // 假设GlobalStore.hostServer是List<Pair<String, String>>
        val match = GlobalStore.hostServer.find { it.first == host }
        return ApiHostInfo(hostName = match?.second)
    }
    
    // 解析URL
    private fun parseUrl(inputUrl: String): String {
        return try {
            val url = java.net.URL(inputUrl)
            val path = url.path
            path.split('/').filter { it.isNotEmpty() }.lastOrNull() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    // 重试逻辑
    private fun shouldRetry(retryFunction: suspend (String, Map<String, Any>, (ApiResponse<Any>) -> Unit, (Throwable) -> Unit) -> Unit): Boolean {
        return when (retryFunction) {
            ::fetchGet -> getRetryCount < MAX_RETRIES
            ::fetchPost -> postRetryCount < MAX_RETRIES
            else -> false
        }
    }
    
    private fun incrementRetryCount(retryFunction: suspend (String, Map<String, Any>, (ApiResponse<Any>) -> Unit, (Throwable) -> Unit) -> Unit) {
        when (retryFunction) {
            ::fetchGet -> getRetryCount++
            ::fetchPost -> postRetryCount++
        }
    }
    
    private fun resetRetryCount() {
        getRetryCount = 0
        postRetryCount = 0
    }
    
    // 显示错误对话框
    private fun showErrorDialog(message: String) {
        // 在实际应用中，这里应该显示一个对话框
        // ErrorDialog.show(message)
        Log.e("HttpUtil", "Network Error: $message")
    }
}

// 数据类
data class ApiHostInfo(val hostName: String?)

// 字符串扩展 - URL编码
private fun String.urlEncode(): String {
    return java.net.URLEncoder.encode(this, "UTF-8")
}