package org.vinaygopinath.launchchat.screens.history

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.vinaygopinath.launchchat.helpers.DetailedActivityHelper
import org.vinaygopinath.launchchat.models.DetailedActivity

class HistoryAdapterTest {
    private lateinit var adapter: HistoryAdapter
    private lateinit var selection: MutableSet<DetailedActivity>


    @Before
    fun setUp() {
        selection = mutableSetOf()
    }

    @Test
    fun selectItem_addsItemToSelection() {
        val item = mock(DetailedActivity::class.java)
        selection.add(item)
        assertEquals(1, selection.size)
    }

    @Test
    fun clearSelection_removesAllItems() {
        val item1 = mock(DetailedActivity::class.java)
        val item2 = mock(DetailedActivity::class.java)
        selection.add(item1)
        selection.add(item2)
        selection.clear()
        assertEquals(0, selection.size)
    }


}

