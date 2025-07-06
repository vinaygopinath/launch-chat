package org.vinaygopinath.launchchat.unittest.helpers

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.vinaygopinath.launchchat.helpers.PhoneNumberHelper
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class PhoneNumberHelperExtractCountryCodeFromInternationalPhoneNumberTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var phoneNumberHelper: PhoneNumberHelper

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun extractsCountryCodeFromInternationalPhoneNumber() {
        val phoneNumberCountryCodeMap = mapOf(
            "+254744444444" to 254,
            "+256744555666" to 256,
            "+971556644553" to 971,
            "+940555666444" to 94
        )

        phoneNumberCountryCodeMap.forEach { (phoneNumber, countryCode) ->
            assertThat(
                phoneNumberHelper.extractCountryCodeFromInternationalPhoneNumber(phoneNumber)
            ).isEqualTo(countryCode)
        }
    }

    @Test
    fun returnsNullWhenPhoneNumberDoesNotContainACountryCode() {
        assertThat(
            phoneNumberHelper.extractCountryCodeFromInternationalPhoneNumber("254744444444")
        ).isEqualTo(null)
    }

    @Test
    fun returnsNullWhenInputIsNotAPhoneNumber() {
        assertThat(
            phoneNumberHelper.extractCountryCodeFromInternationalPhoneNumber("SomeUsername")
        ).isEqualTo(null)
    }
}
