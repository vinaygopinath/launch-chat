package org.vinaygopinath.launchchat.unittest.helpers

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.vinaygopinath.launchchat.helpers.UsernameHelper

@RunWith(JUnit4::class)
class UsernameHelperExtractUsernamesTest {

    private val helper = UsernameHelper()

    @Test
    fun `returns all username-like words`() {
        val usernames = helper.extractUsernames("this contains multiple usernames22")

        assertThat(usernames).containsExactly("this", "contains", "multiple", "usernames22")
    }
}