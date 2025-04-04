package org.vinaygopinath.launchchat.helpers

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class PhoneNumberHelperBuildInternationalPhoneNumberStringTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var phoneNumberHelper: PhoneNumberHelper

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `returns a phone number string in international format given a local phone number with a leading zero and country code`() {
        assertThat(
            phoneNumberHelper.buildInternationalPhoneNumberString("0744444444", 254)
        ).isEqualTo("+254744444444")
    }

    @Test
    fun `returns a phone number string in international format given a local phone number with no leading zero and country code`() {
        assertThat(
            phoneNumberHelper.buildInternationalPhoneNumberString("744444444", 254)
        ).isEqualTo("+254744444444")
    }

    @Test
    fun `returns a phone number string in international format given a phone number already in international format and country code`() {
        assertThat(
            phoneNumberHelper.buildInternationalPhoneNumberString("+254744444444", 254)
        ).isEqualTo("+254744444444")
    }

    @Test
    fun `returns the input phone number string given a phone number and an invalid country code`() {
        assertThat(
            phoneNumberHelper.buildInternationalPhoneNumberString("0254744444444", 111)
        ).isEqualTo("0254744444444")
    }

    @Test
    fun `returns a best-attempt international phone number given an invalid phone number and a valid country code`() {
        assertThat(
            phoneNumberHelper.buildInternationalPhoneNumberString("739********329", 91)
        ).isEqualTo("+91739329") // The input phone number minus the special characters
    }
}