package com.waynetoo.lib_common.extentions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.waynetoo.lib_common.R

/**
 * Created by pwy on 2019/3/11.
 */
fun FragmentActivity.checkPermissions(
    tips: String,
    onGranted: (() -> Unit)?,
    onDenied: (() -> Unit)?,
    vararg permissions: String
) {
    RxPermissions(this)
        .requestEachCombined(
            *permissions
        )
        .subscribe {
            when {
                it.granted -> {
                    // `permission.name` is granted !
                    onGranted?.invoke()
                }
                it.shouldShowRequestPermissionRationale -> {
                    // Denied permission without ask never again
                    onDenied?.invoke()
                }
                else -> {
                    // Denied permission with ask never again
                    // Need to go to the settings
                    AlertDialog.Builder(this)
                        .setMessage(tips)
                        .setPositiveButton(
                            this.resources.getString(R.string.btn_confirm)
                        ) { _, _ ->
                            // 如果用户同意去设置：
                            var intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            var uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                            onDenied?.invoke()
                        }
                        .setNegativeButton(this.resources.getString(R.string.btn_cancel)) { _, _ ->
                            onDenied?.invoke()
                        }
                        .create()
                        .show()
                }
            }
        }
}

