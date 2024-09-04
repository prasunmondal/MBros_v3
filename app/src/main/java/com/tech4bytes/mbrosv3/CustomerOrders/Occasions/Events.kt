package com.tech4bytes.mbrosv3.CustomerOrders.Occasions

import com.prasunmondal.dev.libs.contexts.AppContexts
import com.prasunmondal.dev.libs.gsheet.ContextWrapper
import com.prasunmondal.dev.libs.gsheet.clients.ClientSort
import com.prasunmondal.dev.libs.gsheet.clients.GSheetSerialized
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import java.util.Calendar
import java.util.Date

object Events: GSheetSerialized<EventsModel>(
    context = ContextWrapper(AppContexts.get()),
    scriptURL = ProjectConfig.dBServerScriptURL,
    sheetId = ProjectConfig.get_db_sheet_id(),
    "occasions",
    query = null,
    classTypeForResponseParsing = EventsModel::class.java,
    appendInServer = true,
    appendInLocal = true,
    sort = ClientSort("sortByEventDate") { list: List<EventsModel> -> list.sortedBy { p -> DateUtils.getDate(p.eng_date) }.reversed().reversed() }
) {
    fun getOccasionsInLastNDays(n: Int): List<EventsModel> {
        val listOfEvents: MutableList<EventsModel> = mutableListOf()

        val events = fetchAll().execute()
        val calendar = Calendar.getInstance()
        val todaysDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, n)
        val cutOffDate = calendar.time


        for (event in events) {
            val eventDate: Date? = DateUtils.getDate(event.eng_date)
            if(eventDate != null && eventDate.after(todaysDate) && eventDate.before(cutOffDate)) {
                listOfEvents.add(event)
            }
        }
        return listOfEvents
    }
}