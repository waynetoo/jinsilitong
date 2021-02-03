package com.waynetoo.videotv.utils

import android.util.Log
import com.waynetoo.videotv.BuildConfig

/**
 * @Author: wyl
 * @CreateDate: 2021/2/3 17:43
 * @Description:
 * @UpdateRemark:
 */
object Logger {
    val test = BuildConfig.DEBUG
    val tag = "TV_TV"

    fun log(msg: String) {
        if (test) {
            Log.i(tag, msg)
        }
    }
}