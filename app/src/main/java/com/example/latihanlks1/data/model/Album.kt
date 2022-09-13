package com.example.latihanlks1.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Album (
    val id: String?,
    val name: String?,
    val descrption : String?,
    val imageUrl : String?
) : Parcelable