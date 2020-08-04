package com.passbase.passbase_sdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.project.myapplication.R
import java.io.File

class Congrats : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congrats_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        API.setCustomizeUI(this)

        setupListeners()
        playCongratsAnimation()

        recordStepInBackend()
    }

    private fun recordStepInBackend() {

        API.build.checkAuthenticationDocumentSuccess(Passbase.APIKEY, Passbase.AUTHKEY) {

            println(it)
        }
    }

    // Animation
    private fun playCongratsAnimation() {
        val imageView = findViewById<ImageView>(R.id.passbase_congratsImageView)
        Glide.with(this).asGif().load(R.raw.verifiedbadge).into(imageView)
    }

    private fun setupListeners() {
        val takePhotoBackButton = findViewById<Button>(R.id.passbase_finishButton)
        takePhotoBackButton.setOnClickListener {
            CVibtation(this).vibrate()
            closeSDK()
        }
    }

    // Navigation
    private fun closeSDK() {

        Passbase.onCancelPassbase = {}
        Passbase.onCompletePassbase(Passbase.AUTHKEY)

        try {
            val dir = getExternalFilesDir(null)
            dir?.let {
                File("${it.absolutePath}/video.mp4").delete()
                File("${it.absolutePath}/photo_f.jpg").delete()
                File("${it.absolutePath}/photo_b.jpg").delete()
                return@let
            }
        } catch (e: Exception) {

        }

        ChooseCitizenActivity.tryFinish()
        CheckEmailActivity.tryFinish()
        ChooseDocumentType.tryFinish()
        ErrorScreen.tryFinish()
        MoveHeadInstructions.tryFinish()
        PrepareBacksideID.tryFinish()
        PrepareID.tryFinish()
        StartVerification.tryFinish()
        this.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
    }
}
