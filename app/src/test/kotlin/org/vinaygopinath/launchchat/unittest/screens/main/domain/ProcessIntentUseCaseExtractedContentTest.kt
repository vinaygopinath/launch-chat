package org.vinaygopinath.launchchat.unittest.screens.main.domain

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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.vinaygopinath.launchchat.factories.SettingsFactory
import org.vinaygopinath.launchchat.models.Activity.Source
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.main.domain.GetSettingsUseCase
import org.vinaygopinath.launchchat.screens.main.domain.ProcessIntentUseCase
import org.vinaygopinath.launchchat.utils.DateUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class ProcessIntentUseCaseExtractedContentTest {

    private val getSettingsUseCase: GetSettingsUseCase = mock()
    private val activityRepository: ActivityRepository = mock()
    private val someFixedDate = Instant.now()
    private val dateUtils: DateUtils = mock<DateUtils>().apply {
        whenever(getCurrentInstant()).thenReturn(someFixedDate)
    }
    private val useCase = ProcessIntentUseCase(getSettingsUseCase, activityRepository, dateUtils)
    private val contentResolver: ContentResolver = mock()

    @Before
    fun setUp() {
        whenever(getSettingsUseCase.execute()).thenReturn(
            SettingsFactory.build(isActivityHistoryEnabled = true)
        )
    }

    @Test
    fun `returns no content found when intent is null`() = runTest {
        assertThat(useCase.execute(null, contentResolver).extractedContent)
            .isEqualTo(ProcessIntentUseCase.ExtractedContent.NoContentFound)
    }

    @Test
    fun `returns no content found when intent action is not expected`() = runTest {
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_BOOT_COMPLETED)
        }
        assertThat(useCase.execute(intent, contentResolver).extractedContent)
            .isEqualTo(ProcessIntentUseCase.ExtractedContent.NoContentFound)
    }

    @Test
    fun `returns no content found when intent data is null (View action)`() = runTest {
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn(null)
        }
        assertThat(useCase.execute(intent, contentResolver).extractedContent)
            .isEqualTo(ProcessIntentUseCase.ExtractedContent.NoContentFound)
    }

    @Test
    fun `returns phone number result from tel scheme URI (View action)`() = runTest {
        val phoneNumber = "+1987654321"
        val uri = "some-uri"
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn("tel:$phoneNumber")
            whenever(toUri(0)).thenReturn(uri)
        }
        assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = Source.TEL,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri
            )
        )
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
    fun `returns phone number result and message from smsto scheme URI (View action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = mock<Intent>().apply {
                whenever(action).thenReturn(Intent.ACTION_VIEW)
                whenever(dataString).thenReturn("smsto:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
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
    fun `returns phone number result and message from mms scheme URI (View action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = mock<Intent>().apply {
                whenever(action).thenReturn(Intent.ACTION_VIEW)
                whenever(dataString).thenReturn("mms:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
                ProcessIntentUseCase.ExtractedContent.Result(
                    source = Source.MMS,
                    phoneNumbers = listOf(phoneNumber),
                    rawContent = uri,
                    message = message
                )
            )
        }

    @Test
    fun `returns phone number result and message from mmsto scheme URI (View action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = mock<Intent>().apply {
                whenever(action).thenReturn(Intent.ACTION_VIEW)
                whenever(dataString).thenReturn("mmsto:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }
            assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
                ProcessIntentUseCase.ExtractedContent.Result(
                    source = Source.MMS,
                    phoneNumbers = listOf(phoneNumber),
                    rawContent = uri,
                    message = message
                )
            )
        }

    @Test
    fun `returns possible result when data is of unknown scheme (View action)`() = runTest {
        val dataString = "some-data-string-with-possible-phone-number-88362522-and-other-text"
        val uri = "some-uri"
        val intent = spy<Intent>().apply {
            action = Intent.ACTION_VIEW
            whenever(this.dataString).thenReturn(dataString)
            whenever(toUri(0)).thenReturn(uri)
        }

        assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.PossibleResult(
                source = Source.UNKNOWN,
                rawInputText = dataString,
                rawContent = uri
            )
        )
    }

    @Test
    fun `returns no content found when intent data is null (Send action)`() = runTest {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent(null)
        }
        assertThat(useCase.execute(intent, contentResolver).extractedContent)
            .isEqualTo(ProcessIntentUseCase.ExtractedContent.NoContentFound)
    }

    @Test
    fun `returns phone number result from tel scheme URI (Send action)`() = runTest {
        val phoneNumber = "+1987654321"
        val uri = "some-uri"
        val intent = spy(Intent()).apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent("tel:$phoneNumber")
            whenever(toUri(0)).thenReturn(uri)
        }
        assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = Source.TEXT_SHARE,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri
            )
        )
    }

    @Test
    fun `returns phone number result and message from sms scheme URI (Send action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = spy(Intent()).apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent("sms:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }

            assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
                ProcessIntentUseCase.ExtractedContent.Result(
                    source = Source.TEXT_SHARE,
                    phoneNumbers = listOf(phoneNumber),
                    rawContent = uri,
                    message = message
                )
            )
        }

    @Test
    fun `returns phone number result and message from smsto scheme URI (Send action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = spy(Intent()).apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent("smsto:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }

            assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
                ProcessIntentUseCase.ExtractedContent.Result(
                    source = Source.TEXT_SHARE,
                    phoneNumbers = listOf(phoneNumber),
                    rawContent = uri,
                    message = message
                )
            )
        }

    @Test
    fun `returns phone number result and message from mms scheme URI (Send action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = spy(Intent()).apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent("mms:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }

            assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
                ProcessIntentUseCase.ExtractedContent.Result(
                    source = Source.TEXT_SHARE,
                    phoneNumbers = listOf(phoneNumber),
                    rawContent = uri,
                    message = message
                )
            )
        }

    @Test
    fun `returns phone number result and message from mmsto scheme URI (Send action)`() =
        runTest {
            val phoneNumber = "+1987654321"
            val message = "some-message"
            val uri = "some-uri"
            val intent = spy(Intent()).apply {
                action = Intent.ACTION_SEND
                clipData = buildClipDataWithContent("mmsto:$phoneNumber?body=$message")
                whenever(toUri(0)).thenReturn(uri)
            }

            assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
                ProcessIntentUseCase.ExtractedContent.Result(
                    source = Source.TEXT_SHARE,
                    phoneNumbers = listOf(phoneNumber),
                    rawContent = uri,
                    message = message
                )
            )
        }

    @Test
    fun `returns possible result when data is of unknown scheme (Send action)`() = runTest {
        val clipboardString = "some-string-with-possible-phone-number-88362522-and-other-text"
        val uri = "some-uri"
        val intent = spy<Intent>().apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent(clipboardString)
            whenever(toUri(0)).thenReturn(uri)
        }

        assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.PossibleResult(
                source = Source.TEXT_SHARE,
                rawInputText = clipboardString,
                rawContent = uri
            )
        )
    }

    @Test
    fun `returns result when data is a contact URI (Send action)`() = runTest {
        val phoneNumber1 = "+1-555-444-33-22"
        val phoneNumber2 = "+1-666-555-33-22"
        val uri = Uri.parse("content://some-uri")
        val intent = spy<Intent>().apply {
            action = Intent.ACTION_SEND
            whenever(toUri(0)).thenReturn(uri.toString())
        }
        whenever(intent.extras).thenReturn(
            Bundle().apply {
                putParcelable(Intent.EXTRA_STREAM, uri)
            }
        )

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

        assertThat(useCase.execute(intent, contentResolver).extractedContent).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = Source.CONTACT,
                phoneNumbers = listOf(phoneNumber1, phoneNumber2),
                rawContent = uri.toString()
            )
        )
    }

    private fun buildClipDataWithContent(text: String?): ClipData {
        return ClipData.newPlainText("Some label", text)
    }
}
