package com.waynetoo.lib_common.extentions

/**
 * @Author: wyl
 * @CreateDate: 2020/4/23 11:02
 * @Description:
 * @UpdateRemark:
 */

fun Int.setBit(pos: Int, enable: Boolean): Int {
//    将第 i 位设置为1
    return if (enable) {
        this or (1 shl pos)
    } else {
        val mask = (1 shl pos).inv() //000100
        this and mask //111011
    }
}

/**
 * //获取 整数 num 的第 i 位的值
 */
fun Int.getBit(pos: Int): Boolean {
    return (this and (1 shl pos)) != 0 //true 表示第i位为1,否则为0
}