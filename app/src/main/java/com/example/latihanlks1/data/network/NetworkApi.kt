package com.example.latihanlks1.data.network

import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URLConnection
import java.net.URL
import kotlin.jvm.Throws

class NetworkApi(
    requestURL: String?,
    private val charset: String = "UTF-8",
    private val method: String = "GET",
    private val contentType: String? = null
) {
    private val boundary : String = "===" + System.currentTimeMillis() + "==="
    private val httpConn: HttpURLConnection
    private var outputStream: OutputStream? = null
    private var writer: PrintWriter? = null

    companion object{
        private val LINE_FEED = "\r\n"
        val CONTENT_JSON = "application/json"
    }

    init {
        val url = URL(requestURL)
        httpConn = url.openConnection() as HttpURLConnection
        httpConn.useCaches = false
        httpConn.requestMethod = method

        if (method != "GET"){
            httpConn.doOutput = method == "POST"
            httpConn.doInput = method == "POST"
            if (contentType == CONTENT_JSON){
                httpConn.setRequestProperty("Content-Type", contentType)
            }else{
                httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }
            outputStream = httpConn.outputStream
            writer = PrintWriter(OutputStreamWriter(outputStream, charset), true)
        }
    }

    fun addJsonBody(body: JSONObject) : NetworkApi{
        outputStream?.write(body.toString().toByteArray())
        outputStream?.flush()
        return this
    }

    fun addFormField(name : String, value : String?) : NetworkApi{
        writer?.append("--$boundary")?.append(LINE_FEED)
        writer?.append("Content-Disposition: form-data; name=\"$name\"")
            ?.append(LINE_FEED)
        writer?.append("Content-Type: Text/plain; charset=$charset")?.append(LINE_FEED)
        writer?.append(LINE_FEED)
        writer?.append(value)?.append(LINE_FEED)
        writer?.flush()
        return this
    }

    @Throws(IOException::class)
    fun addFilePart(fieldName: String, UploadFile : File): NetworkApi{
        val fileName: String = UploadFile.name
        writer?.append("--$boundary")?.append(LINE_FEED)
        writer?.append(
            "Content-Disposition: form-data; name=\"" + fieldName
                    + "\"; filename=\"" + fileName + "\"")
            ?.append(LINE_FEED)
        writer?.append((
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName)))
            ?.append(LINE_FEED)
        writer?.append("Content-Transfer-Encoding: binary")?.append(LINE_FEED)
        writer?.append(LINE_FEED)
        writer?.flush()
        val inputStream = FileInputStream(UploadFile)
        val buffer = ByteArray(4096)
        var bytesRead = -1
        while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
            outputStream?.write(buffer, 0, bytesRead)
        }
        outputStream?.flush()
        inputStream.close()
        writer?.append(LINE_FEED)
        writer?.flush()
        return this
    }
    fun addHeaderField(name: String, value: String): NetworkApi{
        writer?.append("$name: $value")?.append(LINE_FEED)
        writer?.flush()
        return this
    }

    @Throws(IOException::class)
    fun execute(): String?{
        try {
            val response = StringBuffer()
            writer?.append(LINE_FEED)?.flush()
            writer?.append("--$boundary--")?.append(LINE_FEED)
            writer?.close()

            val stattus : Int = httpConn.responseCode
            if (stattus in 200 until 300){
                val reader = BufferedReader(InputStreamReader(httpConn.inputStream))
                var line: String?
                while ((reader.readLine().also { line = it }) != null){
                    response.append(line)
                }
                reader.close()
                httpConn.disconnect()
            }else{
                throw IOException("Server returned non-OK status: $stattus")
            }
            return response.toString()
        }catch (e : Exception){
            return null
        }
    }


}