package com.hermesdiary.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebViewAssetLoader
import com.google.android.material.progressindicator.LinearProgressIndicator

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var errorView: LinearLayout
    private lateinit var prefs: SharedPreferences

    companion object {
        private const val PREFS_NAME = "hermes_diary_prefs"
        private const val KEY_SITE_URL = "site_url"
        private const val DEFAULT_URL = "https://save-magnificent-configuring-finest.trycloudflare.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        errorView = findViewById(R.id.errorView)

        setupWebView()
        setupSwipeRefresh()
        setupErrorView()

        loadUrl()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.setSupportZoom(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.allowFileAccess = false
        settings.allowContentAccess = false

        // User agent for mobile
        settings.userAgentString = settings.userAgentString?.replace(
            "Android", "HermesDiary/Android"
        )

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                errorView.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // Only show error for main frame
                if (request?.isForMainFrame == true) {
                    swipeRefresh.isRefreshing = false
                    progressBar.visibility = View.GONE

                    if (!isNetworkAvailable()) {
                        showError("网络不可用\n请检查网络连接后重试")
                    } else {
                        showError("加载失败\n请检查网站地址是否正确")
                    }
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(
            android.R.color.holo_purple,
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light
        )
        swipeRefresh.setProgressBackgroundColorSchemeResource(android.R.color.background_dark)
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
    }

    private fun setupErrorView() {
        val retryButton = errorView.findViewById<Button>(R.id.retryButton)
        retryButton.setOnClickListener {
            errorView.visibility = View.GONE
            loadUrl()
        }

        val settingsButton = errorView.findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            showUrlSettings()
        }
    }

    private fun loadUrl() {
        if (!isNetworkAvailable()) {
            showError("网络不可用\n请检查网络连接后重试")
            return
        }

        val url = getSavedUrl()
        webView.loadUrl(url)
    }

    private fun getSavedUrl(): String {
        return prefs.getString(KEY_SITE_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected == true
        }
    }

    private fun showError(message: String) {
        val errorText = errorView.findViewById<TextView>(R.id.errorText)
        errorText.text = message
        errorView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun showUrlSettings() {
        val currentUrl = getSavedUrl()
        val input = EditText(this).apply {
            setText(currentUrl)
            hint = "输入网站地址"
            selectAll()
        }

        AlertDialog.Builder(this)
            .setTitle("设置")
            .setMessage("输入网站地址（包括 https://）")
            .setView(input)
            .setPositiveButton("保存") { _, _ ->
                val newUrl = input.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    val urlToSave = if (!newUrl.startsWith("http")) {
                        "https://$newUrl"
                    } else {
                        newUrl
                    }
                    prefs.edit().putString(KEY_SITE_URL, urlToSave).apply()
                    loadUrl()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }
}
