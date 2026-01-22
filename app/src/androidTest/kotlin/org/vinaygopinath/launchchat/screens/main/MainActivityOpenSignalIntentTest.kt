package org.vinaygopinath.launchchat.screens.main

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.daos.ChatAppDao
import org.vinaygopinath.launchchat.helpers.AssertionHelper.assertIntentNavigation
import org.vinaygopinath.launchchat.models.ChatApp
import org.vinaygopinath.launchchat.utils.DateUtils
import javax.inject.Inject

@HiltAndroidTest
class MainActivityOpenSignalIntentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var chatAppDao: ChatAppDao

    @Inject
    lateinit var dateUtils: DateUtils

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        ensureChatAppsExist()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    private fun ensureChatAppsExist() {
        runBlocking {
            if (chatAppDao.getCount() == 0) {
                chatAppDao.create(ChatApp.getPredefinedChatApps(dateUtils))
            }
        }
    }

    @Test
    fun launchesSignalChatWithTheEnteredNumber() {
        val phoneNumber = "+1555555555"
        onView(withId(R.id.phone_number_input)).perform(replaceText(phoneNumber))
        val expectedUrl = "https://signal.me/#p/$phoneNumber"

        assertIntentNavigation(Intent.ACTION_VIEW, expectedUrl) {
            onView(withText(ChatApp.PREDEFINED_CHAT_APP_SIGNAL_NAME)).perform(click())
        }
    }

    @Test
    fun launchesSignalChatWithTheEnteredUsername() {
        val username = "testuser"
        onView(withId(R.id.phone_number_input)).perform(replaceText(username))
        val expectedUrl = "https://signal.me/#eu/$username"

        assertIntentNavigation(Intent.ACTION_VIEW, expectedUrl) {
            onView(withText(ChatApp.PREDEFINED_CHAT_APP_SIGNAL_NAME)).perform(click())
        }
    }
}
