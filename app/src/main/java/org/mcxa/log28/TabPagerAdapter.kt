package org.mcxa.log28

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.*

class TabPagerAdapter(fm: FragmentManager, private val context: Context) : FragmentPagerAdapter(fm) {
    private val PAGE_COUNT = 4

    override fun getCount(): Int {
        return PAGE_COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> CycleOverview.newInstance()
            1 -> DayView.newInstance(Calendar.getInstance())
            else -> PageFragment.newInstance(position + 1)
        }
    }
}