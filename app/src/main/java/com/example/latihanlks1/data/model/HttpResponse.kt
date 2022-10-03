package com.example.latihanlks1.data.model

data class HttpResponse<T>(
    val httpStatusCode: Int,
    val responseData: T,
    val rawResponseData: String)