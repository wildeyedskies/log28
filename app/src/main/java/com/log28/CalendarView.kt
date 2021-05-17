package com.log28

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import devs.mulham.horizontalcalendar.utils.Utils
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_calendar_view.*
import pl.rafman.scrollcalendar.contract.MonthScrollListener
import pl.rafman.scrollcalendar.data.CalendarDay
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarView.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarView : Fragment() {
    private val realm = Realm.getDefaultInstance()
    private var periodDateObjects = realm.getPeriodDates()
    //TODO use a tree for better calendar performance?
    private var periodDates = mutableListOf<Long>()
    private val cycleInfo = realm.getCycleInfo()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar_view, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // we should have context at this point
        periodDates = predictFuturePeriods(periodDateObjects.map { d -> d.date }.toMutableList())

        periodDateObjects.addChangeListener {
            results, changeSet ->
            if (changeSet != null) {
                periodDates = predictFuturePeriods(periodDateObjects.map { d -> d.date }.toMutableList())
                scrollCalendar.adapter.notifyDataSetChanged()
            }
        }

        cycleInfo.addChangeListener<CycleInfo> {
            _, changeSet ->
            if (changeSet != null) {
                periodDates = predictFuturePeriods(periodDateObjects.map { d -> d.date }.toMutableList())
                scrollCalendar.adapter.notifyDataSetChanged()
            }
        }
        setupScrollCalendar()
    }

    // setup the calendar
    private fun setupScrollCalendar() {
        // show periods on the calendar as it renders
        val today = Calendar.getInstance()
        scrollCalendar.setDateWatcher({
            year, month, day ->
            if ((year.toLong() * 10000) + (month.toLong() * 100) + day.toLong() in periodDates) {
                CalendarDay.SELECTED
            } else if (year == today.get(Calendar.YEAR) &&
                    month == today.get(Calendar.MONTH) && day == today.get(Calendar.DAY_OF_MONTH)) {
                CalendarDay.TODAY
            } else CalendarDay.DEFAULT
        })

        // we call the underlying activity and tell it to navigate to the day view and set the day
        scrollCalendar.setOnDateClickListener({
            year, month, day ->  val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            // only allow you to go to the day view for dates not in the future
            Log.d(TAG, "day clicked ${cal.formatDate()}")
            //TODO redo this tangled mess with some RX code calendar tap -> event -> dayview updates
            if (cal.before(Calendar.getInstance()) || cal.isToday())
                (this.activity as? MainActivity)?.navToDayView(cal)
        })

        scrollCalendar.setMonthScrollListener(object : MonthScrollListener {
            override fun shouldAddNextMonth(lastDisplayedYear: Int, lastDisplayedMonth: Int): Boolean {
                // don't let the user scroll more than 4 months into the future
                val fourMonths = Calendar.getInstance()
                fourMonths.add(Calendar.MONTH, 4)
                if (fourMonths.get(Calendar.YEAR) == lastDisplayedYear &&
                        fourMonths.get(Calendar.MONTH) == lastDisplayedMonth)
                    return false
                return true
            }

            override fun shouldAddPreviousMonth(firstDisplayedYear: Int, firstDisplayedMonth: Int): Boolean {
                return true
            }
        })
    }

    // TODO there might be an off by 1 error somewhere in here
    private fun predictFuturePeriods(periodDates: MutableList<Long>): MutableList<Long> {
        var cycleStart = periodDates.filter { item ->
            val previousDay = item.toCalendar()
            previousDay.add(Calendar.DAY_OF_MONTH, -1)
            previousDay.formatDate() !in periodDates
        }.max()?.toCalendar()
                ?: return periodDates

        //the earliest day we can predict the next period for is tomorrow
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_MONTH, 1)

        for (i in 1..3) {
            cycleStart.add(Calendar.DAY_OF_MONTH, cycleInfo.cycleLength)

            if (cycleStart.before(tomorrow))
                cycleStart = tomorrow

            val cycleDays = cycleStart.clone() as Calendar
            periodDates.add(cycleDays.formatDate())
            for (j in 2..cycleInfo.periodLength) {
                cycleDays.add(Calendar.DAY_OF_MONTH, 1)
                periodDates.add(cycleDays.formatDate())
            }
        }

        return periodDates
    }

    override fun onDestroyView() {
        super.onDestroyView()
        periodDateObjects.removeAllChangeListeners()
        cycleInfo.removeAllChangeListeners()
    }

    companion object {
        const val TAG = "CALVIEW"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CalendarView.
         */
        fun newInstance() = CalendarView()
    }

}
