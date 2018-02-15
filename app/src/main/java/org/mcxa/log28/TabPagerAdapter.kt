package org.mcxa.log28

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.custom_tab.view.*
import java.util.*

class TabPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {
    private val tabIcons = arrayOf(R.drawable.ic_cycle, R.drawable.ic_plus, R.drawable.ic_calendar, R.drawable.ic_settings)
    val tabText = context.resources.getStringArray(R.array.tab_names)

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

    fun getTabView(position: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_tab, null)
        view.tab_text.text = tabText[position]
        view.tab_icon.setImageResource(tabIcons[position])

        return view
    }

}