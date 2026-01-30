package org.vinaygopinath.launchchat.helpers

import android.content.ContentResolver
import android.content.res.Resources
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import org.vinaygopinath.launchchat.R
import javax.inject.Inject

class ContactHelper @Inject constructor(
    private val resources: Resources,
    private val phoneNumberHelper: PhoneNumberHelper
) {

    data class ContactPhoneNumber(
        val number: String,
        val type: String?
    )

    fun getPhoneNumbersFromContactUri(
        contentResolver: ContentResolver,
        contactUri: Uri
    ): List<ContactPhoneNumber> {
        val contactId = getContactId(contentResolver, contactUri) ?: return emptyList()
        return getPhoneNumbersForContactId(contentResolver, contactId)
    }

    private fun getContactId(contentResolver: ContentResolver, contactUri: Uri): String? {
        val projection = arrayOf(ContactsContract.Contacts._ID)
        contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                if (idIndex >= 0) {
                    return cursor.getString(idIndex)
                }
            }
        }
        return null
    }

    private fun getPhoneNumbersForContactId(
        contentResolver: ContentResolver,
        contactId: String
    ): List<ContactPhoneNumber> {
        val phoneNumbers = mutableListOf<ContactPhoneNumber>()
        val seenNormalizedNumbers = mutableSetOf<String>()

        val projection = arrayOf(
            CommonDataKinds.Phone.NUMBER,
            CommonDataKinds.Phone.TYPE,
            CommonDataKinds.Phone.LABEL
        )
        val selection = "${CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)

        contentResolver.query(
            CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)
            val typeIndex = cursor.getColumnIndex(CommonDataKinds.Phone.TYPE)
            val labelIndex = cursor.getColumnIndex(CommonDataKinds.Phone.LABEL)

            while (cursor.moveToNext()) {
                val number = if (numberIndex >= 0) cursor.getString(numberIndex) else null
                val type = if (typeIndex >= 0) cursor.getInt(typeIndex) else null
                val label = if (labelIndex >= 0) cursor.getString(labelIndex) else null

                addPhoneNumberIfUnique(number, type, label, seenNormalizedNumbers, phoneNumbers)
            }
        }
        return phoneNumbers
    }

    private fun addPhoneNumberIfUnique(
        number: String?,
        type: Int?,
        label: String?,
        seenNormalizedNumbers: MutableSet<String>,
        phoneNumbers: MutableList<ContactPhoneNumber>
    ) {
        if (number.isNullOrBlank()) return

        val normalizedNumber = phoneNumberHelper.normalizePhoneNumber(number)
        if (seenNormalizedNumbers.add(normalizedNumber)) {
            val typeLabel = getPhoneTypeLabel(type, label)
            phoneNumbers.add(ContactPhoneNumber(number, typeLabel))
        }
    }

    private fun getPhoneTypeLabel(type: Int?, customLabel: String?): String? {
        return when (type) {
            CommonDataKinds.Phone.TYPE_HOME -> resources.getString(R.string.contact_phone_type_home)
            CommonDataKinds.Phone.TYPE_MOBILE -> resources.getString(R.string.contact_phone_type_mobile)
            CommonDataKinds.Phone.TYPE_WORK -> resources.getString(R.string.contact_phone_type_work)
            CommonDataKinds.Phone.TYPE_WORK_MOBILE -> resources.getString(R.string.contact_phone_type_work_mobile)
            CommonDataKinds.Phone.TYPE_OTHER -> resources.getString(R.string.contact_phone_type_other)
            CommonDataKinds.Phone.TYPE_CUSTOM -> customLabel
            else -> null
        }
    }
}
