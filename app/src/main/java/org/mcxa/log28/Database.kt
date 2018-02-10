package org.mcxa.log28

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.sql.language.SQLite
import org.mcxa.log28.DayData_Table.date
import org.mcxa.log28.DayData_Table.physicalBleeding
import java.util.*

// format a date as yyyymmdd
fun Calendar.formatDate(): Long {
    return (this.get(Calendar.YEAR).toLong() * 10000) +
            (this.get(Calendar.MONTH).toLong() * 100) +
            this.get(Calendar.DAY_OF_MONTH).toLong()
}

// all the logic for the log28 database
@Table(database = AppDatabase::class)
class DayData(@PrimaryKey var date: Long = Calendar.getInstance().formatDate(),
               @Column(getterName = "getPhysicalBleeding") var physicalBleeding: Boolean = false,
               @Column(getterName = "getPhysicalSpotting") var physicalSpotting: Boolean = false,
               @Column(getterName = "getPhysicalCramping") var physicalCramping: Boolean = false,
               @Column(getterName = "getPhysicalHeadache") var physicalHeadache: Boolean = false,
               @Column(getterName = "getMentalMoodSwings") var mentalMoodSwings: Boolean = false,
               @Column(getterName = "getMentalAnxious") var mentalAnxious: Boolean = false,
               @Column(getterName = "getMentalHappy") var mentalHappy: Boolean = false,
               @Column(getterName = "getMentalSad") var mentalSad: Boolean = false,
               @Column(getterName = "getMentalTired") var mentalTired: Boolean = false) {

    fun getItemState(catIndex: Int, itemIndex: Int): Boolean {
        //TODO update to when statement
        return arrayListOf(
                arrayListOf(
                        physicalBleeding,
                        physicalSpotting,
                        physicalCramping,
                        physicalHeadache
                ),
                arrayListOf(
                        mentalMoodSwings,
                        mentalAnxious
                )
        )[catIndex][itemIndex]
    }

    // updates a column based on the index into the map
    fun update(catIndex: Int, itemIndex: Int) {
        Log.d("DAYDATA", "Update symptom called")

        when(catIndex) {
            0 -> when(itemIndex) {
                0 -> physicalBleeding = !physicalBleeding
                1 -> physicalSpotting = !physicalSpotting
                2 -> physicalCramping = !physicalCramping
                3 -> physicalHeadache = !physicalHeadache
            }
            1 -> when(itemIndex) {
                0 -> mentalMoodSwings = !mentalMoodSwings
                1 -> mentalAnxious = !mentalAnxious
            }
        }

        if (this.exists()) {
            Log.d("DAYDATA", "Updating day object for " + date.toString())
            this.update()
        }
        else {
            Log.d("DAYDATA", "Creating new day object for " + date.toString())
            this.save()
        }
    }

    // static ordering of the members used to render the expandable list
    companion object {
        val categories = arrayListOf("Physical Symptoms", "Mental Symptoms",
                "Sexual Activity", "Physical Activity")

        val items = arrayListOf(
                arrayListOf(
                        "Bleeding",
                        "Spotting",
                        "Cramping",
                        "Headaches"
                ),
                arrayListOf(
                        "Mood swings",
                        "Anxious"
                )
        )
    }
}

@Database(version = AppDatabase.VERSION)
object AppDatabase {
    const val VERSION = 1

    fun getDataByDate(queryDate: Calendar): DayData? {
        return (select from DayData::class where (date eq queryDate.formatDate())).result
    }

    fun setFirstPeriod(firstDay: Calendar, context: Context?) {
        AsyncTask.execute {
            // the kotlin extension syntax for deleting wasn't working
            SQLite.delete().from(DayData::class.java).execute()

            // get the period length
            val periodLength = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString("period_length", "5").toIntOrNull() ?: 5

            // for each day in the period create a DayData object with physical  bleeding
            // set to true, and save it in the database
            for (i in 0 until periodLength) {
                val day = DayData(firstDay.formatDate(), physicalBleeding = true)
                day.save()
                firstDay.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    //days must be formatted as yyyymmdd
    fun getPeriodDates(onComplete: (List<Long>) -> Unit) {
        (select from DayData::class
                where (physicalBleeding eq true)).async list
                {transaction, list -> onComplete.invoke(list.map { d -> d.date })}
    }
}