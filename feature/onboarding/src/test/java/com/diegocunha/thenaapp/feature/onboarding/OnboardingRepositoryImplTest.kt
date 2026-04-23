package com.diegocunha.thenaapp.feature.onboarding

import com.diegocunha.thenaapp.datasource.storage.sharedpreferences.CustomSharedPreferences
import com.diegocunha.thenaapp.feature.onboarding.data.OnboardingRepositoryImpl
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OnboardingRepositoryImplTest {

    private val customSharedPreferences = mockk<CustomSharedPreferences>()

    private lateinit var repository: OnboardingRepositoryImpl

    @Before
    fun setUp() {
        repository = OnboardingRepositoryImpl(customSharedPreferences)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `WHEN hasSeenOnboarding and prefs has no value THEN returns false`() {
        every { customSharedPreferences.getBoolean(any(), false) } returns false

        assertFalse(repository.hasSeenOnboarding())
    }

    @Test
    fun `WHEN hasSeenOnboarding after markOnboardingSeen THEN returns true`() {
        every { customSharedPreferences.getBoolean(any(), false) } returns true

        assertTrue(repository.hasSeenOnboarding())
    }

    @Test
    fun `WHEN markOnboardingSeen THEN persists true to shared preferences`() {
        justRun { customSharedPreferences.putBoolean(any(), any()) }
        repository.markOnboardingSeen()

        verify { customSharedPreferences.putBoolean(any(), true) }
    }
}
