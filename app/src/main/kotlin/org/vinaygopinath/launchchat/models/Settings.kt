package org.vinaygopinath.launchchat.models

import org.vinaygopinath.launchchat.utils.PreferenceUtil

data class Settings(
    val isActivityHistoryEnabled: Boolean,
    val missingCountryCodeAction: MissingCountryCodeAction
) {
    companion object {
        const val KEY_MANAGE_CHAT_APPS = "pref_manage_chat_apps"
        const val KEY_ACTIVITY_HISTORY = "pref_activity_history"

        const val KEY_MISSING_COUNTRY_CODE_ACTION = "missing_country_code_action"
        const val VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_DEFAULT = "default_country_code"
        const val VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_RECENT = "recent_country_code"

        const val KEY_DEFAULT_COUNTRY_CODE = "default_country_code"
        const val KEY_RECENT_COUNTRY_CODE = "recent_country_code"

        fun build(preferenceUtil: PreferenceUtil) = Settings(
            isActivityHistoryEnabled = preferenceUtil.getBoolean(KEY_ACTIVITY_HISTORY, true),
            missingCountryCodeAction = MissingCountryCodeAction.build(preferenceUtil)
        )
    }

    sealed class MissingCountryCodeAction {
        data class DefaultCountryCode(val defaultCountryCode: Int?) : MissingCountryCodeAction()
        data class RecentCountryCode(val recentCountryCode: Int?) : MissingCountryCodeAction()

        companion object {
            fun build(preferenceUtil: PreferenceUtil): MissingCountryCodeAction {
                return when (
                    preferenceUtil.getString(
                        KEY_MISSING_COUNTRY_CODE_ACTION,
                        VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_RECENT
                    )
                ) {
                    VALUE_MISSING_COUNTRY_CODE_ACTION_ENTRY_DEFAULT ->
                        DefaultCountryCode(preferenceUtil.getInt(KEY_DEFAULT_COUNTRY_CODE))

                    else ->
                        RecentCountryCode(preferenceUtil.getInt(KEY_RECENT_COUNTRY_CODE))
                }
            }
        }
    }
}
