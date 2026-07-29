package com.tnyx.features.auth.data.repository

import com.tnyx.features.auth.data.session.InMemoryAuthSessionStore
import com.tnyx.features.auth.domain.model.AuthResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAuthRepositoryTest {
    private val sessionStore = InMemoryAuthSessionStore()
    private val repository = FakeAuthRepository(sessionStore)

    @Test
    fun signIn_withValidCredentials_returnsAuthenticated() = runTest {
        val email = "test@example.com"
        val result = repository.signIn(email, "password123")
        assertTrue(result is AuthResult.Authenticated)
        val session = (result as AuthResult.Authenticated).session
        assertEquals(email, session.email)
        assertEquals("Test", session.displayName)
        assertEquals(false, session.isDemo)
        assertEquals(session, sessionStore.currentSession())
    }

    @Test
    fun signIn_normalizesEmailToStableUserIdentity() = runTest {
        val first = repository.signIn(" Test@Example.com ", "password123")
            as AuthResult.Authenticated
        repository.signOut()
        val second = repository.signIn("test@example.com", "password123")
            as AuthResult.Authenticated

        assertEquals("test@example.com", first.session.email)
        assertEquals(first.session.userId, second.session.userId)
    }

    @Test
    fun signIn_withShortPassword_returnsFailure() = runTest {
        val result = repository.signIn("test@example.com", "12345")
        assertTrue(result is AuthResult.Failure)
        assertEquals("Password must be at least 6 characters", (result as AuthResult.Failure).message)
    }

    @Test
    fun signInWithDemoAccount_returnsAuthenticatedDemoUser() = runTest {
        val result = repository.signInWithDemoAccount()
        assertTrue(result is AuthResult.Authenticated)
        val session = (result as AuthResult.Authenticated).session
        assertEquals("demo@tnyx.app", session.email)
        assertEquals("demo-user", session.userId)
        assertEquals("Demo User", session.displayName)
        assertEquals(true, session.isDemo)
    }

    @Test
    fun signUp_withValidDetails_returnsVerificationRequired() = runTest {
        val email = "newuser@example.com"
        val result = repository.signUp("New User", email, "securepass")
        assertTrue(result is AuthResult.VerificationRequired)
        assertEquals(email, (result as AuthResult.VerificationRequired).email)
        assertNull(sessionStore.currentSession())
    }

    @Test
    fun signUp_withBlankName_returnsFailure() = runTest {
        val result = repository.signUp("  ", "newuser@example.com", "securepass")
        assertTrue(result is AuthResult.Failure)
        assertEquals("Name is required", (result as AuthResult.Failure).message)
    }

    @Test
    fun signUp_withShortPassword_returnsFailure() = runTest {
        val result = repository.signUp("New User", "newuser@example.com", "12345")
        assertTrue(result is AuthResult.Failure)
        assertEquals("Password must be at least 6 characters", (result as AuthResult.Failure).message)
    }

    @Test
    fun verifyOtp_withValidSixDigitCode_returnsAuthenticated() = runTest {
        val email = "verify@example.com"
        val result = repository.verifyOtp(email, "123456")
        assertTrue(result is AuthResult.Authenticated)
        val session = (result as AuthResult.Authenticated).session
        assertEquals(email, session.email)
        assertEquals("Verify", session.displayName)
        assertEquals(false, session.isDemo)
        assertEquals(session, sessionStore.currentSession())
    }

    @Test
    fun verifyOtp_afterSignUp_preservesSubmittedDisplayName() = runTest {
        repository.signUp("Santosh Kumar", "SANTOSH@example.com", "securepass")

        val result = repository.verifyOtp("santosh@example.com", "123456")
            as AuthResult.Authenticated

        assertEquals("santosh@example.com", result.session.email)
        assertEquals("Santosh Kumar", result.session.displayName)
    }

    @Test
    fun verifyOtp_withInvalidCodeLength_returnsFailure() = runTest {
        val result = repository.verifyOtp("verify@example.com", "12345")
        assertTrue(result is AuthResult.Failure)
        assertEquals("Enter the 6-digit code", (result as AuthResult.Failure).message)
    }

    @Test
    fun resendOtp_returnsVerificationRequired() = runTest {
        val email = "resend@example.com"
        val result = repository.resendOtp(email)
        assertTrue(result is AuthResult.VerificationRequired)
        assertEquals(email, (result as AuthResult.VerificationRequired).email)
    }

    @Test
    fun signOut_clearsActiveSession() = runTest {
        repository.signInWithDemoAccount()

        repository.signOut()

        assertNull(sessionStore.currentSession())
    }
}
