package org.vinaygopinath.launchchat.screens.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper
import org.vinaygopinath.launchchat.helpers.NoteDialogHelper
import org.vinaygopinath.launchchat.models.DetailedActivity
import org.vinaygopinath.launchchat.screens.main.MainActivity
import javax.inject.Inject

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {

    @Inject
    lateinit var detailedActivityHelper: DetailedActivityHelper

    private val viewModel: HistoryViewModel by viewModels()

    private val historyAdapter by lazy {
        HistoryAdapter(
            detailedActivityHelper,
            object : HistoryAdapter.HistoryAdapterListener {
                override fun onClick(detailedActivity: DetailedActivity) {
                    startActivity(
                        MainActivity.getHistoryIntent(
                            this@HistoryActivity,
                            detailedActivity.activity
                        )
                    )
                }

                override fun onItemSelectionChanged(selectedItems: Set<DetailedActivity>) {
                    toggleDeleteSelectedItemsMenuItem()
                }

                override fun onNoteButtonClick(detailedActivity: DetailedActivity) {
                    showNoteDialog(detailedActivity)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        initializeView()
        initializeObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_history, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val menuItem = menu.findItem(R.id.action_delete)
        val isItemVisibleNew = historyAdapter.hasSelectedItems()
        val isItemVisibleOld = menuItem.isVisible

        return if (isItemVisibleNew != isItemVisibleOld) {
            menuItem.isVisible = isItemVisibleNew
            true
        } else {
            super.onPrepareOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }

            android.R.id.home -> {
                if (historyAdapter.hasSelectedItems()) {
                    historyAdapter.clearSelection()
                    true
                } else {
                    finish()
                    false
                }
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initializeView() {
        with(findViewById<RecyclerView>(R.id.history_recycler_view)) {
            val linearLayoutManager = LinearLayoutManager(this@HistoryActivity)
            layoutManager = linearLayoutManager
            setPadding(resources.getDimensionPixelSize(R.dimen.padding_medium))
            adapter = historyAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@HistoryActivity,
                    linearLayoutManager.orientation
                )
            )
        }
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.detailedActivities.collectLatest { data ->
                        historyAdapter.submitData(data)
                    }
                }
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        when (uiState) {
                            is HistoryViewModel.UiState.DeleteSuccessful -> {
                                historyAdapter.clearSelection()
                                historyAdapter.refresh()
                            }

                            is HistoryViewModel.UiState.NoteUpdated -> {
                                historyAdapter.refresh()
                            }

                            is HistoryViewModel.UiState.None -> {
                                /* do nothing */
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_text)
            .setPositiveButton(R.string.delete_confirmation_positive_button) { _, _ -> deleteSelectedActivities() }
            .setNeutralButton(R.string.delete_confirmation_negative_button, null)
            .show()
    }

    private fun deleteSelectedActivities() {
        val selectedActivityIds = historyAdapter.getSelectedItems().map { it.activity.id }.toSet()
        viewModel.deleteSelectedActivitiesAndActions(selectedActivityIds)
    }

    private fun toggleDeleteSelectedItemsMenuItem() {
        invalidateOptionsMenu()
    }

    private fun showNoteDialog(detailedActivity: DetailedActivity) {
        NoteDialogHelper.showNoteDialog(this, detailedActivity) { activityId, note ->
            viewModel.updateNote(activityId, note)
        }
    }

    companion object {
        fun getIntent(context: Context): Intent = Intent(context, HistoryActivity::class.java)
    }
}
