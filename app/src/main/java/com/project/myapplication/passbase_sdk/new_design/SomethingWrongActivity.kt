package com.passbase.passbase_sdk.new_design

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.passbase.passbase_sdk.API
import com.project.myapplication.R

class SomethingWrongActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_something_wrong_passbase)
        API.setCustomizeUI(this)

        init()
    }

    private fun init() {

        findViewById<View>(R.id.passbase_something_wrong_title).apply {
            setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<ActionButton>(R.id.passbase_something_wrong_action).apply {
            setOnClick {
                finish()
            }
        }
    }
}