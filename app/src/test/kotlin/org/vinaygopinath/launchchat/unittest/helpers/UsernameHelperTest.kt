package org.vinaygopinath.launchchat.unittest.helpers

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.vinaygopinath.launchchat.helpers.UsernameHelper

@RunWith(JUnit4::class)
class UsernameHelperTest {

    private val helper = UsernameHelper()

    @Test
    fun `containsUsernames returns true when the input contains one username`() {
        assertThat(helper.containsUsernames("johndoe")).isTrue()
    }

    @Test
    fun `containsUsernames returns true when the input contains multiple usernames`() {
        assertThat(helper.containsUsernames("contact johndoe or jane_doe")).isTrue()
    }

    @Test
    fun `containsUsernames returns false when the input is empty`() {
        assertThat(helper.containsUsernames("")).isFalse()
    }

    @Test
    fun `containsUsernames returns false when the input is too short`() {
        assertThat(helper.containsUsernames("ab")).isFalse()
    }

    @Test
    fun `containsUsernames returns true for usernames with underscores and dots`() {
        assertThat(helper.containsUsernames("user.name_123")).isTrue()
    }
}
