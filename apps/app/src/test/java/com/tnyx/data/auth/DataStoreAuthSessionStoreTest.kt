package com.tnyx.data.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.tnyx.shared.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataStoreAuthSessionStoreTest {

    @Test
    fun sessionSurvivesStoreRecreation() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firstStore = DataStoreAuthSessionStore(context)
        firstStore.clearSession()
        val expected = session()

        firstStore.setSession(expected)
        val recreatedStore = DataStoreAuthSessionStore(context)

        assertEquals(expected, recreatedStore.observeSession().first())
        assertEquals(expected, recreatedStore.currentSession())
        recreatedStore.clearSession()
    }

    @Test
    fun clearSessionPersistsSignedOutState() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val firstStore = DataStoreAuthSessionStore(context)
        firstStore.clearSession()
        firstStore.setSession(session())

        firstStore.clearSession()
        val recreatedStore = DataStoreAuthSessionStore(context)

        assertNull(recreatedStore.observeSession().first())
        assertNull(recreatedStore.currentSession())
    }

    private fun session(): AuthSession {
        return AuthSession(
            userId = "persistent-user",
            email = "persistent@example.com",
            displayName = "Persistent User",
            isDemo = false,
        )
    }
}
