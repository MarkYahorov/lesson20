package com.example.lesson20

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiConfiguration.AuthAlgorithm.SHARED
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings.System.DATE_FORMAT
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class ProfileActivity : AppCompatActivity() {

    private lateinit var currentEmail: TextView
    private lateinit var currentFirstName: TextView
    private lateinit var currentSecondName: TextView
    private lateinit var currentBirthday: TextView
    private lateinit var currentNote: TextView
    private lateinit var logoutBtn: Button
    private lateinit var progress: ProgressDialog

    private var profileThread: ProfileThreadToServer? = null
    private val EMAIL = "email"
    private val FIRST_NAME = "firstName"
    private val LAST_NAME = "lastName"
    private val BIRTH_DATE = "birthDate"
    private val NOTE = "note"

    companion object {
        const val URI = "https://pub.zame-dev.org/senla-training-addition/lesson-20.php?method="
        const val SHARED = "SH"
        const val BOOLEAN_FOR_SHRED = "BOOL"
        const val THIS_TOKEN = "thisToken"
        const val THIS_EMAIL = "thisEmail"
        var isFirst = true
    }

    private val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initAll()
    }

    private fun initAll() {
        currentEmail = findViewById(R.id.current_email)
        currentFirstName = findViewById(R.id.current_first_name)
        currentSecondName = findViewById(R.id.current_last_name)
        currentBirthday = findViewById(R.id.current_birthday)
        currentNote = findViewById(R.id.current_note)
        logoutBtn = findViewById(R.id.logout_btn)
    }

    override fun onStart() {
        super.onStart()
        currentNote.movementMethod = ScrollingMovementMethod()
        isFirst =
            getSharedPreferences(SHARED, Context.MODE_PRIVATE).getBoolean(BOOLEAN_FOR_SHRED, true)
        if (isFirst) {
            createProgressDialog()
            createProfileThread()
        }
        logoutListener()
    }

    private fun createProfileThread() {
        val okHttpClient = OkHttpClient()
        val body = createJSON().toRequestBody()

        profileThread = ProfileThreadToServer(okHttpClient, body, actionSetTextInAllFields = {
            Handler(Looper.getMainLooper()).post {
                currentEmail.text = intent.getStringExtra(THIS_EMAIL)
                currentFirstName.text = it!![0]
                currentSecondName.text = it[1]
                currentBirthday.text = formatDate(formatter, it[2].toLong())
                currentNote.text = it[3]
                progress.dismiss()
                setInShared()
            }
        })
        profileThread?.executeOnExecutor(Executors.newFixedThreadPool(2))
    }

    private fun createJSON() = JSONObject()
        .put("token", intent.getStringExtra(THIS_TOKEN))
        .toString()

    private fun formatDate(formatter: SimpleDateFormat, millisForFormat: Long): String {
        return formatter.format(Date(millisForFormat))
    }

    private fun createProgressDialog() {
        progress = ProgressDialog(this)
        progress.show()
        progress.setContentView(R.layout.progress)
        progress.window?.setBackgroundDrawableResource(R.color.transparent)
        progress.setCanceledOnTouchOutside(false)
    }

    private fun logoutListener() {
        logoutBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EMAIL, currentEmail.text.toString())
        outState.putString(FIRST_NAME, currentFirstName.text.toString())
        outState.putString(LAST_NAME, currentSecondName.text.toString())
        outState.putString(BIRTH_DATE, currentBirthday.text.toString())
        outState.putString(NOTE, currentNote.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentEmail.text = savedInstanceState.getString(EMAIL)
        currentFirstName.text = savedInstanceState.getString(FIRST_NAME)
        currentSecondName.text = savedInstanceState.getString(LAST_NAME)
        currentBirthday.text = savedInstanceState.getString(BIRTH_DATE)
        currentNote.text = savedInstanceState.getString(NOTE)
    }

    private fun setInShared() {
        isFirst = false
        getSharedPreferences(SHARED, Context.MODE_PRIVATE)
            .edit()
            .apply {
                putBoolean(BOOLEAN_FOR_SHRED, isFirst)
            }.apply()
    }

    override fun onStop() {
        super.onStop()
        logoutBtn.setOnClickListener(null)
        profileThread?.cancel(true)
    }
}