package com.waynetoo.lib_common

import android.content.ContextWrapper
import androidx.multidex.MultiDexApplication
import com.tencent.bugly.Bugly

/**
 * on 2018/7/13.
 */

open class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        initBugly()
    }

    companion object {
        @JvmStatic
        lateinit var INSTANCE: App
    }


    private fun initBugly() {
        Bugly.init(applicationContext, "448838f190", false);
    }
}

object AppContext : ContextWrapper(App.INSTANCE)