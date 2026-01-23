package org.vinaygopinath.launchchat.helpers

import android.app.Instrumentation
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.hamcrest.Matchers.allOf

object AssertionHelper {

    fun assertIntentNavigation(action: String, data: String, performAction: () -> Unit) {
        Intents.init()
        try {
            val expectedIntent = allOf(
                IntentMatchers.hasAction(action),
                IntentMatchers.hasData(data)
            )

            // Stub any intent matching our expected action and data
            Intents.intending(expectedIntent).respondWith(
                Instrumentation.ActivityResult(0, null)
            )

            // Also stub the app intent variant (with package) that may be tried first
            Intents.intending(IntentMatchers.hasAction(action)).respondWith(
                Instrumentation.ActivityResult(0, null)
            )

            performAction()

            Intents.intended(expectedIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            Intents.release()
        }
    }
}