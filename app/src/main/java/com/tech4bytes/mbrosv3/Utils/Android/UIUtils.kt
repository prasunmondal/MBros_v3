package com.tech4bytes.mbrosv3.Utils.Android

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe


class UIUtils: AppCompatActivity() {

    companion object {

        fun getUIElementValue(view: View): String {
            LogMe.log("Getting value from: " + view.javaClass.simpleName)
            when (view.javaClass.simpleName) {
                MaterialAutoCompleteTextView::class.simpleName -> return "" + (view as EditText).text
                MaterialTextView::class.simpleName -> return "" + (view as MaterialTextView).text.toString()
                AppCompatEditText::class.simpleName -> return "" + (view as AppCompatEditText).text
                TextInputEditText::class.simpleName -> return "" + (view as TextInputEditText).text
                Switch::class.simpleName -> return "" + (view as Switch).isChecked.toString()
                AppCompatImageView::class.simpleName -> return (view.tag as String)
            }
            return ""
        }

        fun setUIElementValue(context: Context, view: View, value: String) {
            var uIElement = view
            when (uIElement.javaClass.simpleName) {
                MaterialAutoCompleteTextView::class.simpleName -> (uIElement as MaterialAutoCompleteTextView).setText(value)
                MaterialTextView::class.simpleName -> (uIElement as MaterialTextView).text = value
                AppCompatEditText::class.simpleName -> (uIElement as EditText).setText(value)
                Switch::class.simpleName -> (uIElement as Switch).isChecked = value.toBoolean()
                SwitchCompat::class.simpleName -> (uIElement as SwitchCompat).isChecked = value.toBoolean()
                AppCompatImageView::class.simpleName -> {
                    var colorToSet = R.color.button_disabled
                    var tag = false.toString()
                    when (value.toBoolean()) {
                        true -> {
                            colorToSet = R.color.button_enabled
                            tag = true.toString()
                        }
                        else -> {
                            var colorToSet = R.color.button_disabled
                            var tag = false.toString()
                        }
                    }
                    (uIElement as AppCompatImageView).setColorFilter(ContextCompat.getColor(context, colorToSet))
                    uIElement.tag = tag
                }
            }
        }
    }
}