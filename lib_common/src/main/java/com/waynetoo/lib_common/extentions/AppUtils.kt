package com.waynetoo.lib_common.extentions

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings

/**
 * @Author: wyl
 * @CreateDate: 2020/5/15 16:34
 * @Description:
 * @UpdateRemark:
 */
fun Context.getAppVersionName(): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun Context.getAppVersionCode(): Long {
    var versionCode: Long = 0
    try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return versionCode
}

fun ContentResolver.getDeviceId(): String {
    var deviceId = Settings.System.getString(this, Settings.Secure.ANDROID_ID)
    deviceId.isEmpty().yes {
        @Suppress("DEPRECATION")
        deviceId = android.os.Build.SERIAL
    }
    deviceId.isEmpty().yes {
        deviceId = "unKnown"
    }
    return deviceId
}

/**
 * 获取当前进程名
 */
fun Context.getCurrentProcessName(): String {
    val pid = Process.myPid()
    var processName = ""
    val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            ?: return ""
    val runningApps = manager.runningAppProcesses ?: return ""
    for (process in runningApps) {
        if (process.pid == pid) {
            processName = process.processName
        }
    }
    return processName
}