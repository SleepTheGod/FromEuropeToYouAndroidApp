package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.theme.DarkModeJsInjector
import com.example.theme.ThemeMode
import com.example.viewmodel.WebUiState
import com.example.viewmodel.WebViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SecureWebView(
    viewModel: WebViewModel,
    uiState: WebUiState,
    modifier: Modifier = Modifier
) {
    val systemInDark = isSystemInDarkTheme()
    val isDarkMode = remember(uiState.themeMode, systemInDark) {
        when (uiState.themeMode) {
            ThemeMode.SYSTEM -> systemInDark
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Store instance in ViewModel
                viewModel.webViewInstance = this

                // Hardware acceleration
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                // Configure Security and Engine
                settings.apply {
                    // JavaScript is strictly enabled as requested
                    javaScriptEnabled = true
                    // DOM Storage & IndexedDB support
                    domStorageEnabled = true
                    databaseEnabled = true

                    // Strict Security Best Practices
                    allowFileAccess = false
                    allowContentAccess = false
                    allowFileAccessFromFileURLs = false
                    allowUniversalAccessFromFileURLs = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                    // Cache Configuration
                    cacheMode = if (uiState.isOfflineMode || !uiState.isNetworkOnline) {
                        WebSettings.LOAD_CACHE_ELSE_NETWORK
                    } else {
                        WebSettings.LOAD_DEFAULT
                    }

                    // Viewport & Zoom
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    builtInZoomControls = true
                    displayZoomControls = false

                    // User Agent
                    userAgentString = "${settings.userAgentString} FromEuropeToYouSecureApp/1.0"
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        viewModel.onProgressChanged(newProgress)
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        if (!title.isNullOrBlank() && view?.url != null) {
                            viewModel.onPageFinished(view.url ?: uiState.currentUrl, title)
                        }
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        url?.let { viewModel.onPageStarted(it) }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        url?.let {
                            viewModel.onPageFinished(it, view?.title)
                            // Acquire active SSL certificate from WebView for inspection
                            viewModel.onSslCertificateAcquired(view?.certificate, it)
                        }

                        // Apply Dark Mode CSS if dark theme active
                        if (isDarkMode) {
                            view?.evaluateJavascript(DarkModeJsInjector.DARK_MODE_CSS_PAYLOAD, null)
                        } else {
                            view?.evaluateJavascript(DarkModeJsInjector.REMOVE_DARK_MODE_PAYLOAD, null)
                        }
                    }

                    override fun onReceivedSslError(
                        view: WebView?,
                        handler: SslErrorHandler?,
                        error: SslError?
                    ) {
                        // CRITICAL MITM DEFENSE: Strictly cancel and abort on SSL errors
                        handler?.cancel()
                        error?.let { viewModel.onReceivedSslError(it) }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request?.isForMainFrame == true) {
                            viewModel.onReceivedPageError(error?.description?.toString() ?: "Network Connection Error")
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val uri = request?.url ?: return false
                        val host = uri.host ?: ""

                        // Allow navigating within fromeuropetoyou.com
                        if (host.contains("fromeuropetoyou.com")) {
                            return false
                        }

                        // Handle external schemes like mailto, tel
                        return try {
                            val scheme = uri.scheme
                            if (scheme == "mailto" || scheme == "tel" || scheme == "maps") {
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                                true
                            } else {
                                // Keep external links or open securely
                                false
                            }
                        } catch (_: Exception) {
                            false
                        }
                    }
                }

                // Initial Load
                loadUrl(uiState.currentUrl)
            }
        },
        update = { webView ->
            viewModel.webViewInstance = webView
            // Update cache mode based on offline state
            val desiredCacheMode = if (uiState.isOfflineMode || !uiState.isNetworkOnline) {
                WebSettings.LOAD_CACHE_ELSE_NETWORK
            } else {
                WebSettings.LOAD_DEFAULT
            }
            if (webView.settings.cacheMode != desiredCacheMode) {
                webView.settings.cacheMode = desiredCacheMode
            }

            // Sync Dark Mode state
            if (isDarkMode) {
                webView.evaluateJavascript(DarkModeJsInjector.DARK_MODE_CSS_PAYLOAD, null)
            } else {
                webView.evaluateJavascript(DarkModeJsInjector.REMOVE_DARK_MODE_PAYLOAD, null)
            }
        }
    )
}
