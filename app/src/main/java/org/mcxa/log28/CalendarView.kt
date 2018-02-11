package org.mcxa.log28

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.raizlabs.android.dbflow.runtime.DirectModelNotifier
import com.raizlabs.android.dbflow.structure.BaseModel
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
    private lateinit var modelChangeListener: DirectModelNotifier.ModelChangedListener<DayData>
    var periodDates = emptyList<Long>()
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
        //TODO figure out if this is a performance issue
        AppDatabase.getPeriodDates({
            list -> periodDates += list
            scrollCalendar.adapter.notifyDataSetChanged()
        })

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

        modelChangeListener = object: DirectModelNotifier.ModelChangedListener<DayData> {
            override fun onTableChanged(tableChanged: Class<*>?, action: BaseModel.Action) {
                //We don't care
            }

            override fun onModelChanged(model: DayData, action: BaseModel.Action) {
                if (action == BaseModel.Action.INSERT || action == BaseModel.Action.UPDATE) {

                    Log.d("CALVIEW", "Model changed, redrawing calendar")
                    if (model.physicalBleeding && model.date !in periodDates) periodDates += model.date
                    else if (!model.physicalBleeding && model.date in periodDates) periodDates -= model.date
                    Log.d("CALVIEW", periodDates.toString())

                    scrollCalendar.adapter.notifyDataSetChanged()
                }
            }
        }

        DirectModelNotifier.get().registerForModelChanges(DayData::class.java, modelChangeListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DirectModelNotifier.get().unregisterForModelChanges(DayData::class.java, modelChangeListener)
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
