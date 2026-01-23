package org.vinaygopinath.launchchat.integrationtest.screens.history.domain

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.vinaygopinath.launchchat.AppDatabase
import org.vinaygopinath.launchchat.factories.ActivityFactory
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.history.domain.UpdateActivityNoteUseCase
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
class UpdateActivityNoteUseCaseTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var useCase: UpdateActivityNoteUseCase

    @Inject
    lateinit var database: AppDatabase

    @Test
    fun `updates the note for the given activity`() = runTest {
        val activity = ActivityFactory.build(id = 10).also { activityRepository.create(it) }

        useCase.execute(activity.id, "This is a test note")

        val updatedNote = queryActivityNote(activity.id)
        assertThat(updatedNote).isEqualTo("This is a test note")
    }

    @Test
    fun `clears the note when given an empty string`() = runTest {
        val activity = ActivityFactory.build(id = 10, note = "Existing note")
            .also { activityRepository.create(it) }

        useCase.execute(activity.id, "")

        val updatedNote = queryActivityNote(activity.id)
        assertThat(updatedNote).isNull()
    }

    @Test
    fun `clears the note when given null`() = runTest {
        val activity = ActivityFactory.build(id = 10, note = "Existing note")
            .also { activityRepository.create(it) }

        useCase.execute(activity.id, null)

        val updatedNote = queryActivityNote(activity.id)
        assertThat(updatedNote).isNull()
    }

    @Test
    fun `does not affect other activities`() = runTest {
        val activityToUpdate = ActivityFactory.build(id = 10)
            .also { activityRepository.create(it) }
        val otherActivity = ActivityFactory.build(id = 11, note = "Other note")
            .also { activityRepository.create(it) }

        useCase.execute(activityToUpdate.id, "Updated note")

        val otherActivityNote = queryActivityNote(otherActivity.id)
        assertThat(otherActivityNote).isEqualTo("Other note")
    }

    private fun queryActivityNote(activityId: Long): String? {
        return database.query("SELECT note FROM activities WHERE id = $activityId", null)
            .use { cursor ->
                if (cursor.count == 0) {
                    return@use null
                }
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex("note")
                if (cursor.isNull(columnIndex)) null else cursor.getString(columnIndex)
            }
    }
}
