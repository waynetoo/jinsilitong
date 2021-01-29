package com.waynetoo.videotv.repository

import com.waynetoo.lib_common.extentions.resettableLazy
import com.waynetoo.lib_common.extentions.resettableManager
import com.waynetoo.lib_common.extentions.yes
import com.waynetoo.lib_common.net.buildCommonClient
import com.waynetoo.lib_common.net.builder
import com.waynetoo.lib_common.net.handleUniformError
import com.waynetoo.videotv.config.Constants
import com.waynetoo.videotv.model.BaseModel
import com.waynetoo.videotv.model.Store
import com.waynetoo.videotv.room.entity.AdInfo
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.*

const val SUCCESS_CODE = 100

interface AdApi {
    /**
     * 选题详情
     */
    @GET("jeeplus/tobaccoAd/adVideos/get")
    fun getAdVideos(): Observable<BaseModel<List<AdInfo>>>

    @GET("jeeplus/tobacco/tobStore/get/{storeNo}")
    fun bindStore(@Path("storeNo") storeNo: String): Observable<BaseModel<Store>>
}

/**
 * resettableLazy  登录后token从无到有，
 * 这两种情况下会重建client，刷新拦截器，添加新的header Authorization
 */
object Service {
    val BASE_URL = "http://39.99.150.10:9000/"
    val lazyMgr = resettableManager()

    private val retrofit by resettableLazy(lazyMgr) {
        builder.client(
            buildCommonClient()
                .addInterceptor(headerInterceptor)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
        )
            .baseUrl(BASE_URL)
            .build()
    }

    val client: AdApi by resettableLazy(lazyMgr) { retrofit.create(AdApi::class.java) }

    private val headerInterceptor by resettableLazy(lazyMgr) {
        Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .method(original.method, original.body)
            val storeNo = Constants.storeNo
            storeNo.isNotBlank().yes {
                request.header(
                    "storeNo", storeNo
                )
            }
            chain.proceed(request.build())
        }
    }
}

inline fun <T : Any> Observable<BaseModel<T>>.commonSubscribe(
    crossinline onSuccess: (T?) -> Unit,
    crossinline onFailure: (BaseModel<T>) -> Unit = {},
    crossinline onError: (Throwable) -> Unit = {},
    crossinline onComplete: () -> Unit = {}
): Disposable {
    val dispose = object : DisposableObserver<BaseModel<T>>() {
        override fun onComplete() {
            onComplete()
        }

        override fun onNext(t: BaseModel<T>) {
            when (t.code) {
                SUCCESS_CODE -> {
                    onSuccess(t.data)
                }
                else -> {
                    onFailure(t)
                }
            }
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
            onError(handleUniformError(e))
        }
    }
    subscribe(dispose)
    return dispose
}
