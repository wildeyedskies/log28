package com.log28

import android.content.Context
import android.net.Uri
import android.util.Log
import io.realm.*
import io.realm.annotations.PrimaryKey
import java.io.File
import java.util.*

private val REALM_FILE_NAME = "default.realm" // change if using custom DB name
private val TMP_REALM_FILE_NAME = "tmp.realm" // first we copy the file to a tmp name to see if it can be opened

private val TAG = "DATABASE"

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

// this code will be used when custom symptoms are added
/*val config = RealmConfiguration.Builder()
        .schemaVersion(2)
        .migration(Migration())
        .build()

class Migration: RealmMigration {
    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
        if (oldVersion < 2) {
            val category = Category("Appetite")
            realm?.executeTransaction {
            }
        }
    }
}*/

// represents a category physical, mental, etc
open class Category(@PrimaryKey var name: String = "", var active: Boolean = true): RealmObject()

fun Realm.setCategoryState(name: String, active: Boolean) {
    this.executeTransactionAsync {
        val mental = it.where(Category::class.java).equalTo("name",
                name).findFirst()
        mental?.active = active
    }
}

// represents an individual symptom bleeding, headaches, etc
open class Symptom(@PrimaryKey var name: String = "",
                   var category: Category? = null,
                   var active: Boolean = true): RealmObject() {
    override fun equals(other: Any?): Boolean {
        if (other !is Symptom) return false
        else return (this.name == other.name && this.category == other.category)
    }

    fun toggleActive() {
        realm.executeTransaction {
            this.active = !this.active
        }
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

open class CycleInfo(var cycleLength: Int = 28, var periodLength: Int = 5): RealmObject()

fun Realm.getCycleInfo(): CycleInfo {
    val realm = this

    realm.beginTransaction()
    val cycleInfo =  realm.where(CycleInfo::class.java).findFirst() ?:
        realm.createObject(CycleInfo::class.java)
    realm.commitTransaction()
    return cycleInfo
}

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
        realm.executeTransaction {
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
            context.resources.getStringArray(R.array.sexual_activity),
            context.resources.getStringArray(R.array.appetite)
    )
    realm.executeTransactionAsync {
        localRealm ->
        // clear any extraneous data
        localRealm.where(Category::class.java).findAll().deleteAllFromRealm()
        localRealm.where(Symptom::class.java).findAll().deleteAllFromRealm()

        var i = 0
        categoryStrings.forEach {
            categoryString ->
            Log.d(TAG, "inserting category $categoryString")
            var category = Category(categoryString, true)
            category = localRealm.copyToRealm(category)

            symptomStrings[i].forEach {
                Log.d(TAG, "inserting symptom $it")
                val symptom = Symptom(it, category, true)
                localRealm.copyToRealm(symptom)
            }
            i++
        }
    }
    realm.close()
}

fun insertAppetite(context: Context) {
    Log.d(TAG, "Insert appetite called")
    val realm = Realm.getDefaultInstance()
    val appetite = context.resources.getStringArray(R.array.categories).get(4)
    val appetiteSymptoms = context.resources.getStringArray(R.array.appetite)

    realm.executeTransactionAsync {
        localRealm ->
        val appetiteCategory = Category(appetite, true)
        localRealm.copyToRealm(appetiteCategory)

        appetiteSymptoms.forEach {
            val symptom = Symptom(it, appetiteCategory, true)
            localRealm.copyToRealm(symptom)
        }
    }
}

fun exportDBToLocation(location: File): String {
    val outputFile = File(location, "log28-backup-${Calendar.getInstance().formatDate()}")
    if (outputFile.exists()) outputFile.delete()

    val realm = Realm.getDefaultInstance()
    realm.writeCopyTo(outputFile)
    realm.close()
    return outputFile.absolutePath
}

fun importDBFromUri(input: Uri?, context: Context): Boolean {
    Log.d(TAG, "Importing database from uri $input")
    if (input == null) return false

    val stream = context.contentResolver.openInputStream(input)
    val tmpFile = File(context.applicationContext.filesDir, TMP_REALM_FILE_NAME)
    stream.copyTo(tmpFile.outputStream())
    return checkAndImportDB(tmpFile, context)
}

fun importDBFromFile(inputFile: File, context: Context): Boolean {
    Log.d(TAG, "Importing database file from ${inputFile.absolutePath}")

    if (!inputFile.exists()) return false

    val tmpFile = File(context.applicationContext.filesDir, TMP_REALM_FILE_NAME)
    inputFile.copyTo(tmpFile, overwrite = true)
    return checkAndImportDB(tmpFile, context)
}

private fun checkAndImportDB(tmpFile: File, context: Context): Boolean {
    val config = RealmConfiguration.Builder()
            .name(TMP_REALM_FILE_NAME)
            .build()

    // ensure the realm database contains the bleeding symptom and is openable
    var realm: Realm? = null
    try {
        realm = Realm.getInstance(config)
        val valid = realm
                ?.where(Symptom::class.java)?.equalTo("name", "Bleeding")?.count() == 1L

        if (!valid) return false
        // copy to the real realm file
        val realmFile = File(context.applicationContext.filesDir, REALM_FILE_NAME)
        tmpFile.copyTo(realmFile, overwrite = true)

    } catch (e: Exception) {
        Log.d(TAG, "Could not import realm")
        e.printStackTrace()
        return false
    } finally {
        realm?.close()
        tmpFile.delete()
    }
    Log.d(TAG, "Database imported")
    return true
}