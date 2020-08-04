package com.passbase.passbase_sdk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.project.myapplication.R

class CheckEmailActivity : AppCompatActivity() {

    //for finish CheckEmailActivity from any activity
    companion object {
        var tryFinish: (() -> Unit) = {}
    }

    private var emailAddress = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_email_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        API.setCustomizeUI(this)

        setupListeners()

        tryFinish = {
            finish()
        }

    }

    private fun setupListeners() {

        val emailEditText = findViewById<EditText>(R.id.passbase_emailAddressTextfield)
        val startButton = findViewById<Button>(R.id.passbase_startButton)
        val exitButtonCheckEmail = findViewById<ImageView>(R.id.passbase_exitButtonCheckEmail)

        //enterEmailAddress()
        emailEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                CVibtation(this).vibrate()
                enterEmailAddress()
            }
            return@setOnEditorActionListener true
        }

        startButton.setOnClickListener {
            CVibtation(this).vibrate()
            enterEmailAddress()
        }

        exitButtonCheckEmail.setOnClickListener {
            exitView()
        }
    }

    // Handlers
    private fun enterEmailAddress() {

        emailAddress = findViewById<EditText>(R.id.passbase_emailAddressTextfield).text.toString()

        if (emailAddress.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {

            Passbase.PREFILLUSEREMAIL = emailAddress

            API.loadAnimation(this, R.id.passbase_exitButtonCheckEmail, R.id.passbase_startButton, true)

            signUpEmailToBackend()
        } else {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_LONG).show()
        }
    }

    private fun signUpEmailToBackend() {

        API.build.signupWithEmail(Passbase.APIKEY, Passbase.AUTHKEY, emailAddress) { json ->

            API.checkResponse(json, "next_step")?.let {
                goToNextView()
            } ?: run {
                API.sendException("CheckEmailActivity: signUpEmailToBackend $json")
                goToErrorScreen()
            }

            runOnUiThread {
                API.loadAnimation(this, R.id.passbase_exitButtonCheckEmail, R.id.passbase_startButton, false)
            }
        }
    }

    // Navigation
    private fun goToNextView() {

        val intent = Intent(this, StartVerification::class.java)
        startActivity(intent)
    }

    private fun goToErrorScreen() {

        val intent = Intent(this, ErrorScreen::class.java)
        startActivity(intent)
    }

    private fun exitView() {

        ChooseCitizenActivity.tryFinish()
        ChooseDocumentType.tryFinish()
        ErrorScreen.tryFinish()
        MoveHeadInstructions.tryFinish()
        PrepareBacksideID.tryFinish()
        PrepareID.tryFinish()
        StartVerification.tryFinish()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
    }

    override fun onDestroy() {
        super.onDestroy()

        tryFinish = {}
    }
}
