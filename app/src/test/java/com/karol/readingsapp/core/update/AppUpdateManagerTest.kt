package com.karol.readingsapp.core.update

import android.content.Context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class AppUpdateManagerTest {

    private lateinit var appUpdateManager: AppUpdateManager
    private val context = mock(Context::class.java)

    @Before
    fun setUp() {
        appUpdateManager = AppUpdateManager(context)
    }

    @Test
    fun `isNewerVersion returns true for higher major version`() {
        assertTrue(appUpdateManager.isNewerVersion("v2.0.0", "1.9.9"))
    }

    @Test
    fun `isNewerVersion returns true for higher minor version`() {
        assertTrue(appUpdateManager.isNewerVersion("1.1.0", "1.0.5"))
    }

    @Test
    fun `isNewerVersion returns true for higher patch version`() {
        assertTrue(appUpdateManager.isNewerVersion("1.0.1", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns false for same version`() {
        assertFalse(appUpdateManager.isNewerVersion("1.0.0", "1.0.0"))
    }

    @Test
    fun `isNewerVersion returns false for lower version`() {
        assertFalse(appUpdateManager.isNewerVersion("1.0.0", "1.1.0"))
    }

    @Test
    fun `isNewerVersion handles v prefix`() {
        assertTrue(appUpdateManager.isNewerVersion("v1.1.0", "v1.0.0"))
    }

    @Test
    fun `isNewerVersion handles different part counts`() {
        assertTrue(appUpdateManager.isNewerVersion("1.1", "1.0.9"))
        assertTrue(appUpdateManager.isNewerVersion("1.1.1", "1.1"))
        assertFalse(appUpdateManager.isNewerVersion("1.1", "1.1.1"))
    }
}
