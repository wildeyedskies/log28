package com.log28

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.log28.databinding.ActivityMainBinding
import com.log28.intro.AppIntroActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private val SETTINGS_CODE = 3392
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val firstStart = preferences.getBoolean("first_start", true)
        // we added appetite in a later version, this code checks if the current system has been updated
        val appetitePresent = preferences.getBoolean("appetite_present", false)

        if (firstStart) {
            val intent = Intent(this, AppIntroActivity::class.java)
            Log.d(TAG, "starting app intro")
            // set up realm
            initializeRealm(this)
            // if this is the first start, the initialize will insert appetite
            preferences.edit().putBoolean("appetite_present", true).apply()
            startActivity(intent)
        } else if (!appetitePresent) {
            insertAppetite(this)
            preferences.edit().putBoolean("appetite_present", true).apply()
        }

        setContentView(view)
        setSupportActionBar(binding.toolbar)

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        val pagerAdapter = TabPagerAdapter(supportFragmentManager,
                this@MainActivity)
        binding.viewPager.adapter = pagerAdapter

        // Give the TabLayout the ViewPager
        binding.slidingTabs.setupWithViewPager(binding.viewPager)

        for (i in pagerAdapter.tabText.indices) {
            binding.slidingTabs.getTabAt(i)?.customView = pagerAdapter.getTabView(i)
        }
    }

    // setup the options menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, SETTINGS_CODE)
                return true
            }
        }
        return false
    }

    // code to close app if database is imported so we can restart
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == SETTINGS_CODE) {
            if (data?.getBooleanExtra("exitMain", false) == true) finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Set the current tab to the day view
     * and set the day based on the day based on the calendar parameter
     * the TabPagerAdapter keeps a copy of the setDayViewDay function pointer which is
     * created when the day view is created
     */
    fun navToDayView(day: Calendar) {
        // go to the index of the day view
        binding.viewPager.currentItem = 1
        (binding.viewPager.adapter as? TabPagerAdapter)?.setDayViewDay(day)
    }

    companion object {
        const val TAG = "MAIN"
    }
}
