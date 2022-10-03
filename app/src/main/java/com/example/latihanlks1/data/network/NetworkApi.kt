package com.example.latihanlks1.data.network

import com.example.latihanlks1.data.model.HttpResponse
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
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

        try {
            this.setRequestProperty()
            bodyRequest?.let{
                this.httpConn.doOutput = true
                it.encode(this.httpConn.outputStream)
            }
            this.httpConn.connect()

            val reader = BufferedReader(InputStreamReader(httpConn.inputStream))
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                this.stringBuff.append(line)
            }
            reader.close()
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
    private val boundary: String = "-------------------${System.currentTimeMillis()}"
    val contentType: String
        get() {
            return "multipart/form-data; boundary=${this.boundary}"
        }
    override fun encode(os: OutputStream) {
        this.bodyForm.forEach { body ->
            os.write(generateFormField(body.key, body.value))
            os.flush()
        }

        this.bodyFile.forEach{body ->
            os.write(generateFormFile(body.key, body.value))
            os.flush()
        }

        val boundaryEnd = "--$boundary--"
        os.write(boundaryEnd.toByteArray())
        os.write(System.lineSeparator().toByteArray())
        os.flush()
        os.close()
    }

    private fun generateFormField(key: String, value: String): ByteArray {
        System.lineSeparator()
        val formField = """
            --$boundary
            Content-Disposition: form-data; name="$key"
            
            $value
            
        """.trimIndent().replace("\n", "\r\n")
        return formField.toByteArray()
    }

    private fun generateFormFile(key: String, value: File): ByteArray {
        val fileName: String = value.name

        val formatFormFile = """
--$boundary
Content-Disposition: form-data; name="$key"; filename="$fileName"
Content-Type: ${URLConnection.guessContentTypeFromName(fileName)}

${value.readText()}

""".trimIndent().replace("\n", "\r\n")
        return formatFormFile.toByteArray()
    }

    fun addFormField(key: String, value: String): FormRequest {
        this.bodyForm[key] = value
        return this
    }

    fun addFormFile(key: String, value: File): FormRequest {
        this.bodyFile[key] = value
        return this
    }
}
