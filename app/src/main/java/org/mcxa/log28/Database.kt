package org.mcxa.log28

import android.content.Context
import android.support.v7.preference.PreferenceManager
import android.util.Log
import io.realm.*
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
open class Category(@PrimaryKey var name: String = "", var active: Boolean = true): RealmObject()

// represents an individual symptom bleeding, headaches, etc
open class Symptom(@PrimaryKey var name: String = "", var category: Category? = null, var active: Boolean = true): RealmObject() {
    override fun equals(other: Any?): Boolean {
        if (other !is Symptom) return false
        else return (this.name == other.name && this.category == other.category)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

open class CycleInfo(var cycleLength: Int, var periodLength: Int)

// represents data from a given day
open class DayData(@PrimaryKey var date: Long = Calendar.getInstance().formatDate(),
                   var symptoms: RealmList<Symptom> = RealmList(),
                   var notes: String = ""): RealmObject() {

    fun hasSymptom(symptom: String): Boolean {
        symptoms.forEach { if (it.name == symptom) return true }
        return false
    }

    // if the symptom is in symptoms, remove it, else add it
    fun toggleSymptom(context: Context?, symptom: Symptom) {
        realm.executeTransaction {
            if (symptom in symptoms)
                symptoms.remove(symptom)
            else
                symptoms.add(symptom)
        }
    }

    fun updateNotes(notes: String) {
        Realm.getDefaultInstance().executeTransaction {
            this.notes = notes
        }
    }
}

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
        localRelam ->
        // clear any extraneous data
        localRelam.where(Category::class.java).findAll().deleteAllFromRealm()
        localRelam.where(Symptom::class.java).findAll().deleteAllFromRealm()

        var i = 0
        categoryStrings.forEach {
            Log.d("DATABASE", "inserting category $it")
            var category = Category(it, true)
            category = localRelam.copyToRealm(category)

            symptomStrings[i].forEach {
                Log.d("DATABASE", "inserting symptom $it")
                val symptom = Symptom(it, category, true)
                localRelam.copyToRealm(symptom)
            }
            i++
        }
    }
}

fun setFirstPeriod(firstDay: Calendar, context: Context?) {
    val realm = Realm.getDefaultInstance()

    // get the period length
    val periodLength = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("period_length", "5").toIntOrNull() ?: 5


    realm.executeTransactionAsync { localRealm ->
        //clear data from previous attempts
        localRealm.where(DayData::class.java).findAll().deleteAllFromRealm()

        // get the bleeding symptom
        //crash if we do not find this
        val bleeding = localRealm.where(Symptom::class.java).equalTo("name", "Bleeding").findFirst()!!

        // for each day in the period create a DayData object with physical  bleeding
        // set to true, and save it in the database
        for (i in 0 until periodLength) {
            val day = DayData(firstDay.formatDate(), RealmList(bleeding))
            localRealm.copyToRealm(day)
            firstDay.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

fun getPeriodDates(): RealmResults<DayData> {
    val realm = Realm.getDefaultInstance()
    return realm.where(DayData::class.java)
            .equalTo("symptoms.name", "Bleeding").findAllAsync()
}

fun getDataByDate(queryDate: Calendar): DayData {
    val realm = Realm.getDefaultInstance()

    realm.beginTransaction()
    val daydata = realm.where(DayData::class.java)
            .equalTo("date", queryDate.formatDate()).findFirst() ?:
            realm.createObject(DayData::class.java, queryDate.formatDate())
    realm.commitTransaction()

    return daydata
}

fun getPeriodDaysDecending(): RealmResults<DayData> {
    val realm = Realm.getDefaultInstance()
    return Realm.getDefaultInstance().where(DayData::class.java)
            .equalTo("symptoms.name", "Bleeding").sort("date", Sort.DESCENDING).findAll()
}

fun getCategories(): RealmResults<Category> {
    return Realm.getDefaultInstance().where(Category::class.java).findAll()
}

fun getSymptoms(): RealmResults<Symptom> {
    return Realm.getDefaultInstance().where(Symptom::class.java).findAll()
}