package com.example.latihanlks1.util

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import java.lang.Exception
import java.net.URI
import java.net.URL

fun loadBitmap(url : String?) : Bitmap?{
    val newurl = URL(url)
    return try {
        BitmapFactory.decodeStream(newurl.openConnection().getInputStream())
    } catch (e: Exception) {
        null
    }
}

fun Context.getRealPathFromURI(contentURI: Uri): String?{
    val result: String?
    val cursor: Cursor? = contentResolver.query(contentURI, null, null, null, null)
    if(cursor == null){
        result = contentURI.path
    }else{
        cursor.moveToFirst()
        val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }
    return result
}