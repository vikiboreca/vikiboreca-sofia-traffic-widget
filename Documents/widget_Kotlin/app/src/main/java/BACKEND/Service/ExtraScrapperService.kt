package BACKEND.Service

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ExtraScrapperService(stop:String, limit:Int) {
    private val client = OkHttpClient()
    private val baseURL = "https://api.livetransport.eu/sofia/virtual-board"
    private val postURL = "$baseURL/$stop?limit=$limit"

    suspend fun getJson():String = suspendCancellableCoroutine { cont->
        val request = Request.Builder().url(postURL).build()
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
}