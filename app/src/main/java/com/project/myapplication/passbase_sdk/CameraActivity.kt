package com.passbase.passbase_sdk

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
import android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
import android.hardware.camera2.CameraDevice.TEMPLATE_RECORD
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.project.myapplication.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CameraActivity : AppCompatActivity() {

    companion object {
        //state for video recording
        const val RS_START = 0
        const val RS_RECORDING = 1
        const val RS_FINISH = 2

        //to limit recording time in seconds
        const val T_RECORD_LIMIT = 4

        //camera mode
        const val M_VIDEO = "video"
        const val M_VIDEO_DOC = "video_doc"
        const val M_PHOTO_F = "photo_f"
        const val M_PHOTO_B = "photo_b"
    }

    // camera mode: video or photo
    private var cameraMode = ""

    //camera values
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewSize: Size = Size(1080, 1920)
    private lateinit var videoSize: Size
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val cameraOpenCloseLock = Semaphore(1)
    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private var mediaRecorder: MediaRecorder? = null
    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraActivity.cameraDevice = cameraDevice
            startPreview()
            configureTransform(textureView.width, textureView.height)
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraActivity.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraActivity.cameraDevice = null
            finish()
        }

    }
    private var pathToVideoFile: String = ""
    private var pathToVideoDocFile: String = ""
    private var pathToPhotoFFile: String = ""
    private var pathToPhotoBFile: String = ""

    private var recordState = RS_START
    private var recordTimer = Timer()

    //views
    private lateinit var textureView: TextureView
    private lateinit var btnRecord: ImageButton
    private lateinit var txtTimer: TextView
    private lateinit var finishRecord: Button
    private lateinit var cancelRecord: Button
    private lateinit var rotateAnimation: ImageView
    private lateinit var imgFrame: ImageView
    private lateinit var imgLine: ImageView
    private lateinit var imgPreview: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_passbase)

        val mode = intent.getStringExtra("mode") ?: ""

        //aic_check mode
        if (mode == M_VIDEO || mode == M_PHOTO_F || mode == M_PHOTO_B || mode == M_VIDEO_DOC) {
            cameraMode = mode
        }

        this.setupListeners()
    }

    override fun onResume() {
        super.onResume()

        startBackgroundThread()

        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

                override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
                    openCamera(width, height)
                }

                override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
                    configureTransform(width, height)
                }

                override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture) = true

                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
            }
        }
    }

    override fun onPause() {

        closeCamera()
        stopBackgroundThread()

        super.onPause()
    }

    override fun onBackPressed() {

        when (recordState) {

            RS_START -> {
            }
            RS_RECORDING -> {
                return
            }
            RS_FINISH -> {
                this.updateUiByState(RS_START)
                return
            }
        }

        super.onBackPressed()
    }

    private fun setupListeners() {

        // init views
        this.btnRecord = findViewById(R.id.passbase_selfie_camera_record)
        this.txtTimer = findViewById(R.id.passbase_selfie_camera_timer)
        this.finishRecord = findViewById(R.id.passbase_selfie_camera_finish)
        this.rotateAnimation = findViewById(R.id.passbase_selfie_round_animation)
        //this.textureView = findViewById(R.id.texture)
        this.cancelRecord = findViewById(R.id.passbase_selfie_camera_cancel)
        this.imgFrame = findViewById(R.id.passbase_selfie_camera_frame)
        this.imgLine = findViewById(R.id.passbase_selfie_camera_line)

        val s = Point()
        windowManager.defaultDisplay.getSize(s)
        if (s.y > 1920) {
            val p = Math.abs(s.y - 1920) / 2
            imgLine.setPadding(0, p, 0, p)
        }

        this.imgPreview = findViewById(R.id.passbase_selfie_camera_img_preview)

        textureView = TextureView(this)
        val dSize = Point()
        windowManager.defaultDisplay.getSize(dSize)
        val w = dSize.x
        val h = (w * 16) / 9
        val lp = LinearLayout.LayoutParams(w, h)
        textureView.layoutParams = lp
        findViewById<LinearLayout>(R.id.passbase_texture_parent).addView(textureView)

        val selfieImageAnimation = findViewById<ImageView>(R.id.passbase_selfie_round_animation)
        Glide.with(this).asGif().load(R.raw.roundarrow).into(selfieImageAnimation)

        this.updateUiByState(RS_START)

        //set paths
        val videoName = "video.mp4"
        val videoDocName = "video_doc.mp4"
        val photoFName = "photo_f.jpg"
        val photoBName = "photo_b.jpg"
        val dir = getExternalFilesDir(null)
        pathToVideoFile = videoName
        pathToVideoDocFile = videoDocName
        pathToPhotoFFile = photoFName
        pathToPhotoBFile = photoBName
        dir?.let {
            pathToVideoFile = "${it.absolutePath}/$videoName"
            pathToVideoDocFile = "${it.absolutePath}/$videoDocName"
            pathToPhotoFFile = "${it.absolutePath}/$photoFName"
            pathToPhotoBFile = "${it.absolutePath}/$photoBName"
        }
    }

    private fun updateUiByState(setState: Int) {

        this.recordState = setState

        when (this.recordState) {

            RS_START -> {
                this.txtTimer.text = getString(R.string.zero)
                this.cancelRecord.text = getString(android.R.string.cancel)
                this.cancelRecord.visibility = View.VISIBLE
                this.cancelRecord.setOnClickListener {
                    finish()
                }
                this.btnRecord.setImageResource(R.drawable.aic_record_start)
                this.btnRecord.visibility = View.VISIBLE
                this.btnRecord.setOnClickListener {
                    this.startRecordingVideo()
                }
                this.finishRecord.visibility = View.GONE
                this.finishRecord.setOnClickListener {}
                this.rotateAnimation.visibility = View.VISIBLE
            }

            RS_RECORDING -> {
                this.cancelRecord.visibility = View.GONE
                this.btnRecord.setImageResource(R.drawable.aic_record_stop)
                this.btnRecord.visibility = View.VISIBLE
                this.btnRecord.setOnClickListener {
                    this.stopRecordingVideo()
                }
                this.finishRecord.visibility = View.GONE
                this.finishRecord.setOnClickListener {}
                this.rotateAnimation.visibility = View.VISIBLE
            }

            RS_FINISH -> {
                this.cancelRecord.text = getString(R.string.retake)
                this.cancelRecord.visibility = View.VISIBLE
                this.cancelRecord.setOnClickListener {
                    this.updateUiByState(RS_START)
                }
                this.btnRecord.visibility = View.GONE
                this.btnRecord.setOnClickListener {}
                this.finishRecord.visibility = View.VISIBLE
                this.finishRecord.setOnClickListener {
                    val intent = Intent()
                    val outPath = when (cameraMode) {
                        M_VIDEO -> pathToVideoFile
                        M_VIDEO_DOC -> pathToVideoDocFile
                        M_PHOTO_F -> pathToPhotoFFile
                        M_PHOTO_B -> pathToPhotoBFile
                        else -> ""
                    }
                    intent.putExtra("result", outPath)
                    setResult(0, intent)
                    finish()
                }
                this.rotateAnimation.visibility = View.GONE
            }
        }

        when (cameraMode) {
            M_PHOTO_F -> {
                this.txtTimer.text = ""
                this.rotateAnimation.visibility = View.GONE
                this.btnRecord.setOnClickListener {
                    this.takePhoto()
                }
                this.imgFrame.visibility = View.VISIBLE
                this.imgLine.visibility = View.VISIBLE
                enableAnim(true)
                this.imgFrame.setImageResource(R.drawable.aic_camera_frame)
                this.imgPreview.visibility = View.GONE
                this.imgPreview.setImageBitmap(null)
                if (recordState == RS_FINISH) {
                    this.imgFrame.visibility = View.GONE
                    this.imgLine.visibility = View.GONE
                    enableAnim(false)
                    this.imgPreview.visibility = View.VISIBLE
                    this.imgPreview.setImageURI(Uri.parse(pathToPhotoFFile))
                }
            }
            M_PHOTO_B -> {
                this.txtTimer.text = ""
                this.rotateAnimation.visibility = View.GONE
                this.btnRecord.setOnClickListener {
                    this.takePhoto()
                }
                this.imgFrame.visibility = View.VISIBLE
                this.imgLine.visibility = View.VISIBLE
                enableAnim(true)
                this.imgFrame.setImageResource(R.drawable.aic_camera_frame)
                this.imgPreview.visibility = View.GONE
                this.imgPreview.setImageBitmap(null)
                if (recordState == RS_FINISH) {
                    this.imgFrame.visibility = View.GONE
                    this.imgLine.visibility = View.GONE
                    enableAnim(false)
                    this.imgPreview.visibility = View.VISIBLE
                    this.imgPreview.setImageURI(Uri.parse(pathToPhotoBFile))
                }
            }
            M_VIDEO -> {
                this.imgFrame.visibility = View.GONE
                this.imgLine.visibility = View.GONE
                enableAnim(false)
                this.imgPreview.visibility = View.GONE
            }

            M_VIDEO_DOC -> {
                this.imgFrame.visibility = View.GONE
                this.imgLine.visibility = View.GONE
                enableAnim(false)
                this.imgPreview.visibility = View.GONE
                this.rotateAnimation.visibility = View.GONE
            }
        }
    }

    private fun checkPermission(): Boolean =
        try {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED)
        } catch (e: Exception) {
            false
        }


    // preview handlers
    private fun openCamera(width: Int, height: Int) {

        if (this.isFinishing) return

        val manager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }

            var cameraId = manager.cameraIdList[0]

            if (cameraMode == M_VIDEO)
                manager.cameraIdList.forEach { id ->
                    val cameraCharacteristics = manager.getCameraCharacteristics(id)
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        cameraId = id
                    }
                }

            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP)
                ?: throw RuntimeException("Cannot get available preview/video sizes")
            videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder::class.java))
            configureTransform(width, height)
            mediaRecorder = MediaRecorder()
            if (checkPermission())
                manager.openCamera(cameraId, stateCallback, null)
        } catch (e: Exception) {

        }
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            closePreviewSession()
            cameraDevice?.close()
            cameraDevice = null
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startPreview() {

        if (cameraDevice == null || !textureView.isAvailable) return
        try {
            closePreviewSession()
            val texture = textureView.surfaceTexture
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(TEMPLATE_PREVIEW)

            val previewSurface = Surface(texture)
            previewRequestBuilder.addTarget(previewSurface)

            cameraDevice?.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        updatePreview()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        //if (activity != null) showToast("Failed")
                    }
                }, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            API.sendException(e)
        }

    }

    private fun updatePreview() {
        if (cameraDevice == null) return

        try {

            // for take PHOTO
            if (cameraMode == M_PHOTO_F) {
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
            }//if (cameraMode == M_PHOTO_F)

            previewRequestBuilder.set(
                CaptureRequest.CONTROL_MODE,
                CameraMetadata.CONTROL_MODE_AUTO
            )
//            previewRequestBuilder.set(
//                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
//                CaptureResult.CONTROL_AE_STATE_CONVERGED
//            )
            HandlerThread("CameraPreview").start()
            captureSession?.setRepeatingRequest(
                previewRequestBuilder.build(),
                null,
                backgroundHandler
            )
        } catch (e: CameraAccessException) {
            API.sendException(e)
        }

    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {

        val viewHeightNew = (viewWidth * 16) / 9

        val r = Point()
        windowManager.defaultDisplay.getSize(r)

        val matrix = Matrix()

        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeightNew.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        //val bufferRect = RectF(0f, 0f, previewSize.width.toFloat(), previewSize.height.toFloat())
        val bufferRect = RectF(0f, 0f, r.x.toFloat(), r.y.toFloat())

        matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.CENTER)

//        val scale = if (r.y == previewSize.height)
//            viewHeightNew.toFloat() / r.y.toFloat()
//        else
//

        val s = 0.75f//(viewHeightNew.toFloat() - dpToPx(100)) / r.y.toFloat() 0.75
        matrix.postScale(1f, s, centerX, centerY - dpToPx(200))

        textureView.setTransform(matrix)
    }


    // video recording
    private fun setUpMediaRecorder() {

        mediaRecorder?.apply {
            setOrientationHint(270)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            if (cameraMode == M_VIDEO)
                setOutputFile(pathToVideoFile)
            if (cameraMode == M_VIDEO_DOC)
                setOutputFile(pathToVideoDocFile)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(24)
            setVideoSize(videoSize.width, videoSize.height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            //setAudioSource(MediaRecorder.AudioSource.MIC)
            //setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }
    }

    private fun startRecordingVideo() {

        if (cameraDevice == null || !textureView.isAvailable) return

        val onStartRecord = Handler {

            this.updateUiByState(RS_RECORDING)

            var i = 0
            recordTimer = Timer()
            recordTimer.schedule(0, 1000) {

                //update view must have in main thread
                runOnUiThread {
                    val time = "${getString(R.string.zero_add)}$i"
                    this@CameraActivity.txtTimer.text = time
                }

                i++
                if (i >= T_RECORD_LIMIT) {
                    runOnUiThread {
                        this@CameraActivity.stopRecordingVideo()
                    }
                }
            }

            return@Handler true
        }

        try {
            closePreviewSession()
            setUpMediaRecorder()
            val texture = textureView.surfaceTexture.apply {
                setDefaultBufferSize(previewSize.width, previewSize.height)
            }

            // Set up Surface for camera preview and MediaRecorder
            val previewSurface = Surface(texture)
            val recorderSurface = mediaRecorder!!.surface
            val surfaces = ArrayList<Surface>().apply {
                add(previewSurface)
                add(recorderSurface)
            }
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(TEMPLATE_RECORD).apply {
                addTarget(previewSurface)
                addTarget(recorderSurface)
            }

            cameraDevice?.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        captureSession = cameraCaptureSession
                        updatePreview()
                        runOnUiThread {
                            mediaRecorder?.start()

                            //start timer for stop recording
                            onStartRecord.sendEmptyMessage(0)
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        //if (activity != null) showToast("Failed")
                    }
                }, backgroundHandler
            )
        } catch (e: IOException) {
            API.sendException(e)
        }

    }

    private fun closePreviewSession() {
        captureSession?.close()
        captureSession = null
    }

    private fun stopRecordingVideo() {

        this.updateUiByState(RS_FINISH)
        recordTimer.cancel()
        recordTimer = Timer()

        captureSession?.stopRepeating()
        captureSession?.abortCaptures()

        mediaRecorder?.apply {
            stop()
            reset()
        }

        startPreview()
    }

    private fun startBackgroundThread() {

        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {

        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            API.sendException(e)
        }
    }

    private fun chooseVideoSize(choices: Array<Size>) = choices.firstOrNull {
        it.width == it.height * 16 / 9 && it.width <= 1080
    } ?: choices[choices.size - 1]

    // take photo
    private fun takePhoto() {

        //lock camera
        captureSession?.capture(
            previewRequestBuilder.build(),
            null, backgroundHandler
        )

        val file = if (cameraMode == M_PHOTO_F) File(pathToPhotoFFile) else File(pathToPhotoBFile)
        val outputPhoto = FileOutputStream(file)
        try {
            val matrix = Matrix()
            val dSize = Point()
            windowManager.defaultDisplay.getSize(dSize)
            val scaleX = previewSize.width.toFloat() / dSize.x.toFloat()
            val scaleY = previewSize.height.toFloat() / dSize.y.toFloat()
            //matrix.postScale(scaleX, scaleY)
            matrix.postScale(1f, 0.75f)
            val b = Bitmap.createBitmap(textureView.bitmap, 0, 0, previewSize.width, previewSize.height, matrix, false)
            b.compress(Bitmap.CompressFormat.JPEG, 100, outputPhoto)
        } catch (e: Exception) {

        } finally {

            //unlock camera
            updatePreview()
            outputPhoto.close()
            updateUiByState(RS_FINISH)
        }

    }


    private fun enableAnim(anim: Boolean, right: Boolean = true) {

        if (anim) {
            imgLine.visibility = View.VISIBLE
        } else {
            imgLine.visibility = View.GONE
            return
        }

        val t = if (right) 550f else 0f
        val duration = 1500L
        this.imgLine.animate().translationX(t).setDuration(duration).start()

        Handler().postDelayed({
            enableAnim(imgLine.visibility == View.VISIBLE, !right)
        }, duration)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }
}