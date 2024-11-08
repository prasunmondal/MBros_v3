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
import com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer.DeliverToCustomerDataModel
import com.tech4bytes.mbrosv3.Finalize.Models.CustomerDataUtils
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
        fun createOrderCard(context: Context = activity, deliveryObj: DeliverToCustomerDataModel): View {
            val layoutInflater = LayoutInflater.from(AppContexts.get())
            val entry = layoutInflater.inflate(R.layout.activity_one_shot_delivery_fragment, null)
            updatePrimaryAttributesInUi(entry, deliveryObj)
            deliveryObj.calculate(entry)
            updateDerivedAttributesInUi(entry, deliveryObj)

            val rateElementContainer =
                entry.findViewById<TextInputLayout>(R.id.osd_rate_for_customer_container)
            val rateElement = entry.findViewById<TextInputEditText>(R.id.osd_rate_for_customer)
            val pcElement = entry.findViewById<EditText>(R.id.one_shot_delivery_fragment_pc)
            val kgElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_kg)
            val paidElement = entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_paid)
            val balanceElement =
                entry.findViewById<TextView>(R.id.one_shot_delivery_fragment_balance_due)
            val sendSMSBtn = entry.findViewById<TextView>(R.id.osd_fragment_send_details)
            rateElementContainer.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_NONE
            deliveryObj.customerAccount = deliveryObj.name

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

            deliveryObj.setRate(context, CustomerDataUtils.getDeliveryRate(deliveryObj.name), entry)

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
                value.setRate(context, getIntOrZero(rateElement.text.toString()), entry)
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
                                value.setRate(context, CustomerDataUtils.getCustomerDefaultRate(value.name), entry)
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

        fun updateRates() {
            uiMaps.forEach {
                val rate = CustomerDataUtils.getCustomerDefaultRate(it.key)
                val rateElement = it.value.findViewById<TextView>(R.id.osd_rate_for_customer)
                rateElement.text = rate.toString()
            }
        }

        fun getName(view: View?): String {
            return view!!.findViewById<TextView>(R.id.one_shot_delivery_fragment_name).text.toString()
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
            val adjustment1 = view.findViewById<TextView>(R.id.osd_delivery_frag_adjustments)

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
            adjustment1.text = obj.adjustments

            // update hint color for pc when kg > 0
            val pcView = view.findViewById<TextView>(R.id.one_shot_delivery_fragment_pc)
            var pcHintColor = R.color.osd_pc_hint_color_1
            if (getDoubleOrZero(obj.deliveredKg) > 0.0) {
                pcHintColor = R.color.osd_pc_hint_color_2
            }
            pcView.setHintTextColor(ContextCompat.getColor(AppContexts.get(), pcHintColor))
        }
    }
}