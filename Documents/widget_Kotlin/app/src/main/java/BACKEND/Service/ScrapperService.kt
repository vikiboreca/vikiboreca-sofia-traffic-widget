package BACKEND.Service

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ScrapperService {

    private val client = OkHttpClient()
    private val baseUrl = "https://www.sofiatraffic.bg"
    private val postUrl = "$baseUrl/bg/trip/getVirtualTable"

    /**
     * Main entry point — gets raw JSON data for a given stop ID.
     */

    suspend fun isIDValid(id:String):Boolean{
        val cookies = getCookies()
        val xToken = getToken(cookies)
        val jsonBody = getBody(id)
        return getJsonValid(xToken, jsonBody, cookies)
    }
    suspend fun getRawData(id: String): String {
        val cookies = getCookies()
        val xToken = getToken(cookies)
        val jsonBody = getBody(id)
        return getJson(xToken, jsonBody, cookies)
    }

    /**
     * Step 1: Get cookies from the server.
     */
    private suspend fun getCookies(): Map<String, String> =
        suspendCancellableCoroutine { cont ->
            val request = Request.Builder()
                .url(postUrl)
                .post("".toRequestBody()) // same as Jsoup POST with no body
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        cont.resumeWithException(IOException("Unexpected response: ${response.code}"))
                        return
                    }

                    val cookieHeaders = response.headers("Set-Cookie")
                    val cookieMap = cookieHeaders.mapNotNull { header ->
                        val parts = header.split(";")[0].split("=")
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }.toMap()

                    cont.resume(cookieMap)
                }
            })
        }

    /**
     * Step 2: Extract XSRF token from cookies.
     */
    private fun getToken(cookies: Map<String, String>): String {
        val token = cookies["XSRF-TOKEN"]
        if (token.isNullOrEmpty()) throw Exception("Missing XSRF-TOKEN")
        return token.substringBefore('%')
    }

    /**
     * Step 3: Build JSON body.
     */
    private fun getBody(id: String): String {
        return "{\"stop\":\"$id\"}"
    }

    /**
     * Step 4: Perform POST request with token, body, and cookies.
     */
    private suspend fun getJson(xToken: String, jsonBody: String, cookies: Map<String, String>): String =
        suspendCancellableCoroutine { cont ->
            val cookieHeader = cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(postUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-XSRF-TOKEN", xToken)
                .addHeader("Cookie", cookieHeader)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        cont.resume(response.body.string())
                    } else {
                        cont.resumeWithException(IOException("Response code ${response.code}"))
                    }
                }
            })
        }
    private suspend fun getJsonValid(xToken: String, jsonBody: String, cookies: Map<String, String>): Boolean =
        suspendCancellableCoroutine { cont ->
            val cookieHeader = cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }

            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(postUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-XSRF-TOKEN", xToken)
                .addHeader("Cookie", cookieHeader)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resume(false)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        cont.resume(true)
                    } else {
                        cont.resume(false)
                    }
                }
            })
        }
}
