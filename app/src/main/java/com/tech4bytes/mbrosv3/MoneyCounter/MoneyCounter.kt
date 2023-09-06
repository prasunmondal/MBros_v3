package com.tech4bytes.mbrosv3.MoneyCounter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textview.MaterialTextView
import com.tech4bytes.mbrosv3.AppData.AppUtils
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedData
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerCalculations
import com.tech4bytes.mbrosv3.Login.ActivityLogin
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import kotlin.math.abs

class MoneyCounter : AppCompatActivity() {

    private val availableDenominations: List<Int> = listOf(2000, 500, 200, 100, 50, 20, 10)
    private lateinit var deductedCashField: EditText
    private lateinit var addedCashField: EditText
    private val mapOfNotesToAmount: MutableMap<Int, Int> = mutableMapOf()
    private val rupeePrefix = "₹"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_money_counter)
        AppContexts.set(this)
        AppUtils.logError()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        initializeUI()
    }

    private fun initializeUI() {
        val container = findViewById<LinearLayout>(R.id.mc_entry_containers)
        deductedCashField = findViewById(R.id.mc_deducted_amount)
        addedCashField = findViewById(R.id.mc_added_amount)
        val aimingAmountField = findViewById<EditText>(R.id.mc_aiming_amount)

        deductedCashField.addTextChangedListener { setAimingAmount() }
        addedCashField.addTextChangedListener { setAimingAmount() }
        aimingAmountField.addTextChangedListener { updateSuccessColors() }

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
        updateTotalAmount()
    }

    private fun setAimingAmount() {
        val aimingAmountField = findViewById<EditText>(R.id.mc_aiming_amount)
        val totalAmountPaidByCustomer = DeliverToCustomerCalculations.getTotalAmountPaidTodayByCustomers()
        val extraExpenses = NumberUtils.getIntOrZero(SingleAttributedData.getRecords().extra_expenses)
        val cashGivenForExtraExpenses = NumberUtils.getIntOrZero(SingleAttributedData.getRecords().extra_cash_given)
        val labourExpenses = NumberUtils.getIntOrZero(SingleAttributedData.getRecords().labour_expenses) + NumberUtils.getIntOrZero(AppConstants.get(AppConstants.DRIVER_SALARY))
        val deductedCash = NumberUtils.getIntOrZero(deductedCashField.text.toString())
        val addedCash = NumberUtils.getIntOrZero(addedCashField.text.toString())
        val fuelExpense =
            if (SingleAttributedData.getRecords().did_refueled.toBoolean())
                NumberUtils.getIntOrZero(SingleAttributedData.getRecords().refueling_amount)
            else 0

        val aimingAmount = (
                totalAmountPaidByCustomer
                        + (cashGivenForExtraExpenses - extraExpenses)
                        - labourExpenses
                        - deductedCash
                        + addedCash
                        - fuelExpense
                )
        aimingAmountField.setText(aimingAmount.toString())
    }

    fun getAimingAmountFromUI(): Int {
        val aimingAmountField = findViewById<EditText>(R.id.mc_aiming_amount)
        return NumberUtils.getIntOrZero(aimingAmountField.text.toString())
    }

    fun getCalculatedTotalAmountFromUI(): Int {
        return NumberUtils.getIntOrZero(findViewById<MaterialTextView>(R.id.mc_totalAmount).text.toString().replace(rupeePrefix, "").trim())
    }

    private fun updateMultipliedAmount(denomination: MaterialTextView, newNoteField: EditText, oldNoteField: EditText, multipliedAmountField: MaterialTextView) {
        val denominationAmount = NumberUtils.getIntOrZero(denomination.text.toString())
        val numberOfNewNotes = NumberUtils.getIntOrZero(newNoteField.text.toString())
        val numberOfOldNotes = NumberUtils.getIntOrZero(oldNoteField.text.toString())
        val multipliedAmount = denominationAmount * (numberOfNewNotes + numberOfOldNotes)
        multipliedAmountField.text = "$rupeePrefix $multipliedAmount"
        mapOfNotesToAmount[denominationAmount] = multipliedAmount
        updateTotalAmount()
    }

    private fun getDenominatedAmount(): Int {
        var totalAmount = 0
        mapOfNotesToAmount.forEach { (key, value) ->
            totalAmount += value
        }
        return totalAmount
    }

    private fun updateTotalAmount() {
        val totalAmount = getDenominatedAmount()
        val diff = totalAmount - getAimingAmountFromUI()
        val diffSymbol = if(diff == 0) "✓" else if(diff > 0) "▲" else "▼"
        val tooltipText = if(diff == 0) "Cash Matched" else if(diff > 0) "Have ₹${abs(diff)} more in cash" else "Need ₹${abs(diff)} more in cash"
        findViewById<MaterialTextView>(R.id.mc_totalAmount).text = "$rupeePrefix $totalAmount"
        findViewById<TextView>(R.id.mc_amount_diff).text = "$diffSymbol ${Math.abs(diff)}"
        findViewById<TextView>(R.id.mc_amount_diff).tooltipText = tooltipText
        updateSuccessColors()
    }

    private fun updateSuccessColors() {
        val amountDifference = getDenominatedAmount() - getAimingAmountFromUI()
        LogMe.log("" + getCalculatedTotalAmountFromUI() + " == " + getAimingAmountFromUI())
        if (getCalculatedTotalAmountFromUI() == getAimingAmountFromUI()) {
            findViewById<LinearLayout>(R.id.mc_totalAmount_container).setBackgroundColor(ContextCompat.getColor(this, R.color.mc_counter_success))
        } else {
            findViewById<LinearLayout>(R.id.mc_totalAmount_container).setBackgroundColor(ContextCompat.getColor(this, R.color.mc_counter_unsuccessful))
        }

        if(amountDifference == 0) {
            findViewById<TextView>(R.id.mc_amount_diff).setTextColor(ContextCompat.getColor(this, R.color.mc_counter_aiming_amount_zero))
        } else {
            findViewById<TextView>(R.id.mc_amount_diff).setTextColor(ContextCompat.getColor(this, R.color.mc_counter_aiming_amount_non_zero))
        }
    }

    override fun onBackPressed() {
        val switchActivityIntent = Intent(this, ActivityLogin::class.java)
        switchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(switchActivityIntent)
    }
}