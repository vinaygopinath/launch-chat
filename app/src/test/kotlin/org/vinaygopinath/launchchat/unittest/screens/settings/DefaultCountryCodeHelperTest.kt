package org.vinaygopinath.launchchat.unittest.screens.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.vinaygopinath.launchchat.screens.settings.DefaultCountryCodeHelper.isValidCountryCode

class DefaultCountryCodeHelperTest {

    @Test
    fun `returns false when input is null`() {
        assertThat(isValidCountryCode(null)).isFalse()
    }

    @Test
    fun `returns true when input is a country code with no spaces`() {
        assertThat(isValidCountryCode("+238")).isTrue()
    }

    @Test
    fun `returns true when input is country code-like, but does not begin with a +`() {
        assertThat(isValidCountryCode("254")).isTrue()
    }

    @Test
    fun `returns false when input is a country code but contains a leading space`() {
        assertThat(isValidCountryCode(" +970")).isFalse()
    }

    @Test
    fun `returns false when input is a country code but contains a trailing space`() {
        assertThat(isValidCountryCode("+970 ")).isFalse()
    }

    @Test
    fun `returns false when input is not a country code`() {
        assertThat(isValidCountryCode("Hi!")).isFalse()
    }
}
