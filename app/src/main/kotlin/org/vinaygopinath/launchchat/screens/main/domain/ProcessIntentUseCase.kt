package org.vinaygopinath.launchchat.screens.main.domain

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import at.bitfire.vcard4android.Contact
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.ContentSource
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.utils.DateUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class ProcessIntentUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val dateUtils: DateUtils
) {

    suspend fun execute(intent: Intent?, contentResolver: ContentResolver): ExtractedContent {
        return when {
            intent == null || !INTERESTED_ACTIONS.contains(intent.action) -> ExtractedContent.NoContentFound
            intent.action == Intent.ACTION_VIEW -> processViewIntent(intent)
            intent.action == Intent.ACTION_SEND -> processClipboardOrContactIntent(
                intent,
                contentResolver
            )

            intent.action == Intent.ACTION_DIAL -> processDialIntent(intent)
            else -> ExtractedContent.PossibleResult(
                source = ContentSource.UNKNOWN,
                rawInputText = intent.dataString?.trim(),
                rawContent = intent.toUri(0)
            )
        }.also { extractedContent ->
            if (extractedContent is ExtractedContent.PossibleResult) {
                activityRepository.create(
                    Activity(
                        content = extractedContent.rawInputText ?: "",
                        source = extractedContent.source,
                        message = null,
                        occurredAt = dateUtils.getCurrentInstant()
                    )
                )
            } else if (extractedContent is ExtractedContent.Result) {
                activityRepository.create(
                    Activity(
                        content = extractedContent.phoneNumbers.joinToString("\n"),
                        source = extractedContent.source,
                        message = extractedContent.message,
                        occurredAt = dateUtils.getCurrentInstant()
                    )
                )
            }
        }
    }

    private fun processViewIntent(intent: Intent): ExtractedContent {
        val data = intent.dataString?.trim()
        return when {
            data == null -> ExtractedContent.NoContentFound
            doesTextStartWithTelScheme(data) -> {
                extractTelSchemeResult(data, intent)
            }

            doesTextStartWithMessageScheme(data) -> {
                extractMessageSchemeResult(data, intent)
            }

            else -> {
                ExtractedContent.PossibleResult(
                    source = ContentSource.UNKNOWN,
                    rawInputText = intent.dataString?.trim(),
                    rawContent = intent.toUri(0)
                )
            }
        }
    }

    private fun processClipboardOrContactIntent(
        intent: Intent,
        contentResolver: ContentResolver
    ): ExtractedContent {
        val clipData = intent.clipData?.getItemAt(0)?.text?.toString()?.trim()
        val uri = getExtraStreamIntentUri(intent)
        return when {
            uri != null -> extractContactResult(contentResolver, uri, intent)
            clipData == null -> ExtractedContent.NoContentFound
            doesTextStartWithTelScheme(clipData) -> extractTelSchemeResult(
                clipData,
                intent,
                ContentSource.TEXT_SHARE
            )

            doesTextStartWithMessageScheme(clipData) -> extractMessageSchemeResult(
                clipData,
                intent,
                ContentSource.TEXT_SHARE
            )

            else -> ExtractedContent.PossibleResult(
                source = ContentSource.TEXT_SHARE,
                rawInputText = clipData.trim(),
                rawContent = intent.toUri(0)
            )
        }
    }

    private fun processDialIntent(intent: Intent): ExtractedContent {
        val data = intent.dataString?.trim()
        return when {
            data == null -> ExtractedContent.NoContentFound
            doesTextStartWithTelScheme(data) -> {
                extractTelSchemeResult(data, intent, ContentSource.DIAL)
            }

            else -> {
                ExtractedContent.PossibleResult(
                    source = ContentSource.UNKNOWN,
                    rawInputText = intent.dataString?.trim(),
                    rawContent = intent.toUri(0)
                )
            }
        }
    }

    private fun doesTextStartWithTelScheme(text: String): Boolean {
        return text.startsWith(TEL_SCHEME, ignoreCase = true)
    }

    private fun extractTelSchemeResult(
        text: String,
        intent: Intent,
        source: ContentSource = ContentSource.TEL
    ): ExtractedContent {
        return ExtractedContent.Result(
            source = source,
            phoneNumbers = listOf(text.substring(TEL_SCHEME.length).trim()),
            rawContent = intent.toUri(0)
        )
    }

    private fun doesTextStartWithMessageScheme(text: String): Boolean {
        return MESSAGE_SCHEMES.any { text.startsWith(it, ignoreCase = true) }
    }

    private fun extractMessageSchemeResult(
        text: String,
        intent: Intent,
        source: ContentSource? = null
    ): ExtractedContent.Result {
        val messageScheme = MESSAGE_SCHEMES.first { text.startsWith(it, ignoreCase = true) }
        val dataWithoutScheme = text.substring(messageScheme.length)

        return ExtractedContent.Result(
            source = source ?: getContentSourceFromMessageScheme(messageScheme),
            phoneNumbers = getPhoneNumbersFromMessageLink(dataWithoutScheme),
            message = getMessageFromMessageLink(dataWithoutScheme),
            rawContent = intent.toUri(0)
        )
    }


    private fun getContentSourceFromMessageScheme(messageScheme: String): ContentSource {
        return when (messageScheme) {
            "sms:", "smsto:" -> ContentSource.SMS
            "mms:", "mmsto:" -> ContentSource.MMS
            else -> throw IllegalStateException("getContentSourceFromMessageScheme was called with an unknown scheme: \"$messageScheme\"")
        }
    }

    private fun getPhoneNumbersFromMessageLink(dataWithoutScheme: String): List<String> {
        // See Kotlin playground for testing: https://pl.kotl.in/4CyPvWiDJ
        val phoneNumberFullString = dataWithoutScheme.split("?")
        return phoneNumberFullString.first().split(",").map { it.trim() }
    }

    private fun getMessageFromMessageLink(dataWithoutScheme: String): String {
        return if (dataWithoutScheme.contains("?body=")) {
            Uri.decode(dataWithoutScheme.split("?body=")[1])
        } else {
            ""
        }.trim()
    }

    private fun getExtraStreamIntentUri(intent: Intent): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.extras?.getParcelable(Intent.EXTRA_STREAM)
        }
    }

    private fun extractContactResult(
        contentResolver: ContentResolver,
        uri: Uri,
        intent: Intent
    ): ExtractedContent {
        return try {
            contentResolver.openInputStream(uri).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val phoneNumbers = Contact.fromReader(reader, false, null)
                        .flatMap { contact -> contact.phoneNumbers }
                        .map { contact -> contact.property.text }
                    ExtractedContent.Result(
                        source = ContentSource.CONTACT_FILE,
                        phoneNumbers = phoneNumbers,
                        rawContent = intent.toUri(0)
                    )
                }
            }
        } catch (e: Exception) {
            ExtractedContent.NoContentFound
        }
    }

    sealed class ExtractedContent {
        data class PossibleResult(
            val source: ContentSource,
            val rawInputText: String?,
            val rawContent: String
        ) : ExtractedContent()

        data class Result(
            val source: ContentSource,
            val phoneNumbers: List<String>,
            val rawContent: String,
            val message: String? = null
        ) : ExtractedContent()

        data object NoContentFound : ExtractedContent()

    }

    companion object {
        private val INTERESTED_ACTIONS = listOf(
            Intent.ACTION_VIEW,
            Intent.ACTION_DIAL,
            Intent.ACTION_SEND,
            Intent.ACTION_SENDTO,
            Intent.ACTION_SEND_MULTIPLE
        )

        const val TEL_SCHEME = "tel:"
        val MESSAGE_SCHEMES = listOf("smsto:", "sms:", "mmsto:", "mms:")
    }

}