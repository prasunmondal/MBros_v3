package com.tech4bytes.mbrosv3.Utils.Android

import android.annotation.SuppressLint
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts


class SystemUtils : AppCompatActivity() {

    companion object {
        @SuppressLint("HardwareIds")
        fun getPhoneId(): String {
            return Settings.Secure.getString(
                AppContexts.get().contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }
    }
}