package com.log28


import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.log28.groupie.HistoryItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import devs.mulham.horizontalcalendar.utils.Utils
import io.realm.Realm
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
    private val realm = Realm.getDefaultInstance()
    private val periodDates = realm.getPeriodDaysDecending()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cycleData = findCycleStartsAndPeriodEnds(periodDates)

        val cycleLengths = findCycleLengths(cycleData.cycleStarts)
        val periodLengths = findPeriodLengths(cycleData)

        Log.d(TAG, "cycleLengths: $cycleLengths, periodLengths: $periodLengths")

        if (cycleLengths.isNotEmpty())
            avg_cycle_length.text = cycleLengths.average().roundToInt().toString()
        else // if we don't have any data yet, just show what's been entered
            avg_cycle_length.text = realm.getCycleInfo().cycleLength.toString()

        if (periodLengths.isNotEmpty())
            avg_period_length.text = periodLengths.average().roundToInt().toString()
        else
            avg_period_length.text = realm.getCycleInfo().periodLength.toString()

        setupPreviousCycles(cycleData.cycleStarts, periodLengths, cycleLengths)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cycle_history, container, false)
    }

    private fun setupPreviousCycles(cycleStarts: List<Calendar>, periodLengths: List<Int>,
                                    cycleLengths: List<Int>) {
        val layout = GridLayoutManager(context, 3)
        val dividerItem = DividerItemDecoration(context, layout.orientation)
        val groupAdapter = GroupAdapter<GroupieViewHolder>()

        previous_cycles.apply {
            layoutManager = layout
            adapter = groupAdapter
            this.addItemDecoration(dividerItem)
        }

        listOf("Cycle Start", "Period Length", "Cycle Length").forEach {
            groupAdapter.add(HistoryItem(it))
        }

        val starts = cycleStarts.map { HistoryItem(it.dateString()) }
        // we need to add one to the cycle because the current cycle isn't complete
        val cycles = cycleLengths.map { HistoryItem(it.toString()) }
                .toMutableList()
        cycles.add(0, HistoryItem("-"))
        val periods = periodLengths.map { HistoryItem(it.toString()) }.toMutableList()
        // if the period is still going, we don't have a length for it
        if (periodLengths.size == cycleLengths.size)
            periods.add(0, HistoryItem("-"))

        starts.forEachIndexed {
            index, item -> groupAdapter.add(item)
            groupAdapter.add(periods[index])
            groupAdapter.add(cycles[index])
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
            }
            if (index == 0 || nextDate.formatDate() != periodDates[index-1]?.date) {
                periodEnds.add(dayData.date.toCalendar())
            }
        }

        return CycleData(cycleStarts, periodEnds)
    }

    //TODO check for off-by-ones and other math errors
    private fun findCycleLengths(cycleStarts: List<Calendar>): List<Int> {
        val cycleLengths = mutableListOf<Int>()
        cycleStarts.forEachIndexed {
            index, date ->
            if (index != 0) {
                cycleLengths.add(Utils.daysBetween(date, cycleStarts[index-1]))
            }
        }

        return cycleLengths
    }

    //TODO check for off-by-ones and other math errors
    private fun findPeriodLengths(cycleData: CycleData): List<Int> {
        val periodLengths = mutableListOf<Int>()
        cycleData.periodEnds.forEachIndexed {
            index, date ->
            if (!date.isToday()) {
                // we need to add 1 because days between doesn't count the last day of the period
                periodLengths.add(Utils.daysBetween(cycleData.cycleStarts[index], date) + 1)
            }
        }

        return periodLengths
    }

    companion object {
        const val TAG = "HISTORYVIEW"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CycleHistory.
         */
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

fun Calendar.dateString(): String {
    // kotlin does not seem to like java's string formatting. I guess I'll do it myself
    val month = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[this.get(Calendar.MONTH)]
    return "$month ${this.get(Calendar.DAY_OF_MONTH)}, ${this.get(Calendar.YEAR)}"
}