package com.example.lesson20

import android.os.AsyncTask
import android.os.Bundle
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject


class ThreadToServer(
    private val okHttpClient: OkHttpClient,
    private val body: RequestBody,
    private val action: (String?) -> Unit
) : AsyncTask<Void, Void, String>() {

    private val ERROR = "error"
    private val MESSAGE = "message"
    private val STATUS = "status"
    private val OK = "ok"
    private val TOKEN = "token"
    private val LOGIN = "login"

    override fun doInBackground(vararg params: Void?): String? {
        try {
            val request = Request.Builder()
                .url(ProfileActivity.URI + LOGIN)
                .post(body)
                .build()
            okHttpClient.newCall(request).execute().use {
                it.body.let { body ->
                    val jsonObj = JSONObject(body?.string())
                    if (jsonObj.getString(STATUS).contains(ERROR)) {
                        return "$ERROR${jsonObj.getString(MESSAGE)}"
                    } else if (jsonObj.getString(STATUS).contains(OK)) {
                        return jsonObj.getString(TOKEN)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result!=null){
            action(result)
        }

    }
}