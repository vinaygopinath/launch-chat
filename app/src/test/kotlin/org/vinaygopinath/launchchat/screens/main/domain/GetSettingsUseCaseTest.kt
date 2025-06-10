package org.vinaygopinath.launchchat.screens.main.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.utils.PreferenceUtil

class GetSettingsUseCaseTest {
    private val preferenceUtil = mock<PreferenceUtil>()
    private val useCase = GetSettingsUseCase(preferenceUtil)

    @Test
    fun `returns the activity history setting`() {
        whenever(
            preferenceUtil.getBoolean(
                eq(R.string.pref_activity_history_key),
                any<Boolean>()
            )
        ).thenReturn(true)

        val settings = useCase.execute()
        assertThat(settings.isActivityHistoryEnabled).isTrue()
    }

    @Test
    fun `defaults to activity history setting being enabled`() {
        whenever(
            preferenceUtil.getBoolean(
                eq(R.string.pref_activity_history_key),
                any<Boolean>()
            )
        ).thenReturn(true)

        useCase.execute()

        verify(preferenceUtil).getBoolean(
            R.string.pref_activity_history_key,
            true
        )
    }
}
