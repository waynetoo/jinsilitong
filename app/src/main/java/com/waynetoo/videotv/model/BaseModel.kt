package com.waynetoo.videotv.model

import kotlinx.serialization.Serializable

/**
 */
@Serializable
data class BaseModel<T>(
    val code: Int,
    var msg: String,
    val data: T?= null
): java.io.Serializable