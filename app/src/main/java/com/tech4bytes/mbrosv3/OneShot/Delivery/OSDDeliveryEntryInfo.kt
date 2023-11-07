package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.Customer.CustomerKYCUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.SMSDetails.SendSMSDetailsUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerData
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class OSDDeliveryEntryInfo {

    companion object {
        var uiMaps: MutableMap<String, View> = mutableMapOf()

        var entrynumber = 1
        fun createOrderCard(context: Context, value: DeliverToCustomerDataModel): View {
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_one_shot_delivery_fragment, null)

            val nameElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_name)
            val rateElementContainer = entry.findViewById<TextInputLayout>(R.id.osd_rate_for_customer_container)
            val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
            val pcElement = entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_pc)
            val kgElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg)
            val paidElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid)
            val balanceElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)
            val moreDetailsContainer = entry.findViewById<LinearLayout>(R.id.one_shot_delivery_fragment_more_details_container)
            val sendSMSBtn = entry.findViewById<TextView>(R.id.osd_fragment_send_details)
            rateElementContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE

            nameElement.text = value.name
            balanceElement.text = value.prevDue
            val deliveryRecord = DeliverToCustomerActivity.getDeliveryRecord(value.name)
            if (deliveryRecord != null) {
                pcElement.setText(NumberUtils.getIntOrBlank(deliveryRecord.deliveredPc))
                kgElement.text = NumberUtils.getDoubleOrBlank(deliveryRecord.deliveredKg)
                paidElement.text = NumberUtils.getIntOrBlank(deliveryRecord.paid)
                val pcHintText = if(NumberUtils.getIntOrZero(deliveryRecord.orderedPc) == 0) "pc" else deliveryRecord.orderedPc
                pcElement.hint = pcHintText
            }

            if (SendSMSDetailsUtils.getSendSMSDetailsNumber(value.name) != null) {
                sendSMSBtn.visibility = View.VISIBLE
                sendSMSBtn.setOnClickListener {
                    val customer = CustomerKYCUtils.getCustomerByEngName(value.name)!!
                    val smsNumber = customer.smsNumber
                    val t = DateUtils.getDate(value.timestamp)
                    val formattedDate = DateUtils.getDateInFormat(t!!, "dd/MM/yyyy")
                    val smsText = customer.smsText
                        .replace("<date>", formattedDate)
                        .replace("<pc>", pcElement.text.toString())
                        .replace("<kg>", kgElement.text.toString())
                        .replace("<paidAmount>", paidElement.text.toString())
                        .replace("<rate>", rateElement.text.toString())
                        .replace("<balanceAmount>", balanceElement.text.toString())

                    SMSUtils.sendSMS(context, smsText, smsNumber)
                    Toast.makeText(context, "SMS Sent: $smsNumber", Toast.LENGTH_LONG).show()
                }
            }

            rateElement.setText("${CustomerDataUtils.getDeliveryRate(value.name)}")
            OSDDeliveryEntryInfo.fragmentUpdateCustomerWiseRateView(context, value, entry)

            val recordContainer = entry.findViewById<CardView>(R.id.one_shot_delivery_fragment_record_container)
            var cardColor = ContextCompat.getColor(context, R.color.one_shot_delivery_odd_card_color)
            if (entrynumber % 2 == 0) {
                cardColor = ContextCompat.getColor(context, R.color.one_shot_delivery_even_card_color)
            }
            entrynumber++
            recordContainer.setBackgroundColor(cardColor)
            uiMaps[value.name] = entry

            return entry
        }

        fun setListeners(context: Context, value: DeliverToCustomerDataModel) {
            val entry = uiMaps[value.name]!!
            val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
            val pcElement = entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_pc)
            val kgElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg)
            val paidElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid)
            val balanceElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)
            val moreDetailsContainer = entry.findViewById<LinearLayout>(R.id.one_shot_delivery_fragment_more_details_container)

            rateElement.doOnTextChanged { text, start, before, count ->
                updateEntry(context as OneShotDelivery, value, entry)
                fragmentUpdateCustomerWiseRateView(context, value, entry)
            }

            pcElement.doOnTextChanged { text, start, before, count ->
                updateEntry(context as OneShotDelivery, value, entry)
            }

            kgElement.doOnTextChanged { text, start, before, count ->
                updateEntry(context as OneShotDelivery, value, entry)
            }

            paidElement.doOnTextChanged { text, start, before, count ->
                updateEntry(context as OneShotDelivery, value, entry)
            }

            balanceElement.setOnClickListener {
                if (moreDetailsContainer.visibility == View.VISIBLE) {
                    moreDetailsContainer.visibility = View.GONE
                } else {
                    moreDetailsContainer.visibility = View.VISIBLE
                }
                updateDetailedInfo(value, entry)
            }
        }

        fun fragmentUpdateCustomerWiseRateView(context: Context, value: DeliverToCustomerDataModel, entry: View) {
            val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
            if (NumberUtils.getIntOrZero(rateElement.text.toString()) != CustomerDataUtils.getCustomerDefaultRate(value.name)) {
                rateElement.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
                rateElement.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                rateElement.setBackgroundColor(0x00000000)
                rateElement.setTextColor(rateElement.textColors.defaultColor)
            }
        }

        fun updateRates() {
            uiMaps.forEach {
                val rate = CustomerDataUtils.getCustomerDefaultRate(it.key)
                val rateElement = it.value.findViewById<TextView>(R.id.osd_rate_for_customer)
                rateElement.text = rate.toString()
            }
        }

        fun updateEntry(context: OneShotDelivery, order: DeliverToCustomerDataModel, entry: View, updateTotals: Boolean = true) {
            val kg = getKgForEntry(entry)
            order.deliveredKg = kg.toString()
            order.deliveredPc = getPcForEntry(entry).toString()
            order.todaysAmount = getTodaysSaleAmountForEntry(entry).toString()
            order.paid = getPaidAmountForEntry(entry).toString()
            order.rate = getRateForEntry(entry).toString()
            order.totalDue = "${NumberUtils.getIntOrZero(order.prevDue) + getTodaysSaleAmountForEntry(entry)}"
            order.balanceDue = "${NumberUtils.getIntOrZero(order.prevDue) + getTodaysSaleAmountForEntry(entry) - getPaidAmountForEntry(entry)}"

            val balanceElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)

            balanceElement.text = getDueBalance(order, entry).toString()
            if(updateTotals) OneShotDelivery.updateTotals(context)
            updateDetailedInfo(order, entry)

            val pc = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc)
            if(kg > 0.0) {
                pc.setHintTextColor(ContextCompat.getColor(context, R.color.osd_pc_hint_color_2))
            } else {
                pc.setHintTextColor(ContextCompat.getColor(context, R.color.osd_pc_hint_color_1))
            }

        }

        private fun updateDetailedInfo(order: DeliverToCustomerDataModel, entry: View) {
            val container = entry.findViewById<LinearLayout>(R.id.one_shot_delivery_fragment_more_details_container)

            if (container.visibility == View.VISIBLE) {
                val prevDue = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_prev_due)
                val kg = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_kg)
                val rate = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_rate)
                val todaysSale = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_sale_total)
                val total = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_total_due)
                val paid = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_paid_amount)
                val balanceDue = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_balance_due)

                prevDue.text = "₹ ${order.prevDue}"
                kg.text = "${order.deliveredKg} kg"
                rate.text = "₹ ${order.rate}"
                todaysSale.text = "₹ ${order.todaysAmount}"
                total.text = "₹ ${order.totalDue}"
                paid.text = "₹ ${order.paid}"
                balanceDue.text = "₹ ${order.balanceDue}"
            }
        }

        private fun getDueBalance(order: DeliverToCustomerDataModel, entry: View): Int {
            val prevBal = order.prevDue
            return NumberUtils.getIntOrZero(prevBal) + getTodaysSaleAmountForEntry(entry) - getPaidAmountForEntry(entry)
        }

        private fun getPcForEntry(entry: View): Int {
            var pc = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc).text.toString()
            val kg = NumberUtils.getDoubleOrZero(entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg).text.toString())
            if(pc.isEmpty() &&  kg > 0.0) {
                pc = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc).hint.toString()
            }
            if (pc.isEmpty())
                return 0
            return NumberUtils.getIntOrZero(pc)
        }

        private fun getKgForEntry(entry: View): Double {
            val kg = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg).text.toString()
            if (kg.isEmpty())
                return 0.0
            return NumberUtils.getDoubleOrZero(kg)
        }

        private fun getPaidAmountForEntry(entry: View): Int {
            val paid = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid).text.toString()
            if (paid.isEmpty())
                return 0
            return paid.toInt()
        }

        private fun getTodaysSaleAmountForEntry(entry: View): Int {
            val kg = getKgForEntry(entry)
            val rate = getRateForEntry(entry)
            val roundUpOffset = 0.000001
            return (kg * rate + roundUpOffset).toInt()
        }

        private fun getRateForEntry(entry: View): Int {
            val rate = entry.findViewById<TextView>(R.id.osd_rate_for_customer).text.toString()
            if (rate.isEmpty())
                return 0
            return rate.toInt()
        }
    }
}