package com.passbase.passbase_sdk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.project.myapplication.R
import org.json.JSONObject
import java.io.IOException

class PrepareID : AppCompatActivity() {

    //for finish PrepareID from any activity
    companion object {
        var tryFinish: (() -> Unit) = {}
    }

    private val cameraPermissionCode = 100
    private var documentIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prepare_id_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        API.setCustomizeUI(this)

        setupListeners()
        playDocumentAnimation()
        uploadAnimation(false)

        documentIndex = intent.getIntExtra("documentIndex", -1)

        tryFinish = {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ObjectDetectionActivity.REQUEST_IMAGE_CAPTURE) {

            val result = data?.getStringExtra("result") ?: ""
            onResult(result)
        }
    }

    private fun onResult(pathToPhotoFile: String) {

        if (pathToPhotoFile.isNotEmpty()) {
            uploadFrontPicDocument(pathToPhotoFile)
        }
    }

    private fun uploadFrontPicDocument(pathToPhotoFile: String) {

        uploadAnimation(true)
        API.build.recordAuthenticationDocumentPicFRONT(Passbase.APIKEY, Passbase.AUTHKEY, pathToPhotoFile) { json ->

            //aic_check response status
            json?.let {
                val jsonObject = JSONObject(it)
                jsonObject.getString("status")?.let { st ->
                    if (st == "success") {

                        goToNextView()
                        recordStepInBackend()
                    }
                } ?: run {
                    API.sendException("PrepareID : uploadFrontPicDocument : $json")
                    goToErrorScreen()
                }
            } ?: run {
                API.sendException("PrepareID : uploadFrontPicDocument : $json")
                goToErrorScreen()
            }
            runOnUiThread {
                uploadAnimation(false)

            }
        }
    }

    private fun recordStepInBackend() {

        API.build.checkAuthenticationDocumentPicFRONT(Passbase.APIKEY, Passbase.AUTHKEY) {

            if (it.isNullOrEmpty()) {

                // Show error message here
                //goToErrorScreen()
                return@checkAuthenticationDocumentPicFRONT
            }

            try {

                val jsonObject = JSONObject(it)
                jsonObject.getString("next_step")?.let { nextStep ->

                    //goToNextView()
                } ?: run {

                    // Show error message here
                    //goToErrorScreen()
                }
            } catch (e: IOException) {

                // Show error message here
                //goToErrorScreen()
            }
        }
    }

    // Animation
    private fun uploadAnimation(show: Boolean) {

        val lay = findViewById<View>(R.id.passbase_photo_f_parent_animation)

        if (show) {
            lay.alpha = 0f
            lay.visibility = View.VISIBLE
        } else {
            lay.alpha = 1f
            lay.visibility = View.GONE
        }

        lay.animate().setDuration(500).alpha(1f).start()
    }

    private fun playDocumentAnimation() {

        val imageView = findViewById<ImageView>(R.id.passbase_prepareFrontIDImageView)
        Glide.with(this).asGif().load(R.raw.idgif).into(imageView)

        val uploadAnimation = findViewById<ImageView>(R.id.passbase_photo_f_upload_animation)
        Glide.with(this).asGif().load(R.raw.loadinggif).into(uploadAnimation)
    }

    private fun setupListeners() {

        val takePhotoFrontButton = findViewById<Button>(R.id.passbase_takePhotoFrontButton)
        val exitButtonPrepareID = findViewById<ImageButton>(R.id.passbase_exitButtonPrepareID)

        takePhotoFrontButton.setOnClickListener {

            CVibtation(this).vibrate()
            takePhoto()
        }
        exitButtonPrepareID.setOnClickListener {
            exitView()
        }
    }

    // Navigation
    private fun takePhoto() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
            return
        }

//        val intent = Intent(this, CameraActivity::class.java)
//        intent.putExtra("mode", CameraActivity.M_PHOTO_F)
//        //response in override fun onActivityResult
//        startActivityForResult(intent, takePhotoCode)

        val intent = Intent(this, ObjectDetectionActivity::class.java)
        intent.putExtra("state", ObjectDetectionActivity.ST_FRONT)
        intent.putExtra("doc_index", documentIndex)
        //response in override fun onActivityResult
        startActivityForResult(intent, ObjectDetectionActivity.REQUEST_IMAGE_CAPTURE)
    }

    private fun goToNextView() {

        //choose nextView : "Congrats" or "PrepareBacksideID"
        //use TRY to aic_check the availability of the array element
        val goTo = try {

            if (
                documentIndex >= 0 &&
                documentIndex < API.documents.size &&
                API.documents[documentIndex].isNeedBackSide
            ) {
                PrepareBacksideID::class.java
            } else
                Congrats::class.java
        } catch (e: Exception) {
            API.sendException(e)
            Congrats::class.java
        }

        val intent = Intent(this, goTo)
        intent.putExtra("documentIndex", documentIndex)
        startActivity(intent)
    }

    private fun goToErrorScreen() {

        val intent = Intent(this, ErrorScreen::class.java)
        startActivity(intent)
    }

    private fun exitView() {

        ChooseCitizenActivity.tryFinish()
        CheckEmailActivity.tryFinish()
        ChooseDocumentType.tryFinish()
        ErrorScreen.tryFinish()
        MoveHeadInstructions.tryFinish()
        PrepareBacksideID.tryFinish()
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
