package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.theme.ThemeMode
import com.example.ui.components.AccountProfileSheet
import com.example.ui.components.BookmarksSheet
import com.example.ui.components.CategoryQuickPicker
import com.example.ui.components.MitmThreatDialog
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.OfflineCacheSheet
import com.example.ui.components.OfflineErrorView
import com.example.ui.components.PullToRefreshContainer
import com.example.ui.components.SecureWebView
import com.example.ui.components.SslInspectorDialog
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.WebViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WebViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val cachedSnapshots by viewModel.cachedSnapshots.collectAsState()
    val securityLogs by viewModel.securityLogs.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    var themeMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                viewModel.openSheet(ActiveSheet.SSL_INSPECTOR)
                            }
                        ) {
                            Text(
                                text = "From Europe To You",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSslSecure) Icons.Default.Shield else Icons.Default.Warning,
                                    contentDescription = "SSL Status",
                                    tint = if (uiState.isSslSecure) EmeraldSuccess else WarningAmber,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.isSslSecure) "SSL PINNED • HTTPS" else "SSL WARNING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isSslSecure) EmeraldSuccess else WarningAmber
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.openSheet(ActiveSheet.CATEGORY_PICKER) },
                            modifier = Modifier.testTag("category_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Collections",
                                tint = GoldPrimary
                            )
                        }
                    },
                    actions = {
                        // Theme Toggle Menu
                        Box {
                            IconButton(
                                onClick = { themeMenuExpanded = true },
                                modifier = Modifier.testTag("theme_toggle_button")
                            ) {
                                val themeIcon = when (uiState.themeMode) {
                                    ThemeMode.SYSTEM -> Icons.Default.Brightness4
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                }
                                Icon(
                                    imageVector = themeIcon,
                                    contentDescription = "Theme",
                                    tint = GoldPrimary
                                )
                            }
                            DropdownMenu(
                                expanded = themeMenuExpanded,
                                onDismissRequest = { themeMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("System Default") },
                                    onClick = {
                                        viewModel.setThemeMode(ThemeMode.SYSTEM)
                                        themeMenuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Brightness4, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Dark Theme (High Contrast)") },
                                    onClick = {
                                        viewModel.setThemeMode(ThemeMode.DARK)
                                        themeMenuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Light Theme") },
                                    onClick = {
                                        viewModel.setThemeMode(ThemeMode.LIGHT)
                                        themeMenuExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) }
                                )
                            }
                        }

                        // Bookmark Toggle for current page
                        IconButton(
                            onClick = { viewModel.toggleBookmarkCurrentUrl() },
                            modifier = Modifier.testTag("toggle_current_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isCurrentUrlBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (uiState.isCurrentUrlBookmarked) GoldPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Notifications Badge
                        IconButton(
                            onClick = { viewModel.openSheet(ActiveSheet.NOTIFICATIONS) },
                            modifier = Modifier.testTag("notifications_sheet_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = GoldPrimary,
                                            contentColor = Color.Black
                                        ) {
                                            Text(unreadCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }

                        // Account Profile / Vault
                        IconButton(
                            onClick = { viewModel.openSheet(ActiveSheet.ACCOUNT_PROFILE) },
                            modifier = Modifier.testTag("account_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account & Vault",
                                tint = if (uiState.userProfile.isAuthenticated) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Animated Progress Bar during Page Loading
                AnimatedVisibility(
                    visible = uiState.isLoading && uiState.progress < 100,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = { (uiState.progress.coerceIn(0, 100)) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .testTag("web_loading_progress_bar"),
                        color = GoldPrimary,
                        trackColor = GoldPrimary.copy(alpha = 0.2f)
                    )
                }

                // Domain & SSL Status Sub-bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (uiState.isSslSecure) EmeraldSuccess else WarningAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.currentUrl,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (uiState.isOfflineMode) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(WarningAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back
                    IconButton(
                        onClick = { viewModel.goBack() },
                        enabled = uiState.canGoBack,
                        modifier = Modifier.testTag("nav_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = if (uiState.canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    // Forward
                    IconButton(
                        onClick = { viewModel.goForward() },
                        enabled = uiState.canGoForward,
                        modifier = Modifier.testTag("nav_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Forward",
                            tint = if (uiState.canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    // Refresh / Reload
                    IconButton(
                        onClick = { viewModel.reload() },
                        modifier = Modifier.testTag("nav_refresh_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isLoading) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = GoldPrimary
                        )
                    }

                    // Home (From Europe To You)
                    IconButton(
                        onClick = { viewModel.goHome() },
                        modifier = Modifier.testTag("nav_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = GoldPrimary
                        )
                    }

                    // Saved Antiques & Bookmarks
                    IconButton(
                        onClick = { viewModel.openSheet(ActiveSheet.BOOKMARKS) },
                        modifier = Modifier.testTag("nav_bookmarks_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bookmarks",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Offline Cache Management
                    IconButton(
                        onClick = { viewModel.openSheet(ActiveSheet.OFFLINE_CACHE) },
                        modifier = Modifier.testTag("nav_offline_cache_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Offline Cache",
                            tint = if (uiState.isOfflineMode) WarningAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share URL
                    IconButton(
                        onClick = { shareCurrentUrl(context, uiState.currentUrl, uiState.pageTitle) },
                        modifier = Modifier.testTag("nav_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pull-to-refresh wrapping WebView
            PullToRefreshContainer(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.triggerPullToRefresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.hasPageError && !uiState.isOfflineMode) {
                    OfflineErrorView(
                        errorMessage = uiState.lastErrorDescription,
                        onRetry = { viewModel.reload() },
                        onOpenOfflineCache = { viewModel.openSheet(ActiveSheet.OFFLINE_CACHE) },
                        onOpenCategories = { viewModel.openSheet(ActiveSheet.CATEGORY_PICKER) }
                    )
                } else {
                    SecureWebView(
                        viewModel = viewModel,
                        uiState = uiState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Modal Sheets and Dialogs
    when (uiState.activeSheet) {
        ActiveSheet.SSL_INSPECTOR -> {
            SslInspectorDialog(
                sslInfo = uiState.sslInfo,
                securityLogs = securityLogs,
                onDismiss = { viewModel.closeSheet() },
                onReProbeHandshake = { viewModel.probeSslHandshake() }
            )
        }
        ActiveSheet.BOOKMARKS -> {
            BookmarksSheet(
                bookmarks = bookmarks,
                onSelectBookmark = { url -> viewModel.navigateTo(url) },
                onDeleteBookmark = { bookmark -> viewModel.deleteBookmark(bookmark) },
                onAddNewBookmark = { title, url, category, note ->
                    viewModel.addCustomBookmark(title, url, category, note)
                },
                onDismiss = { viewModel.closeSheet() },
                currentUrl = uiState.currentUrl,
                currentPageTitle = uiState.pageTitle
            )
        }
        ActiveSheet.OFFLINE_CACHE -> {
            OfflineCacheSheet(
                cacheSizeBytes = uiState.cacheSizeBytes,
                isOfflineMode = uiState.isOfflineMode,
                isNetworkOnline = uiState.isNetworkOnline,
                cachedSnapshots = cachedSnapshots,
                onToggleOfflineMode = { viewModel.toggleOfflineMode() },
                onClearCache = { viewModel.clearCache() },
                onSaveSnapshot = { viewModel.saveCurrentPageSnapshot() },
                onSelectSnapshot = { url -> viewModel.navigateTo(url) },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.NOTIFICATIONS -> {
            NotificationsSheet(
                notifications = notifications,
                onMarkAllAsRead = { viewModel.markNotificationsAsRead() },
                onSendTestNotification = {
                    viewModel.createTestNotification(
                        title = "✨ French Antique Fireplace Mantel Added",
                        message = "A magnificent 18th-century hand-carved Carrara marble fireplace mantel from Provence is now available.",
                        category = "Arrivals"
                    )
                },
                onSelectNotification = { url -> viewModel.navigateTo(url) },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.ACCOUNT_PROFILE -> {
            AccountProfileSheet(
                profile = uiState.userProfile,
                onSignInOAuth = { viewModel.authenticateWithGoogleOAuth() },
                onSignOutOAuth = { viewModel.signOutOAuth() },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        ActiveSheet.CATEGORY_PICKER -> {
            CategoryQuickPicker(
                onSelectCategory = { category -> viewModel.navigateTo(category.urlPath) },
                onDismiss = { viewModel.closeSheet() }
            )
        }
        null, ActiveSheet.ADD_BOOKMARK -> {}
    }

    // MITM Threat Alert Dialog
    if (uiState.isMitmAlertOpen) {
        MitmThreatDialog(
            threatMessage = uiState.mitmThreatMessage,
            onDismiss = { viewModel.dismissMitmAlert() },
            onViewSecurityLog = {
                viewModel.dismissMitmAlert()
                viewModel.openSheet(ActiveSheet.SSL_INSPECTOR)
            }
        )
    }
}

private fun shareCurrentUrl(context: Context, url: String, title: String) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Explore this antique on From Europe To You: $title\n$url")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Antique")
        context.startActivity(shareIntent)
    } catch (_: Exception) {}
}
