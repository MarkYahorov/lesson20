package com.example.lesson20

import android.os.AsyncTask
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class ProfileThreadToServer(
    private val okHttpClient: OkHttpClient,
    private val body: RequestBody,
    val actionSetTextInAllFields: (MutableList<String>?) -> Unit
) : AsyncTask<Void, Void, MutableList<String>>() {

    private val PROFILE = "profile"

    override fun doInBackground(vararg params: Void?): MutableList<String>? {
        try {
            val request = Request.Builder()
                .url(ProfileActivity.URI + PROFILE)
                .post(body)
                .build()
            okHttpClient.newCall(request).execute().use {
                it.body.let { body ->
                    val jsonObj = JSONObject(body!!.string())
                    val firstName = jsonObj.getString("firstName")
                    val lastName = jsonObj.getString("lastName")
                    val birthData = jsonObj.getLong("birthDate")
                    val notes = jsonObj.getString("notes")
                    val list = mutableListOf<String>()
                    list.add(firstName)
                    list.add(lastName)
                    list.add("$birthData")
                    list.add(notes)
                    return list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: MutableList<String>?) {
        super.onPostExecute(result)
        actionSetTextInAllFields(result)
    }
}