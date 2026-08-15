package com.example.cache

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import com.example.data.local.dao.CachedSnapshotDao
import com.example.data.local.entity.CachedSnapshotEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OfflineCacheManager(
    private val context: Context,
    private val cachedSnapshotDao: CachedSnapshotDao
) {
    /**
     * Checks if the device has an active internet connection
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Calculates the total WebView and local cache size in megabytes
     */
    suspend fun getCacheSizeBytes(): Long = withContext(Dispatchers.IO) {
        var size: Long = 0
        try {
            val cacheDir = context.cacheDir
            size += getFolderSize(cacheDir)

            val webViewCacheDir = File(context.applicationInfo.dataDir, "app_webview")
            if (webViewCacheDir.exists()) {
                size += getFolderSize(webViewCacheDir)
            }
        } catch (_: Exception) {}
        size
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0
        var size: Long = 0
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                size += getFolderSize(child)
            }
        } else {
            size += file.length()
        }
        return size
    }

    /**
     * Clears WebView cache, WebStorage, and cookies
     */
    suspend fun clearAllCache(webView: WebView?) = withContext(Dispatchers.Main) {
        try {
            webView?.clearCache(true)
            webView?.clearHistory()
            webView?.clearFormData()
            WebStorage.getInstance().deleteAllData()
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } catch (_: Exception) {}

        withContext(Dispatchers.IO) {
            try {
                context.cacheDir.deleteRecursively()
            } catch (_: Exception) {}
        }
    }

    /**
     * Saves a snapshot of an antique catalog page for offline browsing
     */
    suspend fun savePageSnapshot(url: String, title: String, html: String, category: String = "Catalog") {
        withContext(Dispatchers.IO) {
            val size = html.toByteArray(Charsets.UTF_8).size.toLong()
            val summary = if (html.length > 200) html.substring(0, 200) else html
            cachedSnapshotDao.insertSnapshot(
                CachedSnapshotEntity(
                    url = url,
                    title = title.ifBlank { "From Europe To You - $category" },
                    summary = summary,
                    htmlContent = html,
                    sizeBytes = size,
                    category = category
                )
            )
        }
    }
}
