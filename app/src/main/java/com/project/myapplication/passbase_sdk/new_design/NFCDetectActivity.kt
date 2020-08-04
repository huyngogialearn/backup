package com.passbase.passbase_sdk.new_design

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.passbase.passbase_sdk.API
import com.project.myapplication.R


class NFCDetectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_detect_passbase)
        API.setCustomizeUI(this)

    }
}