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
import kotlinx.coroutines.runBlocking
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
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.ContentSource
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.utils.DateUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class ProcessIntentUseCaseTest {

    private val activityRepository: ActivityRepository = mock()
    private val someFixedDate = Instant.now()
    private val dateUtils: DateUtils = mock<DateUtils>().apply {
        whenever(getCurrentInstant()).thenReturn(someFixedDate)
    }
    private val useCase = ProcessIntentUseCase(activityRepository, dateUtils)
    private val contentResolver: ContentResolver = mock()

    @Test
    fun `returns "no content found" when intent is null`() = runBlocking {
        assertThat(
            useCase.execute(null, contentResolver)
        ).isEqualTo(ProcessIntentUseCase.ExtractedContent.NoContentFound)
    }

    @Test
    fun `returns "no content found" when intent action is not expected`() = runBlocking {
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_BOOT_COMPLETED)
        }
        assertThat(
            useCase.execute(
                intent, contentResolver
            )
        ).isEqualTo(ProcessIntentUseCase.ExtractedContent.NoContentFound)
    }

    @Test
    fun `returns "no content found" when intent data is null (View action)`() = runBlocking {
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn(null)
        }
        assertThat(
            useCase.execute(
                intent, contentResolver
            )
        ).isEqualTo(ProcessIntentUseCase.ExtractedContent.NoContentFound)
    }

    @Test
    fun `returns phone number result from tel scheme URI (View action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val uri = "some-uri"
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn("tel:$phoneNumber")
            whenever(toUri(0)).thenReturn(uri)
        }
        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.TEL,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri
            )
        )
    }

    @Test
    fun `returns phone number result and message from sms scheme URI (View action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn("sms:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }
        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.SMS,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri,
                message = message
            )
        )
    }

    @Test
    fun `returns phone number result and message from smsto scheme URI (View action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn("smsto:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }
        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.SMS,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri,
                message = message
            )
        )
    }

    @Test
    fun `returns phone number result and message from mms scheme URI (View action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn("mms:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }
        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.MMS,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri,
                message = message
            )
        )
    }

    @Test
    fun `returns phone number result and message from mmsto scheme URI (View action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = mock<Intent>().apply {
            whenever(action).thenReturn(Intent.ACTION_VIEW)
            whenever(dataString).thenReturn("mmsto:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }
        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.MMS,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri,
                message = message
            )
        )
    }

    @Test
    fun `returns possible result when data is of unknown scheme (View action)`() = runBlocking {
        val dataString = "some-data-string-with-possible-phone-number-88362522-and-other-text"
        val uri = "some-uri"
        val intent = spy<Intent>().apply {
            action = Intent.ACTION_VIEW
            whenever(this.dataString).thenReturn(dataString)
            whenever(toUri(0)).thenReturn(uri)
        }

        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.PossibleResult(
                source = ContentSource.UNKNOWN,
                rawInputText = dataString,
                rawContent = uri
            )
        )
    }

    @Test
    fun `returns "no content found" when intent data is null (Send action)`() = runBlocking {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent(null)
        }
        assertThat(
            useCase.execute(
                intent, contentResolver
            )
        ).isEqualTo(ProcessIntentUseCase.ExtractedContent.NoContentFound)
    }

    @Test
    fun `returns phone number result from tel scheme URI (Send action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val uri = "some-uri"
        val intent = spy(Intent()).apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent("tel:$phoneNumber")
            whenever(toUri(0)).thenReturn(uri)
        }
        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.TEXT_SHARE,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri
            )
        )
    }

    @Test
    fun `returns phone number result and message from sms scheme URI (Send action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = spy(Intent()).apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent("sms:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }

        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.TEXT_SHARE,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri,
                message = message
            )
        )
    }

    @Test
    fun `returns phone number result and message from smsto scheme URI (Send action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = spy(Intent()).apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent("smsto:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }

        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.TEXT_SHARE,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri,
                message = message
            )
        )
    }

    @Test
    fun `returns phone number result and message from mms scheme URI (Send action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = spy(Intent()).apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent("mms:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }

        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.TEXT_SHARE,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri,
                message = message
            )
        )
    }

    @Test
    fun `returns phone number result and message from mmsto scheme URI (Send action)`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = spy(Intent()).apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent("mmsto:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }

        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.TEXT_SHARE,
                phoneNumbers = listOf(phoneNumber),
                rawContent = uri,
                message = message
            )
        )
    }

    @Test
    fun `returns possible result when data is of unknown scheme (Send action)`() = runBlocking {
        val clipboardString = "some-string-with-possible-phone-number-88362522-and-other-text"
        val uri = "some-uri"
        val intent = spy<Intent>().apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent(clipboardString)
            whenever(toUri(0)).thenReturn(uri)
        }

        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.PossibleResult(
                source = ContentSource.TEXT_SHARE,
                rawInputText = clipboardString,
                rawContent = uri
            )
        )
    }

    @Test
    fun `persists an activity record for a possible result`() = runBlocking {
        val clipboardString = "some-string-with-possible-phone-number-88362522-and-other-text"
        val uri = "some-uri"
        val intent = spy<Intent>().apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent(clipboardString)
            whenever(toUri(0)).thenReturn(uri)
        }

        useCase.execute(intent, contentResolver)

        verify(activityRepository, times(1)).create(
            Activity(
                content = clipboardString,
                source = ContentSource.TEXT_SHARE,
                message = null,
                occurredAt = someFixedDate
            )
        )

        return@runBlocking
    }

    @Test
    fun `persists an activity record for a result`() = runBlocking {
        val phoneNumber = "+1987654321"
        val message = "some-message"
        val uri = "some-uri"
        val intent = spy(Intent()).apply {
            action = Intent.ACTION_SEND
            clipData = buildClipDataWithContent("sms:$phoneNumber?body=$message")
            whenever(toUri(0)).thenReturn(uri)
        }

        useCase.execute(intent, contentResolver)

        verify(activityRepository, times(1)).create(
            Activity(
                content = phoneNumber,
                source = ContentSource.TEXT_SHARE,
                message = message,
                occurredAt = someFixedDate
            )
        )

        return@runBlocking
    }

    @Test
    fun `does not persist an activity record when no content was found`() = runBlocking {
        val intent = spy(Intent())

        useCase.execute(intent, contentResolver)

        verify(activityRepository, never()).create(any())

        return@runBlocking
    }

    @Test
    fun `returns result when data is a contact URI (Send action)`() = runBlocking {
        val phoneNumber1 = "+1-555-444-33-22"
        val phoneNumber2 = "+1-666-555-33-22"
        val uri = Uri.parse("content://some-uri")
        val intent = spy<Intent>().apply {
            action = Intent.ACTION_SEND
            whenever(toUri(0)).thenReturn(uri.toString())
        }
        whenever(intent.extras).thenReturn(Bundle().apply {
            putParcelable(Intent.EXTRA_STREAM, uri)
        })

        val contact = ContactReader.fromVCard(VCard().apply {
            addProperty(Telephone(phoneNumber1))
            addProperty(Telephone(phoneNumber2))
        })
        val os = ByteArrayOutputStream()
        contact.writeVCard(VCardVersion.V4_0, os)
        val inputStream = ByteArrayInputStream(os.toByteArray())
        whenever(contentResolver.openInputStream(any())).thenReturn(inputStream)

        assertThat(useCase.execute(intent, contentResolver)).isEqualTo(
            ProcessIntentUseCase.ExtractedContent.Result(
                source = ContentSource.CONTACT_FILE,
                phoneNumbers = listOf(phoneNumber1, phoneNumber2),
                rawContent = uri.toString()
            )
        )
    }

    private fun buildClipDataWithContent(text: String?): ClipData {
        return ClipData.newPlainText("Some label", text)
    }
}