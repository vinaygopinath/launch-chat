package org.vinaygopinath.launchchat.screens.main.domain

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import at.bitfire.vcard4android.Contact
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.Activity.Source
import org.vinaygopinath.launchchat.repositories.ActivityRepository
import org.vinaygopinath.launchchat.screens.main.MainActivity.Companion.INTENT_EXTRA_HISTORY
import org.vinaygopinath.launchchat.utils.DateUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class ProcessIntentUseCase @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val activityRepository: ActivityRepository,
    private val dateUtils: DateUtils
) {

    suspend fun execute(intent: Intent?, contentResolver: ContentResolver): ProcessedIntent {
        val extractedContent = when {
            intent == null || !INTERESTED_ACTIONS.contains(intent.action) -> ExtractedContent.NoContentFound
            intent.hasExtra(INTENT_EXTRA_HISTORY) -> processHistoryIntent(intent)
            intent.action == Intent.ACTION_VIEW -> processViewIntent(intent)
            intent.action == Intent.ACTION_SEND -> processClipboardOrContactIntent(
                intent,
                contentResolver
            )

            intent.action == Intent.ACTION_DIAL -> processDialIntent(intent)
            intent.action == Intent.ACTION_PROCESS_TEXT -> processTextIntent(intent)
            else -> ExtractedContent.PossibleResult(
                source = Source.UNKNOWN,
                rawInputText = intent.dataString?.trim(),
                rawContent = intent.toUri(0)
            )
        }

        val settings = getSettingsUseCase.execute()

        return ProcessedIntent(
            extractedContent,
            if (settings.isActivityHistoryEnabled) {
                buildActivity(extractedContent)?.let { activityRepository.create(it) }
            } else {
                null
            }
        )
    }

    private fun processHistoryIntent(intent: Intent): ExtractedContent {
        val activity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(INTENT_EXTRA_HISTORY, Activity::class.java)
        } else {
            intent.getParcelableExtra(INTENT_EXTRA_HISTORY)
        }

        if (activity == null) {
            error("History intent did not pass an Activity")
        }

        return ExtractedContent.PossibleResult(
            source = Source.HISTORY,
            rawInputText = activity.content,
            rawContent = "",
        )
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
                    source = Source.UNKNOWN,
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
            else -> processTextShareContent(clipData, intent)
        }
    }

    private fun processTextIntent(intent: Intent): ExtractedContent {
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()?.trim()
        return processTextShareContent(text, intent)
    }

    private fun processTextShareContent(text: String?, intent: Intent): ExtractedContent {
        return when {
            text.isNullOrEmpty() -> ExtractedContent.NoContentFound
            doesTextStartWithTelScheme(text) -> extractTelSchemeResult(
                text,
                intent,
                Source.TEXT_SHARE
            )

            doesTextStartWithMessageScheme(text) -> extractMessageSchemeResult(
                text,
                intent,
                Source.TEXT_SHARE
            )

            else -> ExtractedContent.PossibleResult(
                source = Source.TEXT_SHARE,
                rawInputText = text,
                rawContent = intent.toUri(0)
            )
        }
    }

    private fun processDialIntent(intent: Intent): ExtractedContent {
        val data = intent.dataString?.trim()
        return when {
            data == null -> ExtractedContent.NoContentFound
            doesTextStartWithTelScheme(data) -> {
                extractTelSchemeResult(data, intent, Source.DIAL)
            }

            else -> {
                ExtractedContent.PossibleResult(
                    source = Source.UNKNOWN,
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
        source: Source = Source.TEL
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
        source: Source? = null
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

    private fun getContentSourceFromMessageScheme(messageScheme: String): Source {
        return when (messageScheme) {
            "sms:", "smsto:" -> Source.SMS
            "mms:", "mmsto:" -> Source.MMS
            else -> error(
                "getContentSourceFromMessageScheme was called with an unknown scheme: \"$messageScheme\""
            )
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
                        source = Source.CONTACT_FILE,
                        phoneNumbers = phoneNumbers,
                        rawContent = intent.toUri(0)
                    )
                }
            }
        } catch (ignoredException: Exception) {
            ExtractedContent.NoContentFound
        }
    }

    private fun buildActivity(extractedContent: ExtractedContent): Activity? {
        return when (extractedContent) {
            is ExtractedContent.PossibleResult -> {
                Activity(
                    content = extractedContent.rawInputText.orEmpty(),
                    source = extractedContent.source,
                    message = null,
                    occurredAt = dateUtils.getCurrentInstant()
                )
            }

            is ExtractedContent.Result -> {
                Activity(
                    content = extractedContent.phoneNumbers.joinToString("\n"),
                    source = extractedContent.source,
                    message = extractedContent.message,
                    occurredAt = dateUtils.getCurrentInstant()
                )
            }

            else -> {
                null
            }
        }
    }

    sealed class ExtractedContent {
        data class PossibleResult(
            val source: Source,
            val rawInputText: String?,
            val rawContent: String
        ) : ExtractedContent()

        data class Result(
            val source: Source,
            val phoneNumbers: List<String>,
            val rawContent: String,
            val message: String? = null
        ) : ExtractedContent()

        data object NoContentFound : ExtractedContent()
    }

    data class ProcessedIntent(
        val extractedContent: ExtractedContent,
        val activity: Activity?
    )

    companion object {
        private val INTERESTED_ACTIONS = listOf(
            Intent.ACTION_VIEW,
            Intent.ACTION_DIAL,
            Intent.ACTION_SEND,
            Intent.ACTION_SENDTO,
            Intent.ACTION_SEND_MULTIPLE,
            Intent.ACTION_PROCESS_TEXT
        )

        const val TEL_SCHEME = "tel:"
        val MESSAGE_SCHEMES = listOf("smsto:", "sms:", "mmsto:", "mms:")
    }
}
