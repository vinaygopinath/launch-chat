package org.vinaygopinath.launchchat.helpers

import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import androidx.annotation.StringRes
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.models.Action
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.DetailedActivity
import org.vinaygopinath.launchchat.utils.DateUtils
import javax.inject.Inject

class DetailedActivityHelper @Inject constructor(
    private val dateUtils: DateUtils,
    private val resources: Resources
) {

    @StringRes
    fun getSourceDisplayName(detailedActivity: DetailedActivity): Int {
        return when (detailedActivity.activity.source) {
            Activity.Source.TEL -> R.string.activity_source_tel
            Activity.Source.SMS -> R.string.activity_source_sms
            Activity.Source.MMS -> R.string.activity_source_mms
            Activity.Source.TEXT_SHARE -> R.string.activity_source_text_share
            Activity.Source.CONTACT -> R.string.activity_source_contact
            Activity.Source.DIAL -> R.string.activity_source_dial
            Activity.Source.UNKNOWN -> R.string.activity_source_unknown
            Activity.Source.MANUAL_INPUT -> R.string.activity_source_manual_input
            Activity.Source.HISTORY -> R.string.activity_source_history
        }
    }

    fun getActivityShortTimestamp(detailedActivity: DetailedActivity): String {
        return dateUtils.getDateTimeString(detailedActivity.activity.occurredAt)
    }

    fun getActivityContent(detailedActivity: DetailedActivity): String {
        return detailedActivity.activity.content
    }

    fun isFirstActionVisible(detailedActivity: DetailedActivity): Boolean {
        return detailedActivity.actions.isNotEmpty()
    }

    fun getFirstActionText(detailedActivity: DetailedActivity): Spanned? {
        return if (isFirstActionVisible(detailedActivity)) {
            getActionText(detailedActivity.actions.first())
        } else {
            null
        }
    }

    fun isSecondActionVisible(detailedActivity: DetailedActivity): Boolean {
        return detailedActivity.actions.size >= 2
    }

    fun getSecondActionText(detailedActivity: DetailedActivity): Spanned? {
        return if (isSecondActionVisible(detailedActivity)) {
            getActionText(detailedActivity.actions[1])
        } else {
            null
        }
    }

    private fun getActionText(action: Action): Spanned {
        return getSpannedText(
            resources.getString(
                R.string.action_label,
                action.phoneNumber,
                getActionTypeDisplayName(action)
            )
        )
    }

    private fun getActionTypeDisplayName(action: Action): String {
        return resources.getString(
            when (action.type) {
                Action.Type.WHATSAPP -> R.string.action_type_whatsapp
                Action.Type.WHATSAPP_BUSINESS -> R.string.action_type_whatsapp_business
                Action.Type.SIGNAL -> R.string.action_type_signal
                Action.Type.TELEGRAM -> R.string.action_type_telegram
                Action.Type.SMS -> R.string.action_type_sms
                Action.Type.PHONE_CALL -> R.string.action_type_phone_call
                Action.Type.OTHER -> R.string.action_type_other
            }
        )
    }

    fun isMoreTextVisible(detailedActivity: DetailedActivity): Boolean {
        return detailedActivity.actions.size > 2
    }

    fun getMoreText(detailedActivity: DetailedActivity): String? {
        return if (isMoreTextVisible(detailedActivity)) {
            resources.getString(R.string.action_more, detailedActivity.actions.size - 2)
        } else {
            null
        }
    }

    fun getActionsText(detailedActivity: DetailedActivity): Spanned {
        val actions = detailedActivity.actions

        return if (actions.isEmpty()) {
            SpannedString(resources.getString(R.string.no_action_label))
        } else {
            actions.joinToSpannedString("\n") { action -> getActionText(action) }
        }
    }

    private fun getSpannedText(text: String) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(text)
    }

    private fun <T> Iterable<T>.joinToSpannedString(
        separator: CharSequence = ", ",
        prefix: CharSequence = "",
        postfix: CharSequence = "",
        limit: Int = -1,
        truncated: CharSequence = "...",
        transform: ((T) -> CharSequence)? = null
    ): SpannedString {
        return SpannedString(
            joinTo(
                SpannableStringBuilder(),
                separator,
                prefix,
                postfix,
                limit,
                truncated,
                transform
            )
        )
    }
}
