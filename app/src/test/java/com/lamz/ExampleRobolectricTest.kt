package com.lamz

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamz.data.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MinimalOS", appName)
    }

    @Test
    fun `installed app custom label override test`() {
        val appWithoutCustom = InstalledApp(
            packageName = "com.test.app",
            activityName = "com.test.app.MainActivity",
            label = "Original Name"
        )
        assertEquals("Original Name", appWithoutCustom.displayLabel)

        val appWithCustom = appWithoutCustom.copy(customLabel = "Custom Clean Name")
        assertEquals("Custom Clean Name", appWithCustom.displayLabel)
    }

    @Test
    fun `installed app favorite and hidden state test`() {
        val app = InstalledApp(
            packageName = "com.test.app",
            activityName = "com.test.app.MainActivity",
            label = "Camera",
            isFavorite = true,
            isHidden = false
        )
        assertTrue(app.isFavorite)
        assertFalse(app.isHidden)
    }
}
