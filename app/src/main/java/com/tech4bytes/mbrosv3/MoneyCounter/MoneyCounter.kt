package com.tech4bytes.mbrosv3.MoneyCounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textview.MaterialTextView
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerCalculations
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class MoneyCounter : AppCompatActivity() {

    private val availableDenominations: List<Int> = listOf(2000, 500, 200, 100, 50, 20, 10)
    private lateinit var deductedCashField: EditText
    private lateinit var addedCashField: EditText
    private val mapOfNotesToAmount: MutableMap<Int, Int> = mutableMapOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_counter)

        AppContexts.set(this)
        AppUtils.logError()
        initializeUI()
    }

    private fun initializeUI() {
        val container = findViewById<LinearLayout>(R.id.mc_entry_containers)
        deductedCashField = findViewById(R.id.mc_deducted_amount)
        addedCashField = findViewById(R.id.mc_added_amount)

        deductedCashField.addTextChangedListener { setAimingAmount() }
        addedCashField.addTextChangedListener { setAimingAmount() }

        setAimingAmount()
        availableDenominations.forEach {
            mapOfNotesToAmount[it] = 0
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_money_counter_fragment, null)
            val denomination = entry.findViewById<MaterialTextView>(R.id.mc_denomination)
            val newNoteField = entry.findViewById<EditText>(R.id.mc_newNote)
            val oldNoteField = entry.findViewById<EditText>(R.id.mc_oldNote)
            val multipliedAmountField = entry.findViewById<MaterialTextView>(R.id.mc_multipliedAmount)

            denomination.text = it.toString()
            newNoteField.addTextChangedListener { updateMultipliedAmount(denomination, newNoteField, oldNoteField, multipliedAmountField) }
            oldNoteField.addTextChangedListener { updateMultipliedAmount(denomination, newNoteField, oldNoteField, multipliedAmountField) }
            container.addView(entry)
        }


    }

    private fun setAimingAmount() {
        val aimingAmountField = findViewById<EditText>(R.id.mc_aiming_amount)
        val totalAmountPaidByCustomer = DeliverToCustomerCalculations.getTotalAmountPaidTodayByCustomers()
        val extraExpenses = NumberUtils.getIntOrZero(SingleAttributedData.getRecords().extra_expenses)
        val cashGivenForExtraExpenses = NumberUtils.getIntOrZero(SingleAttributedData.getRecords().extra_cash_given)
        val labourExpenses = NumberUtils.getIntOrZero(SingleAttributedData.getRecords().labour_expenses) + 300
        val deductedCash = NumberUtils.getIntOrZero(deductedCashField.text.toString())
        val addedCash = NumberUtils.getIntOrZero(addedCashField.text.toString())

        val aimingAmount = (
                totalAmountPaidByCustomer
                + (cashGivenForExtraExpenses - extraExpenses)
                - labourExpenses
                - deductedCash
                + addedCash
                )
        aimingAmountField.setText(aimingAmount.toString())
    }

    private fun updateMultipliedAmount(denomination: MaterialTextView, newNoteField: EditText, oldNoteField: EditText, multipliedAmountField: MaterialTextView) {
        val denominationAmount = NumberUtils.getIntOrZero(denomination.text.toString())
        val numberOfNewNotes = NumberUtils.getIntOrZero(newNoteField.text.toString())
        val numberOfOldNotes = NumberUtils.getIntOrZero(oldNoteField.text.toString())
        val multipliedAmount = denominationAmount * (numberOfNewNotes + numberOfOldNotes)
        multipliedAmountField.text = "₹ $multipliedAmount"
        mapOfNotesToAmount[denominationAmount] = multipliedAmount
        updateTotalAmount()
    }

    private fun updateTotalAmount() {
        var totalAmount = 0
        mapOfNotesToAmount.forEach { (key, value) ->
            totalAmount += value
        }
        findViewById<MaterialTextView>(R.id.mc_totalAmount).text = "₹ $totalAmount"
    }
}