package org.vinaygopinath.launchchat.screens.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.ClipboardHelper
import org.vinaygopinath.launchchat.helpers.IntentHelper
import org.vinaygopinath.launchchat.helpers.PhoneNumberHelper
import org.vinaygopinath.launchchat.screens.main.domain.ProcessIntentUseCase
import javax.inject.Inject

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
    lateinit var processIntentUseCase: ProcessIntentUseCase

    private lateinit var phoneNumberInput: TextInputEditText
    private lateinit var phoneNumberInputLayout: TextInputLayout
    private lateinit var messageInput: EditText
    private lateinit var chooseContactButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeView()
        initializeObservers()

        viewModel.processIntent(intent, contentResolver)
    }

    private fun initializeView() {
        phoneNumberInputLayout = findViewById(R.id.phone_number_input_layout)
        phoneNumberInput = findViewById(R.id.phone_number_input)
        messageInput = findViewById(R.id.message_input)
        findViewById<MaterialButton>(R.id.paste_from_clipboard_button).setOnClickListener {
            val content = clipboardHelper.readClipboardContent()
            if (content is ClipboardHelper.ClipboardContent.ClipboardData) {
                phoneNumberInput.setText(content.content)
            }
        }
        chooseContactButton = findViewById(R.id.choose_from_contacts_button)
        findViewById<Button>(R.id.open_whatsapp_button).setOnClickListener {
            startActivityOrShowToast(R.string.toast_whatsapp_not_installed) { number, message ->
                intentHelper.getOpenWhatsappIntent(number, message.ifBlank { null })

            }
        }
        findViewById<Button>(R.id.open_signal_button).setOnClickListener {
            startActivityOrShowToast(R.string.toast_signal_not_installed) { phoneNumber, _ ->
                intentHelper.getOpenSignalIntent(phoneNumber)
            }
        }
        findViewById<Button>(R.id.open_telegram_button).setOnClickListener {
            startActivityOrShowToast(R.string.toast_telegram_not_installed) { phoneNumber, _ ->
                intentHelper.getOpenTelegramIntent(phoneNumber)
            }
        }
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    uiState.extractedContent?.let { handleExtractedContent(it) }
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

    private fun startActivityOrShowToast(
        @StringRes errorToast: Int,
        getButtonIntent: (phoneNumber: String, message: String) -> Intent
    ) {
        phoneNumberInputLayout.error = null
        val phoneNumbers = phoneNumberHelper.extractPhoneNumber(phoneNumberInput.text.toString())
        if (phoneNumbers.isEmpty()) {
            phoneNumberInputLayout.error = getString(R.string.toast_invalid_phone_number)
        } else if (phoneNumbers.size != 1) {
            showPhoneNumberSelectionDialog(phoneNumbers) { selectedNumber ->
                launchActivityIntent(errorToast, getButtonIntent, selectedNumber)
            }
        } else {
            launchActivityIntent(errorToast, getButtonIntent, phoneNumbers.first())
        }
    }

    private fun launchActivityIntent(
        @StringRes errorToast: Int,
        getButtonIntent: (phoneNumber: String, message: String) -> Intent,
        phoneNumber: String
    ) {
        val message = messageInput.text.toString().trim()
        try {
            startActivity(getButtonIntent(phoneNumber, message))
        } catch (e: ActivityNotFoundException) {
            showToast(errorToast)
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

        val phoneNumberList = dialogView.findViewById<ListView>(R.id.phone_number_selection_dialog_list)
        phoneNumberList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

        val dialog = builder.create()
        phoneNumberList.setOnItemClickListener { _, _, position, _ ->
            onNumberSelected(items[position])
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.processIntent(intent, contentResolver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            startActivity(intentHelper.getGithubRepoIntent())
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showToast(@StringRes toastResId: Int) {
        Toast.makeText(this, toastResId, Toast.LENGTH_LONG).show()
    }
}
