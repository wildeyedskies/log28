package org.mcxa.log28

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.mcxa.log28.org.mcxa.log28.intro.AppIntroActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private val tabIcons = arrayOf(R.drawable.ic_cycle, R.drawable.ic_plus, R.drawable.ic_calendar, R.drawable.ic_settings)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // start the intro if it's the first run of the application
        AsyncTask.execute {
            // try to get the first start variable, default to true if it is unset
            val firstStart = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_start", true)

            if (firstStart) {
                val intent = Intent(this, AppIntroActivity::class.java)

                runOnUiThread {
                    Log.d("MAIN", "starting app intro")
                    startActivity(intent)
                }
                // update the preference preventing first start on subsequent runs
                val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                editor.putBoolean("first_start", false)
                // by default all our tracking options should be on
                editor.putBoolean("mental_tracking", true)
                editor.putBoolean("physical_tracking", true)
                editor.putBoolean("sexual_tracking", true)
                editor.apply()
            }
        }

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

    /**
     * Set the current tab to the day view
     * and set the day based on the day based on the calendar parameter
     * the TabPagerAdapter keeps a copy of the setDayViewDay function pointer which is
     * created when the day view is created
     */
    fun navToDayView(day: Calendar) {
        // go to the index of the day view
        viewPager.currentItem = 1
        (viewPager.adapter as? TabPagerAdapter)?.setDayViewDay?.invoke(day)
    }
}
