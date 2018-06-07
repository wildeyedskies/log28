package com.log28

import android.content.Context
import android.support.v7.preference.PreferenceManager
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

// class contains all the realm queries for the application
// queries are set as extension functions to Realm so you can call realm.<query>()

fun Realm.setFirstPeriod(firstDay: Calendar, context: Context?) {
    // get the period length
    val periodLength = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("period_length", "5").toIntOrNull() ?: 5


    this.executeTransactionAsync { localRealm ->
        //clear data from previous attempts
        localRealm.where(DayData::class.java).findAll().forEach {
            it.symptoms.clear()
        }

        // get the bleeding symptom
        //crash if we do not find this
        val bleeding = localRealm.where(Symptom::class.java).equalTo("name", "Bleeding").findFirst()!!

        // for each day in the period create a DayData object with physical  bleeding
        // set to true, and save it in the database
        for (i in 0 until periodLength) {
            val day = localRealm.where(DayData::class.java).equalTo("date", firstDay.formatDate()).findFirst() ?:
            localRealm.createObject(DayData::class.java, firstDay.formatDate())
            day.symptoms = RealmList(bleeding)

            // do not set days in the future
            if (firstDay.isToday()) break
            firstDay.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

fun Realm.getPeriodDates(): RealmResults<DayData> {
    return this.where(DayData::class.java)
            .equalTo("symptoms.name", "Bleeding").findAllAsync()
}

fun Realm.getDataByDate(queryDate: Calendar): DayData {
    this.beginTransaction()
    val daydata = this.where(DayData::class.java)
            .equalTo("date", queryDate.formatDate()).findFirst() ?:
    this.createObject(DayData::class.java, queryDate.formatDate())
    this.commitTransaction()

    return daydata
}

fun Realm.getPeriodDaysDecending(): RealmResults<DayData> {
    return this.where(DayData::class.java)
            .equalTo("symptoms.name", "Bleeding").sort("date", Sort.DESCENDING).findAll()
}

fun Realm.getActiveCategories(): RealmResults<Category> {
    return this.where(Category::class.java).equalTo("active", true).findAll()
}

fun Realm.getActiveSymptoms(): RealmResults<Symptom> {
    return this.where(Symptom::class.java).equalTo("active", true).findAll()
}

fun Realm.getCategories(): RealmResults<Category> {
    return this.where(Category::class.java).findAll()
}

fun Realm.getSymptoms(): RealmResults<Symptom> {
    return this.where(Symptom::class.java).findAll()
}
