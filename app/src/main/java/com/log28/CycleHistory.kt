package com.log28


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.log28.groupie.HistoryItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import devs.mulham.horizontalcalendar.utils.Utils
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_cycle_history.*
import java.util.*
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 * Use the [CycleHistory.newInstance] factory method to
 * create an instance of this fragment.
 */
class CycleHistory : Fragment() {
    data class CycleData(val cycleStarts: List<Calendar>, val periodEnds: List<Calendar>)

    private val periodDates = getPeriodDaysDecending()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cycleData = findCycleStartsAndPeriodEnds(periodDates)

        avg_cycle_length.text = findAverageCycleLength(cycleData.cycleStarts).toString()
        avg_period_length.text = findAveragePeriodLength(cycleData).toString()
        setupPreviousCycles(cycleData)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cycle_history, container, false)
    }

    private fun setupPreviousCycles(cycleData: CycleData) {
        val layout = GridLayoutManager(context, 3)
        val dividerItem = DividerItemDecoration(context, layout.orientation)
        val groupAdapter = GroupAdapter<ViewHolder>()

        previous_cycles.apply {
            layoutManager = layout
            adapter = groupAdapter
            this.addItemDecoration(dividerItem)
        }

        listOf("Cycle Start", "Period Length","Cycle Length", "Feb 8, 2018", "5", "-", "Jan 6, 2018", "6", "29").forEach {
            groupAdapter.add(HistoryItem(it))
        }
    }

    //TODO fix performance (maybe make all the dates calendars)
    private fun findCycleStartsAndPeriodEnds(periodDates: RealmResults<DayData>): CycleData {
        val cycleStarts = mutableListOf<Calendar>()
        val periodEnds = mutableListOf<Calendar>()

        periodDates.forEachIndexed { index, dayData ->
            val previousDate = dayData.date.toCalendar()
            val nextDate = dayData.date.toCalendar()
            previousDate.add(Calendar.DAY_OF_MONTH, -1)
            nextDate.add(Calendar.DAY_OF_MONTH, 1)

            if (index == periodDates.lastIndex || previousDate.formatDate() != periodDates[index+1]?.date) {
                cycleStarts.add(dayData.date.toCalendar())
            } else if (index == 0 || nextDate.formatDate() != periodDates[index-1]?.date) {
                periodEnds.add(dayData.date.toCalendar())
            }
        }
        Log.d("HISTORYVIEW", "cyclestarts $cycleStarts\nperiodEnds $periodEnds")

        return CycleData(cycleStarts, periodEnds)
    }

    //TODO check for off-by-ones and other math errors
    private fun findAverageCycleLength(cycleStarts: List<Calendar>): Int {
        val cycleLengths = mutableListOf<Int>()
        cycleStarts.forEachIndexed {
            index, date ->
            if (index != 0) {
                cycleLengths.add(Utils.daysBetween(date, cycleStarts[index-1]))
            }
        }

        return cycleLengths.average().roundToInt()
    }

    //TODO check for off-by-ones and other math errors
    private fun findAveragePeriodLength(cycleData: CycleData): Int {
        val periodLengths = mutableListOf<Int>()

        cycleData.periodEnds.forEachIndexed {
            index, date ->
            if (!date.isToday()) {
                // we need to add 1 because days between doesn't count the last day of the period
                periodLengths.add(Utils.daysBetween(cycleData.cycleStarts[index], date) + 1)
            }
        }

        return periodLengths.average().roundToInt()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CycleHistory.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): CycleHistory {
            val fragment = CycleHistory()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}

fun Calendar.isToday(): Boolean {
    return Utils.isSameDate(this, Calendar.getInstance())
}