package app.bear.android.gms

import android.accounts.AccountManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that the auth-related services and account infrastructure
 * are correctly registered and queryable.
 */
@RunWith(AndroidJUnit4::class)
class AuthServiceTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun accountAuthenticatorService_isRegistered() {
        val pm = context.packageManager
        val intent = Intent("android.accounts.AccountAuthenticator").apply {
            setPackage("app.bear.android.gms")
        }
        val services = pm.queryIntentServices(intent, 0)
        assertTrue(
            "GoogleLoginService (authenticator) must be registered",
            services.isNotEmpty()
        )
    }

    @Test
    fun accountManager_returnsAuthenticatorDescriptor() {
        val am = AccountManager.get(context)
        val authenticators = am.authenticatorTypes
        val bearAuth = authenticators.firstOrNull { it.type.startsWith("app.bear") }
        assertNotNull("Bear MicroG authenticator must be discoverable via AccountManager", bearAuth)
    }

    @Test
    fun loginActivity_isResolvable() {
        val pm = context.packageManager
        val component = ComponentName(
            "app.bear.android.gms",
            "org.microg.gms.auth.login.LoginActivity"
        )
        val ai = pm.getActivityInfo(component, 0)
        assertNotNull("LoginActivity must be registered in the manifest", ai)
    }

    @Test
    fun syncAdapter_isRegistered() {
        val pm = context.packageManager
        val intent = Intent("android.content.SyncAdapter").apply {
            setPackage("app.bear.android.gms")
        }
        val services = pm.queryIntentServices(intent, 0)
        assertTrue(
            "ContactSyncService (sync adapter) must be registered",
            services.isNotEmpty()
        )
    }
}
