package com.log28

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import com.log28.intro.AppIntroActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firstStart = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("first_start", true)

        if (firstStart) {
            val intent = Intent(this, AppIntroActivity::class.java)
            Log.d(TAG, "starting app intro")
            // set up realm
            initializeRealm(this)
            startActivity(intent)
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        val pagerAdapter = TabPagerAdapter(supportFragmentManager,
                this@MainActivity)
        viewPager.adapter = pagerAdapter

        // Give the TabLayout the ViewPager
        sliding_tabs.setupWithViewPager(viewPager)

        for (i in pagerAdapter.tabText.indices) {
            sliding_tabs.getTabAt(i)?.customView = pagerAdapter.getTabView(i)
        }
    }

    // setup the options menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return false
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
        (viewPager.adapter as? TabPagerAdapter)?.setDayViewDay(day)
    }

    companion object {
        const val TAG = "MAIN"
    }
}
