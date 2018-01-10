package org.mcxa.log28

import android.app.Application
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager


class Log28 : Application() {
    override fun onCreate() {
        super.onCreate()
        // This instantiates DBFlow
        FlowManager.init(FlowConfig.Builder(this).build())
        // add for verbose logging
        // FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
    }
}