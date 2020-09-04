package com.java.zhanghx

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//TODO
@Parcelize
data class Response (
    val pageSize: Int, //新闻总页数
    val total: Int //符合条件的新闻总数
) : Parcelable
