package com.tech4bytes.mbrosv3.MoneyCounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class MoneyCounter : AppCompatActivity() {

    private val availableDenominations: List<Int> = listOf(2000, 500, 200, 100, 50, 20, 10)

    private val mapOfNotesToAmount: MutableMap<Int, Int> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_counter)

        AppContexts.set(this)
        AppUtils.logError()
        initializeUI()
    }

    private fun initializeUI() {
        availableDenominations.forEach {
            mapOfNotesToAmount[it] = 0
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_money_counter_fragment, null)
            val denomination = entry.findViewById<EditText>(R.id.mc_denomination)
            val newNoteField = entry.findViewById<EditText>(R.id.mc_newNote)
            val oldNoteField = entry.findViewById<EditText>(R.id.mc_oldNote)
            val multipliedAmountField = entry.findViewById<EditText>(R.id.mc_multipliedAmount)

            denomination.setText(it.toString())
            newNoteField.addTextChangedListener { updateMultipliedAmount(denomination, newNoteField, oldNoteField, multipliedAmountField) }
            oldNoteField.addTextChangedListener { updateMultipliedAmount(denomination, newNoteField, oldNoteField, multipliedAmountField) }
        }
    }

    private fun updateMultipliedAmount(denomination: EditText, newNoteField: EditText, oldNoteField: EditText, multipliedAmountField: EditText) {
        val denomination = NumberUtils.getIntOrZero(denomination.text.toString())
        val numberOfNewNotes = NumberUtils.getIntOrZero(newNoteField.text.toString())
        val numberOfOldNotes = NumberUtils.getIntOrZero(oldNoteField.text.toString())
        val multipliedAmount = denomination * (numberOfNewNotes + numberOfOldNotes)
        multipliedAmountField.setText("$multipliedAmount")
        mapOfNotesToAmount[denomination] = multipliedAmount
    }
}