package org.vinaygopinath.launchchat.screens.main

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
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
class MainActivityPhoneNumberSelectionDialogTest {

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
    fun showsThePhoneNumberSelectionDialogTitleAndMessageWhenMultiplePhoneNumbersAreEntered() {
        val phoneNumbers = "+1555555555 +2663388373"
        onView(withId(R.id.phone_number_input)).perform(replaceText(phoneNumbers))

        onView(withText(ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME)).perform(click())

        onView(withText(R.string.phone_number_selection_dialog_title))
            .check(matches(isDisplayed()))

        onView(withText(R.string.phone_number_selection_dialog_message))
            .check(matches(isDisplayed()))
    }

    @Test
    fun showsPhoneNumbersInThePhoneNumberSelectionDialogWhenMultiplePhoneNumbersAreEntered() {
        val firstPhoneNumber = "+1555555555"
        val secondPhoneNumber = "+2663388373"
        val phoneNumbers = "$firstPhoneNumber $secondPhoneNumber"
        onView(withId(R.id.phone_number_input)).perform(replaceText(phoneNumbers))

        onView(withText(ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME)).perform(click())

        onView(withText(firstPhoneNumber)).check(matches(withEffectiveVisibility(VISIBLE)))
        onView(withText(secondPhoneNumber)).check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun launchesWhatsAppWhenAPhoneNumberInThePhoneNumberSelectionDialogIsSelected() {
        val firstPhoneNumber = "+1555555555"
        val secondPhoneNumber = "+2663388373"
        val phoneNumbers = "$firstPhoneNumber $secondPhoneNumber"
        onView(withId(R.id.phone_number_input)).perform(replaceText(phoneNumbers))

        onView(withText(ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME)).perform(click())

        val expectedUrl = "whatsapp://send/?phone=$firstPhoneNumber&text="
        assertIntentNavigation(Intent.ACTION_VIEW, expectedUrl) {
            onView(withText(firstPhoneNumber)).perform(click())
        }
    }

    @Test
    fun showsPhoneNumberSelectionDialogWhenInputContainsPhoneNumbersMixedWithText() {
        val firstPhoneNumber = "+1555555555"
        val secondPhoneNumber = "+2663388373"
        val mixedInput = "Call me at $firstPhoneNumber or $secondPhoneNumber for more info"
        onView(withId(R.id.phone_number_input)).perform(replaceText(mixedInput))

        onView(withText(ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME)).perform(click())

        onView(withText(R.string.phone_number_selection_dialog_title))
            .check(matches(isDisplayed()))
        onView(withText(firstPhoneNumber)).check(matches(withEffectiveVisibility(VISIBLE)))
        onView(withText(secondPhoneNumber)).check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun showsPhoneNumberSelectionDialogWhenInputContainsPhoneNumbersOnSeparateLines() {
        val firstPhoneNumber = "+1555555555"
        val secondPhoneNumber = "+2663388373"
        val multilineInput = "$firstPhoneNumber\n$secondPhoneNumber"
        onView(withId(R.id.phone_number_input)).perform(replaceText(multilineInput))

        onView(withText(ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME)).perform(click())

        onView(withText(R.string.phone_number_selection_dialog_title))
            .check(matches(isDisplayed()))
        onView(withText(firstPhoneNumber)).check(matches(withEffectiveVisibility(VISIBLE)))
        onView(withText(secondPhoneNumber)).check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun showsPhoneNumberSelectionDialogWhenInputContainsMixOfPhoneNumbersAndUsernames() {
        val firstPhoneNumber = "+1555555555"
        val secondPhoneNumber = "+2663388373"
        val mixedInput = "$firstPhoneNumber ricardo292 $secondPhoneNumber rodrigo"
        onView(withId(R.id.phone_number_input)).perform(replaceText(mixedInput))

        onView(withText(ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME)).perform(click())

        onView(withText(R.string.phone_number_selection_dialog_title))
            .check(matches(isDisplayed()))
        onView(withText(firstPhoneNumber)).check(matches(withEffectiveVisibility(VISIBLE)))
        onView(withText(secondPhoneNumber)).check(matches(withEffectiveVisibility(VISIBLE)))
    }

    @Test
    fun launchesWhatsAppDirectlyWhenInputContainsSinglePhoneNumberMixedWithUsernames() {
        val phoneNumber = "+1555555555"
        val mixedInput = "$phoneNumber ricardo292 rodrigo"
        onView(withId(R.id.phone_number_input)).perform(replaceText(mixedInput))

        val expectedUrl = "whatsapp://send/?phone=$phoneNumber&text="
        assertIntentNavigation(Intent.ACTION_VIEW, expectedUrl) {
            onView(withText(ChatApp.PREDEFINED_CHAT_APP_WHATSAPP_NAME)).perform(click())
        }
    }
}
