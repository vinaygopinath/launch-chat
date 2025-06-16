package org.vinaygopinath.launchchat.screens.history

import androidx.test.runner.AndroidJUnit4
import org.vinaygopinath.launchchat.screens.main.MainActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.Test
import org.junit.runner.RunWith
import org.vinaygopinath.launchchat.R

class HistoryAdapterInstrumentedTest {

    val activityRule = ActivityTestRule(MainActivity::class.java, true, true)

    @Test
    fun recyclerView_isDisplayed() {
        ActivityScenario.launch(MainActivity::class.java).use {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val recyclerView = activityRule.activity.findViewById<RecyclerView>(R.id.history_recycler_view)
            assert(recyclerView != null)
        }
    }
}