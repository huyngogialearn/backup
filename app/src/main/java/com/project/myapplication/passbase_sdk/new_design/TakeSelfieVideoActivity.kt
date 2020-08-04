package com.passbase.passbase_sdk.new_design

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.facetec.zoom.sdk.*
import com.passbase.passbase_sdk.API
import com.passbase.passbase_sdk.CVibtation
import com.passbase.passbase_sdk.Passbase
import com.project.myapplication.R
import org.jcodec.api.android.AndroidSequenceEncoder
import java.io.File

class TakeSelfieVideoActivity : AppCompatActivity() {

    companion object {
        var tryFinish = {}
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

    //views
    private lateinit var actionBtn: ActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_take_selfie_video_passbase)
        API.setCustomizeUI(this)

        tryFinish = {
            finish()
        }

        initZoom()

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == cameraPermissionCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeZoomVideo()
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
            val result =
                data?.getParcelableExtra<ZoomVerificationResult>(ZoomSDK.EXTRA_VERIFY_RESULTS)
            onZoomVerificationResult(result)
        }
    }

    private fun init() {

        actionBtn = findViewById(R.id.passbase_take_selfie_video_action)
        actionBtn.setOnClick {
            CVibtation(this@TakeSelfieVideoActivity).vibrate()
            actionBtn.startLoading()
            Handler().postDelayed({
                takeZoomVideo()
                //delete after handle
//                uploadVideoAndLivenessScore("",1f)
                actionBtn.stopLoading()
            }, 1000L)
        }

        findViewById<View>(R.id.passbase_take_selfie_video_title).apply {
            setOnClickListener {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<ImageView>(R.id.passbase_take_selfie_video_animation).apply {
            Glide.with(this@TakeSelfieVideoActivity).asGif().load(R.raw.headgif_new).into(this)
        }

        findViewById<ProgressBar>(R.id.passbase_take_selfie_video_progress).apply {
            progress = 30
            val a = ObjectAnimator.ofInt(this, "progress", 40)
            a.duration = 600
            a.repeatCount = 0
            a.startDelay = 1000
            a.start()
        }

        findViewById<View>(R.id.passbase_take_selfie_video_back).apply {
            setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
            }
        }
    }

    private fun initZoom() {

        ZoomSDK.setFacemapEncryptionKey(ZOOM_PUBLIC_KEY)
        ZoomSDK.preload(this)
        ZoomSDK.preload(this)

        ZoomSDK.initialize(this, ZOOM_LICENCE_KEY, object : ZoomSDK.InitializeCallback() {
            override fun onCompletion(successful: Boolean) {
                if (!successful) {

                    API.sendException("MoveHeadInstruction : initZoom  ${ZoomSDK.getStatus(this@TakeSelfieVideoActivity)}")
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

        zoomCustomization.feedbackCustomization.backgroundColor =
            Color.BLACK//Color.argb(0, 255, 255, 255)
        zoomCustomization.feedbackCustomization.textColor =
            Color.WHITE

        zoomCustomization.ovalCustomization.strokeWidth = 2
        zoomCustomization.ovalCustomization.strokeColor = Color.argb(10, 0, 0, 0)
        zoomCustomization.ovalCustomization.progressColor1 =
            ContextCompat.getColor(this, R.color.passbase_blue)
        zoomCustomization.ovalCustomization.progressColor2 =
            ContextCompat.getColor(this, R.color.passbase_blue)
        zoomCustomization.ovalCustomization.progressStrokeWidth = 4
        zoomCustomization.ovalCustomization.progressRadialOffset = 0

        ZoomSDK.setCustomization(zoomCustomization)
    }

    private fun takeZoomVideo() {

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
    }

    private fun startZoom() {

        println("startZoom")

        runOnUiThread {
            if (ZoomSDK.getStatus(this) == ZoomSDKStatus.INITIALIZED) {
                println("startZoom ZoomSDKStatus.INITIALIZED")
                val verificationIntent =
                    Intent(this, ZoomVerificationActivity::class.java)
                startActivityForResult(verificationIntent, ZoomSDK.REQUEST_CODE_VERIFICATION)
            }
        }
    }

    private fun onZoomVerificationResult(result: ZoomVerificationResult?) {

        result?.let {

            val resultStatus = result.status

            // capture failed
            if (resultStatus != ZoomVerificationStatus.USER_PROCESSED_SUCCESSFULLY) return

            uploadAnimation(true)

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

                try {

                    val fileDir = File(this.filesDir.path)
                    if (!fileDir.exists()) {
                        fileDir.mkdir()
                    }
                    val e = AndroidSequenceEncoder.createSequenceEncoder(
                        File(
                            fileDir,
                            "zoom_video.mp4"
                        ), 1
                    )

                    val handlerOnEndEncode = Handler {

                        val file = (File(this.filesDir, "zoom_video.mp4"))
                        if (file.exists()) {

                            println("Video file exists")
                            val livenessScore = result.faceMetrics?.devicePartialLivenessScore ?: 0f
                            //val livenessScore = 0f
                            uploadVideoAndLivenessScore(file.absolutePath, livenessScore)
                        } else {
                            API.sendException("MoveHeadInstruction : onZoomVerificationResult : Video file no exists")
                            goToErrorScreen()
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
                    goToErrorScreen()
                }


            } ?: run {
                uploadAnimation(false)
                return
            }

        }
    }

    private fun uploadVideoAndLivenessScore(file: String, livenessScore: Float) {

        if (API.DEBUG) {
            goToNextView()
            return
        }

        API.build.recordFaceVideo(Passbase.APIKEY, Passbase.AUTHKEY, file) {

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

    private fun uploadAnimation(show: Boolean) {

        findViewById<View>(R.id.passbase_take_selfie_upload_animation).apply {
            visibility = if (show) View.VISIBLE else View.GONE

            setOnClickListener { }
        }

        findViewById<View>(R.id.passbase_take_selfie_video_back).apply {
            visibility = if (show) View.GONE else View.VISIBLE
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
                uploadAnimation(false)
            }, 400)
            startActivity(Intent(this, SelectDocumentActivity::class.java))
        }
    }
}