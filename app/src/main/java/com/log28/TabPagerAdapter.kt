package com.log28

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.custom_tab.view.*
import java.util.*

class TabPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {
    private val tabIcons = arrayOf(R.drawable.ic_cycle, R.drawable.ic_plus, R.drawable.ic_calendar, R.drawable.ic_file_chart)
    val tabText = context.resources.getStringArray(R.array.tab_names)

    private val PAGE_COUNT = 4
    private lateinit var dayView: DayView
    private var dayViewDay = Calendar.getInstance()

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> CycleOverview.newInstance()
            1 -> {
                val view = DayView.newInstance(dayViewDay)
                dayView = view
                view
            }
            2 -> CalendarView.newInstance()
            3 -> CycleHistory.newInstance()
            else -> CycleOverview.newInstance()
        }
    }

    //TODO find a cleaner way to do this
    // if the day view has been initialized, run navToDay, otherwise store the day
    // and set it when the day view is created
    fun setDayViewDay(day: Calendar) {
        if (this::dayView.isInitialized) {
            dayView.navigateToDay.invoke(day)
        } else {
            dayViewDay = day
        }
    }

    fun getTabView(position: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_tab, null)
        view.tab_text.text = tabText[position]
        view.tab_icon.setImageResource(tabIcons[position])

        return view
    }

}