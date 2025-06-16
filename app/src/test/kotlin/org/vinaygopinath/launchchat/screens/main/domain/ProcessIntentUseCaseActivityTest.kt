package org.vinaygopinath.launchchat.screens.main.domain

import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import at.bitfire.vcard4android.ContactReader
import com.google.common.truth.Truth.assertThat
import ezvcard.VCard
import ezvcard.VCardVersion
import ezvcard.property.Telephone
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.vinaygopinath.launchchat.factories.SettingsFactory
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.Activity.Source
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.utils.DateUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class ProcessIntentUseCaseActivityTest {

    private val getSettingsUseCase: GetSettingsUseCase = mock()
    private val activityRepository: ActivityRepository = mock()
    private val someFixedDate = Instant.now()
    private val dateUtils: DateUtils = mock<DateUtils>().apply {
        whenever(getCurrentInstant()).thenReturn(someFixedDate)
    }
    private val useCase = ProcessIntentUseCase(getSettingsUseCase, activityRepository, dateUtils)
    private val contentResolver: ContentResolver = mock()

    @Test
    fun `does not log an activity when intent is null`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        assertThat(useCase.execute(null, contentResolver).activity).isNull()
    }

    @Test
    fun `does not log an activity when intent is not expected`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_BOOT_COMPLETED)
        }
        assertThat(useCase.execute(intent, contentResolver).activity).isNull()
    }

    @Test
    fun `does not log an activity when intent data is null (View action)`() = runTest {
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn(null)
        }
        assertThat(useCase.execute(intent, contentResolver).activity).isNull()
    }

    @Test
    fun `logs a phone number activity from tel scheme URI (View action)`() = runTest {
        val phoneNumber = "+1987654321"
        val uri = "some-uri"
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn("tel:$phoneNumber")
            whenever(toUri(0)).thenReturn(uri)
        }
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        whenever(activityRepository.create(any())).thenAnswer { answer ->
            answer.getArgument<Activity>(0)
        }

        val processedIntent = useCase.execute(intent, contentResolver)
        assertThat(processedIntent.activity).isNotNull()
        val activity = processedIntent.activity!!

        assertThat(activity.source).isEqualTo(Source.TEL)
        assertThat(activity.content).isEqualTo(phoneNumber)
        assertThat(activity.message).isEqualTo(null)
        assertThat(activity.occurredAt).isEqualTo(someFixedDate)
    }

    @Test
    fun `returns phone number result and message from sms scheme URI (View action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = mock<Intent>().apply {
                whenever(action).thenReturn(Intent.ACTION_VIEW)
                whenever(dataString).thenReturn("sms:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))

            assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
                ProcessIntentUseCase.ExtractedContent.Result(
                    source = Source.SMS,
                    phoneNumbers = listOf(phoneNumber),
                    rawContent = uri,
                    message = message
                )
            )
        }

    @Test
    fun `logs activity with phone number and message from sms scheme URI (View action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = mock<Intent>().apply {
                whenever(action).thenReturn(Intent.ACTION_VIEW)
                whenever(dataString).thenReturn("sms:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.SMS)
            assertThat(activity.content).isEqualTo(phoneNumber)
            assertThat(activity.message).isEqualTo(message)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `logs activity with phone number and message from smsto scheme URI (View action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = mock<Intent>().apply {
                whenever(action).thenReturn(Intent.ACTION_VIEW)
                whenever(dataString).thenReturn("smsto:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.SMS)
            assertThat(activity.content).isEqualTo(phoneNumber)
            assertThat(activity.message).isEqualTo(message)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `logs activity with phone number and message from mms scheme URI (View action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = mock<Intent>().apply {
                whenever(action).thenReturn(Intent.ACTION_VIEW)
                whenever(dataString).thenReturn("mms:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.MMS)
            assertThat(activity.content).isEqualTo(phoneNumber)
            assertThat(activity.message).isEqualTo(message)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `logs activity with phone number message from mmsto scheme URI (View action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = mock<Intent>().apply {
                whenever(action).thenReturn(Intent.ACTION_VIEW)
                whenever(dataString).thenReturn("mmsto:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.MMS)
            assertThat(activity.content).isEqualTo(phoneNumber)
            assertThat(activity.message).isEqualTo(message)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `logs activity with possible result when data is of unknown scheme (View action)`() =
        runTest {
            val dataString = "some-data-string-with-possible-phone-number-88362522-and-other-text"
            val uri = "some-uri"
            val intent = spy<Intent>().apply {
                action = Intent.ACTION_VIEW
                whenever(this.dataString).thenReturn(dataString)
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.UNKNOWN)
            assertThat(activity.content).isEqualTo(dataString)
            assertThat(activity.message).isEqualTo(null)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `does not log an activity when intent data is null (Send action)`() = runTest {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent(null)
        }
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))

        assertThat(useCase.execute(intent, contentResolver).activity).isNull()
    }

    @Test
    fun `does not log an activity when the setting is disabled`() = runTest {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent(null)
        }
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = false))

        assertThat(useCase.execute(intent, contentResolver).activity).isNull()
        verify(activityRepository, never()).create(any())
    }

    @Test
    fun `logs activity with phone number from tel scheme URI (Send action)`() = runTest {
        val phoneNumber = "+1987654321"
        val uri = "some-uri"
        val intent = spy(Intent()).apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent("tel:$phoneNumber")
            whenever(toUri(0)).thenReturn(uri)
        }
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
        whenever(activityRepository.create(any())).thenAnswer { answer ->
            answer.getArgument<Activity>(0)
        }

        val processedIntent = useCase.execute(intent, contentResolver)
        assertThat(processedIntent.activity).isNotNull()
        val activity = processedIntent.activity!!

        assertThat(activity.source).isEqualTo(Source.TEXT_SHARE)
        assertThat(activity.content).isEqualTo(phoneNumber)
        assertThat(activity.message).isEqualTo(null)
        assertThat(activity.occurredAt).isEqualTo(someFixedDate)
    }

    @Test
    fun `logs activity with phone number and message from sms scheme URI (Send action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = spy(Intent()).apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent("sms:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.TEXT_SHARE)
            assertThat(activity.content).isEqualTo(phoneNumber)
            assertThat(activity.message).isEqualTo(message)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `logs activity with phone number and message from smsto scheme URI (Send action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = spy(Intent()).apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent("smsto:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.TEXT_SHARE)
            assertThat(activity.content).isEqualTo(phoneNumber)
            assertThat(activity.message).isEqualTo(message)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `logs activity with phone number and message from mms scheme URI (Send action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = spy(Intent()).apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent("mms:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.TEXT_SHARE)
            assertThat(activity.content).isEqualTo(phoneNumber)
            assertThat(activity.message).isEqualTo(message)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `logs activity with phone number and message from mmsto scheme URI (Send action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = spy(Intent()).apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent("mmsto:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.TEXT_SHARE)
            assertThat(activity.content).isEqualTo(phoneNumber)
            assertThat(activity.message).isEqualTo(message)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `logs activity with possible result when data is of unknown scheme (Send action)`() =
        runTest {
            val clipboardString = "some-string-with-possible-phone-number-88362522-and-other-text"
            val uri = "some-uri"
            val intent = spy<Intent>().apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent(clipboardString)
                whenever(toUri(0)).thenReturn(uri)
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.TEXT_SHARE)
            assertThat(activity.content).isEqualTo(clipboardString)
            assertThat(activity.message).isEqualTo(null)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    @Test
    fun `persists an activity record for a possible result`() = runTest {
        val clipboardString = "some-string-with-possible-phone-number-88362522-and-other-text"
        val uri = "some-uri"
        val intent = spy<Intent>().apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent(clipboardString)
            whenever(toUri(0)).thenReturn(uri)
        }
        whenever(getSettingsUseCase.execute())
            .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))

        useCase.execute(intent, contentResolver)

        verify(activityRepository, times(1)).create(
            Activity(
                content = clipboardString,
                source = Source.TEXT_SHARE,
                message = null,
                occurredAt = someFixedDate
            )
        )
    }

    @Test
    fun `logs activity with phone numbers when data is a contact URI (Send action)`() =
        runTest {
            val phoneNumber1 = "+1-555-444-33-22"
            val phoneNumber2 = "+1-666-555-33-22"
            val uri = Uri.parse("content://some-uri")
            val intent = spy<Intent>().apply {
                action = Intent.ACTION_SEND
                whenever(toUri(0)).thenReturn(uri.toString())
            }
            whenever(getSettingsUseCase.execute())
                .thenReturn(SettingsFactory.build(isActivityHistoryEnabled = true))
            whenever(intent.extras).thenReturn(
                Bundle().apply {
                    putParcelable(Intent.EXTRA_STREAM, uri)
                }
            )
            whenever(activityRepository.create(any())).thenAnswer { answer ->
                answer.getArgument<Activity>(0)
            }

            val contact = ContactReader.fromVCard(
                VCard().apply {
                    addProperty(Telephone(phoneNumber1))
                    addProperty(Telephone(phoneNumber2))
                }
            )
            val os = ByteArrayOutputStream()
            contact.writeVCard(VCardVersion.V4_0, os)
            val inputStream = ByteArrayInputStream(os.toByteArray())
            whenever(contentResolver.openInputStream(any())).thenReturn(inputStream)

            val processedIntent = useCase.execute(intent, contentResolver)
            assertThat(processedIntent.activity).isNotNull()
            val activity = processedIntent.activity!!

            assertThat(activity.source).isEqualTo(Source.CONTACT_FILE)
            assertThat(activity.content).isEqualTo(
                listOf(
                    phoneNumber1,
                    phoneNumber2
                ).joinToString("\n")
            )
            assertThat(activity.message).isEqualTo(null)
            assertThat(activity.occurredAt).isEqualTo(someFixedDate)
        }

    private fun buildClipDataWithContent(text: String?): ClipData {
        return ClipData.newPlainText("Some label", text)
    }
}
