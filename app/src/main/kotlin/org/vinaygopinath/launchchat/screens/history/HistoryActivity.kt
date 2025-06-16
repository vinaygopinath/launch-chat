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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper
import org.vinaygopinath.launchchat.models.DetailedActivity
import org.vinaygopinath.launchchat.screens.main.MainActivity
import javax.inject.Inject

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity() {


    private val viewModel: HistoryViewModel by viewModels()
    private var isSelectionMode = false

    private fun enterSelectionMode(selected: DetailedActivity){
        isSelectionMode = true
        historyAdapter.selectItem(selected)
        invalidateOptionsMenu()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        historyAdapter.clearSelection()
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history_menu, menu)
        menu?.findItem(R.id.action_delete)?.isVisible = isSelectionMode
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                showDeleteConfirmationDialog()
                true
            }
            android.R.id.home -> {
                exitSelectionMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_text)
            .setPositiveButton(R.string.delete_confirmation_positive_button) { _, _ -> deleteSelectedEntries() }
            .setNeutralButton(R.string.delete_confirmation_negative_button, null)
            .show()
    }

    private fun deleteSelectedEntries() {
        val selected = historyAdapter.getSelectedItems()
        lifecycleScope.launch {
            viewModel.deleteActivitiesAndActions(selected)
            exitSelectionMode()
        }
    }

    @Inject
    lateinit var detailedActivityHelper: DetailedActivityHelper

    private val historyAdapter by lazy {
        HistoryAdapter(
            detailedActivityHelper,
            object : HistoryAdapter.HistoryClickListener {
                override fun onClick(detailedActivity: DetailedActivity) {
                    startActivity(
                        MainActivity.getHistoryIntent(
                            this@HistoryActivity,
                            detailedActivity.activity
                        )
                    )
                }
            },
            selectionListener = object : HistoryAdapter.SelectionListener{
                override fun onSelectionChanged(selectedCount: Int) {
                    invalidateOptionsMenu()
                }

                override fun onItemLongPress(detailedActivity: DetailedActivity) {
                    enterSelectionMode(detailedActivity)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        initializeView()
        initializeObservers()
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
            viewModel.detailedActivities.collectLatest { data ->
                historyAdapter.submitData(data)
            }
        }
    }

    companion object {
        fun getIntent(context: Context): Intent = Intent(context, HistoryActivity::class.java)
    }
}