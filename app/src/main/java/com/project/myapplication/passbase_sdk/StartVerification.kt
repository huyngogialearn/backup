package com.passbase.passbase_sdk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.project.myapplication.R

class StartVerification : AppCompatActivity() {

    //for finish StartVerification from any activity
    companion object {
        var tryFinish: (() -> Unit) = {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_verification_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        API.setCustomizeUI(this)

        setupListeners()

        tryFinish = {
            finish()
        }
    }

    private fun setupListeners() {

        val startButton = findViewById<Button>(R.id.passbase_startButton2)
        val exitButtonStartVerification = findViewById<ImageButton>(R.id.passbase_exitButtonStartVerification)

        startButton.setOnClickListener {
            CVibtation(this).vibrate()
            goToNextView()
        }
        exitButtonStartVerification.setOnClickListener {
            exitView()
        }
    }

    // Navigation
    private fun goToNextView() {

        // just for debug
        //val intent = Intent(this, ChooseDocumentType::class.java)

        val intent = Intent(this, MoveHeadInstructions::class.java)
        startActivity(intent)
    }

    private fun exitView() {

        ChooseCitizenActivity.tryFinish()
        CheckEmailActivity.tryFinish()
        ChooseDocumentType.tryFinish()
        ErrorScreen.tryFinish()
        MoveHeadInstructions.tryFinish()
        PrepareBacksideID.tryFinish()
        PrepareID.tryFinish()
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
