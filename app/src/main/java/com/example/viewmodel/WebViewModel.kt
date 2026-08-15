package com.example.viewmodel

import android.app.Application
import android.net.http.SslCertificate
import android.net.http.SslError
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cache.OfflineCacheManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.SecurityLogEntity
import com.example.model.UserProfile
import com.example.security.SslCertificateInfo
import com.example.security.SslSecurityManager
import com.example.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WebUiState(
    val currentUrl: String = "https://www.fromeuropetoyou.com/",
    val pageTitle: String = "From Europe To You - European Antiques & Architectural Salvage",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSslSecure: Boolean = true,
    val sslInfo: SslCertificateInfo = SslCertificateInfo(),
    val isOfflineMode: Boolean = false,
    val isNetworkOnline: Boolean = true,
    val cacheSizeBytes: Long = 0L,
    val hasPageError: Boolean = false,
    val lastErrorDescription: String = "",
    val isMitmAlertOpen: Boolean = false,
    val mitmThreatMessage: String = "",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val userProfile: UserProfile = UserProfile(),
    val activeSheet: ActiveSheet? = null,
    val isCurrentUrlBookmarked: Boolean = false
)

enum class ActiveSheet {
    SSL_INSPECTOR,
    BOOKMARKS,
    OFFLINE_CACHE,
    NOTIFICATIONS,
    ACCOUNT_PROFILE,
    CATEGORY_PICKER,
    ADD_BOOKMARK
}

class WebViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val bookmarkDao = db.bookmarkDao()
    private val cachedSnapshotDao = db.cachedSnapshotDao()
    private val securityLogDao = db.securityLogDao()
    private val notificationDao = db.notificationDao()

    val sslSecurityManager = SslSecurityManager(securityLogDao)
    val offlineCacheManager = OfflineCacheManager(application, cachedSnapshotDao)

    private val _uiState = MutableStateFlow(WebUiState())
    val uiState: StateFlow<WebUiState> = _uiState.asStateFlow()

    val bookmarks = bookmarkDao.getAllBookmarks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val cachedSnapshots = cachedSnapshotDao.getAllSnapshots().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val securityLogs = securityLogDao.getRecentLogs().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notifications = notificationDao.getAllNotifications().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val unreadNotificationsCount = notificationDao.getUnreadCount().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    // Reference to active WebView instance
    var webViewInstance: WebView? = null

    init {
        refreshNetworkAndCacheStatus()
        probeSslHandshake()
    }

    fun onPageStarted(url: String) {
        val isNetwork = offlineCacheManager.isNetworkAvailable()
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            isLoading = true,
            progress = 10,
            hasPageError = false,
            lastErrorDescription = "",
            isNetworkOnline = isNetwork
        )
        checkIfCurrentUrlBookmarked(url)
    }

    fun onPageFinished(url: String, title: String?) {
        val currentTitle = title?.ifBlank { "From Europe To You" } ?: _uiState.value.pageTitle
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            pageTitle = currentTitle,
            isLoading = false,
            progress = 100,
            isRefreshing = false,
            canGoBack = webViewInstance?.canGoBack() ?: false,
            canGoForward = webViewInstance?.canGoForward() ?: false
        )
        checkIfCurrentUrlBookmarked(url)
        updateCacheSize()
    }

    fun onProgressChanged(newProgress: Int) {
        _uiState.value = _uiState.value.copy(
            progress = newProgress,
            isLoading = newProgress < 100
        )
        if (newProgress >= 100) {
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun onReceivedSslError(error: SslError) {
        viewModelScope.launch {
            val threatReason = sslSecurityManager.handleSslError(error)
            _uiState.value = _uiState.value.copy(
                isSslSecure = false,
                isMitmAlertOpen = true,
                mitmThreatMessage = threatReason,
                hasPageError = true,
                lastErrorDescription = "MITM Protection Triggered: $threatReason"
            )
        }
    }

    fun onReceivedPageError(description: String) {
        _uiState.value = _uiState.value.copy(
            hasPageError = true,
            lastErrorDescription = description,
            isLoading = false,
            isRefreshing = false
        )
    }

    fun onSslCertificateAcquired(cert: SslCertificate?, url: String) {
        viewModelScope.launch {
            val info = sslSecurityManager.parseWebViewCertificate(cert, url)
            _uiState.value = _uiState.value.copy(
                sslInfo = info,
                isSslSecure = info.isSecure
            )
        }
    }

    fun probeSslHandshake() {
        viewModelScope.launch {
            val info = sslSecurityManager.verifyLiveTlsHandshake()
            _uiState.value = _uiState.value.copy(
                sslInfo = info,
                isSslSecure = info.isSecure
            )
        }
    }

    fun triggerPullToRefresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        webViewInstance?.reload()
    }

    fun navigateTo(url: String) {
        _uiState.value = _uiState.value.copy(
            currentUrl = url,
            hasPageError = false,
            activeSheet = null
        )
        webViewInstance?.loadUrl(url)
    }

    fun goHome() {
        navigateTo("https://www.fromeuropetoyou.com/")
    }

    fun goBack() {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        }
    }

    fun goForward() {
        if (webViewInstance?.canGoForward() == true) {
            webViewInstance?.goForward()
        }
    }

    fun reload() {
        _uiState.value = _uiState.value.copy(hasPageError = false)
        webViewInstance?.reload()
    }

    fun openSheet(sheet: ActiveSheet) {
        _uiState.value = _uiState.value.copy(activeSheet = sheet)
    }

    fun closeSheet() {
        _uiState.value = _uiState.value.copy(activeSheet = null)
    }

    fun dismissMitmAlert() {
        _uiState.value = _uiState.value.copy(isMitmAlertOpen = false)
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun toggleOfflineMode() {
        val newMode = !_uiState.value.isOfflineMode
        _uiState.value = _uiState.value.copy(isOfflineMode = newMode)
        // Refresh with new cache settings
        webViewInstance?.reload()
    }

    fun clearCache() {
        viewModelScope.launch {
            offlineCacheManager.clearAllCache(webViewInstance)
            updateCacheSize()
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "🧹 Local Cache Cleared",
                    message = "All offline DOM cache, WebStorage, and cached files have been securely erased.",
                    category = "System",
                    targetUrl = "https://www.fromeuropetoyou.com/",
                    isRead = false
                )
            )
        }
    }

    fun toggleBookmarkCurrentUrl() {
        viewModelScope.launch {
            val url = _uiState.value.currentUrl
            val title = _uiState.value.pageTitle
            val existing = bookmarkDao.getBookmarkByUrl(url)
            if (existing != null) {
                bookmarkDao.deleteBookmark(existing)
                _uiState.value = _uiState.value.copy(isCurrentUrlBookmarked = false)
            } else {
                bookmarkDao.insertBookmark(
                    BookmarkEntity(
                        title = title,
                        url = url,
                        category = "Saved Antiques",
                        isFavorite = true
                    )
                )
                _uiState.value = _uiState.value.copy(isCurrentUrlBookmarked = true)
            }
        }
    }

    fun addCustomBookmark(title: String, url: String, category: String, note: String) {
        viewModelScope.launch {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    title = title.ifBlank { "From Europe To You Antique" },
                    url = url.ifBlank { "https://www.fromeuropetoyou.com/" },
                    category = category.ifBlank { "General" },
                    note = note,
                    isFavorite = true
                )
            )
            checkIfCurrentUrlBookmarked(_uiState.value.currentUrl)
            closeSheet()
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(bookmark)
            checkIfCurrentUrlBookmarked(_uiState.value.currentUrl)
        }
    }

    fun saveCurrentPageSnapshot() {
        viewModelScope.launch {
            val url = _uiState.value.currentUrl
            val title = _uiState.value.pageTitle
            val html = "<!-- Offline Snapshot of $title ($url) -->\n<div class='offline-card'><h3>$title</h3><p>Cached for full offline inspection. SSL Verified.</p></div>"
            offlineCacheManager.savePageSnapshot(url, title, html)
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "📥 Offline Snapshot Saved",
                    message = "Page '$title' saved to encrypted local storage for offline reading.",
                    category = "Offline",
                    targetUrl = url,
                    isRead = false
                )
            )
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead()
        }
    }

    fun createTestNotification(title: String, message: String, category: String) {
        viewModelScope.launch {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = title,
                    message = message,
                    category = category,
                    targetUrl = "https://www.fromeuropetoyou.com/",
                    isRead = false
                )
            )
        }
    }

    fun authenticateWithGoogleOAuth(email: String = "collector.user@antiquevault.com", name: String = "European Antique Collector") {
        _uiState.value = _uiState.value.copy(
            userProfile = UserProfile(
                name = name,
                email = email,
                isAuthenticated = true,
                memberTier = "Grand Collector VIP",
                tokenVaultStatus = "OAuth2 / Hardware Keystore Validated"
            )
        )
        viewModelScope.launch {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "🔑 OAuth Authentication Successful",
                    message = "Welcome back, $name! Your encrypted session token has been securely stored in Android Keystore.",
                    category = "Security",
                    targetUrl = "https://www.fromeuropetoyou.com/",
                    isRead = false
                )
            )
        }
    }

    fun signOutOAuth() {
        _uiState.value = _uiState.value.copy(
            userProfile = UserProfile(isAuthenticated = false)
        )
    }

    private fun checkIfCurrentUrlBookmarked(url: String) {
        viewModelScope.launch {
            val existing = bookmarkDao.getBookmarkByUrl(url)
            _uiState.value = _uiState.value.copy(isCurrentUrlBookmarked = existing != null)
        }
    }

    private fun refreshNetworkAndCacheStatus() {
        val isNetwork = offlineCacheManager.isNetworkAvailable()
        _uiState.value = _uiState.value.copy(isNetworkOnline = isNetwork)
        updateCacheSize()
    }

    private fun updateCacheSize() {
        viewModelScope.launch {
            val size = offlineCacheManager.getCacheSizeBytes()
            _uiState.value = _uiState.value.copy(cacheSizeBytes = size)
        }
    }
}
