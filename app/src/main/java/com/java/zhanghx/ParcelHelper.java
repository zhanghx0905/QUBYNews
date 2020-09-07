package com.java.zhanghx;

import android.os.Parcelable;

import androidx.annotation.NonNull;


public class ParcelHelper {
    private ParcelHelper() {
    }

    // https://stackoverflow.com/questions/51799353/how-to-use-parcel-readtypedlist-along-with-parcelize-from-kotlin-android-exte
    @NonNull
    @SuppressWarnings("unchecked")
    public static final Parcelable.Creator<News> NEWS_CREATOR = News.CREATOR;

}