package com.example.latihanlks1.Util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.lang.Exception
import java.net.URL

fun loadbitmap(url : String?) : Bitmap?{
    val newurl = URL(url)
    return try {
        BitmapFactory.decodeStream(newurl.openConnection().getInputStream())
    } catch (e: Exception) {
        null
    }
}