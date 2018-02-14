package org.mcxa.log28

import android.content.Context
import android.support.v7.preference.PreferenceManager
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.Sort
import io.realm.annotations.PrimaryKey
import java.util.Calendar

// format a date as yyyymmdd
fun Calendar.formatDate(): Long {
    return (this.get(Calendar.YEAR).toLong() * 10000) +
            (this.get(Calendar.MONTH).toLong() * 100) +
            this.get(Calendar.DAY_OF_MONTH).toLong()
}

// parse a yyyymmdd long
fun Long.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, (this / 10000).toInt())
    cal.set(Calendar.MONTH, (this / 100).toInt() % 100)
    cal.set(Calendar.DAY_OF_MONTH, (this % 100).toInt())
    return cal
}

// represents a category physical, mental, etc
open class Category(@PrimaryKey val name: String, var active: Boolean): RealmObject()

// represents an individual symptom bleeding, headaches, etc
open class Symptom(@PrimaryKey val name: String, val category: Category, var active: Boolean): RealmObject()

// represents data from a given day
open class DayData(
        @PrimaryKey val date: Long,
        val symptoms: RealmList<Symptom>
): RealmObject()

fun initializeRealm(context: Context) {
    val realm = Realm.getDefaultInstance()
    val categoryStrings = context.resources.getStringArray(R.array.categories)
    val symptomStrings = listOf(
            context.resources.getStringArray(R.array.physical_symptoms),
            context.resources.getStringArray(R.array.mental_symptoms),
            context.resources.getStringArray(R.array.physical_activity),
            context.resources.getStringArray(R.array.sexual_activity)
    )
    realm.executeTransactionAsync {
        localRelam -> var i = 0
        categoryStrings.forEach {
            val category = Category(it, true)
            localRelam.copyToRealm(category)

            symptomStrings[i].forEach {
                val symptom = Symptom(it, category, true)
                localRelam.copyToRealm(symptom)
            }
            i++
        }
    }
}

fun setFirstPeriod(firstDay: Calendar, context: Context?) {
    val realm = Realm.getDefaultInstance()
    val oldData = realm.where(DayData::class.java).findAll()
    // get the bleeding symptom
    val bleeding = realm.where(Symptom::class.java).equalTo("name", "Bleeding").findFirst()!! //crash if we do not find this

    // get the period length
    val periodLength = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("period_length", "5").toIntOrNull() ?: 5


    realm.executeTransactionAsync { localRealm ->
        //clear data from previous attempts
        oldData.deleteAllFromRealm()
        // for each day in the period create a DayData object with physical  bleeding
        // set to true, and save it in the database
        for (i in 0 until periodLength) {
            val day = DayData(firstDay.formatDate(), RealmList(bleeding))
            localRealm.copyToRealm(day)
            firstDay.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

fun getPeriodDates(): Observable<DayData> {
    val realm = Realm.getDefaultInstance()
    return realm.where(DayData::class.java).equalTo("symptoms.name", "Bleeding").findAllAsync().toObservable()
}

fun getDataByDate(queryDate: Calendar): DayData? {
    return Realm.getDefaultInstance().where(DayData::class.java).equalTo("date", queryDate.formatDate()).findFirst()
}

fun getStartOfCurrentCycle(): Long? {
    val realm = Realm.getDefaultInstance()
    val periodDays = realm.where(DayData::class.java)
            .equalTo("symptoms.name", "Bleeding").sort("date", Sort.DESCENDING).findAll()
    //TODO make this handle the case of a period longer than 30 days straight
    val dates = periodDays.subList(0, 30).map { d -> d.date }
    return dates.filter { it - 1 !in dates }.max()
}