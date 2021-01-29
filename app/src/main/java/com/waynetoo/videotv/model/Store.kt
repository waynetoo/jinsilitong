package com.waynetoo.videotv.model

import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable


/**
{
{
"id": 1, // 播放序号，
"storeNo": "111111", //  门店的编号
"videoName": "123456", //视频的名称,就是香烟的条形码
"downloadUrl": "https://aaa/bbb/ccc.mp4" //视频的下载地址
"md5": "srsbj434b4j" //视频的md5值,
"modifiedTimes": 2 //代表这个视频 被用户修改的次数
},
{
"id": 2, //播放序号
"storeNo": "111111", //  门店的编号
"videoName": "123456", //视频的名称
"downloadUrl": "https://aaa/bbb/ccc.jpg" // 图片 的下载地址
"md5": "srsbj434b4j" //视频的md5值,
"modifiedTimes": 3 //代表这个视频 被用户修改的次数
}
}
 */
/**
 *         "storeNo": "320121109489",
"storeName": "南京市江宁区萍嫣百货超市店"
 */
@Serializable
data class Store(
    val storeNo: String = "",
    val storeName: String = ""
) : java.io.Serializable

