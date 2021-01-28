package com.waynetoo.videotv.model
import kotlinx.serialization.Serializable


/**
 * {
"status": 0,
"msg": "操作成功",
"data": {
"topicId": "436630356752610304",
"oldTopicContent": null,
"topicContent": "内容",
"topicMsg": "阿鲁我还跑去玩",
"partnerIds": "88008,",
"meetTime": "2019-12-10",
"stepStatus": 1,
"reviewerIds": null,
"withdraw": null,
"flowId": null,
"created_at": "2019-12-10 17:00",
"topicType": 2,
"topicTypeName": "市领导重要活动",
"topicCenter": 3,
"topicCenterName": "政务中心",
"topicDegree": 1,
"partners": [{
"userName": "XXX",
"userId": "88008"
}]
}
}
 */
@Serializable
data class Topic(
    val topicId: String = "",
    val flowId: String? = "",
    val topicContent: String = "",
    val topicMsg: String? = "",
    val created_at: String = "",
    val stepStatus: String,
    val partners: List<Partner>?,
    var reviewerIds: String?,
    val meetTime: String,  // 上会时间
    val withdraw: Int? = 0, // 1有撤回按钮 0没有撤回按钮
    val topicType: String?,
    val topicTypeName: String?,
    val topicCenter: String?,
    val topicCenterName: String?,
    val topicDegree: String?
) : java.io.Serializable

@Serializable
data class Partner(
    val userName: String = "",
    val userId: String = ""
) : java.io.Serializable


@Serializable
data class TopicHistory(
    val created_at: String = "",
    val reviewerName: String = "",
    val flowComment: String? = "",
    val reviewContent: String? = "",
    val status: Int,
    val reviewStatus: Int  // 1为通过，其他不通过
) {
    fun isPass() = 1 == reviewStatus
}

@Serializable
data class TopicWithdrawRequest(
    val flowId: String,
    val topicId: String
)

@Serializable
data class TopicCommitRequest(
    val reviewerIds: String?,  // 审核人员ids
    val meetTime: String,  // 上会时间
    val partnerIds: String,  // 合作选题人ids
    val topicContent: String,  // 选题内容
    val topicMsg: String,  // 选题留言
    val topicId: String?,
    val topicType: String,
    val topicDegree: String,
    val topicCenter: String
)

@Serializable
data class TopicFlowRequest(
    val comments: String,
    val flowId: String,
    val topicId: String,
    val passFlag: Int,  // 1通过 0拒绝 2转审
    val partnerIds: String,
    val reviewerIds: String,
    val meetTime: String,
    val topicType: String,
    val topicDegree: String,
    val topicCenter: String,
    val topicContent: String,
    val topicMsg: String
)

@Serializable
data class TopicCenterModel(
    var centerName: String,
    var id: String
)

@Serializable
data class TopicTypeModel(
    var typeName: String,
    var id: String
)



