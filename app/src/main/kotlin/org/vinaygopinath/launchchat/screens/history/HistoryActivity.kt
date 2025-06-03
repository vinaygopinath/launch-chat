package org.vinaygopinath.launchchat.screens.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
    private var actionMode: ActionMode? = null
    private val selectedItems = mutableSetOf<DetailedActivity>()

    @Inject
    lateinit var detailedActivityHelper: DetailedActivityHelper

    private val historyAdapter by lazy {
        HistoryAdapter(
            detailedActivityHelper,
            object : HistoryAdapter.HistoryClickListener {
                override fun onClick(detailedActivity: DetailedActivity) {
                    if (actionMode != null) {
                        toggleSelection(detailedActivity)
                    } else {
                        startActivity(
                            MainActivity.getHistoryIntent(
                                this@HistoryActivity,
                                detailedActivity.activity
                            )
                        )
                    }
                }

    fun onLongClick(detailedActivity: DetailedActivity): Boolean{
        if (actionMode == null){
            actionMode = startActionMode(actionModeCallback)
        }
        toggleSelection(detailedActivity)
        return true
    }

    private val actionModeCallback = object : ActionMode.Callback {
         override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.menu_history_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

         override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            if (item?.itemId == R.id.action_delete) {
                deleteSelectedItems()
                mode?.finish()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            selectedItems.clear()
            historyAdapter.clearSelection()
            actionMode = null
        }
    }

    private fun toggleSelection(item: DetailedActivity) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        historyAdapter.toggleSelection(item)
        actionMode?.title = "${selectedItems.size} geselecteerd"
        if (selectedItems.isEmpty()) {
            actionMode?.finish()
        }
    }

    private fun deleteSelectedItems() {
        // Verwijder de geselecteerde items uit de dataset
        viewModel.deleteActivities(selectedItems)
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
            viewModel.detailedActivities.collectLatest { pagingData ->
                historyAdapter.submitData(pagingData)
            }
        }
    }

    companion object {
        fun getIntent(context: Context): Intent = Intent(context, HistoryActivity::class.java)
    }


}