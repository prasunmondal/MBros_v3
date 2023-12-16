package com.tech4bytes.mbrosv3.CustomerOrders.DeliverOrders.deliverToACustomer

import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.ProjectConfig

object DeliverToCustomerDataHandler: Tech4BytesSerializable(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "deliverOrders",
    appendInServer = true,
    appendInLocal = true,
    cacheObjectType = object : TypeToken<ArrayList<DeliverToCustomerDataModel>>() {}.type)