package org.vinaygopinath.launchchat.unittest.screens.main.domain

import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.vinaygopinath.launchchat.factories.SettingsFactory
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.Activity.Source
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.main.domain.GetSettingsUseCase
import org.vinaygopinath.launchchat.screens.main.domain.ProcessIntentUseCase
import org.vinaygopinath.launchchat.screens.main.domain.ProcessIntentUseCase.ExtractedContent
import org.vinaygopinath.launchchat.utils.DateUtils
import java.time.Instant

class ProcessIntentUseCaseTest {
    private val getSettingsUseCase = mock<GetSettingsUseCase>()
    private val activityRepository = mock<ActivityRepository>()
    private val someFixedDate = Instant.now()
    private val dateUtils: DateUtils = mock<DateUtils>().apply {
        whenever(getCurrentInstant()).thenReturn(someFixedDate)
    }
    private val contentResolver = mock<ContentResolver>()

    private val useCase = ProcessIntentUseCase(
        getSettingsUseCase = getSettingsUseCase,
        activityRepository = activityRepository,
        dateUtils = dateUtils
    )

    // region ACTION_PROCESS_TEXT tests

    @Test
    fun `ACTION_PROCESS_TEXT with plain text returns PossibleResult with TEXT_SHARE source`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_PROCESS_TEXT)
            whenever(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).thenReturn("Hello World")
            whenever(toUri(0)).thenReturn("intent://...")
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isInstanceOf(ExtractedContent.PossibleResult::class.java)
        val possibleResult = result.extractedContent as ExtractedContent.PossibleResult
        assertThat(possibleResult.source).isEqualTo(Source.TEXT_SHARE)
        assertThat(possibleResult.rawInputText).isEqualTo("Hello World")
    }

    @Test
    fun `ACTION_PROCESS_TEXT with tel scheme returns Result with TEXT_SHARE source`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_PROCESS_TEXT)
            whenever(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).thenReturn("tel:+1234567890")
            whenever(toUri(0)).thenReturn("intent://...")
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isInstanceOf(ExtractedContent.Result::class.java)
        val extractedResult = result.extractedContent as ExtractedContent.Result
        assertThat(extractedResult.source).isEqualTo(Source.TEXT_SHARE)
        assertThat(extractedResult.phoneNumbers).containsExactly("+1234567890")
    }

    @Test
    fun `ACTION_PROCESS_TEXT with sms scheme returns Result with TEXT_SHARE source`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        // Note: Using sms scheme without body to avoid Uri.decode() which isn't mocked in unit tests
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_PROCESS_TEXT)
            whenever(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).thenReturn("sms:+1234567890")
            whenever(toUri(0)).thenReturn("intent://...")
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isInstanceOf(ExtractedContent.Result::class.java)
        val extractedResult = result.extractedContent as ExtractedContent.Result
        assertThat(extractedResult.source).isEqualTo(Source.TEXT_SHARE)
        assertThat(extractedResult.phoneNumbers).containsExactly("+1234567890")
    }

    @Test
    fun `ACTION_PROCESS_TEXT with empty text returns NoContentFound`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_PROCESS_TEXT)
            whenever(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).thenReturn("")
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isEqualTo(ExtractedContent.NoContentFound)
    }

    @Test
    fun `ACTION_PROCESS_TEXT with null text returns NoContentFound`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_PROCESS_TEXT)
            whenever(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).thenReturn(null)
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isEqualTo(ExtractedContent.NoContentFound)
    }

    @Test
    fun `ACTION_PROCESS_TEXT trims whitespace from text`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_PROCESS_TEXT)
            whenever(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).thenReturn("  +1234567890  ")
            whenever(toUri(0)).thenReturn("intent://...")
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isInstanceOf(ExtractedContent.PossibleResult::class.java)
        val possibleResult = result.extractedContent as ExtractedContent.PossibleResult
        assertThat(possibleResult.rawInputText).isEqualTo("+1234567890")
    }

    @Test
    fun `ACTION_PROCESS_TEXT creates activity when history is enabled`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        whenever(activityRepository.create(any()))
            .thenAnswer { answer -> answer.getArgument<Activity>(0) }

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_PROCESS_TEXT)
            whenever(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).thenReturn("Hello World")
            whenever(toUri(0)).thenReturn("intent://...")
        }

        val result = useCase.execute(intent, contentResolver)

        verify(activityRepository).create(any())
        assertThat(result.activity).isNotNull()
        assertThat(result.activity?.source).isEqualTo(Source.TEXT_SHARE)
        assertThat(result.activity?.content).isEqualTo("Hello World")
    }

    @Test
    fun `ACTION_PROCESS_TEXT does not create activity when history is disabled`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_PROCESS_TEXT)
            whenever(getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).thenReturn("Hello World")
            whenever(toUri(0)).thenReturn("intent://...")
        }

        val result = useCase.execute(intent, contentResolver)

        verify(activityRepository, never()).create(any())
        assertThat(result.activity).isNull()
    }

    // endregion

    // region ACTION_SEND tests (to verify shared processTextShareContent behavior)

    @Test
    fun `ACTION_SEND with plain text uses processTextShareContent`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val clipDataItem = mock<ClipData.Item>().apply {
            whenever(text).thenReturn("Shared text")
        }
        val clipData = mock<ClipData>().apply {
            whenever(getItemAt(0)).thenReturn(clipDataItem)
        }
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_SEND)
            whenever(this.clipData).thenReturn(clipData)
            whenever(extras).thenReturn(null)
            whenever(toUri(0)).thenReturn("intent://...")
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isInstanceOf(ExtractedContent.PossibleResult::class.java)
        val possibleResult = result.extractedContent as ExtractedContent.PossibleResult
        assertThat(possibleResult.source).isEqualTo(Source.TEXT_SHARE)
        assertThat(possibleResult.rawInputText).isEqualTo("Shared text")
    }

    @Test
    fun `ACTION_SEND with tel scheme uses processTextShareContent`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val clipDataItem = mock<ClipData.Item>().apply {
            whenever(text).thenReturn("tel:+9876543210")
        }
        val clipData = mock<ClipData>().apply {
            whenever(getItemAt(0)).thenReturn(clipDataItem)
        }
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_SEND)
            whenever(this.clipData).thenReturn(clipData)
            whenever(extras).thenReturn(null)
            whenever(toUri(0)).thenReturn("intent://...")
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isInstanceOf(ExtractedContent.Result::class.java)
        val extractedResult = result.extractedContent as ExtractedContent.Result
        assertThat(extractedResult.source).isEqualTo(Source.TEXT_SHARE)
        assertThat(extractedResult.phoneNumbers).containsExactly("+9876543210")
    }

    @Test
    fun `ACTION_SEND with empty clipData returns NoContentFound`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_SEND)
            whenever(clipData).thenReturn(null)
            whenever(extras).thenReturn(null)
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isEqualTo(ExtractedContent.NoContentFound)
    }

    // endregion

    // region General tests

    @Test
    fun `null intent returns NoContentFound`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val result = useCase.execute(null, contentResolver)

        assertThat(result.extractedContent).isEqualTo(ExtractedContent.NoContentFound)
    }

    @Test
    fun `unrecognized action returns NoContentFound`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        val intent = mock<Intent>().apply {
            whenever(action).thenReturn("com.example.UNKNOWN_ACTION")
        }

        val result = useCase.execute(intent, contentResolver)

        assertThat(result.extractedContent).isEqualTo(ExtractedContent.NoContentFound)
    }

    // endregion
}
