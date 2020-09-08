package com.java.zhanghx

import android.graphics.Bitmap

fun String.toArticle(): String =
    this.split('\n')
        .map { if (it.trimStart() == it) "\u3000\u3000$it" else it } // 开头没有空格的话就加两个中文空格
        .joinToString("\n\n", postfix = "\n\n") // 每段结尾(包括最后一段)加两个换行


fun scale(old: Bitmap): Bitmap {
    val resultWidth = SCREEN_WIDTH * 2/ 3
    val scale = resultWidth.toFloat() / old.width
    return Bitmap.createScaledBitmap(old, resultWidth, (old.height * scale).toInt(), true)
}

