package org.mcxa.log28

import android.util.Log
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.*
import org.mcxa.log28.DayData_Table.date
import java.util.*

fun Calendar.formatDate(): String {
    return this.get(Calendar.YEAR).toString() + this.get(Calendar.MONTH).toString() + this.get(Calendar.DAY_OF_MONTH).toString()
}

// all the logic for the log28 database
@Table(database = AppDatabase::class)
class DayData(@PrimaryKey var date: String = Calendar.getInstance().formatDate(),
               @Column(getterName = "getPhysicalBleeding") var physicalBleeding: Boolean = false,
               @Column(getterName = "getPhysicalSpotting") var physicalSpotting: Boolean = false,
               @Column(getterName = "getPhysicalCramping") var physicalCramping: Boolean = false,
               @Column(getterName = "getPhysicalHeadache") var physicalHeadache: Boolean = false,
               @Column(getterName = "getMentalMoodSwings") var mentalMoodSwings: Boolean = false,
               @Column(getterName = "getMentalAnxious") var mentalAnxious: Boolean = false,
               @Column(getterName = "getMentalHappy") var mentalHappy: Boolean = false,
               @Column(getterName = "getMentalSad") var mentalSad: Boolean = false,
               @Column(getterName = "getMentalTired") var mentalTired: Boolean = false) {

    // this function formats the data for the ExpandableListView
    fun buildSymptomMap() : Map<String,List<Symptom>> {
        Log.d("DAYDATA", "building symptom map")
        return mapOf(
                "Physical" to arrayListOf<Symptom>(
                        Symptom("Bleeding", physicalBleeding),
                        Symptom("Spotting", physicalSpotting),
                        Symptom("Cramping", physicalCramping),
                        Symptom("Headache", physicalHeadache)
                ),
                "Mental" to arrayListOf<Symptom>(
                        Symptom("Mood Swings", mentalMoodSwings),
                        Symptom("Anxious", mentalAnxious)
                )
        )
    }

    // updates a column based on the index into the map
    fun updateSymptom(catIndex: Int, itemIndex: Int) {
        Log.d("DAYDATA", "Update symptoms called")
        val map = mapOf(
            "Physical" to arrayListOf(
                    { physicalBleeding = !physicalBleeding },
                    { physicalSpotting = !physicalSpotting },
                    { physicalCramping = !physicalCramping },
                    { physicalHeadache = !physicalHeadache }
            ),
            "Mental" to arrayListOf(
                    { mentalMoodSwings = !mentalMoodSwings },
                    { mentalAnxious = !mentalAnxious }
            )
        )

        val category = map.keys.toList()[catIndex]
        map[category]?.get(itemIndex)?.invoke()

        if (this.exists()) {
            Log.d("DAYDATA", "Updating day object for " + date.toString())
            this.update()
        }
        else {
            Log.d("DAYDATA", "Creating new day object for " + date.toString())
            this.save()
        }
    }
}

@Database(version = AppDatabase.VERSION)
object AppDatabase {
    const val VERSION = 1

    fun getDataByDate(queryDate: Calendar): DayData? {
        return (select from DayData::class where (date eq queryDate.formatDate())).result
    }
}