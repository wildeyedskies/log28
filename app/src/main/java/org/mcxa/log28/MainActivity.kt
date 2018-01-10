package org.mcxa.log28

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View


class MainActivity : AppCompatActivity() {
    private val tabIcons = arrayOf(R.drawable.ic_cycle, R.drawable.ic_plus, R.drawable.ic_calendar, R.drawable.ic_settings)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager.adapter = TabPagerAdapter(supportFragmentManager,
                this@MainActivity)

        // Give the TabLayout the ViewPager
        sliding_tabs.setupWithViewPager(viewPager)

        for (i in tabIcons.indices) {
            sliding_tabs.getTabAt(i)?.setIcon(tabIcons[i])
        }
    }
}
