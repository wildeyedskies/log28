package com.log28.intro

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_last_period.*
import com.log28.R
import com.log28.formatDate
import com.log28.setFirstPeriod
import io.realm.Realm
import pl.rafman.scrollcalendar.contract.MonthScrollListener
import pl.rafman.scrollcalendar.data.CalendarDay
import java.util.*

class LastPeriodFragment: Fragment() {
    private val realm = Realm.getDefaultInstance()

    var dateSelected: Calendar? = null

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_last_period, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO prevent going too far into the past or future
        last_period_calendar.setMonthScrollListener(object : MonthScrollListener {
            override fun shouldAddNextMonth(lastDisplayedYear: Int, lastDisplayedMonth: Int): Boolean {
                return true
            }

            override fun shouldAddPreviousMonth(firstDisplayedYear: Int, firstDisplayedMonth: Int): Boolean {
                return true
            }
        })

        // highlight today
        val today = Calendar.getInstance()
        last_period_calendar.setDateWatcher {
            year, month, day ->
            if (year == today.get(Calendar.YEAR) &&
                    month == today.get(Calendar.MONTH) && day == today.get(Calendar.DAY_OF_MONTH)) {
                CalendarDay.TODAY
            } else if (year == dateSelected?.get(Calendar.YEAR) &&
                    month == dateSelected?.get(Calendar.MONTH) && day == dateSelected?.get(Calendar.DAY_OF_MONTH)) {
                Log.d("LASTPERIOD", "highlighting $year, $month, $day. dateSelected is ${dateSelected?.formatDate()}")
                CalendarDay.SELECTED
            } else CalendarDay.DEFAULT
        }


        // set the first period in the database
        last_period_calendar.setOnDateClickListener {
            year, month, day -> val firstDay = Calendar.getInstance()
            firstDay.set(Calendar.YEAR, year)
            firstDay.set(Calendar.MONTH, month)
            firstDay.set(Calendar.DAY_OF_MONTH, day)
            Log.d("LASTPERIOD", "click on day ${firstDay.formatDate()}")

            if (firstDay.before(Calendar.getInstance())) {
                dateSelected = firstDay
                (this.activity as AppIntroActivity).setupComplete = true
                realm.setFirstPeriod(firstDay.clone() as Calendar, this.context)
            }
        }
    }

    companion object {
        fun newInstance(): LastPeriodFragment {
            val fragment = LastPeriodFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}