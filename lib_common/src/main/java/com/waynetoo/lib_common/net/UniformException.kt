package com.waynetoo.lib_common.net

import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.R
import java.lang.Exception
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * on 2019/3/8
 */
fun <T : Throwable> handleUniformError(e: T): Throwable {
    return when (e) {
        is SocketTimeoutException -> UniformNetError(AppContext.getString(R.string.timeout_connection))
        is SocketException -> {
            when (e) {
                is ConnectException -> UniformNetError(AppContext.getString(R.string.error_connection))
                else -> UniformNetError(AppContext.getString(R.string.error_network))
            }
        }
        is UnknownHostException -> UniformNetError(AppContext.getString(R.string.check_network_is_connected))
        else -> e
    }
}

class UniformNetError(message: String?) : Exception(message)

class DataEmptyException(message: String = "数据为空") : Exception(message)