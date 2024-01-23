package com.tech4bytes.mbrosv3.Utils.Android

import android.view.View
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.function.Consumer
import kotlin.math.pow

class MeteredNumbers {
    val firstNumberView: EditText
    val secondNumberView: EditText
    val secondNumberLength: Int
    var initialValue: Int = 0

    constructor(firstNumberView: EditText, secondNumberView: EditText, secondNumberLength: Int) {
        this.firstNumberView = firstNumberView
        this.secondNumberView = secondNumberView
        this.secondNumberLength = secondNumberLength
    }

    private fun getFirstPartOfNumber(number: Int): Int {
        val exponent: Int = 10.0.pow(secondNumberLength.toDouble()).toInt()
        val firstPart = (number / exponent).toString()
        return NumberUtils.getIntOrZero(firstPart)
    }

    private fun getSecondPartOfNumber(number: Int): Int {
        val exponent: Int = 10.0.pow(secondNumberLength.toDouble()).toInt()
        val secondPart = (number % exponent).toString()
        return NumberUtils.getIntOrZero(secondPart)
    }

    fun setNumber(number: Int, secondPartOnChangeListeners: Consumer<View>? = null) {
        initialValue = number
        val firstNumber = getFirstPartOfNumber(number)
        val secondNumber = getSecondPartOfNumber(number)
        firstNumberView.setText(NumberUtils.getIntOrBlank(firstNumber.toString()))
        secondNumberView.setText(NumberUtils.getIntOrBlank(secondNumber.toString()))

        if(secondPartOnChangeListeners != null)
        {
            setListeners(secondPartOnChangeListeners)
        }
    }

    fun setListeners(secondPartOnChangeListeners: Consumer<View>? = null) {
        secondNumberView.doOnTextChanged { text, start, before, count ->
            checkAndUpdateFirstPart()
            secondPartOnChangeListeners?.accept(secondNumberView)
        }
    }

    private fun checkAndUpdateFirstPart() {
        val kmSecondPart = secondNumberView.text.toString()
        if(kmSecondPart.length < secondNumberLength) {
            firstNumberView.setText(getFirstPartOfNumber(initialValue).toString())
            // mark the second part as error
            return
        }

        if(getNumber() != null && getNumber()!! < initialValue) {
            firstNumberView.setText((NumberUtils.getIntOrZero(firstNumberView.text.toString()) + 1).toString())
        }
    }

    fun getNumber(): Int? {
        if(secondNumberView.text.length < secondNumberLength) {
            return null
        }
        return NumberUtils.getIntOrZero(firstNumberView.text.toString() + secondNumberView.text.toString())
    }
}