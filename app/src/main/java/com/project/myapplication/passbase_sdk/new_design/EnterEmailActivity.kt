package com.passbase.passbase_sdk.new_design

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.passbase.passbase_sdk.*
import com.project.myapplication.R

class EnterEmailActivity : AppCompatActivity() {

    companion object {
        var tryFinish = {}
    }

    private lateinit var actionBtn: ActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_email_passbase)
        API.setCustomizeUI(this)

        tryFinish = {
            finish()
        }

        init()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        tryFinish = {}
    }

    private fun init() {

        actionBtn = findViewById(R.id.passbase_enter_email_action)
        actionBtn.setEnable(false)

        actionBtn.setOnClick {
            CVibtation(this@EnterEmailActivity).vibrate()
            enterEmailAddress()
        }

        findViewById<View>(R.id.passbase_enter_email_title).apply {
            setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<EditText>(R.id.passbase_enter_email_email).apply {

            setText(Passbase.PREFILLUSEREMAIL)
            if (checkEmail(Passbase.PREFILLUSEREMAIL)) {
                actionBtn.setEnable(true)
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    enterEmailAddress()
                }
                return@setOnEditorActionListener true
            }

            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val emailAddress = this@apply.text.toString()
                    val enable = checkEmail(emailAddress)
                    actionBtn.setEnable(enable)
                }
            })
        }


        findViewById<ProgressBar>(R.id.passbase_enter_email_progress).apply {
            progress = 20
            val a = ObjectAnimator.ofInt(this, "progress", 30)
            a.duration = 600
            a.repeatCount = 0
            a.startDelay = 1000
            a.start()
        }

        findViewById<View>(R.id.passbase_enter_email_back).apply {
            setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
            }
        }
    }

    private fun enterEmailAddress() {

        val emailAddress = findViewById<EditText>(R.id.passbase_enter_email_email).text.toString()

        if (checkEmail(emailAddress)) {

            actionBtn.startLoading()
            Passbase.PREFILLUSEREMAIL = emailAddress
            signUpEmailToBackend(emailAddress)
        } else {
            //Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_LONG).show()
        }
    }

    private fun signUpEmailToBackend(emailAddress: String) {

        if (API.DEBUG) {
            goToNextView()
            return
        }

        API.build.signupWithEmail(Passbase.APIKEY, Passbase.AUTHKEY, emailAddress) { json ->

            API.checkResponse(json, "next_step")?.let {
                goToNextView()
            } ?: run {
                API.sendException("CheckEmailActivity: signUpEmailToBackend $json")
                goToErrorScreen()
            }
        }
    }

    private fun checkEmail(p: String): Boolean {
        var r = false
        if (p.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(p).matches()) {
            r = true
        }
        return r
    }

    private fun goToErrorScreen() {

        runOnUiThread {
            Handler().postDelayed({
                actionBtn.stopLoading()
            }, 400)
            val intent = Intent(this, SomethingWrongActivity::class.java)
            startActivity(intent)
        }
    }

    private fun goToNextView() {

        runOnUiThread {
            Handler().postDelayed({
                actionBtn.stopLoading()
            }, 400)
            startActivity(Intent(this, TakeSelfieVideoActivity::class.java))
        }
    }
}