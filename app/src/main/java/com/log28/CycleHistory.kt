package com.log28


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.realm.RealmResults
import java.util.*
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 * Use the [CycleHistory.newInstance] factory method to
 * create an instance of this fragment.
 */
class CycleHistory : Fragment() {
    data class CycleData(val cycleStarts: List<Long>, val periodEnds: List<Long>)

    private val periodDates = getPeriodDaysDecending()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cycleData = findCycleStartsAndPeriodEnds(periodDates)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cycle_history, container, false)
    }

    private fun findCycleStartsAndPeriodEnds(periodDates: RealmResults<DayData>): CycleData {
        val cycleStarts = mutableListOf<Long>()
        val periodEnds = mutableListOf<Long>()

        periodDates.forEachIndexed { index, dayData ->
            val previousDate = dayData.date.toCalendar()
            val nextDate = dayData.date.toCalendar()
            previousDate.add(Calendar.DAY_OF_MONTH, -1)
            nextDate.add(Calendar.DAY_OF_MONTH, 1)

            if (index == periodDates.lastIndex || previousDate.formatDate() != periodDates[index+1]?.date) {
                cycleStarts.add(dayData.date)
            } else if (index == 0 || nextDate.formatDate() != periodDates[index-1]?.date) {
                periodEnds.add(dayData.date)
            }
        }
        Log.d("HISTORYVIEW", "cyclestarts $cycleStarts\nperiodEnds $periodEnds")

        return CycleData(cycleStarts, periodEnds)
    }

    private fun findAverageCycleLength(cycleStarts: List<Long>): Int {
        val cycleLengths = mutableListOf<Int>()
        cycleStarts.forEachIndexed {
            index, date ->
        }

        return cycleLengths.average().roundToInt()
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
