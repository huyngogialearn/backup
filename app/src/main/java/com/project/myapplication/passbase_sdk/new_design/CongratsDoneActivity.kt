package com.passbase.passbase_sdk.new_design

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.passbase.passbase_sdk.API
import com.passbase.passbase_sdk.CVibtation
import com.passbase.passbase_sdk.Passbase
import com.project.myapplication.R
import java.io.File

class CongratsDoneActivity : AppCompatActivity() {

    companion object {
        var tryFinish: (() -> Unit) = {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        API.setLocale(this, API.language)
        setContentView(R.layout.activity_congrats_done)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        API.setCustomizeUI(this)

        tryFinish = {
            finish()
        }

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

    private fun init() {

        findViewById<View>(R.id.passbase_congrats_done_title).apply {
            setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<View>(R.id.passbase_congrats_done_finish).apply {
            setOnClickListener {
                API.finish()
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
            }
        }

        findViewById<View>(R.id.passbase_congrats_done_action).apply {
            setOnClickListener {
                CVibtation(this@CongratsDoneActivity).vibrate()
                closeSDK()
            }
        }

        findViewById<ProgressBar>(R.id.passbase_congrats_done_progress).apply {
            progress = 90
            val a = ObjectAnimator.ofInt(this, "progress", 100)
            a.duration = 600
            a.repeatCount = 0
            a.startDelay = 1000
            a.start()
        }

        findViewById<View>(R.id.passbase_congrats_done_finish).apply {
            setOnClickListener {
                closeSDK()
            }
        }

        initAnimation()
        recordStepInBackend()
    }

    // Animation
    private fun initAnimation() {

        val delay = 1000L
        val duration = 600L

        findViewById<ImageView>(R.id.passbase_congrats_done_animation).apply {
            Glide.with(this@CongratsDoneActivity).asGif().load(R.raw.verifiedbadge).into(this)
            translationY = 300f
            animate().setDuration(duration).translationY(0f).setStartDelay(delay).start()
        }

        showView(R.id.passbase_congrats_done_title, delay, duration)
        showView(R.id.passbase_congrats_done_finish, delay, duration)
        showView(R.id.passbase_congrats_done_text1, delay, duration)
        showView(R.id.passbase_congrats_done_text2, delay, duration)
        showView(R.id.passbase_congrats_done_action, delay, duration)
        showView(R.id.passbase_congrats_done_progress, delay, duration)
    }

    private fun showView(resId: Int, delay: Long = 3600L, duration: Long = 600L) {
        findViewById<View>(resId).apply {
            alpha = 0f
            animate().setDuration(duration).alpha(1f).setStartDelay(delay).start()
        }
    }

    private fun recordStepInBackend() {

        if (API.DEBUG) {
            return
        }

        API.build.checkAuthenticationDocumentSuccess(Passbase.APIKEY, Passbase.AUTHKEY) {

            println(it)
        }
    }

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

        API.finish()
        finish()
    }
}