package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.theme.ThemeMode
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WebViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: WebViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsState()
      val systemDark = isSystemInDarkTheme()

      val isDarkTheme = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
      }

      // Handle Android back button navigation in WebView
      BackHandler(enabled = uiState.canGoBack) {
        viewModel.goBack()
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        MainScreen(viewModel = viewModel)
      }
    }
  }
}

