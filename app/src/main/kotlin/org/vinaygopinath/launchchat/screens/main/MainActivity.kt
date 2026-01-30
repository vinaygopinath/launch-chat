package org.vinaygopinath.launchchat.screens.main

import android.Manifest
import android.app.ComponentCaller
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.ChatAppHelper
import org.vinaygopinath.launchchat.helpers.ClipboardHelper
import org.vinaygopinath.launchchat.helpers.ContactHelper
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper
import org.vinaygopinath.launchchat.helpers.IntentHelper
import org.vinaygopinath.launchchat.helpers.NoteDialogHelper
import org.vinaygopinath.launchchat.helpers.PhoneNumberHelper
import org.vinaygopinath.launchchat.helpers.UsernameHelper
import org.vinaygopinath.launchchat.models.Action
import org.vinaygopinath.launchchat.models.Activity
import org.vinaygopinath.launchchat.models.ChatApp
import org.vinaygopinath.launchchat.models.DetailedActivity
import org.vinaygopinath.launchchat.screens.history.HistoryActivity
import org.vinaygopinath.launchchat.screens.main.domain.ProcessIntentUseCase
import org.vinaygopinath.launchchat.screens.settings.SettingsActivity
import javax.inject.Inject
import org.vinaygopinath.launchchat.models.ChatApp.InputType as ChatInputType

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var phoneNumberHelper: PhoneNumberHelper

    @Inject
    lateinit var clipboardHelper: ClipboardHelper

    @Inject
    lateinit var intentHelper: IntentHelper

    @Inject
    lateinit var detailedActivityHelper: DetailedActivityHelper

    @Inject
    lateinit var chatAppHelper: ChatAppHelper

    @Inject
    lateinit var usernameHelper: UsernameHelper

    @Inject
    lateinit var contactHelper: ContactHelper

    private val requestContactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchContactPicker()
        } else {
            showToast(R.string.contacts_permission_denied_toast)
        }
    }

    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        contactUri?.let { handleSelectedContact(it) }
    }

    private val historyAdapter by lazy {
        RecentDetailedActivityAdapter(
            detailedActivityHelper,
            recentHistoryClickListener
        )
    }

    private val chatAppButtonAdapter by lazy {
        ChatAppButtonAdapter(chatAppHelper) { chatApp ->
            onChatAppButtonClick(chatApp)
        }
    }

    private lateinit var phoneNumberInput: TextInputEditText
    private lateinit var phoneNumberInputLayout: TextInputLayout
    private lateinit var messageInput: EditText
    private lateinit var historyTitle: MaterialTextView
    private lateinit var historyListView: RecyclerView
    private lateinit var historyViewAllButton: Button
    private lateinit var chatAppButtonList: RecyclerView

    private val recentHistoryClickListener by lazy {
        object : RecentDetailedActivityAdapter.Companion.RecentHistoryClickListener {
            override fun onRecentHistoryItemClick(detailedActivity: DetailedActivity) {
                if (phoneNumberInput.text.isNullOrEmpty()) {
                    viewModel.logActivityFromHistory(detailedActivity.activity)
                } else {
                    showReplaceInputWithHistoryDialog(detailedActivity.activity)
                }
            }

            override fun onNoteButtonClick(detailedActivity: DetailedActivity) {
                showNoteDialog(detailedActivity)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeView()
        initializeObservers()

        viewModel.processIntent(intent, contentResolver)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchSettings()
    }

    private fun initializeView() {
        phoneNumberInputLayout = findViewById(R.id.phone_number_input_layout)
        phoneNumberInput = findViewById(R.id.phone_number_input)
        messageInput = findViewById(R.id.message_input)
        historyTitle = findViewById(R.id.history_title)
        historyListView = findViewById(R.id.history_list)
        phoneNumberInput.addTextChangedListener(
            afterTextChanged = {
                if (phoneNumberInput.isFocused) {
                    return@addTextChangedListener
                }

                updatePhoneNumberInputType()
            }
        )
        with(historyListView) {
            val linearLayoutManager = LinearLayoutManager(this@MainActivity)
            layoutManager = linearLayoutManager
            adapter = historyAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    linearLayoutManager.orientation
                )
            )
        }
        findViewById<MaterialButton>(R.id.paste_from_clipboard_button).setOnClickListener {
            val content = clipboardHelper.readClipboardContent()
            if (content is ClipboardHelper.ClipboardContent.ClipboardData) {
                phoneNumberInput.setText(content.content)
            }
        }
        findViewById<View>(R.id.choose_from_contacts_button).setOnClickListener {
            checkContactsPermissionAndLaunchPicker()
        }
        chatAppButtonList = findViewById(R.id.chat_app_button_list)
        with(chatAppButtonList) {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = chatAppButtonAdapter
        }
        historyViewAllButton = findViewById(R.id.history_view_all)
        historyViewAllButton.setOnClickListener {
            startActivity(HistoryActivity.getIntent(this))
        }
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        uiState.extractedContent?.let {
                            handleExtractedContent(it)
                            updatePhoneNumberInputType()
                        }
                    }
                }
                launch {
                    viewModel.getRecentDetailedActivities().collectLatest { detailedActivityList ->
                        toggleHistoryViews(
                            showHistory = detailedActivityList.isNotEmpty() &&
                                viewModel.uiState.value.settings?.isActivityHistoryEnabled != false
                        )
                        historyAdapter.setItems(detailedActivityList)
                    }
                }
                launch {
                    viewModel.getEnabledChatApps().collectLatest { chatApps ->
                        chatAppButtonAdapter.submitList(chatApps)
                    }
                }
            }
        }
    }

    private fun handleExtractedContent(extractedContent: ProcessIntentUseCase.ExtractedContent) {
        if (extractedContent is ProcessIntentUseCase.ExtractedContent.Result) {
            if (extractedContent.phoneNumbers.size == 1) {
                phoneNumberInput.setText(extractedContent.phoneNumbers.first())
            } else if (extractedContent.phoneNumbers.size > 1) {
                phoneNumberInput.setText(extractedContent.phoneNumbers.joinToString("\n"))
            }

            if (extractedContent.message != null) {
                messageInput.setText(extractedContent.message)
            }
        } else if (extractedContent is ProcessIntentUseCase.ExtractedContent.PossibleResult) {
            if (extractedContent.rawInputText != null) {
                phoneNumberInput.setText(extractedContent.rawInputText)
            }
        }
    }

    private fun showPhoneNumberSelectionDialog(
        phoneNumbers: List<String>,
        onNumberSelected: (String) -> Unit
    ) {
        val items = phoneNumbers.toTypedArray()
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.phone_number_selection_dialog_title)

        val dialogView = layoutInflater.inflate(R.layout.dialog_phone_number_selection, null)
        builder.setView(dialogView)

        val phoneNumberList =
            dialogView.findViewById<ListView>(R.id.phone_number_selection_dialog_list)
        phoneNumberList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

        val dialog = builder.create()
        phoneNumberList.setOnItemClickListener { _, _, position, _ ->
            onNumberSelected(items[position])
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showUsernameSelectionDialog(
        usernames: List<String>,
        onUsernameSelected: (String) -> Unit
    ) {
        val items = usernames.toTypedArray()
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.username_selection_dialog_title)

        val dialogView = layoutInflater.inflate(R.layout.dialog_phone_number_selection, null)
        builder.setView(dialogView)

        val usernameList =
            dialogView.findViewById<ListView>(R.id.phone_number_selection_dialog_list)
        usernameList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

        val dialog = builder.create()
        usernameList.setOnItemClickListener { _, _, position, _ ->
            onUsernameSelected(items[position])
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        viewModel.processIntent(intent, contentResolver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_main_about -> {
                startActivity(intentHelper.getGithubRepoIntent())
                true
            }

            R.id.action_main_settings -> {
                startActivity(SettingsActivity.getIntent(this))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showToast(@StringRes toastResId: Int) {
        Toast.makeText(this, toastResId, Toast.LENGTH_LONG).show()
    }

    private fun checkContactsPermissionAndLaunchPicker() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchContactPicker()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_CONTACTS
            ) -> {
                showContactsPermissionRationale()
            }
            else -> {
                requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun showContactsPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle(R.string.contacts_permission_required_title)
            .setMessage(R.string.contacts_permission_required_message)
            .setPositiveButton(R.string.contacts_permission_required_positive_button) { _, _ ->
                requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            .setNegativeButton(R.string.contacts_permission_required_negative_button, null)
            .show()
    }

    private fun launchContactPicker() {
        pickContactLauncher.launch(null)
    }

    private fun handleSelectedContact(contactUri: Uri) {
        val phoneNumbers = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        when {
            phoneNumbers.isEmpty() -> {
                showToast(R.string.contacts_no_phone_number_toast)
            }
            phoneNumbers.size == 1 -> {
                val number = phoneNumbers.first().number
                phoneNumberInput.setText(number)
                viewModel.setContactPickerSource(number)
            }
            else -> {
                showContactPhoneNumberSelectionDialog(phoneNumbers)
            }
        }
    }

    private fun showContactPhoneNumberSelectionDialog(phoneNumbers: List<ContactHelper.ContactPhoneNumber>) {
        val displayItems = phoneNumbers.map { phoneNumber ->
            if (phoneNumber.type != null) {
                "${phoneNumber.number} (${phoneNumber.type})"
            } else {
                phoneNumber.number
            }
        }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.contact_phone_number_selection_dialog_title)

        val dialogView = layoutInflater.inflate(R.layout.dialog_phone_number_selection, null)
        builder.setView(dialogView)

        val phoneNumberList =
            dialogView.findViewById<ListView>(R.id.phone_number_selection_dialog_list)
        phoneNumberList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayItems)

        val dialog = builder.create()
        phoneNumberList.setOnItemClickListener { _, _, position, _ ->
            val number = phoneNumbers[position].number
            phoneNumberInput.setText(number)
            viewModel.setContactPickerSource(number)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun toggleHistoryViews(showHistory: Boolean) {
        historyViewAllButton.isVisible = showHistory
        historyListView.isVisible = showHistory
        historyTitle.isVisible = showHistory
    }

    private fun showReplaceInputWithHistoryDialog(activity: Activity) {
        AlertDialog.Builder(this)
            .setTitle(R.string.replace_input_title)
            .setMessage(R.string.replace_input_message)
            .setPositiveButton(R.string.replace_input_positive_button) { _, _ ->
                viewModel.logActivityFromHistory(activity)
            }
            .setNeutralButton(R.string.replace_input_neutral_button, null)
            .show()
    }

    private fun updatePhoneNumberInputType() {
        val inputText = phoneNumberInput.text.toString()
        val newInputType =
            if (inputText.isBlank() || phoneNumberHelper.doesTextMatchPhoneNumberRegex(inputText)) {
                InputType.TYPE_CLASS_PHONE
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            }
        phoneNumberInput.inputType = newInputType
    }

    private fun detectInputType(input: String): ChatInputType {
        if (input.isBlank()) return ChatInputType.EMPTY
        return if (phoneNumberHelper.containsPhoneNumbers(input)) {
            ChatInputType.PHONE_NUMBER
        } else {
            ChatInputType.USERNAME
        }
    }

    private fun showNoteDialog(detailedActivity: DetailedActivity) {
        NoteDialogHelper.showNoteDialog(this, detailedActivity) { activityId, note ->
            viewModel.updateNote(activityId, note)
        }
    }

    private fun onChatAppButtonClick(chatApp: ChatApp) {
        phoneNumberInputLayout.error = null
        val input = phoneNumberInput.text.toString()
        val inputType = detectInputType(input)

        when {
            inputType == ChatInputType.EMPTY -> {
                phoneNumberInputLayout.error = getString(R.string.error_empty_input)
            }
            inputType == ChatInputType.PHONE_NUMBER && chatApp.identifierType.supportsPhoneNumbers() -> {
                handlePhoneNumberLaunch(chatApp, input)
            }
            inputType == ChatInputType.USERNAME && chatApp.identifierType.supportsUsernames() -> {
                handleUsernameLaunch(chatApp, input)
            }
            else -> {
                phoneNumberInputLayout.error = getString(R.string.error_incompatible_input)
            }
        }
    }

    private fun handlePhoneNumberLaunch(chatApp: ChatApp, input: String) {
        val phoneNumbers = phoneNumberHelper.extractPhoneNumbers(input)
        if (phoneNumbers.isEmpty()) {
            phoneNumberInputLayout.error = getString(R.string.toast_invalid_phone_number)
        } else if (phoneNumbers.size != 1) {
            showPhoneNumberSelectionDialog(phoneNumbers) { selectedNumber ->
                launchChatAppWithPhoneNumber(chatApp, selectedNumber)
            }
        } else {
            launchChatAppWithPhoneNumber(chatApp, phoneNumbers.first())
        }
    }

    private fun handleUsernameLaunch(chatApp: ChatApp, input: String) {
        val usernames = usernameHelper.extractUsernames(input)
        if (usernames.isEmpty()) {
            phoneNumberInputLayout.error = getString(R.string.error_empty_input)
        } else if (usernames.size != 1) {
            showUsernameSelectionDialog(usernames) { selectedUsername ->
                launchChatAppWithUsername(chatApp, selectedUsername)
            }
        } else {
            launchChatAppWithUsername(chatApp, usernames.first())
        }
    }

    private fun launchChatAppWithPhoneNumber(chatApp: ChatApp, phoneNumber: String) {
        val possiblePhoneNumberWithCountryCode = viewModel.prefixCountryCode(phoneNumber)
        val message = messageInput.text.toString().trim()
        val intents = intentHelper.getChatAppIntentsForPhoneNumber(
            chatApp,
            possiblePhoneNumberWithCountryCode,
            message.ifBlank { null }
        )
        if (launchWithFallback(intents)) {
            viewModel.logAction(
                Action.Type.fromChatAppName(chatApp.name),
                possiblePhoneNumberWithCountryCode,
                message.ifBlank { null },
                phoneNumberInput.text.toString()
            )
        }
    }

    private fun launchChatAppWithUsername(chatApp: ChatApp, username: String) {
        val message = messageInput.text.toString().trim()
        val intents = intentHelper.getChatAppIntentsForUsername(
            chatApp,
            username,
            message.ifBlank { null }
        )
        if (launchWithFallback(intents)) {
            viewModel.logAction(
                Action.Type.fromChatAppName(chatApp.name),
                username,
                message.ifBlank { null },
                phoneNumberInput.text.toString()
            )
        }
    }

    private fun launchWithFallback(intents: IntentHelper.ChatAppLaunchIntents): Boolean {
        val appIntent = intents.appIntent
        val urlIntent = intents.urlIntent

        if (appIntent != null) {
            try {
                startActivity(appIntent)
                return true
            } catch (_: ActivityNotFoundException) {
                // App intent failed, try URL fallback
            }
        }

        if (urlIntent != null) {
            try {
                startActivity(urlIntent)
                return true
            } catch (_: ActivityNotFoundException) {
                // URL intent also failed
            }
        }

        showToast(R.string.toast_chat_app_not_installed)
        return false
    }

    companion object {
        const val INTENT_EXTRA_HISTORY = "intent_extra_history"

        fun getHistoryIntent(context: Context, activity: Activity): Intent {
            return Intent(context, MainActivity::class.java).apply {
                setAction(ACTION_VIEW)
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(INTENT_EXTRA_HISTORY, activity)
            }
        }
    }
}
