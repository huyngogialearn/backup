package com.passbase.passbase_sdk

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.passbase.passbase_sdk.new_design.CameraPreview
import com.project.myapplication.R
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.schedule

class ObjectDetectionActivity : AppCompatActivity() {

    companion object {
        //state for video recording
        const val RS_START = 0
        const val RS_RECORDING = 1
        const val RS_FINISH = 2

        //state
        const val ST_FRONT = "front"
        const val ST_BACK = "back"

        const val REQUEST_IMAGE_CAPTURE = 1022
    }

    //camera values
    private lateinit var cameraPreview: CameraPreview
    private var isFlash = false

    //values
    private var pathToPhotoFile: String = ""
    private var recordState = RS_START
    private var holdTimer: Timer? = null
    private var takeManuallyTimer: Timer? = null
    private var holdTimerCounter = 0
    private val holdTimerCounterConst = 4
    private var tryDetect = true
    private var hintMessage = ""
//    private var compareBitmapSize: Size = Size(0, 0)

    private var compareRectMode = RectF(.15f, 0.27f, 0.86f, 0.58f)
    private var compareRectBigMode = RectF(.15f, 0.27f, 0.86f, 0.58f)

    private var isBigMode = false
    private val takeManuallyDelay = 15000L
    private val docList = if (API.DEBUG) API.documents else API.dynamicDocument

    //views
    private lateinit var leftDetectCorner: ImageView
    private lateinit var topDetectCorner: ImageView
    private lateinit var rightDetectCorner: ImageView
    private lateinit var bottomDetectCorner: ImageView
    private lateinit var hintText: TextView
    private lateinit var previewImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_object_detection_passbase)

        API.setCustomizeUI(this)
//
//        val state = intent.getStringExtra("state")
//        val mode = intent.getIntExtra("doc_index", -1)
//        //if Passport
//        //isBigMode
//        if (mode >= 0)
//            if (docList[mode].isBigCornerMode) {
//                findViewById<View>(R.id.passbase_detect_center_image).apply {
//                    val lp = layoutParams
//                    lp.width = dpToPx(300)
//                    lp.height = dpToPx(220)
//                    layoutParams = lp
//                    isBigMode = true
//                }
//            }
//
//        hintMessage = when (state) {
//            ST_BACK -> {
//                getString(R.string.scan_back_side)
//            }
//            ST_FRONT -> {
//                getString(R.string.scan_front_side)
//            }
//            else -> {
//                getString(R.string.scan_your_document)
//            }
//        }
//
//        setupListeners()
    }
//
}