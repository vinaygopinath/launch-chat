package org.vinaygopinath.launchchat.screens.chatapps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.ChatAppHelper
import org.vinaygopinath.launchchat.models.ChatApp
import javax.inject.Inject

@AndroidEntryPoint
class ChatAppListActivity : AppCompatActivity() {

    @Inject
    lateinit var chatAppHelper: ChatAppHelper

    private val viewModel: ChatAppListViewModel by viewModels()

    private val adapter by lazy {
        ChatAppListAdapter(
            chatAppHelper,
            object : ChatAppListAdapter.ChatAppListAdapterListener {
                override fun onClick(chatApp: ChatApp) {
                    navigateToEditor(chatApp.id)
                }

                override fun onEnabledToggled(chatApp: ChatApp, isEnabled: Boolean) {
                    viewModel.toggleEnabled(chatApp, isEnabled)
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_app_list)

        initializeToolbar()
        initializeRecyclerView()
        initializeFab()
        initializeObservers()
    }

    private fun initializeToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setTitle(R.string.chat_app_list_title)
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

    private fun initializeRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.chat_app_list_recycler_view)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, linearLayoutManager.orientation)
        )
    }

    private fun initializeFab() {
        val fab = findViewById<FloatingActionButton>(R.id.chat_app_list_fab)
        fab.setOnClickListener {
            navigateToEditor(null)
        }
    }

    private fun initializeObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatApps.collectLatest { chatApps ->
                    adapter.submitList(chatApps)
                }
            }
        }
    }

    private fun navigateToEditor(chatAppId: Long?) {
        startActivity(ChatAppEditorActivity.getIntent(this, chatAppId))
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, ChatAppListActivity::class.java)
        }
    }
}
