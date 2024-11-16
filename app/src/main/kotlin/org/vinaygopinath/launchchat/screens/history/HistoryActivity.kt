package org.vinaygopinath.launchchat.screens.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

    private lateinit var recyclerView: RecyclerView

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
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            RecyclerView(this).apply {
                layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.MATCH_PARENT
                )
            }.also {
                recyclerView = it
            }
        )

        initializeView()
        initializeObservers()
    }

    private fun initializeView() {
        with(recyclerView) {
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