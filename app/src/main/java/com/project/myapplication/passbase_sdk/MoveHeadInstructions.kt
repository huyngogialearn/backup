package com.passbase.passbase_sdk

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.project.myapplication.R
import com.facetec.zoom.sdk.*
import org.jcodec.api.android.AndroidSequenceEncoder
import java.io.File


class MoveHeadInstructions : AppCompatActivity() {

    companion object {
        //for finish MoveHeadInstructions from any activity
        var tryFinish: (() -> Unit) = {}
    }

    val MY_ZOOM_DEVELOPER_APP_TOKEN = "dBM5CqkrBIYhkLPg8hSauUmyBdk6VPoN"
    val ZOOM_LICENCE_KEY = "dYe6EFmNJreWgyEOY0hxI4kgU8xBmJRI"
    val ZOOM_PRIVATE_KEY =
        "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC4zUZn0XtEqS9P\n" +
                "qB8r9p7gIKwo4UrDDoJgey2XAZD87rpDR4W3A7LxDzUbBlNfbuCasQ8zthFMQ24Z\n" +
                "i4u1i/fx1kVtzuSaxYjNVixE38RuYpemZeZiPrH3qFGusJ9DdA0f4fwEi1VGVRDn\n" +
                "K/MnEUJLDx27gL8LrRlpQv+YJF2GsGKEgNg8Nap8NQbCFzoEPzfr736sBFw6j/JO\n" +
                "s4Exh0BTfIMj3MaVEw+0k/dRAeqbDEOczcAlEyrkqXiv9jF8qNQrj/LO6H+zsR7C\n" +
                "21tTtXDxf/K9YifmeECy9j4le2RTpTLH+gG2JVxmxMzsJzxTdsLWGFYNWTrDASJT\n" +
                "DC4Yhi0PAgMBAAECggEAeCHhUd3onyLIQaUN1nt0nkg/AjgqbJCDCosogNCg2vup\n" +
                "k9DryKQb7/6tzAqZqiEe2xCczEXgCs6OxQZk5Li/jrN24MIs87vTiYAqOP+p5wQq\n" +
                "hx4Y3ObatB70MnZYofXQDljOhnm5DoZ//XchktTyXm7OKjSwfLujGD1YgCOrtMDb\n" +
                "mIDNQFdwUPH0e1EzN/b50GlHbnCYaRpYzRyvSNMEFIT7PoM+Qx/Mlab/eBRuq7l2\n" +
                "VEE1KB1StQUUMCC6azUmR4QAukFUltwkL7ecvf981M21Gbiy75A15rOu6BvrS4yJ\n" +
                "F/WfrAnU5gu9g/frQ++cfUqgkkIep520WCbxgm+zSQKBgQDmX9kxGi5z9G+w196I\n" +
                "9GnHQvmpmsApSJ11dSBd2CnqZpx98Qclu4LQVPKWAgY39P6fRgQQUAzX2Av6EJi3\n" +
                "OwNE+ZRl3wqRIj6DAAq5zbcbRpxMSPl7gGGZKPXWjqwggP+qydfHn5kE3utfW1G1\n" +
                "ZbKsQL/u5TkJ2gL5WotQezwBnQKBgQDNW7Ndpf4yE02tLneaMNPrz7rFZ1zazSfg\n" +
                "wJGLF0CMGVKyZF0MOMyY7CcPlIHteblulaHkm2C35taPfpSqqLcRCfw42hoi/T6B\n" +
                "IU6i3yvS05jF5y/2fmfpMY1V1XdW3MjlaUBQ5m/5Vbf8FpKzL4jr30p8rPASX8gr\n" +
                "coBvYK0PmwKBgQCXaM11kTCQs0mpH+e5eXIALYurJfM/7uLWLNnN8+Fwlmop9/zr\n" +
                "lrbeN8aMQt5VbqbehYTsN8CJKAyPGNNWMenvLl2TKqlmQ+xz6tYh0guTWLAnDxsx\n" +
                "SWpb4Gja0EBeyInAdRJluAY5Bk4KCRnFsAZjYdcDqxH24JttyU2g4q3InQKBgQC/\n" +
                "g7ioKokArE3xp3c1HjpVHbqCJN5M061QBmb1f27b+TXNVpoMLuBn8TyQZCAzlySf\n" +
                "A/8iUuGFQEtzMUp2SbfItTY60vQzE1f6V1SdwmF9UYaVcgCh8cr4iBTWKqkMIgVd\n" +
                "JaYsjGt67T0TZP3VDAy2ZNxzn1iY2ii0lClBNbjP5wKBgDzKn9WYDAYuCk6CyCeP\n" +
                "nBzBLlskSPKzNV14NtSuj1Kx8dwum+hlHg/U9M5VtxRy+UvWdmAk1Cmt3yIpKLvg\n" +
                "kAKTHBGWlcwhkr1dZtQL1K4D+RKdU+he2UnOVsfLcRNStEgM5lrFDUAYx1aYLbOC\n" +
                "9XF/uTSO8Xai3Uag1/7z0NEe\n" +
                "-----END PRIVATE KEY-----"
    val ZOOM_PUBLIC_KEY =
        "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuM1GZ9F7RKkvT6gfK/ae\n" +
                "4CCsKOFKww6CYHstlwGQ/O66Q0eFtwOy8Q81GwZTX27gmrEPM7YRTENuGYuLtYv3\n" +
                "8dZFbc7kmsWIzVYsRN/EbmKXpmXmYj6x96hRrrCfQ3QNH+H8BItVRlUQ5yvzJxFC\n" +
                "Sw8du4C/C60ZaUL/mCRdhrBihIDYPDWqfDUGwhc6BD836+9+rARcOo/yTrOBMYdA\n" +
                "U3yDI9zGlRMPtJP3UQHqmwxDnM3AJRMq5Kl4r/YxfKjUK4/yzuh/s7EewttbU7Vw\n" +
                "8X/yvWIn5nhAsvY+JXtkU6Uyx/oBtiVcZsTM7Cc8U3bC1hhWDVk6wwEiUwwuGIYt\n" +
                "DwIDAQAB\n" +
                "-----END PUBLIC KEY-----"

    private val cameraPermissionCode = 100
    private val takeVideoCode = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_head_instructions_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        API.setCustomizeUI(this)

        initZoom()

        setupListeners()
        playHeadSpinningAnimation()
        uploadAnimation(false)

        tryFinish = {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == cameraPermissionCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            this.takeVideo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == takeVideoCode) {

            val result = data?.getStringExtra("result") ?: ""
            //onResult(result)
        }

        // make sure the result was returned correctly
        if (requestCode == ZoomSDK.REQUEST_CODE_VERIFICATION) {
            val result = data?.getParcelableExtra<ZoomVerificationResult>(ZoomSDK.EXTRA_VERIFY_RESULTS)
            onZoomVerificationResult(result)
        }
    }

    // Animation
    private fun playHeadSpinningAnimation() {

        val imageView = findViewById<ImageView>(R.id.passbase_moveHeadImageView)
        Glide.with(this).asGif().load(R.raw.headgif_new).into(imageView)

        val uploadAnimation = findViewById<ImageView>(R.id.passbase_video_upload_animation)
        Glide.with(this).asGif().load(R.raw.loadinggif).into(uploadAnimation)
    }

    private fun setupListeners() {

        val startRecordingSelfieVideo = findViewById<Button>(R.id.passbase_startRecordingSelfieVideo)
        val exitButtonMoveHead = findViewById<ImageButton>(R.id.passbase_exitButtonMoveHead)

        startRecordingSelfieVideo.setOnClickListener {
            CVibtation(this).vibrate()
            this.takeVideo()
        }

        exitButtonMoveHead.setOnClickListener {
            this.exitView()
        }
    }

    private fun initZoom() {

        ZoomSDK.setFacemapEncryptionKey(ZOOM_PUBLIC_KEY)
        ZoomSDK.preload(this)

        ZoomSDK.initialize(this, ZOOM_LICENCE_KEY, object : ZoomSDK.InitializeCallback() {
            override fun onCompletion(successful: Boolean) {
                if (!successful) {

                    API.sendException("MoveHeadInstruction : initZoom  ${ZoomSDK.getStatus(this@MoveHeadInstructions)}")
                    println("ZOOM unsuccessful")
                    return
                } else {

                }

                println("ZOOM successful")
                setZoomCustomizationBasedOnDevice()
                ZoomSDK.setAuditTrailType(ZoomAuditTrailType.FULL_RESOLUTION)
            }
        })
    }

    private fun setZoomCustomizationBasedOnDevice() {

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val zoomCustomization = ZoomCustomization()

        zoomCustomization.showPreEnrollmentScreen = false
        zoomCustomization.enableLowLightMode = false

        zoomCustomization.frameCustomization.backgroundColor = Color.WHITE
        zoomCustomization.frameCustomization.sizeRatio = 1f

        zoomCustomization.feedbackCustomization.backgroundColor = Color.BLACK//Color.argb(0, 255, 255, 255)
        zoomCustomization.feedbackCustomization.textColor =
            Color.WHITE

        zoomCustomization.ovalCustomization.strokeWidth = 2
        zoomCustomization.ovalCustomization.strokeColor = Color.argb(10, 0, 0, 0)
        zoomCustomization.ovalCustomization.progressColor1 = ContextCompat.getColor(this, R.color.passbase_blue)
        zoomCustomization.ovalCustomization.progressColor2 = ContextCompat.getColor(this, R.color.passbase_blue)
        zoomCustomization.ovalCustomization.progressStrokeWidth = 4
        zoomCustomization.ovalCustomization.progressRadialOffset = 0

        ZoomSDK.setCustomization(zoomCustomization)
    }

    private fun takeVideo() {

        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionCode
            )

            return
        }

        startZoom()

//        val intent = Intent(this, CameraActivity::class.java)
//        intent.putExtra("mode", "video")
//        startActivityForResult(intent, takeVideoCode)
    }

    private fun startZoom() {

        println("startZoom")

        if (ZoomSDK.getStatus(this@MoveHeadInstructions) == ZoomSDKStatus.INITIALIZED) {
            println("startZoom2")
            val verificationIntent = Intent(this@MoveHeadInstructions, ZoomVerificationActivity::class.java)
            startActivityForResult(verificationIntent, ZoomSDK.REQUEST_CODE_VERIFICATION)
        }
    }

    private fun onZoomVerificationResult(result: ZoomVerificationResult?) {

        result?.let {

            //println("ZoomVerificationResult not null")
            val resultStatus = result.status

            // capture failed
            if (resultStatus != ZoomVerificationStatus.USER_PROCESSED_SUCCESSFULLY) return

            val sessionID = result.sessionId

            //val countOfZoomSessionsPerformed = getCountOfZoomSessionsPerformed()

            if (result.faceMetrics != null) {

                val zoomFacemap = result.faceMetrics!!.zoomFacemap
                //val devicePartialLivenessResult = result.faceMetrics!!.devicePartialLiveness
                //val devicePartialLivenessScore = result.faceMetrics!!.devicePartialLivenessScore
                //val retryReason = result.faceMetrics!!.zoomRetryReason
                val auditTrail = result.faceMetrics!!.auditTrail
                val auditTrailHistory = result.faceMetrics!!.auditTrailHistory
            }

            result.faceMetrics?.auditTrail?.let { arrayBmp ->

                uploadAnimation(true)

                try {

                    val fileDir = File(this@MoveHeadInstructions.filesDir.path)
                    if (!fileDir.exists()) {
                        fileDir.mkdir()
                    }
                    val e = AndroidSequenceEncoder.createSequenceEncoder(File(fileDir, "zoom_video.mp4"), 1)

                    val handlerOnEndEncode = Handler {

                        val file = (File(this@MoveHeadInstructions.filesDir, "zoom_video.mp4"))
                        if (file.exists()) {

                            println("Video file exists")
                            val livenessScore = result.faceMetrics?.devicePartialLivenessScore ?: 0f
                            //val livenessScore = 0f
                            uploadVideoAndLivenessScore(file.absolutePath, livenessScore)
                        } else {
                            API.sendException("MoveHeadInstruction : onZoomVerificationResult : Video file no exists")
                        }

                        return@Handler true
                    }

                    AsyncTask.execute {

                        arrayBmp.forEach { bmp ->
                            e.encodeImage(bmp)
                        }
                        e.finish()
                        handlerOnEndEncode.sendEmptyMessageDelayed(0, 500)
                    }

                } catch (e: Exception) {
                    API.sendException(e)
                }


            } ?: run {
                uploadAnimation(false)
                return
            }

        }
    }

    private fun uploadVideoAndLivenessScore(file: String, livenessScore: Float) {

        println("uploadVideoAndLivenessScore $file $livenessScore")

        API.build.recordFaceVideo(Passbase.APIKEY, Passbase.AUTHKEY, file) {

            runOnUiThread {
                uploadAnimation(false)
            }
            recordStepInBackend()
            goToNextView()
        }

        var score = livenessScore
        if (livenessScore == 1f) {
            score = 0.99f
        }

        API.build.sendLivenessScore(Passbase.APIKEY, Passbase.AUTHKEY, score) {

        }
    }

    private fun recordStepInBackend() {

        API.build.checkFaceVideo(Passbase.APIKEY, Passbase.AUTHKEY) { json ->

            API.checkResponse(json, "next_step")?.let {
                //goToNextView()
            } ?: run {
                //goToErrorScreen()
            }
        }
    }

    // Navigation
    private fun uploadAnimation(show: Boolean) {

        val lay = findViewById<View>(R.id.passbase_video_parent_animation)

        if (show) {
            lay.alpha = 0f
            lay.visibility = View.VISIBLE
        } else {
            lay.alpha = 1f
            lay.visibility = View.GONE
        }

        lay.animate().setDuration(200).alpha(1f).start()
    }

    private fun goToNextView() {

        val intent = Intent(this, ChooseDocumentType::class.java)
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
