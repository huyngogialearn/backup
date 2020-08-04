package com.passbase.passbase_sdk

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
//import io.sentry.Sentry
import org.json.JSONObject
import java.util.*

class Passbase(private val context: Context) {

    companion object {

        var APIKEY = ""
        var AUTHKEY = ""
        var PREFILLUSEREMAIL = ""

        var onCompletePassbase: (authKey: String) -> Unit = {}
        var onCancelPassbase: () -> Unit = {}
    }

    private var additionalAttributes: Array<Pair<String, String>> = emptyArray()

    fun onCompletePassbase(onComplete: (authKey: String) -> Unit) {
        onCompletePassbase = onComplete
    }

    fun onCancelPassbase(onCancel: () -> Unit) {
        onCancelPassbase = onCancel
    }

    // Initialization of the SDK with apiKey and saved static
    fun initialize(
        apiKey: String,
        prefillUserEmail: String = "",
        additionalAttributes: Array<Pair<String, String>> = emptyArray()
    ) {

        APIKEY = apiKey
        PREFILLUSEREMAIL = prefillUserEmail
        this.additionalAttributes = additionalAttributes

        println("SDK has been initialized")
    }

    // Starts the verification process programmatically
    fun startVerification() {

        API.language = Locale.getDefault().language
//        API.language = "fr"
//        API.setLocale(this, API.language) in first ChooseCitizenActivity activity

        if (API.DEBUG) {
            val intent = Intent(
                context,
                Class.forName("com.passbase.passbase_sdk.new_design.StartScreenActivity")
//                Class.forName("com.passbase.passbase_sdk.ObjectDetectionActivity")
            )
            intent.putExtra("doc_index", 0)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            android.os.Handler().postDelayed({
                context.startActivity(intent)
            }, 100L)
            return
        }


        // init Sentry crashlytics
//        Sentry.init(API.SENTRY_DSN, io.sentry.DefaultSentryClientFactory())

        // Obtains an auth Key from the API key, which is used later for user identification
        API.build.createAuthentication(APIKEY, additionalAttributes) { json ->

            try {
                json?.let {
                    val jsonObject = JSONObject(it)
                    val nextStep = jsonObject.getString("next_step")
                    val key = jsonObject.getString("key")

                    AUTHKEY = key

                    if (nextStep.isNullOrEmpty() || key.isNullOrEmpty()) {
                        //show Authentication error
                        return@createAuthentication
                    }

                    val intent = Intent(
                        context,
                        Class.forName("com.passbase.passbase_sdk.new_design.StartScreenActivity")
                        //Class.forName("com.passbase.passbase_sdk.StartVerification")
                    )
                    context.startActivity(intent)

                    /*---get available doc types---*/
                    getAvailableDocs()
                } ?: run {
                    val intent = Intent(
                        context,
                        Class.forName("com.passbase.passbase_sdk.ErrorScreen")
                    )
                    context.startActivity(intent)
                }

            } catch (e: Exception) {

                API.sendException(e)

                val intent = Intent(
                    context,
                    Class.forName("com.passbase.passbase_sdk.ErrorScreen")
                )
                context.startActivity(intent)

                return@createAuthentication
            }

        }//API.build.createAuthentication(Passbase.APIKEY)
    }

    /*---setters UI---*/
    fun setProgressBarColor(color: Int) {

        API.progressBarColor = color
    }

    fun setActionButtonColor(color: Int) {

        API.actionButtonColor = color
    }

    private fun setFont(typeface: Typeface?) {

        API.font = typeface
    }

    private fun getAvailableDocs() {

        API.build.getDocumentType(APIKEY, AUTHKEY) {

            try {
                val json = JSONObject(it)
                val docs = json.getJSONArray("document_types")
                API.dynamicDocument = emptyArray()
                for (i in 0 until docs.length()) {
                    val doc = docs.getString(i)
                    API.documents.forEach { document ->
                        if (document.id.toLowerCase() == doc.toLowerCase()) {
                            API.dynamicDocument = API.dynamicDocument.plus(document)
                        }
                    }
                }
            } catch (e: Exception) {

                API.dynamicDocument = emptyArray()
                API.documents.forEach { document ->
                    API.dynamicDocument = API.dynamicDocument.plus(document)
                }
            }

        }
    }

}