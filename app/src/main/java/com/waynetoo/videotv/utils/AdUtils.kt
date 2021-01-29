package com.waynetoo.videotv.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Author: weiyunl
 * @CreateDate: 2021/1/29 13:05
 * @Description:
 * @UpdateRemark:
 */


fun checkUpdate() {
    var job = GlobalScope.launch(Dispatchers.Main) {
        var content = fetchData()
        Log.d("Coroutine", content)
    }


}

suspend fun fetchData(): String {
    delay(5000)
    return "content"
}