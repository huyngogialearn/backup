package com.passbase.passbase_sdk.new_design

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Camera
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.IOException


class CameraPreview(val activity: AppCompatActivity) : TextureView.SurfaceTextureListener {

    private var parentView: ViewGroup? = null

    //add in activity in onResume() part
    fun onResume() {

        if (mTextureView?.isAvailable == true) {
            openCamera(mTextureView?.surfaceTexture)
        } else {
            mTextureView?.surfaceTextureListener = this
        }
    }

    fun init(view: ViewGroup) {

        parentView = view

        orientations.append(Surface.ROTATION_0, 90)
        orientations.append(Surface.ROTATION_90, 0)
        orientations.append(Surface.ROTATION_180, 270)
        orientations.append(Surface.ROTATION_270, 180)

        mTextureView = TextureView(activity)
        mTextureView?.surfaceTextureListener = this

        view.addView(mTextureView)
    }

    fun setFlash(p: Boolean) {

        val params = mCamera?.getParameters()
        params?.flashMode = if (p) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
        mCamera?.parameters = params
        mCamera?.startPreview()
    }

    val getBitmap: Bitmap?
        get() {
            return mTextureView?.bitmap
        }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {

    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean {
        if (mCamera != null) {
            mCamera?.stopPreview()
            mCamera?.release()
        }

        return true
    }

    override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
        openCamera(p0)
    }

    private var mCamera: Camera? = null
    private var mTextureView: TextureView? = null
    private var orientations = SparseIntArray()

    private fun openCamera(surface: SurfaceTexture?) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        configureTransform()

        mCamera = Camera.open(0)
        try {
            mCamera?.setPreviewTexture(surface)
            val rotation = activity.windowManager.defaultDisplay
                .rotation
            mCamera?.setDisplayOrientation(orientations.get(rotation))
            val params = mCamera?.parameters
            params?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            mCamera?.parameters = params
            mCamera?.startPreview()
        } catch (ioe: IOException) {
        }

    }

    private fun configureTransform() {

        if (mTextureView == null) return

        val r = Point(mTextureView?.width!!, mTextureView?.height!!)
        activity.windowManager.defaultDisplay.getSize(r)

        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, r.x.toFloat(), r.y.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        matrix.postScale(1f, 0.9f, centerX, centerY)
        mTextureView?.setTransform(matrix)

        val scale = getScale()
        mTextureView?.scaleX = scale
        mTextureView?.scaleY = scale
    }

    private fun getScale(): Float {

        if (parentView == null) return 1f

        val r = Point(parentView?.width!!, parentView?.height!!)
        val s = r.y.toFloat() / (r.x.toFloat() * 16f / 9f)
        println("getScale $s")
        //1.0789063 16x9
        return s + 0.05f
    }
}