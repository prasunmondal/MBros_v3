package com.tech4bytes.mbrosv3

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.util.UUID

class DeviceIDUtil(private val context: Context) {

    companion object {
        private const val PREF_UNIQUE_ID = "PREF_UNIQUE_ID"
        private const val DEFAULT_ANDROID_ID = "9774d56d682e549c"
    }

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE)

    private val uniqueID: String
        get() {
            // Check if the unique ID is already stored in SharedPreferences
            val storedID = sharedPreferences.getString(PREF_UNIQUE_ID, null)
            if (storedID != null) {
                return storedID
            }

            // Generate a new unique ID
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            val newID: String = if (DEFAULT_ANDROID_ID == androidId) {
                // Generate a random UUID if the default value is detected
                UUID.randomUUID().toString()
            } else {
                // Generate a UUID based on the Android ID
                UUID.nameUUIDFromBytes(androidId.toByteArray()).toString()
            }

            // Store the unique ID in SharedPreferences
            sharedPreferences.edit().putString(PREF_UNIQUE_ID, newID).apply()
            return newID
        }

    fun getUniqueID(): String {
        return uniqueID
    }
}

