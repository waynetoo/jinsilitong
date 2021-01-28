package com.waynetoo.videotv.config

import com.waynetoo.lib_common.AppContext
import com.waynetoo.lib_common.extentions.Preference

/**
 * @Author: weiyunl
 * @CreateDate: 2021/1/28 18:00
 * @Description:
 * @UpdateRemark:
 */
object Constants {
 var storeNo: String by Preference(AppContext, "storeNo", "")

}