package org.vinaygopinath.launchchat.screens.history

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper

class HistoryAdapterTest {
    private lateinit var adapter: HistoryAdapter

    @Before
    fun setUp() {
        val detailedActivityHelper = mock(DetailedActivityHelper::class.java)
        val listener = mock(HistoryAdapter.HistoryClickListener::class.java)
        val selectionListener = mock(HistoryAdapter.SelectionListener::class.java)
        adapter = HistoryAdapter(
            detailedActivityHelper,
            listener,
            selectionListener
        )
    }

    @Test
    fun itemCount_isCorrect() {
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun onBindViewHolder_bindsDataCorrectly() {
        val viewHolder = mock(HistoryAdapter.HistoryViewHolder::class.java)
        adapter.onBindViewHolder(viewHolder, 0)
    }
}

