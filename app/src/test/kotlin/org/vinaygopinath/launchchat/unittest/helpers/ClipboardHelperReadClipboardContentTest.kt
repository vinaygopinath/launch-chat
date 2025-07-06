package org.vinaygopinath.launchchat.unittest.helpers

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.vinaygopinath.launchchat.helpers.ClipboardHelper
import org.vinaygopinath.launchchat.helpers.ClipboardHelper.ClipboardContent

@RunWith(JUnit4::class)
class ClipboardHelperReadClipboardContentTest {

    private val manager = mock<ClipboardManager>()
    private val helper = ClipboardHelper(manager)

    @Test
    fun `returns empty when clipboard is empty`() {
        setUpMocks(hasPrimaryClip = false)

        val content = helper.readClipboardContent()

        assertThat(content).isEqualTo(ClipboardContent.Empty)
    }

    @Test
    fun `returns empty when description is empty`() {
        setUpMocks(hasPrimaryClip = true, hasDescription = false)

        val content = helper.readClipboardContent()

        assertThat(content).isEqualTo(ClipboardContent.Empty)
    }

    @Test
    fun `returns empty when clipboard does not contain any items`() {
        setUpMocks(hasPrimaryClip = true, hasDescription = true, itemCount = 0)

        val content = helper.readClipboardContent()

        assertThat(content).isEqualTo(ClipboardContent.Empty)
    }

    @Test
    fun `returns invalid type when description mime type is not plain text`() {
        setUpMocks(
            hasPrimaryClip = true,
            hasDescription = true,
            itemCount = 1,
            mimeType = ClipDescription.MIMETYPE_TEXT_HTML
        )

        val content = helper.readClipboardContent()

        assertThat(content).isEqualTo(ClipboardContent.InvalidClipboardData)
    }

    @Test
    fun `returns the clipboard text when clipboard data is valid`() {
        val copiedPhoneNumber = "+1555555555"
        setUpMocks(
            hasPrimaryClip = true,
            hasDescription = true,
            itemCount = 1,
            mimeType = ClipDescription.MIMETYPE_TEXT_PLAIN,
            firstItem = copiedPhoneNumber
        )

        val content = helper.readClipboardContent()

        assertThat(content is ClipboardContent.ClipboardData).isTrue()
        assertThat((content as ClipboardContent.ClipboardData).content).isEqualTo(copiedPhoneNumber)
    }

    private fun setUpMocks(
        hasPrimaryClip: Boolean = false,
        hasDescription: Boolean = false,
        mimeType: String = "",
        itemCount: Int = 0,
        firstItem: String = ""
    ) {
        if (hasPrimaryClip) {
            val mockItem = mock<ClipData.Item>().apply {
                whenever(text).thenReturn(firstItem)
            }
            val mockClipData = mock<ClipData>().apply {
                whenever(this.itemCount).thenReturn(itemCount)
                whenever(getItemAt(0)).thenReturn(mockItem)
            }
            whenever(manager.primaryClip).thenReturn(mockClipData)
        } else {
            whenever(manager.primaryClip).thenReturn(null)
        }

        if (hasDescription) {
            val mockDescription = mock<ClipDescription>().apply {
                `when`(hasMimeType(any())).thenAnswer { invocation ->
                    invocation.getArgument<String>(0) == mimeType
                }
            }
            whenever(manager.primaryClipDescription).thenReturn(mockDescription)
        } else {
            whenever(manager.primaryClipDescription).thenReturn(null)
        }
    }
}
