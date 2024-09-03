package com.tech4bytes.mbrosv3.Utils.Android

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
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.tech4bytes.mbrosv3.R
import com.prasunmondal.dev.libs.contexts.AppContexts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class UIUtils : AppCompatActivity() {

    companion object {

        fun getUIElementValue(view: View): String {
            when (view.javaClass.simpleName) {
                MaterialAutoCompleteTextView::class.simpleName -> return "" + (view as EditText).text
                MaterialTextView::class.simpleName,
                AppCompatTextView::class.simpleName,
                -> return "" + (view as TextView).text.toString()
                AppCompatEditText::class.simpleName -> return "" + (view as AppCompatEditText).text
                TextInputEditText::class.simpleName -> return "" + (view as TextInputEditText).text
                Switch::class.simpleName -> return "" + (view as Switch).isChecked.toString()
                AppCompatImageView::class.simpleName -> return (view.tag as String)
            }
            return ""
        }

        fun getTextOrHint(view: TextView): String {
            return if (view.text.toString() == "") {
                view.hint.toString()
            } else {
                view.text.toString()
            }
        }
//
//        fun setUIElementValue(view: View, value: String) {
//            setUIElementValue(AppContexts.get(), view, value)
//        }

        fun setUIElementValue(view: View, value: String) {
            val uIElement = view
            when (uIElement.javaClass.simpleName) {
                MaterialAutoCompleteTextView::class.simpleName,
                AppCompatTextView::class.simpleName,
                MaterialTextView::class.simpleName,
                -> (uIElement as TextView).text = value

                AppCompatEditText::class.simpleName -> (uIElement as EditText).setText(value)
                TextInputEditText::class.simpleName -> (uIElement as TextInputEditText).setText(value)

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
                    (uIElement as AppCompatImageView).setColorFilter(ContextCompat.getColor(AppContexts.get(), colorToSet))
                    uIElement.tag = tag
                }
            }
        }

        fun addDebouncedOnTextChangeListener(inputField: TextView, func: () -> Unit) {
            val debouncePeriod: Long = 300 // Delay in milliseconds
            var debounceJob: Job? = null

            inputField.doOnTextChanged { text, _, _, _ ->
                debounceJob?.cancel() // Cancel the previous debounce job
                debounceJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(debouncePeriod) // Wait for the debounce period
                    func.invoke()
                }
            }
        }
    }
}