package com.waynetoo.lib_common.extentions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


fun Context.isIntentExisting(
    action: String
): Boolean {
    val packageManager = packageManager
    val intent = Intent(action)
    val resolveInfo = packageManager.queryIntentActivities(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY
    )
    return resolveInfo.size > 0
}