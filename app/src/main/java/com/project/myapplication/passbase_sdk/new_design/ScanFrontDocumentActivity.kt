package com.passbase.passbase_sdk.new_design

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.passbase.passbase_sdk.*
import com.project.myapplication.R
import org.json.JSONObject
import java.io.IOException

class ScanFrontDocumentActivity : AppCompatActivity() {

    companion object {
        var tryFinish = {}
    }

    private val cameraPermissionCode = 100

    private var documentIndex = 0
    private val docList = if (API.DEBUG) API.documents else API.dynamicDocument

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_front_document_passbase)
        API.setCustomizeUI(this)

        tryFinish = {
            finish()
        }

        documentIndex = intent.getIntExtra("documentIndex", 0)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ObjectDetectionActivity.REQUEST_IMAGE_CAPTURE) {

            val result = data?.getStringExtra("result") ?: ""
            if (result.isNotEmpty()) {
                uploadFrontPicDocument(result)
            }
        }
    }

    private fun init() {

        findViewById<View>(R.id.passbase_scan_front_document_title).apply {
            setOnClickListener {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<ImageView>(R.id.passbase_scan_front_document_animation).apply {
            Glide.with(this@ScanFrontDocumentActivity).asGif().load(R.raw.idgif).into(this)
        }

        findViewById<ProgressBar>(R.id.passbase_scan_front_document_progress).apply {
            progress = 50
            val a = ObjectAnimator.ofInt(this, "progress", 60)
            a.duration = 600
            a.repeatCount = 0
            a.startDelay = 1000
            a.start()
        }

        findViewById<ActionButton>(R.id.passbase_scan_front_document_action).apply {
            setOnClick {
                goToNextView()
                //TODO delete
//                CVibtation(this@ScanFrontDocumentActivity).vibrate()
//                takePhoto()
            }
        }

        findViewById<View>(R.id.passbase_scan_front_document_back).apply {
            setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
            }
        }

        findViewById<TextView>(R.id.passbase_scan_front_document_text1).apply {
            val t = "$text ${docList[documentIndex].name}"
            text = t
        }
    }

    private fun takePhoto() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionCode
            )
            return
        }

        val intent = Intent(this, ObjectDetectionActivity::class.java)
        intent.putExtra("state", ObjectDetectionActivity.ST_FRONT)
        intent.putExtra("doc_index", documentIndex)
        //response in override fun onActivityResult
        //and go to fun uploadFrontPicDocument()
        startActivityForResult(intent, ObjectDetectionActivity.REQUEST_IMAGE_CAPTURE)
    }

    private fun uploadFrontPicDocument(pathToPhotoFile: String) {

        uploadAnimation(true)

        if (API.DEBUG) {
            goToNextView()
            return
        }

        API.build.recordAuthenticationDocumentPicFRONT(
            Passbase.APIKEY,
            Passbase.AUTHKEY,
            pathToPhotoFile
        ) { json ->

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

    private fun uploadAnimation(show: Boolean) {

        findViewById<View>(R.id.passbase_scan_front_document_upload_animation).apply {
            visibility = if (show) View.VISIBLE else View.GONE
            setOnClickListener { }
        }

        findViewById<View>(R.id.passbase_scan_front_document_back).apply {
            visibility = if (show) View.GONE else View.VISIBLE
        }

    }

    private fun goToErrorScreen() {

        runOnUiThread {
            Handler().postDelayed({
                findViewById<ActionButton>(R.id.passbase_scan_front_document_action).stopLoading()
            }, 400)
            val intent = Intent(this, SomethingWrongActivity::class.java)
            startActivity(intent)
        }
    }

    private fun goToNextView() {

        runOnUiThread {

            Handler().postDelayed({
                uploadAnimation(false)
            }, 300L)

            val intent = if (docList[documentIndex].isNeedBackSide)
                Intent(this, ScanBackDocumentActivity::class.java)
            else
                Intent(this, SecurelyDataTransferActivity::class.java)
            intent.putExtra("documentIndex", documentIndex)
            startActivity(intent)
        }
    }
}