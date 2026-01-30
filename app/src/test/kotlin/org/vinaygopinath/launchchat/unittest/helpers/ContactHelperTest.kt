package org.vinaygopinath.launchchat.unittest.helpers

import android.content.ContentResolver
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.vinaygopinath.launchchat.R
import org.vinaygopinath.launchchat.helpers.ContactHelper
import org.vinaygopinath.launchchat.helpers.PhoneNumberHelper

@RunWith(RobolectricTestRunner::class)
class ContactHelperTest {

    private val contentResolver = mock<ContentResolver>()
    private val contactUri = mock<Uri>()
    private val resources = mock<Resources>()
    private val phoneNumberHelper = mock<PhoneNumberHelper>()
    private val contactHelper = ContactHelper(resources, phoneNumberHelper)

    @Before
    fun setUp() {
        // Default setup - no contact found
        whenever(contentResolver.query(eq(contactUri), any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(null)
        whenever(
            contentResolver.query(
                eq(ContactsContract.CommonDataKinds.Phone.CONTENT_URI),
                any(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        )
            .thenReturn(null)

        // Setup resources mock for phone type labels
        whenever(resources.getString(R.string.contact_phone_type_home)).thenReturn("Home")
        whenever(resources.getString(R.string.contact_phone_type_mobile)).thenReturn("Mobile")
        whenever(resources.getString(R.string.contact_phone_type_work)).thenReturn("Work")
        whenever(resources.getString(R.string.contact_phone_type_work_mobile)).thenReturn("Work Mobile")
        whenever(resources.getString(R.string.contact_phone_type_other)).thenReturn("Other")

        // Setup phoneNumberHelper mock to normalize by keeping only digits and +
        whenever(phoneNumberHelper.normalizePhoneNumber(any())).thenAnswer { invocation ->
            val number = invocation.getArgument<String>(0)
            number.filter { it.isDigit() || it == '+' }
        }
    }

    @Test
    fun `returns empty list when contact ID cannot be retrieved`() {
        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).isEmpty()
    }

    @Test
    fun `returns empty list when contact has no phone numbers`() {
        setupContactIdAndPhoneNumbers("123", emptyList())

        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).isEmpty()
    }

    @Test
    fun `returns single phone number when contact has one number`() {
        val phoneNumber = "+1555123456"
        setupContactIdAndPhoneNumbers(
            "123",
            listOf(
                PhoneNumberData(phoneNumber, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null)
            )
        )

        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).hasSize(1)
        assertThat(result[0].number).isEqualTo(phoneNumber)
        assertThat(result[0].type).isEqualTo("Mobile")
    }

    @Test
    fun `returns multiple phone numbers when contact has several`() {
        val mobileNumber = "+1555123456"
        val workNumber = "+1555654321"
        setupContactIdAndPhoneNumbers(
            "123",
            listOf(
                PhoneNumberData(mobileNumber, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null),
                PhoneNumberData(workNumber, ContactsContract.CommonDataKinds.Phone.TYPE_WORK, null)
            )
        )

        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).hasSize(2)
        assertThat(result[0].number).isEqualTo(mobileNumber)
        assertThat(result[0].type).isEqualTo("Mobile")
        assertThat(result[1].number).isEqualTo(workNumber)
        assertThat(result[1].type).isEqualTo("Work")
    }

    @Test
    fun `returns home type label for home phone number`() {
        setupContactIdAndPhoneNumbers(
            "123",
            listOf(
                PhoneNumberData("+1555111111", ContactsContract.CommonDataKinds.Phone.TYPE_HOME, null)
            )
        )

        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).hasSize(1)
        assertThat(result[0].type).isEqualTo("Home")
    }

    @Test
    fun `returns custom label for custom phone type`() {
        val customLabel = "Emergency"
        setupContactIdAndPhoneNumbers(
            "123",
            listOf(
                PhoneNumberData("+1555111111", ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM, customLabel)
            )
        )

        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).hasSize(1)
        assertThat(result[0].type).isEqualTo(customLabel)
    }

    @Test
    fun `skips blank phone numbers`() {
        setupContactIdAndPhoneNumbers(
            "123",
            listOf(
                PhoneNumberData("", ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null),
                PhoneNumberData("+1555123456", ContactsContract.CommonDataKinds.Phone.TYPE_WORK, null)
            )
        )

        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).hasSize(1)
        assertThat(result[0].number).isEqualTo("+1555123456")
    }

    @Test
    fun `deduplicates phone numbers with different formatting`() {
        val formattedNumber = "+1 555 123 456"
        val strippedNumber = "+1555123456"
        setupContactIdAndPhoneNumbers(
            "123",
            listOf(
                PhoneNumberData(formattedNumber, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null),
                PhoneNumberData(strippedNumber, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null)
            )
        )

        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).hasSize(1)
        // Should keep the first occurrence (formatted number)
        assertThat(result[0].number).isEqualTo(formattedNumber)
    }

    @Test
    fun `keeps distinct phone numbers even with similar formatting`() {
        val number1 = "+1 555 123 456"
        val number2 = "+1 555 654 321"
        setupContactIdAndPhoneNumbers(
            "123",
            listOf(
                PhoneNumberData(number1, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, null),
                PhoneNumberData(number2, ContactsContract.CommonDataKinds.Phone.TYPE_WORK, null)
            )
        )

        val result = contactHelper.getPhoneNumbersFromContactUri(contentResolver, contactUri)

        assertThat(result).hasSize(2)
        assertThat(result[0].number).isEqualTo(number1)
        assertThat(result[1].number).isEqualTo(number2)
    }

    private fun setupContactIdAndPhoneNumbers(contactId: String, phoneNumbers: List<PhoneNumberData>) {
        // Create contact ID cursor
        val contactCursor = createContactIdCursor(contactId)
        whenever(contentResolver.query(eq(contactUri), any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(contactCursor)

        // Create phone numbers cursor
        val phoneCursor = createPhoneNumbersCursor(phoneNumbers)
        whenever(
            contentResolver.query(
                eq(ContactsContract.CommonDataKinds.Phone.CONTENT_URI),
                any(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        )
            .thenReturn(phoneCursor)
    }

    private fun createContactIdCursor(contactId: String): Cursor {
        return mock<Cursor>().apply {
            whenever(moveToFirst()).thenReturn(true)
            whenever(getColumnIndex(ContactsContract.Contacts._ID)).thenReturn(0)
            whenever(getString(0)).thenReturn(contactId)
        }
    }

    private fun createPhoneNumbersCursor(phoneNumbers: List<PhoneNumberData>): Cursor {
        return mock<Cursor>().apply {
            var currentIndex = -1

            whenever(moveToNext()).thenAnswer {
                currentIndex++
                currentIndex < phoneNumbers.size
            }
            whenever(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).thenReturn(0)
            whenever(getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)).thenReturn(1)
            whenever(getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)).thenReturn(2)

            whenever(getString(0)).thenAnswer {
                if (currentIndex >= 0 && currentIndex < phoneNumbers.size) {
                    phoneNumbers[currentIndex].number
                } else {
                    null
                }
            }
            whenever(getInt(1)).thenAnswer {
                if (currentIndex >= 0 && currentIndex < phoneNumbers.size) {
                    phoneNumbers[currentIndex].type
                } else {
                    0
                }
            }
            whenever(getString(2)).thenAnswer {
                if (currentIndex >= 0 && currentIndex < phoneNumbers.size) {
                    phoneNumbers[currentIndex].label
                } else {
                    null
                }
            }
        }
    }

    private data class PhoneNumberData(
        val number: String,
        val type: Int,
        val label: String?
    )
}
