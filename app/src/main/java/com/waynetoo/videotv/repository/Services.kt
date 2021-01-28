package com.waynetoo.videotv.repository

import com.waynetoo.lib_common.extentions.resettableLazy
import com.waynetoo.lib_common.extentions.resettableManager
import com.waynetoo.lib_common.extentions.yes
import com.waynetoo.lib_common.net.buildCommonClient
import com.waynetoo.lib_common.net.builder
import com.waynetoo.lib_common.net.handleUniformError
import com.waynetoo.videotv.model.BaseModel
import com.waynetoo.videotv.model.Topic
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.*

/**
 * Created by pwy on 2019-09-03.
 */

const val TOPIC_SUCCESS_CODE = 0
const val TOPIC_TOKEN_EXPIRED = -1


interface TopicApi {
    /**
     * 选题详情
     */
    @GET("topic/detail/{topicId}")
    fun getTopicDetail(@Path("topicId") topicId: String): Observable<BaseModel<Topic>>
}

/**
 * resettableLazy  登录后token从无到有，
 * 这两种情况下会重建client，刷新拦截器，添加新的header Authorization
 */
object Service {
    val BASE_URL = "https://ttopic.zijinshan.org/"
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

    val client: TopicApi by resettableLazy(lazyMgr) { retrofit.create(TopicApi::class.java) }

    private val headerInterceptor by resettableLazy(lazyMgr) {
        Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .method(original.method, original.body)
            val storeNo = ""
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
            when (t.status) {
                TOPIC_SUCCESS_CODE -> {
                    onSuccess(t.data)
                }
                TOPIC_TOKEN_EXPIRED -> {  // token异常，跳转登录页面
                    onFailure(t)
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
