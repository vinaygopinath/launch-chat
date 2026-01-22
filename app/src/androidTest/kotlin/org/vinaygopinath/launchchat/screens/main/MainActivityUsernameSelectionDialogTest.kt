package org.vinaygopinath.launchchat.screens.main

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
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
class MainActivityUsernameSelectionDialogTest {

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
    fun showsTheUsernameSelectionDialogTitleWhenMultipleUsernamesAreEntered() {
        val usernames = "ricardo292 rodrigo"
        onView(withId(R.id.phone_number_input)).perform(replaceText(usernames))

        onView(withId(R.id.chat_app_button_list)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText(ChatApp.PREDEFINED_CHAT_APP_TELEGRAM_NAME),
                click()
            )
        )

        onView(withText(R.string.username_selection_dialog_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun showsUsernamesInTheUsernameSelectionDialogWhenMultipleUsernamesAreEntered() {
        val firstUsername = "ricardo292"
        val secondUsername = "rodrigo"
        val usernames = "$firstUsername $secondUsername"
        onView(withId(R.id.phone_number_input)).perform(replaceText(usernames))

        onView(withId(R.id.chat_app_button_list)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText(ChatApp.PREDEFINED_CHAT_APP_TELEGRAM_NAME),
                click()
            )
        )

        onView(withText(firstUsername)).check(matches(withEffectiveVisibility(VISIBLE)))
        onView(withText(secondUsername)).check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun launchesTelegramWhenAUsernameInTheUsernameSelectionDialogIsSelected() {
        val firstUsername = "ricardo292"
        val secondUsername = "rodrigo"
        val usernames = "$firstUsername $secondUsername"
        onView(withId(R.id.phone_number_input)).perform(replaceText(usernames))

        onView(withId(R.id.chat_app_button_list)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText(ChatApp.PREDEFINED_CHAT_APP_TELEGRAM_NAME),
                click()
            )
        )

        val expectedUrl = "https://t.me/$firstUsername"
        assertIntentNavigation(Intent.ACTION_VIEW, expectedUrl) {
            onView(withText(firstUsername)).perform(click())
        }
    }

    @Test
    fun launchesSignalWhenAUsernameInTheUsernameSelectionDialogIsSelected() {
        val firstUsername = "ricardo292"
        val secondUsername = "rodrigo"
        val usernames = "$firstUsername $secondUsername"
        onView(withId(R.id.phone_number_input)).perform(replaceText(usernames))

        onView(withId(R.id.chat_app_button_list)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText(ChatApp.PREDEFINED_CHAT_APP_SIGNAL_NAME),
                click()
            )
        )

        val expectedUrl = "https://signal.me/#eu/$firstUsername"
        assertIntentNavigation(Intent.ACTION_VIEW, expectedUrl) {
            onView(withText(firstUsername)).perform(click())
        }
    }

    @Test
    fun showsUsernameSelectionDialogWhenInputContainsUsernamesOnSeparateLines() {
        val firstUsername = "ricardo292"
        val secondUsername = "rodrigo"
        val multilineInput = "$firstUsername\n$secondUsername"
        onView(withId(R.id.phone_number_input)).perform(replaceText(multilineInput))

        onView(withId(R.id.chat_app_button_list)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                withText(ChatApp.PREDEFINED_CHAT_APP_TELEGRAM_NAME),
                click()
            )
        )

        onView(withText(R.string.username_selection_dialog_title))
            .check(matches(isDisplayed()))
        onView(withText(firstUsername)).check(matches(withEffectiveVisibility(VISIBLE)))
        onView(withText(secondUsername)).check(matches(withEffectiveVisibility(VISIBLE)))
    }
}
