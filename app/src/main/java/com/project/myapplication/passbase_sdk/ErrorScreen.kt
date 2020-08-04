package com.passbase.passbase_sdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.project.myapplication.R

class ErrorScreen : AppCompatActivity() {

    //for finish ErrorScreen from any activity
    companion object {
        var tryFinish: (() -> Unit) = {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        API.setLocale(this, API.language)
        setContentView(R.layout.activity_error_screen_passbase)

        API.setCustomizeUI(this)

        setupListeners()

        tryFinish = {
            finish()
        }
    }

    private fun setupListeners() {
        val exitButtonErrorScreen = findViewById<ImageButton>(R.id.passbase_exitButtonErrorScreen)

        exitButtonErrorScreen.setOnClickListener {
            exitView()
        }
    }

    // Navigation
    private fun exitView() {

        ChooseCitizenActivity.tryFinish()
        CheckEmailActivity.tryFinish()
        ChooseDocumentType.tryFinish()
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
