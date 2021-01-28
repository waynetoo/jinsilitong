package com.waynetoo.lib_common.extentions

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by pwy on 2019-09-05.
 */

fun Date.formatDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(this)
}

fun String.parseDate(): Date {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.parse(this)
}

/**
 * 格式化传入的时间
 *
 * @param time      需要格式化的时间
 * @param formatStr 格式化的格式
 * @return
 */
fun Long.formatTime(formatStr: String): String {
    val format = SimpleDateFormat(formatStr, Locale.getDefault())
    return format.format(Date(this))
}