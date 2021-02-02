package com.waynetoo.videotv.app

import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import com.waynetoo.lib_common.App
import com.waynetoo.lib_common.AppContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * @Author: wyl
 * @CreateDate: 2021/1/28 9:37
 * @Description:
 * @UpdateRemark:
 */
class Application : App() {
    override fun onCreate() {
        super.onCreate()
        initParam()
    }

    private fun initParam() {
        val DEFAULT_TIMEOUT = 60L
        val downLoaderFactory = DownloadOkHttp3Connection.Factory().setBuilder(
            OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        )
        val builder = OkDownload.Builder(AppContext)
            .connectionFactory(downLoaderFactory)
        OkDownload.setSingletonInstance(builder.build())
        DownloadDispatcher.setMaxParallelRunningCount(1)
    }
}