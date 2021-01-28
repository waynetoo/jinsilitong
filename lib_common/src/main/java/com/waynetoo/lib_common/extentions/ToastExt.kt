package com.waynetoo.lib_common.extentions

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import android.widget.Toast

/**
 * on 2018/7/24.
 */
private var toast: Toast? = null

@SuppressLint("ShowToast")
fun Context.toast(text: CharSequence) {
    toast ?: let {
        toast = Toast.makeText(this, null, Toast.LENGTH_SHORT)
    }
    toast?.apply {
        setText(text)
        show()
    }
}

/**
 * @param resId 字符串资源
 */
fun Context.toast(@StringRes resId: Int) {
    toast(getString(resId))
}

@SuppressLint("ShowToast")
fun <T : androidx.fragment.app.Fragment> T.toast(text: CharSequence) {
    context?.toast(text)
}

/**
 * @param resId 字符串资源
 */
fun <T : androidx.fragment.app.Fragment> T.toast(@StringRes resId: Int) {
    toast(getString(resId))
}