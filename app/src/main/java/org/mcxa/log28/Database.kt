package org.mcxa.log28

import android.util.Log
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
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

    fun getPeriodDatesForMonth(year: Int, month: Int): List<Long> {
        val minDate = (year.toLong() * 10000) + (month.toLong() * 100)
        val maxDate = minDate + 32

        val days = (select from DayData::class where (date greaterThan minDate)
                and (date lessThan maxDate)
                and (physicalBleeding eq true)).list

        return days.map { d -> d.date }
    }
}