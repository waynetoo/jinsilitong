package com.waynetoo.videotv.app

import android.content.Context
import com.waynetoo.lib_common.App

/**
 * @Author: wyl
 * @CreateDate: 2021/1/28 9:37
 * @Description:
 * @UpdateRemark:
 */
class Application : App() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        initParam()
    }

    private fun initParam() {
    }
}