package com.tech4bytes.mbrosv3.Utils.ContactsUtils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log


object Contacts {
    var contactList: ArrayList<Contact> = ArrayList()

    private val PROJECTION = arrayOf(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    fun getNumberByName(name: String, returnValueIfNotFound: String? = null): String? {
        contactList.forEach {
            if (it.name == name)
                return it.phoneNumber
        }
        return returnValueIfNotFound
    }

    fun getNameByNumber(number: String, returnValueIfNotFound: String? = null): String? {
        contactList.forEach {
            if (it.phoneNumber == number)
                return it.name
        }
        return returnValueIfNotFound
    }

    fun getContactList(context: Context, useCache: Boolean = true) {
        if (useCache && contactList.isNotEmpty())
            return

        val cr: ContentResolver = context.contentResolver
        val cursor: Cursor? = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            PROJECTION,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        if (cursor != null) {
            val mobileNoSet = HashSet<String>()
            try {
                val nameIndex: Int = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex: Int =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                var name: String
                var number: String
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex)
                    number = cursor.getString(numberIndex)
                    number = number.replace(" ", "")
                    if (!mobileNoSet.contains(number)) {
                        contactList.add(Contact(name, number))
                        mobileNoSet.add(number)
                        Log.d(
                            "hvy", "onCreaterrView  Phone Number: name = " + name
                                    + " No = " + number
                        )
                    }
                }
            } finally {
                cursor.close()
            }
        }
    }
}
