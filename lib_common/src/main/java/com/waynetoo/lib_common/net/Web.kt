package com.waynetoo.lib_common.net

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * on 2019/3/11
 */
private const val DEFAULT_TIMEOUT = 6L

val builder: Retrofit.Builder by lazy {
    Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
}

val commonClient: OkHttpClient by lazy {
    buildCommonClient()
        .addInterceptor(HttpLoggingInterceptor().apply {
            this.level =HttpLoggingInterceptor.Level.HEADERS
        }).build()
}

fun buildCommonClient(): OkHttpClient.Builder {
    return OkHttpClient.Builder()
        .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
}