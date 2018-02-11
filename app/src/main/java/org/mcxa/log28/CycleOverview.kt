package org.mcxa.log28


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import devs.mulham.horizontalcalendar.utils.Utils
import kotlinx.android.synthetic.main.fragment_cycle_overview.*
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [CycleOverview.newInstance] factory method to
 * create an instance of this fragment.
 */
class CycleOverview : Fragment() {
    // whenever the cycle or period lengths change, recalculate everything
    private val prefListener = {
        _: SharedPreferences, key: String ->
            if (key == "cycle_length" || key == "period_length") calculateNextPeriod()
    }

    private val modelChangeListener = {
        _: DayData -> calculateNextPeriod()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(prefListener)
        DayData.registerForPeriodUpdates(modelChangeListener)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cycle_overview, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(prefListener)
        DayData.unregisterForPeriodUpdates(modelChangeListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        today_log_list.layoutManager = layoutManager

        calculateNextPeriod()
    }

    //TODO there might be an off by one error here
    private fun calculateNextPeriod() {
        Log.d("OVERVIEW", "calculating next period")
        AppDatabase.getStartOfCurrentCycle {
            date ->
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val cycleLength = prefs.getString("cycle_length", "28").toInt()
            val periodLength = prefs.getString("period_length", "5").toInt()
            val cycleStart = date!!.toCalendar()

            // updateModel the text views with the correct days until
            val cycleDay = Utils.daysBetween(cycleStart, Calendar.getInstance())
            Log.d("OVERVIEW", "cycle length $cycleLength, periodLength $periodLength cycle day is $cycleDay")
            // on period
            if (cycleDay < periodLength) {
                days_until_text.text = getString(R.string.days_left_in_period)
                days_until_number.text = (periodLength - cycleDay).toString()
            } else if (cycleDay < cycleLength) {
                days_until_text.text = getString(R.string.days_until_period)
                days_until_number.text = (cycleLength - cycleDay).toString()
            } else {
                days_until_text.text = getString(R.string.days_late)
                days_until_number.text = (cycleDay - cycleLength).toString()
            }

            // updateModel the progress bar
            cycle_view.setCycleData(cycleLength, periodLength, cycleDay)
            this.view?.postInvalidate()
        }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CycleOverview.
         */
        fun newInstance(): CycleOverview {
            val fragment = CycleOverview()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}

// this class draws the custom graphic for showing the cycle
// it is not designed to be used outside of this fragment
class CycleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val paintRed = Paint(Paint.ANTI_ALIAS_FLAG)
    val paintGrey = Paint(Paint.ANTI_ALIAS_FLAG)
    val paintIndicator = Paint(Paint.ANTI_ALIAS_FLAG)

    var periodLength = 5
    var cycleLength = 28
    var cycleDay: Int? = null

    init {
        paintRed.color = Color.parseColor("#D32F2F")
        paintRed.style = Paint.Style.FILL
        paintGrey.color = Color.parseColor("#BDBDBD")
        paintGrey.style = Paint.Style.FILL
        paintIndicator.color = Color.parseColor("#FFFFFF")
        paintIndicator.alpha = 80
        paintIndicator.style = Paint.Style.FILL
    }

    fun setCycleData(cycleLength: Int, periodLength: Int, cycleDay: Int) {
        this.periodLength = periodLength
        this.cycleLength = cycleLength
        this.cycleDay = cycleDay
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))
    }

    private fun measureWidth(widthMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(widthMeasureSpec)
        val size = MeasureSpec.getSize(widthMeasureSpec)

        if (mode == MeasureSpec.EXACTLY)
            return size
        if (mode == MeasureSpec.AT_MOST)
            return size
        else
            return dpOrSpToPx(context, 82.toFloat()).toInt()
    }

    private fun measureHeight(heightMeasureSpec: Int): Int {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(heightMeasureSpec)

        if (mode == MeasureSpec.EXACTLY)
            return size
        else
            return dpOrSpToPx(context, 32.toFloat()).toInt()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRect(0.toFloat(),0.toFloat(),width.toFloat(),height.toFloat(), paintGrey)
        canvas?.drawRect(0.toFloat(),0.toFloat(),width * periodLength / cycleLength.toFloat(),height.toFloat(), paintRed)

        // only draw the cycle indicator if the period is not late
        if (cycleDay != null && cycleDay!! < cycleLength) {
            canvas?.drawRect(width.toFloat() * (cycleDay!! + 1) / cycleLength,0.toFloat(),width.toFloat(),
                    height.toFloat(), paintIndicator)
        }
    }

    // convert a dp or sp value to px
    private fun dpOrSpToPx(context: Context, dpOrSpValue: Float): Float {
        return dpOrSpValue * context.resources.displayMetrics.density
    }
}