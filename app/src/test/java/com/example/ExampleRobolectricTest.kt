package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.SslSecurityManager
import com.example.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("From Europe To You", appName)
  }

  @Test
  fun `verify ssl domain constants`() {
    assertEquals("www.fromeuropetoyou.com", SslSecurityManager.TARGET_HOSTNAME)
    assertEquals("fromeuropetoyou.com", SslSecurityManager.TARGET_APEX)
    assertTrue(SslSecurityManager.TRUSTED_PINS.isNotEmpty())
  }

  @Test
  fun `verify theme mode enum values`() {
    val modes = ThemeMode.values()
    assertTrue(modes.contains(ThemeMode.SYSTEM))
    assertTrue(modes.contains(ThemeMode.DARK))
    assertTrue(modes.contains(ThemeMode.LIGHT))
  }
}
