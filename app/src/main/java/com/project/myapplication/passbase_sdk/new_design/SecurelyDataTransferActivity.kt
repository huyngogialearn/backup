package com.passbase.passbase_sdk.new_design

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.passbase.passbase_sdk.API
import com.passbase.passbase_sdk.CVibtation
import com.project.myapplication.R

class SecurelyDataTransferActivity : AppCompatActivity() {

    companion object {
        //for finish StartScreenActivity from any activity
        var tryFinish: (() -> Unit) = {}
    }

    private var documentIndex = 0
    private val docList = if (API.DEBUG) API.documents else API.dynamicDocument

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        API.setLocale(this, API.language)
        setContentView(R.layout.activity_securely_data_transfer_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
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

    private fun init() {

        findViewById<View>(R.id.passbase_securely_data_transfer_title).apply {
            setOnClickListener {
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<ProgressBar>(R.id.passbase_securely_data_transfer_progress).apply {
            progress = 80
            val a = ObjectAnimator.ofInt(this, "progress", 90)
            a.duration = 600
            a.repeatCount = 0
            a.startDelay = 1000
            a.start()
        }

        findViewById<View>(R.id.passbase_securely_data_transfer_back).apply {
            setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
            }
        }

        findViewById<View>(R.id.passbase_securely_data_transfer_action).apply {
            setOnClickListener {
                CVibtation(this@SecurelyDataTransferActivity).vibrate()
                goToNextView()
            }
        }

        findViewById<TextView>(R.id.passbase_securely_data_transfer_text3).apply {
            val t = "${docList[documentIndex].name} ${getString(R.string.document)}"
            text = t
        }
    }

    private fun goToNextView() {

        startActivity(Intent(this, CongratsDoneActivity::class.java))
    }
}