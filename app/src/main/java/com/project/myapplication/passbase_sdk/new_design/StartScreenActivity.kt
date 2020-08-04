package com.passbase.passbase_sdk.new_design

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.passbase.passbase_sdk.API
import com.passbase.passbase_sdk.CVibtation
import com.passbase.passbase_sdk.Passbase
import com.project.myapplication.R


class StartScreenActivity : AppCompatActivity() {

    companion object {
        //for finish StartScreenActivity from any activity
        var tryFinish: (() -> Unit) = {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        API.setLocale(this, API.language)
        setContentView(R.layout.activity_start_screen_passbase)

        println("StartScreenActivity setContentView")
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        println("StartScreenActivity overridePendingTransition")
        API.setCustomizeUI(this)
        println("StartScreenActivity setCustomizeUI")

        tryFinish = {
            finish()
        }

        init()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
    }

    private fun init() {

        findViewById<View>(R.id.passbase_start_screen_title).apply {
            setOnClickListener {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<TextView>(R.id.passbase_start_screen_text3).apply {
            movementMethod = LinkMovementMethod.getInstance()
        }

        findViewById<View>(R.id.passbase_start_screen_finish).apply {
            setOnClickListener {
                Passbase.onCancelPassbase()
                API.finish()
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
            }
        }

        findViewById<View>(R.id.passbase_start_screen_action).apply {
            setOnClickListener {
                CVibtation(this@StartScreenActivity).vibrate()
                goToNextView()
            }
        }

        initAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        tryFinish = {}
    }

    // Animation
    private fun initAnimation() {

        val delay = 3600L
        val duration = 600L

        findViewById<ImageView>(R.id.passbase_start_screen_preview_animation).apply {
            Glide.with(this@StartScreenActivity).asGif().load(R.raw.lockgif).into(this)
            animate().setDuration(duration).alpha(0f).setStartDelay(delay).start()
        }

        findViewById<ImageView>(R.id.passbase_start_screen_animation).apply {
            Handler().postDelayed({
                try {
                    Glide.with(this@StartScreenActivity).asGif().load(R.raw.start).into(this)
                } catch (e: Exception) {
                }
            }, delay)
        }

        showView(R.id.passbase_start_screen_animation, delay, duration)
        showView(R.id.passbase_start_screen_finish, delay, duration)
        showView(R.id.passbase_start_screen_text1, delay, duration)
        showView(R.id.passbase_start_screen_text2, delay, duration)
        showView(R.id.passbase_start_screen_action, delay, duration)
        showView(R.id.passbase_start_screen_progress, delay, duration)
        showView(R.id.passbase_start_screen_text3, delay, duration)
    }

    private fun showView(resId: Int, delay: Long = 3600L, duration: Long = 600L) {

        try {
            findViewById<View>(resId).apply {
                alpha = 0f
                animate().setDuration(duration).alpha(1f).setStartDelay(delay).start()
            }
        } catch (e: Exception) {
            API.sendException(e)
        }

    }

    private fun goToNextView() {

        startActivity(Intent(this, ChooseCountryActivity::class.java))
    }
}