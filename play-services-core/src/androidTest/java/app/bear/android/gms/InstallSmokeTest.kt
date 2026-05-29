package app.bear.android.gms

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests that verify the APK is correctly installed and the core
 * components are accessible. Run on a real device or emulator:
 *   ./gradlew connectedDefaultDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class InstallSmokeTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun packageName_isCorrect() {
        assertEquals("app.bear.android.gms", context.packageName)
    }

    @Test
    fun applicationInfo_isNotNull() {
        val ai = context.packageManager.getApplicationInfo(context.packageName, 0)
        assertNotNull(ai)
    }

    @Test
    fun authContentProvider_isAccessible() {
        val uri = android.net.Uri.parse("content://app.bear.android.gms/accounts")
        val pm = context.packageManager
        val providers = pm.queryContentProviders(null, 0, PackageManager.GET_META_DATA)
        val found = providers.any { it.providerInfo.authority.contains("app.bear.android.gms") }
        assertTrue("At least one Bear MicroG content provider must be registered", found)
    }

    @Test
    fun gmsAccountType_isRegistered() {
        val am = android.accounts.AccountManager.get(context)
        val types = am.authenticatorTypes.map { it.type }
        assertTrue(
            "Account type 'app.bear' must be registered by the authenticator",
            types.any { it.startsWith("app.bear") }
        )
    }
}
