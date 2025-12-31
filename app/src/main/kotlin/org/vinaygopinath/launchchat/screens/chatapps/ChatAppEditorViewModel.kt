package org.vinaygopinath.launchchat.screens.chatapps

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.helpers.ImageStorageHelper
import org.vinaygopinath.launchchat.models.ChatApp
import org.vinaygopinath.launchchat.repositories.ChatAppRepository
import org.vinaygopinath.launchchat.utils.DateUtils
import javax.inject.Inject

@HiltViewModel
class ChatAppEditorViewModel @Inject constructor(
    private val chatAppRepository: ChatAppRepository,
    private val imageStorageHelper: ImageStorageHelper,
    private val dateUtils: DateUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private var editingChatAppId: Long? = null
    private var originalIconPath: String? = null
    private var isPredefinedChatApp: Boolean = false

    fun loadChatApp(chatAppId: Long?) {
        if (chatAppId == null) {
            _uiState.value = UiState.Ready(isEditing = false, isPredefined = false)
            return
        }

        viewModelScope.launch {
            val chatApp = chatAppRepository.getChatAppById(chatAppId)
            if (chatApp == null) {
                _uiState.value = UiState.Error("Chat app not found")
                return@launch
            }

            editingChatAppId = chatAppId
            originalIconPath = chatApp.iconUri
            isPredefinedChatApp = chatApp.isPredefined

            _formState.value = FormState(
                name = chatApp.name,
                identifierType = chatApp.identifierType,
                phoneNumberFormat = chatApp.phoneNumberFormat
                    ?: ChatApp.PhoneNumberFormat.WITH_PLUS_PREFIX,
                phoneNumberIntent = chatApp.phoneNumberLaunchIntent ?: "",
                phoneNumberUrl = chatApp.phoneNumberLaunchUrl ?: "",
                usernameIntent = chatApp.usernameLaunchIntent ?: "",
                usernameUrl = chatApp.usernameLaunchUrl ?: "",
                intentPackage = chatApp.intentPackageSelection ?: "",
                iconPath = chatApp.iconUri
            )
            _uiState.value = UiState.Ready(isEditing = true, isPredefined = chatApp.isPredefined)
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateIdentifierType(identifierType: ChatApp.IdentifierType) {
        _formState.value = _formState.value.copy(identifierType = identifierType)
    }

    fun updatePhoneNumberFormat(format: ChatApp.PhoneNumberFormat) {
        _formState.value = _formState.value.copy(phoneNumberFormat = format)
    }

    fun updatePhoneNumberIntent(intent: String) {
        _formState.value = _formState.value.copy(phoneNumberIntent = intent)
    }

    fun updatePhoneNumberUrl(url: String) {
        _formState.value = _formState.value.copy(phoneNumberUrl = url)
    }

    fun updateUsernameIntent(intent: String) {
        _formState.value = _formState.value.copy(usernameIntent = intent)
    }

    fun updateUsernameUrl(url: String) {
        _formState.value = _formState.value.copy(usernameUrl = url)
    }

    fun updateIntentPackage(packageName: String) {
        _formState.value = _formState.value.copy(intentPackage = packageName)
    }

    fun setIcon(uri: Uri) {
        viewModelScope.launch {
            val savedPath = imageStorageHelper.saveIcon(uri)
            if (savedPath != null) {
                _formState.value = _formState.value.copy(iconPath = savedPath)
            }
        }
    }

    fun clearIcon() {
        val currentPath = _formState.value.iconPath
        if (currentPath != null && currentPath != originalIconPath) {
            imageStorageHelper.deleteIcon(currentPath)
        }
        _formState.value = _formState.value.copy(iconPath = null)
    }

    fun save() {
        val form = _formState.value
        val validationError = validateForm(form)
        if (validationError != null) {
            _uiState.value = UiState.ValidationError(validationError)
            _uiState.value = UiState.Ready(
                isEditing = editingChatAppId != null,
                isPredefined = isPredefinedChatApp
            )
            return
        }

        viewModelScope.launch {
            val launchType = determineLaunchType(form)
            val phoneNumberFormat = if (form.identifierType.supportsPhoneNumbers()) {
                form.phoneNumberFormat
            } else {
                null
            }

            val chatApp = ChatApp(
                id = editingChatAppId ?: 0,
                name = form.name.trim(),
                identifierType = form.identifierType,
                launchType = launchType,
                intentPackageSelection = form.intentPackage.takeIf { it.isNotBlank() },
                phoneNumberLaunchIntent = form.phoneNumberIntent.takeIf {
                    it.isNotBlank() && form.identifierType.supportsPhoneNumbers()
                },
                phoneNumberLaunchUrl = form.phoneNumberUrl.takeIf {
                    it.isNotBlank() && form.identifierType.supportsPhoneNumbers()
                },
                usernameLaunchIntent = form.usernameIntent.takeIf {
                    it.isNotBlank() && form.identifierType.supportsUsernames()
                },
                usernameLaunchUrl = form.usernameUrl.takeIf {
                    it.isNotBlank() && form.identifierType.supportsUsernames()
                },
                createdAt = dateUtils.getCurrentInstant(),
                deletedAt = null,
                isPredefined = false,
                isEnabled = true,
                iconUri = form.iconPath,
                phoneNumberFormat = phoneNumberFormat
            )

            if (originalIconPath != null && originalIconPath != form.iconPath) {
                imageStorageHelper.deleteIcon(originalIconPath!!)
            }

            chatAppRepository.saveChatApp(chatApp)
            _uiState.value = UiState.Saved
        }
    }

    fun delete() {
        val id = editingChatAppId ?: return

        viewModelScope.launch {
            val form = _formState.value
            if (form.iconPath != null) {
                imageStorageHelper.deleteIcon(form.iconPath)
            }
            chatAppRepository.deleteChatApp(id)
            _uiState.value = UiState.Deleted
        }
    }

    private fun validateForm(form: FormState): String? {
        if (form.name.isBlank()) {
            return "Name is required"
        }

        if (form.identifierType.supportsPhoneNumbers()) {
            validatePhoneNumberFields(form)?.let { return it }
        }

        if (form.identifierType.supportsUsernames()) {
            validateUsernameFields(form)?.let { return it }
        }

        return null
    }

    private fun validatePhoneNumberFields(form: FormState): String? {
        val hasLaunchMethod = form.phoneNumberIntent.isNotBlank() || form.phoneNumberUrl.isNotBlank()
        if (!hasLaunchMethod) {
            return "At least one phone number launch method (intent or URL) is required"
        }

        val hasPlaceholderInIntent = form.phoneNumberIntent.isBlank() ||
            form.phoneNumberIntent.contains(PLACEHOLDER_PHONE_NUMBER)
        val hasPlaceholderInUrl = form.phoneNumberUrl.isBlank() ||
            form.phoneNumberUrl.contains(PLACEHOLDER_PHONE_NUMBER)
        if (!hasPlaceholderInIntent || !hasPlaceholderInUrl) {
            return "Phone number templates must contain $PLACEHOLDER_PHONE_NUMBER"
        }

        return null
    }

    private fun validateUsernameFields(form: FormState): String? {
        val hasLaunchMethod = form.usernameIntent.isNotBlank() || form.usernameUrl.isNotBlank()
        if (!hasLaunchMethod) {
            return "At least one username launch method (intent or URL) is required"
        }

        val hasPlaceholderInIntent = form.usernameIntent.isBlank() ||
            form.usernameIntent.contains(PLACEHOLDER_USERNAME)
        val hasPlaceholderInUrl = form.usernameUrl.isBlank() ||
            form.usernameUrl.contains(PLACEHOLDER_USERNAME)
        if (!hasPlaceholderInIntent || !hasPlaceholderInUrl) {
            return "Username templates must contain $PLACEHOLDER_USERNAME"
        }

        return null
    }

    private fun determineLaunchType(form: FormState): ChatApp.LaunchType {
        val hasPhoneIntent =
            form.phoneNumberIntent.isNotBlank() && form.identifierType.supportsPhoneNumbers()
        val hasPhoneUrl =
            form.phoneNumberUrl.isNotBlank() && form.identifierType.supportsPhoneNumbers()
        val hasUsernameIntent =
            form.usernameIntent.isNotBlank() && form.identifierType.supportsUsernames()
        val hasUsernameUrl =
            form.usernameUrl.isNotBlank() && form.identifierType.supportsUsernames()

        val hasAnyIntent = hasPhoneIntent || hasUsernameIntent
        val hasAnyUrl = hasPhoneUrl || hasUsernameUrl

        return when {
            hasAnyIntent && hasAnyUrl -> ChatApp.LaunchType.BOTH_URL_AND_INTENT
            hasAnyIntent -> ChatApp.LaunchType.INTENT_ONLY
            else -> ChatApp.LaunchType.URL_ONLY
        }
    }

    data class FormState(
        val name: String = "",
        val identifierType: ChatApp.IdentifierType = ChatApp.IdentifierType.PHONE_NUMBER_ONLY,
        val phoneNumberFormat: ChatApp.PhoneNumberFormat = ChatApp.PhoneNumberFormat.WITH_PLUS_PREFIX,
        val phoneNumberIntent: String = "",
        val phoneNumberUrl: String = "",
        val usernameIntent: String = "",
        val usernameUrl: String = "",
        val intentPackage: String = "",
        val iconPath: String? = null
    )

    sealed class UiState {
        data object Loading : UiState()
        data class Ready(val isEditing: Boolean, val isPredefined: Boolean) : UiState()
        data class Error(val message: String) : UiState()
        data class ValidationError(val message: String) : UiState()
        data object Saved : UiState()
        data object Deleted : UiState()
    }

    companion object {
        private const val PLACEHOLDER_PHONE_NUMBER = "[phone-number]"
        private const val PLACEHOLDER_USERNAME = "[username]"
    }
}
