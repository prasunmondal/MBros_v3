package com.tech4bytes.mbrosv3.CustomerOrders.MoneyDeposit.CustomerDeposit

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.ClientSort
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig

object CustomerMoneyDeposit : GSheetSerialized<CustomerMoneyDepositModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURL,
    sheetId = ProjectConfig.get_db_sheet_id(),
    tabName = "moneyDeposits",
    query = null,
    modelClass = CustomerMoneyDepositModel::class.java,
    sort = ClientSort("sortByEventDate") { list: List<CustomerMoneyDepositModel> -> list.sortedBy { it.id }.reversed() }
)