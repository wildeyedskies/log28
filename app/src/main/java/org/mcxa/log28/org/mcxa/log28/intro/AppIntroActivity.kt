package org.mcxa.log28.org.mcxa.log28.intro

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment
import org.mcxa.log28.R

class AppIntroActivity: AppIntro2() {
    var setupComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Note that we do not let users skip this because we need to
        // know cycle and period lengths for the app to function
        showSkipButton(false)

        addSlide(AppIntro2Fragment.newInstance("Welcome to log28",
                "log28 will help you keep track of your period", R.drawable.ic_notebook, Color.parseColor("#1976D2")))
        addSlide(CycleIntroFragment.newInstance())
        addSlide(LastPeriodFragment.newInstance())

        setBarColor(Color.parseColor("#0D47A1"));
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        if (setupComplete) {
            AsyncTask.execute {
                // updateModel the preference preventing first start on subsequent runs
                val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                editor.putBoolean("first_start", false)
                // by default all our tracking options should be on
                editor.putBoolean("mental_tracking", true)
                editor.putBoolean("physical_tracking", true)
                editor.putBoolean("sexual_tracking", true)
                editor.apply()

            }
            finish()
        }
    }
}