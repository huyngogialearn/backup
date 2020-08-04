package com.project.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.passbase.passbase_sdk.Passbase
import android.graphics.Color
import com.passbase.passbase_sdk.PassbaseButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val passbaseRef = Passbase(this)

        // Additional attributes set
        val additionalAttributes = arrayOf(
            Pair("userId", "a2ab4-f2jc4-k2dd4-1fa3x")
        )

        // Initialization, prefilled email & additional attributes
        passbaseRef.initialize(
            "YOUR_PUBLISHABLE_API_KEY",
//                "LekEiYIVLlABsWImmfK5BwFzogwUrPRdFUxa66BBkRkgd3NLfdBRL5JirH5RLPiC"
            additionalAttributes = additionalAttributes)     // optional parameter

        val verificationButton = findViewById<PassbaseButton>(R.id.passbaseVerificationButton)

        verificationButton.setOnClickListener {

            passbaseRef.startVerification()

            // Handling verifications via callbacks
            passbaseRef.onCancelPassbase {
                println("MainActivity onCancelPassbase")
            }
            passbaseRef.onCompletePassbase { authKey ->
                println("MainActivity onCompletePassbase $authKey")
            }
        }
    }
}
