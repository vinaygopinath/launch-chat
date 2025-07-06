package org.vinaygopinath.launchchat.unittest.helpers

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.vinaygopinath.launchchat.helpers.TextHelper

class TextHelperPasswordRegexTest {

    @Test
    fun `returns true when text is phone number with no formatting and no country code`() {
        val inputText = "111222333"

        assertThat(TextHelper.doesTextMatchPhoneNumberRegex(inputText)).isTrue()
    }

    @Test
    fun `returns true when text is phone number with no formatting and country code`() {
        val inputText = "+1111222333"

        assertThat(TextHelper.doesTextMatchPhoneNumberRegex(inputText)).isTrue()
    }

    @Test
    fun `returns true when text is phone number with space formatting`() {
        val inputText = "111 222 333"

        assertThat(TextHelper.doesTextMatchPhoneNumberRegex(inputText)).isTrue()
    }

    @Test
    fun `returns true when text is phone number with parentheses and space formatting`() {
        val inputText = "(111) 222 333"

        assertThat(TextHelper.doesTextMatchPhoneNumberRegex(inputText)).isTrue()
    }

    @Test
    fun `returns true when text is phone number with hyphen formatting`() {
        val inputText = "111-222-333"

        assertThat(TextHelper.doesTextMatchPhoneNumberRegex(inputText)).isTrue()
    }

    @Test
    fun `returns false when text includes non-numerical and non-formatting characters`() {
        val inputText = "Please contact us on this phone number: +1(111)-222 333"

        assertThat(TextHelper.doesTextMatchPhoneNumberRegex(inputText)).isFalse()
    }
}
