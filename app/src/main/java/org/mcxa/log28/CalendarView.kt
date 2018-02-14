package org.mcxa.log28

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var modelChangeListener: (DayData) -> Unit
    val periodDates = mutableListOf<Long>()
    // the months for which we have loaded the period data for. This should always be a contiguous range

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // grab all dates for periods in the background.
        getPeriodDates().doOnComplete {
            scrollCalendar.adapter.notifyDataSetChanged()
        }.subscribe {
            periodDates.add(it.date)
        }

        // show periods on the calendar as it renders
        val today = Calendar.getInstance()
        scrollCalendar.setDateWatcher({
            year, month, day ->
            if ((year.toLong() * 10000) + (month.toLong() * 100) + day.toLong() in periodDates) {
                Log.d("CALVIEW", "Period found at " + year.toString() + " " + month.toString())
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
            Log.d("CALVIEW", "day clicked ${cal.formatDate()}")
            //TODO redo this tangled mess with some RX code calendar tap -> event -> dayview updates
            if (cal.before(Calendar.getInstance()))
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

        modelChangeListener =  {
            daydata ->
                Log.d("CALVIEW", "Model changed, redrawing calendar")
                if (daydata.hasSymptom("Bleeding") && daydata.date !in periodDates) periodDates += daydata.date
                else if (!daydata.hasSymptom("Bleeding") && daydata.date in periodDates) periodDates -= daydata.date
                scrollCalendar.adapter.notifyDataSetChanged()
        }
    }

    // TODO there might be an off by 1 error somewhere in here
    fun predictFuturePeriods(periodDates: MutableList<Long>): MutableList<Long> {
        val cycleStart = periodDates.filter { item -> item -1 !in periodDates }.max()!!.toCalendar()

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val periodLength = prefs.getString("period_length", "5").toInt()
        val cycleLength = prefs.getString("cycle_length", "28").toInt()

        for (i in 1..3) {
            cycleStart.add(Calendar.DAY_OF_MONTH, cycleLength)
            val cycleDays = cycleStart.clone() as Calendar
            periodDates.add(cycleDays.formatDate())
            for (j in 2..periodLength) {
                cycleDays.add(Calendar.DAY_OF_MONTH, 1)
                periodDates.add(cycleDays.formatDate())
            }
        }

        return periodDates
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CalendarView.
         */
        fun newInstance(): CalendarView {
            val fragment = CalendarView()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}
