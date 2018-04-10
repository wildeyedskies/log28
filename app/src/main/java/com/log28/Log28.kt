package com.log28

import android.app.Application
import android.content.Context
import io.realm.Realm
import org.acra.annotation.*
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.annotation.AcraCore
import org.acra.sender.HttpSender

// This is the code that sets up crash reporting
// When the application crashes, we have a notification that asks the user to send a crash report
// It is sent currently to a ACRA server running on my network.
// TODO get an actual server for crash reproting
@AcraCore(buildConfigClass = BuildConfig::class)
@AcraHttpSender(uri = "http://crash.log28.com:55000/send", httpMethod = HttpSender.Method.POST)
@AcraNotification(resTitle = R.string.crash_title,
        resText = R.string.crash_text,
        resChannelName = R.string.notification_channel)
class Log28 : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this);
    }
}
