package org.vinaygopinath.launchchat.screens.main

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
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
class MainActivityOpenPhoneCallIntentTest {

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
        ensurePhoneCallIsEnabled()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    private fun ensurePhoneCallIsEnabled() {
        runBlocking {
            if (chatAppDao.getCount() == 0) {
                chatAppDao.create(ChatApp.getPredefinedChatApps(dateUtils))
            }
            val phoneCallApp = chatAppDao.getAllChatApps().first()
                .find { it.name == ChatApp.PREDEFINED_CHAT_APP_PHONE_CALL_NAME }
            phoneCallApp?.let {
                chatAppDao.updateEnabled(it.id, true)
            }
            // Give the database time to propagate the change
            Thread.sleep(100)
        }
    }

    @Test
    fun launchesPhoneCallWithTheEnteredNumber() {
        val phoneNumber = "+1555555555"
        onView(withId(R.id.phone_number_input)).perform(replaceText(phoneNumber))

        assertIntentNavigation(Intent.ACTION_DIAL, "tel:$phoneNumber") {
            // Scroll to and click the Phone Call button in the horizontal RecyclerView
            onView(withId(R.id.chat_app_button_list)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    withText(ChatApp.PREDEFINED_CHAT_APP_PHONE_CALL_NAME),
                    click()
                )
            )
        }
    }
}
