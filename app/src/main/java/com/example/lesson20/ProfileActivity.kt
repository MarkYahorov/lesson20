package com.example.lesson20

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
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

    private var profileThread: ProfileThreadToServer? = null

    companion object {
        const val URI = "https://pub.zame-dev.org/senla-training-addition/lesson-20.php?method="
        const val SHARED = "SH"
        const val BOOLEAN_FOR_SHRED = "BOOL"
        const val THIS_TOKEN = "thisToken"
        var isFirst = true
        const val EMAIL = "email"
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val BIRTH_DATE = "birthDate"
        const val NOTE = "note"
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
        setTextInFieldsFromSharedPref()
        logoutListener()
    }

    private fun formatDate(formatter: SimpleDateFormat, millisForFormat: Long): String {
        return formatter.format(Date(millisForFormat))
    }

    private fun logoutListener() {
        logoutBtn.setOnClickListener {
            setInShared(true)
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

    private fun setInShared(outOrIn: Boolean) {
        isFirst = outOrIn
        getSharedPreferences(SHARED, Context.MODE_PRIVATE)
            .edit()
            .apply {
                putBoolean(BOOLEAN_FOR_SHRED, isFirst)
                putString(EMAIL, currentEmail.text.toString())
                putString(FIRST_NAME, currentFirstName.text.toString())
                putString(LAST_NAME, currentSecondName.text.toString())
                putString(BIRTH_DATE, currentBirthday.text.toString())
                putString(NOTE, currentNote.text.toString())
            }.apply()
    }

    private fun setTextInFieldsFromSharedPref() {
        currentEmail.text = getSharedPreferences(SHARED, Context.MODE_PRIVATE).getString(
            EMAIL,
            currentEmail.text.toString()
        )
        currentFirstName.text = getSharedPreferences(SHARED, Context.MODE_PRIVATE).getString(
            FIRST_NAME,
            currentFirstName.text.toString()
        )
        currentSecondName.text = getSharedPreferences(SHARED, Context.MODE_PRIVATE).getString(
            LAST_NAME,
            currentSecondName.text.toString()
        )
        currentBirthday.text = getSharedPreferences(SHARED, Context.MODE_PRIVATE).getString(
            BIRTH_DATE,
            currentBirthday.text.toString()
        )
        currentNote.text = getSharedPreferences(SHARED, Context.MODE_PRIVATE).getString(
            NOTE,
            currentNote.text.toString()
        )
    }

    override fun onStop() {
        super.onStop()
        logoutBtn.setOnClickListener(null)
        profileThread?.cancel(true)
    }
}