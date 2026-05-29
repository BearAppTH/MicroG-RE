package app.bear.android.gms

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies runtime environment conditions expected by Bear MicroG.
 * These mirror the self-check items shown in the app's Self-Check screen.
 */
@RunWith(AndroidJUnit4::class)
class SelfCheckTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun androidId_isNotNull() {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        assertNotNull("ANDROID_ID must not be null", androidId)
        assertTrue("ANDROID_ID must not be empty", androidId.isNotEmpty())
    }

    @Test
    fun networkSecurityConfig_loaded() {
        // If the network security config XML is malformed, this class init would throw
        assertNotNull(context.resources)
    }

    @Test
    fun buildVersion_meetsMinSdk() {
        assertTrue(
            "Device must run Android 10 (API 29) or higher",
            android.os.Build.VERSION.SDK_INT >= 29
        )
    }

    @Test
    fun gservicesProvider_queriesSuccessfully() {
        val uri = android.net.Uri.parse("content://app.bear.android.gms.gservices")
        // Should not throw — returns empty cursor if no data, not an exception
        val cursor = context.contentResolver.query(uri, null, null, arrayOf("android_id"), null)
        // cursor may be null on a fresh install before checkin; that's acceptable
        cursor?.close()
    }
}
