package com.tech4bytes.mbrosv3.OneShot.Delivery

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationEnums
import com.tech4bytes.mbrosv3.AppUsers.Authorization.DataAuth.AuthorizationUtils
import com.tech4bytes.mbrosv3.BusinessData.SingleAttributedDataUtils
import com.tech4bytes.mbrosv3.BusinessLogic.DeliveryCalculations
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.SendInfoTexts.Whatsapp.Whatsapp
import com.tech4bytes.mbrosv3.Utils.Contexts.AppContexts
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Logs.LogMe.LogMe
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils

class OSDLoadInfo {

    companion object {
        private fun isSendLoadInfoEnabled(): Boolean {
            val metadata = SingleAttributedDataUtils.getRecords()
            val numberToSendInfo = AppConstants.GeneratedKeys.getWhatsappNumber(metadata.load_account)
            val templateToSendInfo = AppConstants.GeneratedKeys.getTemplateToSendInfo(metadata.load_account)
            val isSendLoadInfoEnabled = numberToSendInfo.isNotEmpty() && templateToSendInfo.isNotEmpty()

            if (!isSendLoadInfoEnabled) {
                LogMe.log("Send load info disabled. Either '$numberToSendInfo' or '$templateToSendInfo' is not configured")
            } else {
                LogMe.log("Send load info enabled.")
            }

            return isSendLoadInfoEnabled
        }

        fun initializeUI(context: OneShotDelivery, loadPcElement: EditText, loadKgElement: EditText, loadAvgWtElement: TextView) {
            val record = SingleAttributedDataUtils.getRecords()

            val loadCompanyBranchArea = context.findViewById<TextView>(R.id.osd_load_company_branch_area)
            context.runOnUiThread {
                loadCompanyBranchArea.text = "${record.load_companyName} / ${record.load_branch} / ${record.load_area}"
            }
        }

        fun setListeners(context: OneShotDelivery, loadPcElement: EditText, loadKgElement: EditText, loadAvgWtElement: TextView, deliveryPriceElement: EditText) {
            val record = SingleAttributedDataUtils.getRecords()

            loadPcElement.doOnTextChanged { text, start, before, count ->
                record.actualLoadPc = loadPcElement.text.toString()
                updateRelatedFields_LoadPcKg(loadPcElement, loadKgElement, loadAvgWtElement)
                OneShotDelivery.updateTotals(context)
            }

            loadKgElement.doOnTextChanged { text, start, before, count ->
                record.actualLoadKg = loadKgElement.text.toString()
                updateRelatedFields_LoadPcKg(loadPcElement, loadKgElement, loadAvgWtElement)
                OneShotDelivery.updateTotals(context)
            }

            deliveryPriceElement.doOnTextChanged { text, start, before, count ->
                record.bufferRate = DeliveryCalculations.getBufferPrice(record.finalFarmRate, deliveryPriceElement.text.toString()).toString()

                SingleAttributedDataUtils.saveToLocal(record)
                OSDDeliveryEntryInfo.updateRates()
            }
        }

        fun updateRelatedFields_LoadPcKg(loadPcElement: EditText, loadKgElement: EditText, loadAvgWtElement: TextView) {
            var avgWt = "N/A"
            try {
                avgWt = NumberUtils.roundOff3places(NumberUtils.getDoubleOrZero(loadKgElement.text.toString()) / NumberUtils.getIntOrZero(loadPcElement.text.toString())).toString()
            } catch (_: Exception) {
                LogMe.log("Error while getting avg")
            } finally {
                LogMe.log(avgWt)
                loadAvgWtElement.text = avgWt
            }
        }

        fun sendLoadInfoToCompany(loadedPc: String, loadedKg: String) {
            val metadata = SingleAttributedDataUtils.getRecords()
            val numberToSendInfo = AppConstants.GeneratedKeys.getWhatsappNumber(metadata.load_account)
            val templateToSendInfo = AppConstants.GeneratedKeys.getTemplateToSendInfo(metadata.load_account)

            val formattedDate = DateUtils.getDateInFormat("dd/MM/yyyy")
            val text = templateToSendInfo
                .replace("<date>", formattedDate)
                .replace("<loadPc>", loadedPc)
                .replace("<loadKg>", loadedKg)
                .replace("<loadCompanyName>", metadata.load_companyName)
            Whatsapp.sendMessage(AppContexts.get(), numberToSendInfo, text)
        }

        fun updateSingleAttributedDataOnUI(context: OneShotDelivery, loadPcElement: EditText, loadKgElement: EditText) {
            context.runOnUiThread {
                if (!isSendLoadInfoEnabled()) {
                    context.findViewById<TextView>(R.id.osd_btn_send_load_info_to_account_payee).visibility = View.GONE
                }

                loadPcElement.setText(SingleAttributedDataUtils.getRecords().actualLoadPc)
                loadKgElement.setText(SingleAttributedDataUtils.getRecords().actualLoadKg)
                context.findViewById<TextView>(R.id.osd_company_name).text = SingleAttributedDataUtils.getRecords().load_account
            }

            if (AuthorizationUtils.isAuthorized(AuthorizationEnums.SHOW_DELIVERY_RATE)) {
                val deliveryPriceElement = context.findViewById<EditText>(R.id.one_shot_delivery_price)
                context.runOnUiThread {
                    deliveryPriceElement.setText(
                        DeliveryCalculations.getBaseDeliveryPrice(SingleAttributedDataUtils.getRecords().finalFarmRate, SingleAttributedDataUtils.getRecords().bufferRate).toString()
                    )
                }
            } else {
                context.runOnUiThread {
                    context.findViewById<TextInputLayout>(R.id.osd_farm_rate_container).visibility = View.GONE
                }
            }
        }
    }
}