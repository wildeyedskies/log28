package org.mcxa.log28


import android.os.Bundle
import android.support.v4.app.Fragment
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat


/**
 * A simple [Fragment] subclass.
 */
class SettingsView : PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CalendarView.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): SettingsView {
            val fragment = SettingsView()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }


}