package com.tech4bytes.mbrosv3.CustomerOrders.Occasions

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.reflect.TypeToken
import com.tech4bytes.mbrosv3.AppData.Tech4BytesSerializable
import com.tech4bytes.mbrosv3.ProjectConfig
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import java.util.Calendar
import java.util.Date

object Events: Tech4BytesSerializable<EventsModel>(
    ProjectConfig.dBServerScriptURL,
    ProjectConfig.get_db_sheet_id(),
    "occasions",
    object : TypeToken<ArrayList<EventsModel>?>() {}.type,
    appendInServer = true,
    appendInLocal = true
) {

    override fun <T : Any> sortResults(list: ArrayList<T>): ArrayList<T> {
        return (list as ArrayList<EventsModel>).sortedBy { p -> p.eng_date }.reversed() as ArrayList<T>
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun getOccasionsInLastNDays(n: Int): List<EventsModel> {
        val listOfEvents: MutableList<EventsModel> = mutableListOf()

        val events = get()
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