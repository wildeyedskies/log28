package com.log28


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceDataStore
import android.util.Log
import android.util.SparseArray
import android.view.MenuItem
import android.widget.Toast
import com.github.isabsent.filepicker.SimpleFilePickerDialog
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File

// we need to access this in both SettingsView and SettingsActivity
private val callBackArray = SparseArray<()->Unit>()
// used SDK < 19 (file picker dialogue)
private val SELECT_RESTORE_FILE_TAG = "selectRestore"

/**
 * A simple [Fragment] subclass.
 */
class SettingsView : PreferenceFragmentCompat() {
    private val BACKUP_REQUEST = 1
    private val RESTORE_REQUEST = 2

    // used for SDK => 19 (storage access framework)
    private val SELECT_RESTORE_FILE_CODE = 0

    private val realm = Realm.getDefaultInstance()

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {

        preferenceManager.preferenceDataStore = RealmPreferenceDataStore(context, realm)
        setPreferencesFromResource(R.xml.preferences, rootKey);

        callBackArray.put(BACKUP_REQUEST, backupDatabase)
        callBackArray.put(RESTORE_REQUEST, restoreDatabase)

        findPreference("backup_data")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            checkPermissionAndExecute(BACKUP_REQUEST)
            true
        }
        findPreference("restore_data")?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            checkPermissionAndExecute(RESTORE_REQUEST)
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private val backupDatabase = {
        Log.d("SETTINGS", "backing up realm")
        val path = exportDBToLocation(Environment.getExternalStorageDirectory())
        Toast.makeText(context, resources.getString(R.string.db_exported, path), Toast.LENGTH_LONG).show()
    }

    private val restoreDatabase = {
        // SDK < 19 we cannot use storage access framework
        if (Build.VERSION.SDK_INT < 19) {
            SimpleFilePickerDialog.build(Environment.getExternalStorageDirectory().absolutePath,
                    SimpleFilePickerDialog.CompositeMode.FILE_ONLY_SINGLE_CHOICE)
                    .title(resources.getString(R.string.select_restore))
                    .show(this, SELECT_RESTORE_FILE_TAG)
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, SELECT_RESTORE_FILE_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_RESTORE_FILE_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data

            val success = importDBFromUri(uri, context!!)
            if (success) Toast.makeText(context!!, resources.getString(R.string.restore_success), Toast.LENGTH_LONG).show()
            else Toast.makeText(context!!, resources.getString(R.string.restore_failed), Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermissionAndExecute(callbackValue: Int) {
        // on pre-marshmellow just call the function and return
        // we don't need to handle runtime permissions
        if (Build.VERSION.SDK_INT < 23) {
            callBackArray[callbackValue].invoke()
            return
        }

        val permission = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this.activity as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    callbackValue
            )
        } else callBackArray[callbackValue].invoke()
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

class SettingsActivity : AppCompatActivity(), SimpleFilePickerDialog.InteractionListenerString {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        // draw the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    // exit when the back button is pressed
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    // This doesn't work from the SettingsView fragment, works here though
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callBackArray[requestCode]?.invoke()
        } else {
            Toast.makeText(this, resources.getString(R.string.permission_error), Toast.LENGTH_LONG).show()
        }
    }

    // dialogue result
    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
        when (dialogTag) {
            // this only happens SDK < 19
            SELECT_RESTORE_FILE_TAG -> {
                val path = extras.getString(SimpleFilePickerDialog.SELECTED_SINGLE_PATH)

                val success = importDBFromFile(File(path), this)
                if (success) Toast.makeText(this, resources.getString(R.string.restore_success), Toast.LENGTH_LONG).show()
                else Toast.makeText(this, resources.getString(R.string.restore_failed), Toast.LENGTH_LONG).show()
            }
        }
        return false
    }

    override fun showListItemDialog(title: String?, folderPath: String?, mode: SimpleFilePickerDialog.CompositeMode?, dialogTag: String?) {
        SimpleFilePickerDialog.build(folderPath, mode)
                .title(title)
                .show(this, dialogTag)
    }
}

// this class is kind of a hack. It persists preferences in a realm CycleInfo object
class RealmPreferenceDataStore(context: Context?, private val realm: Realm): PreferenceDataStore() {
    private val mentalSymptoms = context?.resources!!.getStringArray(R.array.categories)[1]
    private val physicalActivity = context?.resources!!.getStringArray(R.array.categories)[2]
    private val sexualActivity = context?.resources!!.getStringArray(R.array.categories)[3]

    //TODO clean this up once we're sure it works
    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return when(key) {
            "mental_tracking" ->
                realm.where(Category::class.java)
                        .equalTo("name", mentalSymptoms).findFirst()?.active ?: defValue
            "physical_tracking" ->
                realm.where(Category::class.java)
                    .equalTo("name", physicalActivity).findFirst()?.active ?: defValue
            "sexual_tracking" ->
                realm.where(Category::class.java)
                        .equalTo("name", sexualActivity).findFirst()?.active ?: defValue
            else -> super.getBoolean(key, defValue)
        }
    }

    override fun putBoolean(key: String?, value: Boolean) {
        Log.d(TAG, "put boolean called for $key")
        when(key) {
            "mental_tracking" -> realm.setCategoryState(mentalSymptoms, value)
            "physical_tracking" -> realm.setCategoryState(physicalActivity, value)
            "sexual_tracking" -> realm.setCategoryState(sexualActivity, value)
            else -> super.putBoolean(key, value)
        }
    }

    override fun getString(key: String?, defValue: String?): String? {
        Log.d(TAG, "get string called for $key")
        return when(key) {
            "period_length" -> realm.getCycleInfo().periodLength.toString()
            "cycle_length" -> realm.getCycleInfo().cycleLength.toString()
            else -> super.getString(key, defValue)
        }
    }

    override fun putString(key: String?, value: String?) {
        when(key) {
            "period_length" -> realm.executeTransactionAsync {
                it.where(CycleInfo::class.java).findFirst()?.periodLength = value!!.toInt()
            }
            "cycle_length" -> realm.executeTransactionAsync {
                it.where(CycleInfo::class.java).findFirst()?.cycleLength = value!!.toInt()
            }
            else -> super.putString(key, value)
        }
    }

    companion object {
        const val TAG = "SETTINGS"
    }
}