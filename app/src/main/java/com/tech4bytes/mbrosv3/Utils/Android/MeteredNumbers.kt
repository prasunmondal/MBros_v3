package com.tech4bytes.mbrosv3.Utils.Android

import android.view.View
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import org.apache.commons.lang3.StringUtils
import java.util.function.Consumer

class MeteredNumbers {
    val firstNumberView: EditText
    val secondNumberView: EditText
    val secondNumberLength: Int
    var startingValue: Int = 0

    constructor(firstNumberView: EditText, secondNumberView: EditText, secondNumberLength: Int) {
        this.firstNumberView = firstNumberView
        this.secondNumberView = secondNumberView
        this.secondNumberLength = secondNumberLength
    }

    private fun getFirstPartOfNumber(number: Int): String {
        return StringUtils.left(number.toString(), number.toString().length - secondNumberLength)
//        val exponent: Int = 10.0.pow(secondNumberLength.toDouble()).toInt()
//        val firstPart = (number / exponent).toString()
//        return NumberUtils.getIntOrZero(firstPart).toString()
    }

    private fun getSecondPartOfNumber(number: Int): String {
        return StringUtils.right(number.toString(), secondNumberLength)
    }

    fun setNumber(number: Int, updateStartingValue: Boolean, secondPartOnChangeListeners: Consumer<View>? = null) {
        if(updateStartingValue)
            startingValue = number

        val firstNumber = getFirstPartOfNumber(number)
        val secondNumber = getSecondPartOfNumber(number)
        firstNumberView.setText(firstNumber)
        secondNumberView.setText(secondNumber)

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
            firstNumberView.setText(getFirstPartOfNumber(startingValue).toString())
            // mark the second part as error
            return
        }

        if(getNumber() != null && getNumber()!! < startingValue) {
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