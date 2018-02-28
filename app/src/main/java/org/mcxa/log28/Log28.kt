package org.mcxa.log28

import android.app.Application
import android.content.Context
import io.realm.Realm
import org.acra.annotation.*
import org.acra.ACRA
import org.acra.annotation.AcraCore



//@AcraCore(buildConfigClass = BuildConfig::class)
//@AcraMailSender(mailTo = "crashreport@log28.com")
class Log28 : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }

    /*override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        ACRA.init(this);
    }*/
}
