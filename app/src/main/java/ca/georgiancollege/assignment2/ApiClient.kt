package ca.georgiancollege.assignment2

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

class ApiClient {
    private val client = OkHttpClient()

    fun get(url: String, callback: Callback) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(callback)
    }
}