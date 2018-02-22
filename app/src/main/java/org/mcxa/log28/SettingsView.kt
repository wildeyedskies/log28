package org.mcxa.log28


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.preference.PreferenceDataStore
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat


/**
 * A simple [Fragment] subclass.
 */
class SettingsView : PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        preferenceManager.preferenceDataStore = RealmPreferenceDataStore()
    }

    companion object {

        fun newInstance(): SettingsView {
            val fragment = SettingsView()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}

// this class is kind of a hack. It persists preferences in a realm CycleInfo object
class RealmPreferenceDataStore: PreferenceDataStore() {
    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return super.getBoolean(key, defValue)
    }

    override fun putBoolean(key: String?, value: Boolean) {
        super.putBoolean(key, value)
    }

    override fun getString(key: String?, defValue: String?): String? {
        return super.getString(key, defValue)
    }

    override fun putString(key: String?, value: String?) {
        super.putString(key, value)
    }
}