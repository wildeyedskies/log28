package org.mcxa.log28

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import devs.mulham.horizontalcalendar.HorizontalCalendar
import java.util.*
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener
import kotlinx.android.synthetic.main.fragment_day_view.*
import android.widget.ExpandableListView.OnChildClickListener

/**
 * Handles the day view
 */
class DayView : Fragment() {
    // this is a lambda that does nothing until the list is initialized, at which point it changes the day on the list
    var changeDay: (c: Calendar) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_day_view, container, false)

        // setup the top calendar
        val startDate = Calendar.getInstance()
        startDate.add(Calendar.MONTH, -1)
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.MONTH, 1)

        val currentdate = Calendar.getInstance()
        currentdate.timeInMillis = savedInstanceState?.getLong("date") ?: Calendar.getInstance().timeInMillis

        val horizontalCalendar = HorizontalCalendar.Builder(rootView, R.id.topCalendar)
                .defaultSelectedDate(currentdate)
                .range(startDate, endDate)
                .datesNumberOnScreen(5).build()

        horizontalCalendar.calendarListener = object : HorizontalCalendarListener() {
            override fun onDateSelected(date: Calendar, position: Int) {
                changeDay.invoke(date)
                // this little bit of code extends the range of the dates
                val cDate = date.clone() as Calendar
                cDate.add(Calendar.DAY_OF_YEAR, -5)
                if (startDate.after(cDate)) {
                    Log.d("DAYVIEW", "setting range")
                    startDate.add(Calendar.MONTH, -1)

                    horizontalCalendar.setRange(startDate, endDate)
                    horizontalCalendar.selectDate(date, true)
                    horizontalCalendar.refresh()
                }
            }
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentdate = Calendar.getInstance()
        currentdate.timeInMillis = savedInstanceState?.getLong("date") ?: Calendar.getInstance().timeInMillis

        // set up the expandable list
        val listAdapter = DayExpandableListAdapter(this.activity, currentdate)
        changeDay = {
            c -> listAdapter.changeDay(c)
        }
        expandableListView.setAdapter(listAdapter)
        expandableListView.setOnChildClickListener(OnChildClickListener { parent, v, groupPosition, childPosition, id ->
            listAdapter.toggleStatus(groupPosition, childPosition)
            true
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DayView.
         */
        fun newInstance(date: Calendar): DayView {
            val fragment = DayView()
            val args = Bundle()
            args.putLong("date", date.timeInMillis)
            fragment.arguments = args
            return fragment
        }
    }
}
