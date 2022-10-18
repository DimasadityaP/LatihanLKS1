package com.example.latihanlks1.data.network

import com.example.latihanlks1.data.model.HttpResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

interface BodyRequest {
    fun encode(os: OutputStream)
}

class NetworkApi() {
    private val headers = mutableMapOf<String, String>()
    private var method: String = "GET"
    private var requestURL: String = ""
    private var bodyRequest: BodyRequest? = null
    private lateinit var httpConn: HttpURLConnection
    private val stringBuff: StringBuffer = StringBuffer()

    fun setMethod(method: String): NetworkApi {
        this.method = method
        return this
    }

    fun setRequestURL(url: String): NetworkApi {
        this.requestURL = url
        return this
    }

    fun addHeader(key: String, value: String): NetworkApi {
        this.headers[key] = value
        return this
    }

    fun withBody(bodyRequest: BodyRequest): NetworkApi {
        this.bodyRequest = bodyRequest
        return this
    }

    private fun setRequestProperty() {
        this.httpConn.requestMethod = this.method
        this.headers.forEach{ header ->
            this.httpConn.setRequestProperty(header.key, header.value)
        }
    }

    @Throws(IOException::class)
    fun execute(): NetworkApi {
        val url = URL(requestURL)
        this.httpConn = url.openConnection() as HttpURLConnection
        this.httpConn.useCaches = false
        this.httpConn.doInput = true

        try {
            this.setRequestProperty()
            this.bodyRequest?.let{
                this.httpConn.doOutput = true
                it.encode(this.httpConn.outputStream)
            }
            this.httpConn.connect()

            this.stringBuff.append(httpConn.inputStream.reader().readText())
        } catch (e: Exception) {
            throw e
        } finally {
            this.httpConn.disconnect()
        }

        return this
    }

    @Throws(Exception::class)
    fun <T> asJSON(cls: Class<T>): HttpResponse<T> {
        val instance: T = cls.getConstructor(String::class.java).newInstance(this.stringBuff.toString())
        return HttpResponse(this.httpConn.responseCode, instance, this.stringBuff.toString())
    }
}

class JsonRequest<T>(private val body: T): BodyRequest {
    companion object {
        const val contentType = "application/json"
    }

    override fun encode(os: OutputStream) {
        when (body) {
            is JSONObject, is JSONArray -> {
                os.write(body.toString().toByteArray())
                os.flush()
                os.close()
            }
            else -> throw Exception("Wrong type class")
        }
    }

}

class FormRequest(): BodyRequest {
    private val bodyForm = mutableMapOf<String, String>()
    private val bodyFile = mutableMapOf<String, File>()
    private val boundary: String = "${System.currentTimeMillis()}"
    val contentType: String
        get() {
            return "multipart/form-data; boundary=${this.boundary}"
        }

    override fun encode(os: OutputStream) {
        this.bodyForm.forEach { body ->
            os.write(generateFormField(body.key, body.value))
        }

        this.bodyFile.forEach{body ->
            os.write(generateFormFile(body.key, body.value))
        }

        os.write("--$boundary--$LINE_FEED".toByteArray())
        os.flush()
        os.close()
    }

    private fun generateFormField(key: String, value: String): ByteArray {
        val formField = "--$boundary$LINE_FEED" +
                "Content-Disposition: form-data; name=\"$key\"$LINE_FEED$LINE_FEED" +
                "$value$LINE_FEED"
        return formField.toByteArray()
    }

    private fun generateFormFile(key: String, value: File): ByteArray {
        val fileName: String = value.name
        val formFile = "--$boundary$LINE_FEED" +
                "Content-Disposition: form-data; name=\"$key\"; filename=\"$fileName\"$LINE_FEED" +
                "Content-Type: ${URLConnection.guessContentTypeFromName(fileName)}$LINE_FEED$LINE_FEED"
        return formFile.toByteArray()
                .plus(value.readBytes())
                .plus(LINE_FEED.toByteArray())
    }

    fun addFormField(key: String, value: String): FormRequest {
        this.bodyForm[key] = value
        return this
    }

    fun addFormFile(key: String, value: File): FormRequest {
        this.bodyFile[key] = value
        return this
    }

    companion object {
        private const val LINE_FEED = "\r\n"
    }
}

class XWWWFormUrlEncoded() : BodyRequest {
    private val bodyForm = mutableMapOf<String, String>()

    companion object {
        private const val LINE_FEED = "\r\n"
        private const val contentType = "application/x-www-form-urlencoded"
    }

    override fun encode(os: OutputStream) {
        var idx: Int = 0
        this.bodyForm.forEach { body ->
            var formatData = "${body.key}=${body.value}"
            if (idx != this.bodyForm.size-1) {
                formatData += "&"
            }

            os.write(formatData.toByteArray())
            idx++
        }
        os.flush()
        os.close()
    }

    fun addFormField(key: String, value: String): XWWWFormUrlEncoded {
        this.bodyForm[key] = value
        return this
    }
}