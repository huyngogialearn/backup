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

class PrepareBacksideID : AppCompatActivity() {

    //for finish PrepareBacksideID from any activity
    companion object {
        var tryFinish: (() -> Unit) = {}
    }

    private val cameraPermissionCode = 100
    private var documentIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prepare_backside_id_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        setupListeners()

        API.setCustomizeUI(this)

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
            uploadBackPicDocument(pathToPhotoFile)
        }
    }

    private fun setupListeners() {
        val takePhotoBackButton = findViewById<Button>(R.id.passbase_takePhotoBackButton)
        val exitButtonPrepareBacksideID = findViewById<ImageButton>(R.id.passbase_exitButtonPrepareBacksideID)

        takePhotoBackButton.setOnClickListener {
            CVibtation(this).vibrate()
            takePhoto()
        }
        exitButtonPrepareBacksideID.setOnClickListener {
            exitView()
        }
    }

    private fun uploadBackPicDocument(pathToPhotoFile: String) {

        uploadAnimation(true)
        API.build.recordAuthenticationDocumentPicBACK(Passbase.APIKEY, Passbase.AUTHKEY, pathToPhotoFile) { json ->

            //aic_check response status
            json?.let {
                val jsonObject = JSONObject(it)
                jsonObject.getString("status")?.let { st ->
                    if (st == "success") {

                        goToNextView()
                        recordStepInBackend()
                    }
                } ?: run {
                    API.sendException("PrepareBacksideID : uploadBackPicDocument : $json")
                    goToErrorScreen()
                }
            } ?: run {
                API.sendException("PrepareBacksideID : uploadBackPicDocument : $json")
                goToErrorScreen()
            }
            runOnUiThread {
                uploadAnimation(false)
            }
        }
    }

    private fun recordStepInBackend() {

        API.build.checkAuthenticationDocumentPicBACK(Passbase.APIKEY, Passbase.AUTHKEY) { json ->

            //aic_check response status
            json?.let {
                val jsonObject = JSONObject(it)
                jsonObject.getString("status")?.let { st ->
                    if (st == "success") {

                        //goToNextView()
                    }
                } ?: run {

                    // Show error message here
                    //goToErrorScreen()
                }
            } ?: run {

                // Show error message here
                //goToErrorScreen()
            }
            runOnUiThread {
                uploadAnimation(false)

            }
        }
    }

    // Animation
    private fun uploadAnimation(show: Boolean) {

        val lay = findViewById<View>(R.id.passbase_photo_b_parent_animation)

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
        val imageView = findViewById<ImageView>(R.id.passbase_prepareBackIDImageView)
        Glide.with(this).asGif().load(R.raw.idgif).into(imageView)

        val uploadAnimation = findViewById<ImageView>(R.id.passbase_photo_b_upload_animation)
        Glide.with(this).asGif().load(R.raw.loadinggif).into(uploadAnimation)
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
//        intent.putExtra("mode", "photo_b")
//        startActivityForResult(intent, takePhotoCode)

        val intent = Intent(this, ObjectDetectionActivity::class.java)
        intent.putExtra("state", ObjectDetectionActivity.ST_BACK)
        intent.putExtra("doc_index", documentIndex)
        //response in override fun onActivityResult
        startActivityForResult(intent, ObjectDetectionActivity.REQUEST_IMAGE_CAPTURE)
    }

    private fun goToNextView() {

        val intent = Intent(this, Congrats::class.java)
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
