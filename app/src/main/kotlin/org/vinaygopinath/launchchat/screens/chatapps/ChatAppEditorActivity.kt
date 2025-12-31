package org.vinaygopinath.launchchat.screens.chatapps

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.ChatAppHelper
import org.vinaygopinath.launchchat.models.ChatApp
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ChatAppEditorActivity : AppCompatActivity() {

    @Inject
    lateinit var chatAppHelper: ChatAppHelper

    private val viewModel: ChatAppEditorViewModel by viewModels()

    private lateinit var iconView: ImageView
    private lateinit var selectIconButton: MaterialButton
    private lateinit var clearIconButton: MaterialButton
    private lateinit var nameInput: TextInputEditText
    private lateinit var identifierTypeGroup: RadioGroup
    private lateinit var phoneSection: LinearLayout
    private lateinit var phoneFormatGroup: RadioGroup
    private lateinit var phoneIntentInput: TextInputEditText
    private lateinit var phoneUrlInput: TextInputEditText
    private lateinit var usernameSection: LinearLayout
    private lateinit var usernameIntentInput: TextInputEditText
    private lateinit var usernameUrlInput: TextInputEditText
    private lateinit var packageInput: TextInputEditText
    private lateinit var predefinedWarning: View
    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: MaterialButton

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.setIcon(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_app_editor)

        initializeViews()
        initializeToolbar()
        initializeListeners()
        initializeObservers()

        val chatAppId = intent.getLongExtra(EXTRA_CHAT_APP_ID, -1L).takeIf { it != -1L }
        viewModel.loadChatApp(chatAppId)
    }

    private fun initializeViews() {
        iconView = findViewById(R.id.chat_app_editor_icon)
        selectIconButton = findViewById(R.id.chat_app_editor_icon_button)
        clearIconButton = findViewById(R.id.chat_app_editor_clear_icon_button)
        nameInput = findViewById(R.id.chat_app_editor_name_input)
        identifierTypeGroup = findViewById(R.id.chat_app_editor_identifier_type_group)
        phoneSection = findViewById(R.id.chat_app_editor_phone_section)
        phoneFormatGroup = findViewById(R.id.chat_app_editor_phone_format_group)
        phoneIntentInput = findViewById(R.id.chat_app_editor_phone_intent_input)
        phoneUrlInput = findViewById(R.id.chat_app_editor_phone_url_input)
        usernameSection = findViewById(R.id.chat_app_editor_username_section)
        usernameIntentInput = findViewById(R.id.chat_app_editor_username_intent_input)
        usernameUrlInput = findViewById(R.id.chat_app_editor_username_url_input)
        packageInput = findViewById(R.id.chat_app_editor_package_input)
        predefinedWarning = findViewById(R.id.chat_app_editor_predefined_warning)
        saveButton = findViewById(R.id.chat_app_editor_save_button)
        deleteButton = findViewById(R.id.chat_app_editor_delete_button)
    }

    private fun initializeToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeListeners() {
        selectIconButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        clearIconButton.setOnClickListener {
            viewModel.clearIcon()
        }

        nameInput.doAfterTextChanged { text ->
            viewModel.updateName(text?.toString() ?: "")
        }

        identifierTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            val identifierType = when (checkedId) {
                R.id.chat_app_editor_identifier_phone_only -> ChatApp.IdentifierType.PHONE_NUMBER_ONLY
                R.id.chat_app_editor_identifier_username_only -> ChatApp.IdentifierType.USERNAME_ONLY
                R.id.chat_app_editor_identifier_both -> ChatApp.IdentifierType.BOTH_PHONE_NUMBER_AND_USERNAME
                else -> ChatApp.IdentifierType.PHONE_NUMBER_ONLY
            }
            viewModel.updateIdentifierType(identifierType)
        }

        phoneFormatGroup.setOnCheckedChangeListener { _, checkedId ->
            val format = when (checkedId) {
                R.id.chat_app_editor_phone_format_with_plus -> ChatApp.PhoneNumberFormat.WITH_PLUS_PREFIX
                R.id.chat_app_editor_phone_format_without_plus -> ChatApp.PhoneNumberFormat.WITHOUT_PLUS_PREFIX
                R.id.chat_app_editor_phone_format_raw -> ChatApp.PhoneNumberFormat.RAW
                else -> ChatApp.PhoneNumberFormat.WITH_PLUS_PREFIX
            }
            viewModel.updatePhoneNumberFormat(format)
        }

        phoneIntentInput.doAfterTextChanged { text ->
            viewModel.updatePhoneNumberIntent(text?.toString() ?: "")
        }

        phoneUrlInput.doAfterTextChanged { text ->
            viewModel.updatePhoneNumberUrl(text?.toString() ?: "")
        }

        usernameIntentInput.doAfterTextChanged { text ->
            viewModel.updateUsernameIntent(text?.toString() ?: "")
        }

        usernameUrlInput.doAfterTextChanged { text ->
            viewModel.updateUsernameUrl(text?.toString() ?: "")
        }

        packageInput.doAfterTextChanged { text ->
            viewModel.updateIntentPackage(text?.toString() ?: "")
        }

        saveButton.setOnClickListener {
            viewModel.save()
        }

        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        handleUiState(state)
                    }
                }
                launch {
                    viewModel.formState.collectLatest { form ->
                        updateFormUi(form)
                    }
                }
            }
        }
    }

    private fun handleUiState(state: ChatAppEditorViewModel.UiState) {
        when (state) {
            is ChatAppEditorViewModel.UiState.Loading -> {
                // Show loading if needed
            }
            is ChatAppEditorViewModel.UiState.Ready -> {
                supportActionBar?.setTitle(
                    when {
                        state.isPredefined -> R.string.chat_app_editor_title_view
                        state.isEditing -> R.string.chat_app_editor_title_edit
                        else -> R.string.chat_app_editor_title_add
                    }
                )
                predefinedWarning.isVisible = state.isPredefined
                saveButton.isVisible = !state.isPredefined
                deleteButton.isVisible = state.isEditing && !state.isPredefined
                setIconButtonsVisible(!state.isPredefined)
            }
            is ChatAppEditorViewModel.UiState.Error -> {
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                finish()
            }
            is ChatAppEditorViewModel.UiState.ValidationError -> {
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
            }
            is ChatAppEditorViewModel.UiState.Saved -> {
                Toast.makeText(this, R.string.chat_app_editor_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
            is ChatAppEditorViewModel.UiState.Deleted -> {
                Toast.makeText(this, R.string.chat_app_editor_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateFormUi(form: ChatAppEditorViewModel.FormState) {
        updateTextInputIfChanged(nameInput, form.name)
        updateIdentifierTypeUi(form.identifierType)
        updatePhoneFormatUi(form.phoneNumberFormat)
        updateTextInputIfChanged(phoneIntentInput, form.phoneNumberIntent)
        updateTextInputIfChanged(phoneUrlInput, form.phoneNumberUrl)
        updateTextInputIfChanged(usernameIntentInput, form.usernameIntent)
        updateTextInputIfChanged(usernameUrlInput, form.usernameUrl)
        updateTextInputIfChanged(packageInput, form.intentPackage)
        updateIconUi(form.iconPath)
    }

    private fun updateTextInputIfChanged(input: TextInputEditText, value: String) {
        if (input.text.toString() != value) {
            input.setText(value)
        }
    }

    private fun updateIdentifierTypeUi(identifierType: ChatApp.IdentifierType) {
        val identifierTypeId = when (identifierType) {
            ChatApp.IdentifierType.PHONE_NUMBER_ONLY -> R.id.chat_app_editor_identifier_phone_only
            ChatApp.IdentifierType.USERNAME_ONLY -> R.id.chat_app_editor_identifier_username_only
            ChatApp.IdentifierType.BOTH_PHONE_NUMBER_AND_USERNAME -> R.id.chat_app_editor_identifier_both
        }
        if (identifierTypeGroup.checkedRadioButtonId != identifierTypeId) {
            identifierTypeGroup.check(identifierTypeId)
        }
        phoneSection.visibility = if (identifierType.supportsPhoneNumbers()) View.VISIBLE else View.GONE
        usernameSection.visibility = if (identifierType.supportsUsernames()) View.VISIBLE else View.GONE
    }

    private fun updatePhoneFormatUi(phoneNumberFormat: ChatApp.PhoneNumberFormat) {
        val phoneFormatId = when (phoneNumberFormat) {
            ChatApp.PhoneNumberFormat.WITH_PLUS_PREFIX -> R.id.chat_app_editor_phone_format_with_plus
            ChatApp.PhoneNumberFormat.WITHOUT_PLUS_PREFIX -> R.id.chat_app_editor_phone_format_without_plus
            ChatApp.PhoneNumberFormat.RAW -> R.id.chat_app_editor_phone_format_raw
        }
        if (phoneFormatGroup.checkedRadioButtonId != phoneFormatId) {
            phoneFormatGroup.check(phoneFormatId)
        }
    }

    private fun updateIconUi(iconPath: String?) {
        if (iconPath != null) {
            val file = File(iconPath)
            if (file.exists()) {
                iconView.setImageDrawable(Drawable.createFromPath(iconPath))
            } else {
                iconView.setImageResource(R.drawable.ic_chat_app_default)
            }
            clearIconButton.isVisible = true
        } else {
            iconView.setImageResource(R.drawable.ic_chat_app_default)
            clearIconButton.isVisible = false
        }
    }

    private fun setIconButtonsVisible(visible: Boolean) {
        selectIconButton.isVisible = visible
        clearIconButton.isVisible = visible && viewModel.formState.value.iconPath != null
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.chat_app_editor_delete_title)
            .setMessage(R.string.chat_app_editor_delete_message)
            .setPositiveButton(R.string.chat_app_editor_delete) { _, _ ->
                viewModel.delete()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val EXTRA_CHAT_APP_ID = "extra_chat_app_id"

        fun getIntent(context: Context, chatAppId: Long?): Intent {
            return Intent(context, ChatAppEditorActivity::class.java).apply {
                if (chatAppId != null) {
                    putExtra(EXTRA_CHAT_APP_ID, chatAppId)
                }
            }
        }
    }
}
