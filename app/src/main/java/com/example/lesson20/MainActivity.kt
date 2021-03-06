package com.example.lesson20

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View.VISIBLE
import android.widget.*
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var loginText: EditText
    private lateinit var passwordText: EditText
    private lateinit var loginBtn: Button
    private lateinit var threadToServer: ThreadToServer
    private lateinit var profileThread: ProfileThreadToServer
    private lateinit var progress: ProgressDialog
    private lateinit var errorText: TextView

    private val ERROR = "error"
    private val EMAIL = "email"
    private val PASSWORD = "password"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!getSharedPreferences(ProfileActivity.SHARED, Context.MODE_PRIVATE).getBoolean(
                ProfileActivity.BOOLEAN_FOR_SHRED, true
            )
        ) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        initAll()
    }

    private fun initAll() {
        loginText = findViewById(R.id.login_text)
        passwordText = findViewById(R.id.password_text)
        loginBtn = findViewById(R.id.login_btn)
        errorText = findViewById(R.id.error_message)
    }

    override fun onStart() {
        super.onStart()
        loginBtn.isEnabled = false
        loginTextListenerChanged()
        passwordTextListenerChanged()
        loginBtnListener()
    }

    private fun loginBtnListener() {
        loginBtn.setOnClickListener {
            createProgressDialog()
            createClient()
        }
    }

    private fun createClient() {
        val okHttpClient = OkHttpClient()
        val body = createJSON().toRequestBody()
        startThread(okHttpClient, body)
    }

    private fun startThread(
        okHttpClient: OkHttpClient,
        body: RequestBody
    ) {
        threadToServer = ThreadToServer(okHttpClient, body, action = {
            Handler(Looper.getMainLooper()).post {
                if (it?.substring(0, 5).equals(ERROR)) {
                    errorText.visibility = VISIBLE
                    errorText.text = it?.substring(5, it.lastIndex)
                    loginText.setText("")
                    passwordText.setText("")
                    progress.dismiss()
                } else {
                    val bodyToSecondThread = JSONObject()
                        .put("token", it)
                        .toString().toRequestBody()
                    profileThread = ProfileThreadToServer(
                        okHttpClient,
                        bodyToSecondThread,
                        actionSetTextInAllFields = {
                            setInShared(ProfileActivity.EMAIL, loginText.text.toString())
                            setInShared(ProfileActivity.FIRST_NAME, it!![0])
                            setInShared(ProfileActivity.LAST_NAME, it[1])
                            setInShared(ProfileActivity.BIRTH_DATE, it[2])
                            setInShared(ProfileActivity.NOTE, it[3])
                        })
                    profileThread.executeOnExecutor(Executors.newFixedThreadPool(2))
                    startActivity(Intent(this, ProfileActivity::class.java))
                    progress.dismiss()
                }
            }
        })
        threadToServer.executeOnExecutor(Executors.newFixedThreadPool(1))
    }

    private fun createJSON() = JSONObject()
        .put(EMAIL, loginText.text.toString())
        .put(PASSWORD, passwordText.text.toString())
        .toString()

    private fun createJSONToSecondThread() = JSONObject()
        .put("token", intent.getStringExtra(ProfileActivity.THIS_TOKEN))
        .toString()

    private fun createProgressDialog() {
        progress = ProgressDialog(this@MainActivity)
        progress.show()
        progress.setContentView(R.layout.progress)
        progress.window?.setBackgroundDrawableResource(R.color.transparent)
        progress.setCanceledOnTouchOutside(false)
    }

    private fun loginTextListenerChanged() {
        loginText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginBtn.isEnabled = loginText.text.isNotEmpty() && passwordText.text.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun passwordTextListenerChanged() {
        passwordText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loginBtn.isEnabled = loginText.text.isNotEmpty() && passwordText.text.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun setInShared(key: String, putString: String) {
        ProfileActivity.isFirst = false
        getSharedPreferences(ProfileActivity.SHARED, Context.MODE_PRIVATE)
            .edit()
            .apply {
                putBoolean(ProfileActivity.BOOLEAN_FOR_SHRED, ProfileActivity.isFirst)
                putString(key, putString)
            }.apply()
    }
}