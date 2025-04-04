package org.vinaygopinath.launchchat.screens.main.domain

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.vinaygopinath.launchchat.models.Settings.Companion.KEY_DEFAULT_COUNTRY_CODE
import org.vinaygopinath.launchchat.models.Settings.Companion.KEY_MISSING_COUNTRY_CODE_ACTION
import org.vinaygopinath.launchchat.models.Settings.Companion.KEY_RECENT_COUNTRY_CODE
import org.vinaygopinath.launchchat.models.Settings.Companion.VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_DEFAULT
import org.vinaygopinath.launchchat.models.Settings.Companion.VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_RECENT
import org.vinaygopinath.launchchat.utils.PreferenceUtil
import javax.inject.Inject


@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
class PrefixCountryCodeUseCaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var useCase: PrefixCountryCodeUseCase

    @Inject
    lateinit var preferenceUtil: PreferenceUtil

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `updates the previous country code if the given phone number is in international format`() {
        preferenceUtil.setInt(KEY_RECENT_COUNTRY_CODE, 90)

        useCase.execute("+919876543210")

        assertThat(preferenceUtil.getInt(KEY_RECENT_COUNTRY_CODE)).isEqualTo(91)
    }

    @Test
    fun `defaults to prefixing the most recent country code when missing country code action is not set`() {
        // This isn't needed - just demonstrating that the missing country code action isn't set.
        preferenceUtil.clear(KEY_MISSING_COUNTRY_CODE_ACTION)
        preferenceUtil.setInt(KEY_RECENT_COUNTRY_CODE, 52)

        assertThat(useCase.execute("754333222")).isEqualTo("+52754333222")
    }

    @Test
    fun `uses the most recent country code when missing country code action is 'use the most recent'`() {
        preferenceUtil.setString(
            KEY_MISSING_COUNTRY_CODE_ACTION,
            VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_RECENT
        )
        preferenceUtil.setInt(KEY_RECENT_COUNTRY_CODE, 960)

        assertThat(useCase.execute("754333222")).isEqualTo("+960754333222")
    }

    @Test
    fun `uses the default country code when missing country code action is 'use the default'`() {
        preferenceUtil.setString(
            KEY_MISSING_COUNTRY_CODE_ACTION,
            VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_DEFAULT
        )
        preferenceUtil.setInt(KEY_RECENT_COUNTRY_CODE, 960)
        preferenceUtil.setInt(KEY_DEFAULT_COUNTRY_CODE, 254)

        assertThat(useCase.execute("754333222")).isEqualTo("+254754333222")
    }

    @Test
    fun `returns the given number if action is recent country code but it is not set`() {
        preferenceUtil.setString(
            KEY_MISSING_COUNTRY_CODE_ACTION,
            VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_RECENT
        )
        // Recent country code not set

        assertThat(useCase.execute("754333222")).isEqualTo("754333222")
    }

    @Test
    fun `returns the given number if action is default country code but it is not set`() {
        preferenceUtil.setString(
            KEY_MISSING_COUNTRY_CODE_ACTION,
            VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_DEFAULT
        )
        // Default country code not set

        assertThat(useCase.execute("754333222")).isEqualTo("754333222")
    }

    @Test
    fun `returns the given number if it is already in international format`() {
        preferenceUtil.setString(
            KEY_MISSING_COUNTRY_CODE_ACTION,
            VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_DEFAULT
        )
        preferenceUtil.setInt(KEY_RECENT_COUNTRY_CODE, 91)
        preferenceUtil.setInt(KEY_DEFAULT_COUNTRY_CODE, 94)

        assertThat(useCase.execute("+254777444555")).isEqualTo("+254777444555")
    }
}