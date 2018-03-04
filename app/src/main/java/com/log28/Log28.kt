package com.log28

import android.app.Application
import android.content.Context
import io.realm.Realm
import org.acra.annotation.*
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.annotation.AcraCore
import org.acra.sender.HttpSender

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
