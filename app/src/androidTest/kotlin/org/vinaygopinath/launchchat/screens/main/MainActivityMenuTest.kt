package org.vinaygopinath.launchchat.screens.main

import android.content.Intent
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.vinaygopinath.launchchat.Constants
import org.vinaygopinath.launchchat.helpers.AssertionHelper.assertIntentNavigation

@HiltAndroidTest
class MainActivityMenuTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
        launch(MainActivity::class.java)
    }

    @Test
    fun launchesGithubPageOnAboutClick() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())

        assertIntentNavigation(Intent.ACTION_VIEW, Constants.GITHUB_REPO_URL) {
            onView(withText("About")).perform(click())
        }
    }
}