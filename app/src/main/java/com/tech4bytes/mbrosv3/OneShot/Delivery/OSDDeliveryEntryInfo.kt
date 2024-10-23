package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.prasunmondal.dev.libs.contexts.AppContexts
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.Customer.CustomerKYC
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.SMSDetails.SendSMSDetailsUtils
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerActivity
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
import com.tech4bytes.mbrosv3.Payments.Staged.StagedPaymentUtils
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Sms.SMSUtils
import com.tech4bytes.mbrosv3.Utils.Android.UIUtils
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils.Companion.getDoubleOrZero
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils.Companion.getIntOrZero

class OSDDeliveryEntryInfo {

    companion object {
        var uiMaps: MutableMap<String, View> = mutableMapOf()
        lateinit var activity: Activity

        fun setActivityContext(_activity: Activity) {
            activity = _activity
        }

        private var entrynumber = 1
        fun createOrderCard(context: Context, deliveryObj: DeliverToCustomerDataModel): View {
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_one_shot_delivery_fragment, null)
            updatePrimaryAttributesInUi(entry, deliveryObj)

            val nameElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_name)
            val rateElementContainer =
                entry.findViewById<TextInputLayout>(R.id.osd_rate_for_customer_container)
            val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
            val pcElement = entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_pc)
            val kgElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg)
            val paidCashElement =
                entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paidCash)
            val paidOnlineElement =
                entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paidOnline)
            val paidElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid)
            val balanceElement =
                entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)
            val sendSMSBtn = entry.findViewById<TextView>(R.id.osd_fragment_send_details)
            rateElementContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE

            deliveryObj.customerAccount = deliveryObj.name
//            nameElement.text = deliveryObj.name
//            balanceElement.text = deliveryObj.prevDue
            val deliveryRecord = DeliverToCustomerActivity.getDeliveryRecord(deliveryObj.name)
//            paidOnlineElement.text =
//                NumberUtils.getIntOrBlank(StagedPaymentUtils.getStagedPayments(deliveryObj.name).paidAmount)

            if (deliveryRecord != null) {
//                pcElement.setText(NumberUtils.getIntOrBlank(deliveryRecord.deliveredPc))
//                kgElement.text = NumberUtils.getDoubleOrBlank(deliveryRecord.deliveredKg)
//                paidOnlineElement.text = NumberUtils.getIntOrBlank(deliveryRecord.paidOnline)
//                paidElement.text =
//                    (getIntOrZero(deliveryRecord.paidCash) + getIntOrZero(paidOnlineElement.text.toString())).toString()
//                paidCashElement.text = NumberUtils.getIntOrBlank(deliveryRecord.paidCash)

            }

            if (SendSMSDetailsUtils.getSendSMSDetailsNumber(deliveryObj.name) != null) {
                sendSMSBtn.visibility = View.VISIBLE
                sendSMSBtn.setOnClickListener {
                    val smsNumber = CustomerKYC.getCustomerByEngName(deliveryObj.name)!!.smsNumber
                    val t = DateUtils.getDate(deliveryObj.timestamp)
                    val formattedDate = DateUtils.getDateInFormat(t!!, "dd/MM/yyyy")
                    val smsText = CustomerKYC.getCustomerByEngName(deliveryObj.name)!!.smsText
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

            rateElement.setText("${CustomerDataUtils.getDeliveryRate(deliveryObj.name)}")
            fragmentUpdateCustomerWiseRateView(context, deliveryObj, entry)

            val recordContainer =
                entry.findViewById<CardView>(R.id.one_shot_delivery_fragment_record_container)
            var cardColor =
                ContextCompat.getColor(context, R.color.one_shot_delivery_odd_card_color)
            if (entrynumber % 2 == 0) {
                cardColor =
                    ContextCompat.getColor(context, R.color.one_shot_delivery_even_card_color)
            }
            entrynumber++
            recordContainer.setBackgroundColor(cardColor)
            uiMaps[deliveryObj.name] = entry

            return entry
        }

        @SuppressLint("SuspiciousIndentation")
        fun setListeners(context: Context, value: DeliverToCustomerDataModel) {
            val entry = uiMaps[value.name]!!
            val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
            val pcElement = entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_pc)
            val kgElement = entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_kg)
            val balanceElement =
                entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)
            val moreDetailsContainer =
                entry.findViewById<LinearLayout>(R.id.one_shot_delivery_fragment_more_details_container)
            val paidOnlineElement =
                entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_paidOnline)
            val paidCashElement =
                entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_paidCash)

            UIUtils.addDebouncedOnTextChangeListener(rateElement) {
                value.setRate(getIntOrZero(rateElement.text.toString()), entry)
                fragmentUpdateCustomerWiseRateView(context, value, entry)
            }

            UIUtils.addDebouncedOnTextChangeListener(pcElement) {
                val kg = getDoubleOrZero(kgElement.text.toString())
                if(kg > 0.0)
                    value.setDeliveredPc(getIntOrZero(UIUtils.getTextOrHint(pcElement)), entry)
            }

            UIUtils.addDebouncedOnTextChangeListener(kgElement) {
                value.setDeliveredPc(getIntOrZero(UIUtils.getTextOrHint(pcElement)), entry)
                value.setDeliveredKg(getDoubleOrZero(kgElement.text.toString()), entry)
            }

            UIUtils.addDebouncedOnTextChangeListener(paidCashElement) {
                value.setPaidCash(getIntOrZero(paidCashElement.text.toString()), entry)
            }

            UIUtils.addDebouncedOnTextChangeListener(paidOnlineElement) {
                value.setPaidOnline(getIntOrZero(paidOnlineElement.text.toString()), entry)
            }

            val refreshRateButton = entry.findViewById<ImageView>(R.id.one_shot_delivery_fragment_refresh_btn)
                if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_RATE_RESET_BUTTON)) {
                    refreshRateButton.setOnClickListener {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(context as OneShotDelivery)
                        builder.setMessage("Customer rate will reset. Do you want to restore the original rate?")
                            .setTitle("Restore Rate")
                            .setPositiveButton("Yes") { dialog, id ->
                                // CONFIRM
                                rateElement.setText("${CustomerDataUtils.getCustomerDefaultRate(value.name)}")
                                fragmentUpdateCustomerWiseRateView(context, value, entry)
                            }
                            .setNegativeButton("No") { dialog, id ->
                                // CANCEL
                            }.setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    }
                }
                else
                {
                    refreshRateButton.visibility = View.GONE
                }

            balanceElement.setOnClickListener {
                if (moreDetailsContainer.visibility == View.VISIBLE) {
                    moreDetailsContainer.visibility = View.GONE
                } else {
                    moreDetailsContainer.visibility = View.VISIBLE
                }
            }
        }

        fun fragmentUpdateCustomerWiseRateView(
            context: Context,
            value: DeliverToCustomerDataModel,
            entry: View
        ) {
            val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
            val refreshRateButton = entry.findViewById<ImageView>(R.id.one_shot_delivery_fragment_refresh_btn)
            if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_RATE_RESET_BUTTON) && getIntOrZero(rateElement.text.toString()) != CustomerDataUtils.getCustomerDefaultRate(value.name)) {
                rateElement.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
                rateElement.setTextColor(ContextCompat.getColor(context, R.color.white))
                refreshRateButton.visibility = View.VISIBLE
            } else {
                rateElement.setBackgroundColor(0x00000000)
                rateElement.setTextColor(rateElement.textColors.defaultColor)
                refreshRateButton.visibility = View.GONE
            }
        }

        fun updateRates() {
            uiMaps.forEach {
                val rate = CustomerDataUtils.getCustomerDefaultRate(it.key)
                val rateElement = it.value.findViewById<TextView>(R.id.osd_rate_for_customer)
                rateElement.text = rate.toString()
            }
        }

        private fun updateAutoAdjustmentBalance(order: DeliverToCustomerDataModel, entry: View) {
            val autoAdjustments = BalanceReferralCalculations.getTotalDiscountFor(order.name)
            val autoAdjustmentLayout: LinearLayout =
                entry.findViewById(R.id.osd_fragment_auto_adjustments_layout)

            autoAdjustmentLayout.visibility =
                if (autoAdjustments.transferAmount == 0) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            val autoAdjustmentBalance: TextView =
                entry.findViewById(R.id.osd_fragment_auto_adjustments)
            val autoAdjustmentJustification: TextView =
                entry.findViewById(R.id.osd_fragment_auto_adjustments_justification)
            val balanceBeforeAdjustmentElement: TextView =
                entry.findViewById(R.id.osd_fragment_balance_before_adjustments)

            balanceBeforeAdjustmentElement.text = order.totalBalance
            autoAdjustmentJustification.text = autoAdjustments.message
            autoAdjustmentBalance.text = autoAdjustments.transferAmount.toString()
            val adjustingBalance = autoAdjustmentBalance.text.toString()
            order.khataBalance = (getIntOrZero(order.khataBalance) + getIntOrZero(adjustingBalance)).toString()
        }

        fun updatePrimaryAttributesInUi(view: View, obj: DeliverToCustomerDataModel) {
            val viewName = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_name)
            val viewDeliveredPc = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc)
            val viewDeliveredKg = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg)
            val viewPaidCash = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_paidCash)
            val viewPaidOnline = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_paidOnline)
            val viewRate = view.findViewById<TextView>(R.id.osd_rate_for_customer)

            val pcHintText =
                if (getIntOrZero(obj.orderedPc) == 0) "pc" else obj.orderedPc

            viewName.text = obj.name
            viewDeliveredPc.hint = pcHintText
            viewPaidCash.text = obj.paidCash
            viewPaidOnline.text = obj.paidOnline
            viewRate.text = obj.rate

            if(pcHintText != obj.deliveredPc && getIntOrZero(obj.deliveredPc) != 0)
                viewDeliveredPc.text = obj.deliveredPc

            if(getDoubleOrZero(obj.deliveredKg) > 0.0)
                viewDeliveredKg.text = obj.deliveredKg
        }

        fun updateDerivedAttributesInUi(view: View, obj: DeliverToCustomerDataModel) {
            val viewAvgKg = view.findViewById<TextView>(R.id.osd_entry_avg_kg)
            val viewPaid1 = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid)
            val viewKhataDue1 = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)
            val viewPrevBal = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_prev_due)
            val viewTodaysSaleAmount = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_sale_total)
            val viewKg = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_kg)
            val viewRate = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_rate)
            val viewPrevPlusToday = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_total_due)
            val viewPaid2 = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_paid_amount)
            val viewKhataDue2 = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_more_details_container_balance_due)
            val viewOtherBalance = view.findViewById<TextView>(R.id.osd_lh_balance)
            val viewTotalBalance = view.findViewById<TextView>(R.id.osd_total_balance_including_lh)

            val kg = getDoubleOrZero(obj.deliveredKg)
            val pc = getIntOrZero(obj.orderedPc)

            if (AuthorizationUtils.isAuthorized(AuthorizationEnums.OSD_SHOW_DELIVERY_AVG_WT) && kg > 0.0 && pc != 0) {
                viewAvgKg.text = "${NumberUtils.roundOff3places((kg / pc))} kg/pc"
            }

            viewPaid1.text = obj.paid
            viewKhataDue1.text = obj.khataBalance
            viewPrevBal.text = obj.prevDue
            viewTodaysSaleAmount.text = obj.deliverAmount
            viewKg.text = obj.deliveredKg
            viewRate.text = obj.rate
            viewPrevPlusToday.text = "${getIntOrZero(obj.prevDue) + getIntOrZero(obj.deliverAmount)}"
            viewPaid2.text = obj.paid
            viewKhataDue2.text = obj.khataBalance
            viewOtherBalance.text = obj.otherBalances
            viewTotalBalance.text = obj.totalBalance

            OneShotDelivery.updateTotals()
        }
    }
}