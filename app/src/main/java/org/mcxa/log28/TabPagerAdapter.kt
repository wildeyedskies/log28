package org.mcxa.log28

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.*

class TabPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {
    private val PAGE_COUNT = 4
    lateinit var setDayViewDay: (c: Calendar) -> Unit

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> CycleOverview.newInstance()
            1 -> {
                val view = DayView.newInstance()
                setDayViewDay = {
                    c -> view.navigateToDay.invoke(c)
                }
                view
            }
            2 -> CalendarView.newInstance()
            3 -> SettingsView.newInstance()
            else -> CycleOverview.newInstance()
        }
    }

}