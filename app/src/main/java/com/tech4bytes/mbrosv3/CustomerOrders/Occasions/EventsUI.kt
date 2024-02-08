package com.tech4bytes.mbrosv3.CustomerOrders.Occasions

import android.app.Activity
import android.os.Build
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.tech4bytes.mbrosv3.AppData.RemoteAppConstants.AppConstants
import com.tech4bytes.mbrosv3.R
import com.tech4bytes.mbrosv3.Utils.Date.DateUtils
import com.tech4bytes.mbrosv3.Utils.Numbers.NumberUtils
import java.util.Date

object EventsUI {
    val daysOfEvents = NumberUtils.getIntOrZero(AppConstants.get(AppConstants.EVENTS_SHOW_N_DAYS))

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun showEvents(activity: Activity, eventsLayout: LinearLayout) {
        if(daysOfEvents <= 0) {
            // if AppConstants.EVENTS_SHOW_N_DAYS == 0 -> hide the events window
            eventsLayout.visibility = View.GONE
            return
        }

        eventsLayout.visibility = View.VISIBLE
        val eventsScrollView = eventsLayout.findViewById<ScrollView>(R.id.smsordering_events_scroll_layout_container)
        val listContainer = eventsLayout.findViewById<LinearLayout>(R.id.smsordering_events_container)
        val eventsViewToggleBtn = eventsLayout.findViewById<TextView>(R.id.smsordering_toggle_events_view)

        val events = Events.getOccasionsInLastNDays(daysOfEvents)

        setEventsDrawerListener(eventsScrollView, eventsViewToggleBtn, events)

        activity.runOnUiThread {
            setToggleBtnText(eventsScrollView, eventsViewToggleBtn, events)
        }

        activity.runOnUiThread {
            events.forEach { event ->
                val eventDate: Date = DateUtils.getDate(event.eng_date)!!
                val dateString = DateUtils.getDateInFormat(eventDate, "dd MMM")
                val yearString = DateUtils.getDateInFormat(eventDate, "yyyy")
                val dayString = DateUtils.getDateInFormat(eventDate, "E")

                val entry = activity.layoutInflater.inflate(R.layout.activity_sms_ordering_event_fragments, null)
                entry.findViewById<TextView>(R.id.smsorder_event_year).text = yearString
                entry.findViewById<TextView>(R.id.smsorder_event_date).text = dateString
                entry.findViewById<TextView>(R.id.smsorder_event_day_name).text = dayString
                entry.findViewById<TextView>(R.id.smsorder_event_name).text = event.occassion_name
                listContainer.addView(entry)
            }
        }
    }

    fun setEventsDrawerListener(
        eventsScrollView: ScrollView,
        eventsViewToggleBtn: TextView,
        events: List<EventsModel>
    ) {
        eventsViewToggleBtn.setOnClickListener {
            onToggleEventsView(eventsScrollView, eventsViewToggleBtn, events)
        }
    }

    fun onToggleEventsView(
        eventsScrollView: ScrollView,
        eventsViewToggleBtn: TextView,
        events: List<EventsModel>
    ) {
        eventsScrollView.visibility = if (eventsScrollView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        setToggleBtnText(eventsScrollView, eventsViewToggleBtn, events)
    }

    fun setToggleBtnText(eventsScrollView: ScrollView,
                         eventsViewToggleBtn: TextView,
                         events: List<EventsModel>) {

        val numberOfEvents = events.size
        val viewText = if (eventsScrollView.visibility == View.VISIBLE) "HIDE EVENTS" else "SHOW EVENTS"
        eventsViewToggleBtn.text = "$viewText  / $numberOfEvents in $daysOfEvents days"
    }
}