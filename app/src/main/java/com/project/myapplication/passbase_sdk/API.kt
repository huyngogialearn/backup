package com.passbase.passbase_sdk

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.passbase.passbase_sdk.new_design.*
import com.project.myapplication.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection


class API {

    //class for document types
    class Document(
        val name: String,
        val id: String,
        val iconRes: Int,
        val isNeedBackSide: Boolean,
        val isBigCornerMode: Boolean
    )

    companion object {

        //todo false
        const val DEBUG = false

        val build: API = API()

        private const val HOST = "https://app.passbase.com/api/v1"

        private const val STEP = "authentications/steps"
        private const val FILES = "authentications/document_files"
        private const val ASSESSMENTS = "authentication_assessments"
        private const val DOC_TYPES = "authentications/document_types"

        const val SENTRY_DSN = "https://128c9e4211f34105afe92d0941c7900e@sentry.io/1472658"

        // init static documents array
        val documents = arrayOf(
            Document(
                "Passport",
                "PASSPORT",
                R.drawable.aic_passport,
                isNeedBackSide = false,
                isBigCornerMode = true
            ),
            Document(

                "Driving License",
                "DRIVERS_LICENSE",
                R.drawable.aic_driver_license,
                isNeedBackSide = true,
                isBigCornerMode = false
            ),
            Document(
                "National ID Card",
                "NATIONAL_ID_CARD",
                R.drawable.aic_national_id,
                isNeedBackSide = true,
                isBigCornerMode = false
            )
        )

        var dynamicDocument = emptyArray<Document>()

        /*---customize UI---*/
        var bgColor: Int = Color.WHITE
        var progressBarColor: Int = Color.rgb(0, 69, 255)
        var actionButtonColor: Int = Color.rgb(0, 69, 255)
        var font: Typeface? = null
        var language: String = "en" //

        fun setCustomizeUI(activity: AppCompatActivity) {

            val viewBackground = CViewBackground(activity)

            activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

            when (activity) {
                is ChooseCitizenActivity -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_progressBar30)
                        .setProgressColor(progressBarColor)
                    viewBackground.forView(R.id.passbase_country_parent_constraint, bgColor)
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_country_parent_constraint) as ViewGroup,
                            it
                        )
                    }
                    viewBackground.forView(R.id.passbase_startButton_country, actionButtonColor)
                }
                is CheckEmailActivity -> {
                    viewBackground.forView(R.id.passbase_layoutCheckEmail, bgColor)
                    viewBackground.forView(R.id.passbase_startButton, actionButtonColor)
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_layoutCheckEmail) as ViewGroup,
                            it
                        )
                    }
                }
                is ChooseDocumentType -> {
                    activity.findViewById<ProgressBar>(R.id.progressBar5)
                        .setProgressColor(progressBarColor)
                    viewBackground.forView(R.id.passbase_layoutChooseDocType, bgColor)
                    viewBackground.forView(R.id.nextButtonDocumentChose, actionButtonColor)
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_layoutChooseDocType) as ViewGroup,
                            it
                        )
                    }
                }
                is Congrats -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_progressBar3)
                        .setProgressColor(progressBarColor)
                    viewBackground.forView(R.id.passbase_layoutCongrats, bgColor)
                    viewBackground.forView(R.id.passbase_finishButton, actionButtonColor)
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_layoutCongrats) as ViewGroup,
                            it
                        )
                    }
                }
                is ErrorScreen -> {
                    viewBackground.forView(R.id.passbase_layoutError, bgColor)
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_layoutError) as ViewGroup,
                            it
                        )
                    }
                }
                is MoveHeadInstructions -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_progressBar2)
                        .setProgressColor(progressBarColor)
                    viewBackground.forView(R.id.passbase_constraintLayout, bgColor)
                    viewBackground.forView(
                        R.id.passbase_startRecordingSelfieVideo,
                        actionButtonColor
                    )
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_constraintLayout) as ViewGroup,
                            it
                        )
                    }
                }
                is PrepareBacksideID -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_progressBar6)
                        .setProgressColor(progressBarColor)
                    viewBackground.forView(R.id.passbase_layoutPrepareBacksideId, bgColor)
                    viewBackground.forView(R.id.passbase_takePhotoBackButton, actionButtonColor)
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_layoutPrepareBacksideId) as ViewGroup,
                            it
                        )
                    }
                }
                is PrepareID -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_progressBar4)
                        .setProgressColor(progressBarColor)
                    viewBackground.forView(R.id.passbase_layoutPrepareId, bgColor)
                    viewBackground.forView(R.id.passbase_takePhotoFrontButton, actionButtonColor)
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_layoutPrepareId) as ViewGroup,
                            it
                        )
                    }
                }
                is StartVerification -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_progressBar)
                        .setProgressColor(progressBarColor)
                    viewBackground.forView(R.id.passbase_layoutStartVerification, bgColor)
                    viewBackground.forView(R.id.passbase_startButton2, actionButtonColor)
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_layoutStartVerification) as ViewGroup,
                            it
                        )
                    }
                }
                is ObjectDetectionActivity -> {
                    font?.let {
                        CFont.forLayout(
                            activity.findViewById<View>(R.id.passbase_detect_camera_parent) as ViewGroup,
                            it
                        )
                    }
                    viewBackground.forButton(R.id.passbase_detect_hint_confirm, actionButtonColor)
                    viewBackground.forButton(R.id.passbase_detect_take_manually, actionButtonColor)
                }

                is StartScreenActivity -> {

                    activity.findViewById<ProgressBar>(R.id.passbase_start_screen_progress).apply {
                        setProgressColor(progressBarColor)
                    }
                    viewBackground.forButton(R.id.passbase_start_screen_action, actionButtonColor)
                }
                is ChooseCountryActivity -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_choose_country_progress)
                        .apply {
                            setProgressColor(progressBarColor)
                        }
                    activity.findViewById<ActionButton>(R.id.passbase_choose_country_action)
                        .setColor(actionButtonColor)
                }
                is EnterEmailActivity -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_enter_email_progress)
                        .apply {
                            setProgressColor(progressBarColor)
                        }
                    activity.findViewById<ActionButton>(R.id.passbase_enter_email_action)
                        .setColor(actionButtonColor)
                }
                is TakeSelfieVideoActivity -> {

                    activity.findViewById<ProgressBar>(R.id.passbase_take_selfie_video_progress)
                        .apply {
                            setProgressColor(progressBarColor)
                        }

                    viewBackground.forView(
                        R.id.passbase_take_selfie_upload_progress,
                        progressBarColor
                    )

                    activity.findViewById<ActionButton>(R.id.passbase_take_selfie_video_action)
                        .setColor(actionButtonColor)

                }
                is SelectDocumentActivity -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_select_document_progress)
                        .apply {
                            setProgressColor(progressBarColor)
                        }
                    activity.findViewById<ActionButton>(R.id.passbase_select_document_action)
                        .setColor(actionButtonColor)
                }
                is ScanFrontDocumentActivity -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_scan_front_document_progress)
                        .apply {
                            setProgressColor(progressBarColor)
                        }
                    activity.findViewById<ActionButton>(R.id.passbase_scan_front_document_action)
                        .setColor(actionButtonColor)
                    activity.findViewById<ActionButton>(R.id.passbase_scan_front_document_upload_progress)
                        .setColor(actionButtonColor)

                }
                is ScanBackDocumentActivity -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_scan_back_document_progress)
                        .apply {
                            setProgressColor(progressBarColor)
                        }
                    activity.findViewById<ActionButton>(R.id.passbase_scan_back_document_action)
                        .setColor(actionButtonColor)
                    activity.findViewById<ActionButton>(R.id.passbase_scan_back_document_upload_progress)
                        .setColor(actionButtonColor)

                }
                is SecurelyDataTransferActivity -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_securely_data_transfer_progress)
                        .apply {
                            setProgressColor(progressBarColor)
                        }
                    viewBackground.forButton(
                        R.id.passbase_securely_data_transfer_action,
                        actionButtonColor
                    )
                }
                is CongratsDoneActivity -> {
                    activity.findViewById<ProgressBar>(R.id.passbase_congrats_done_progress)
                        .apply {
                            setProgressColor(progressBarColor)
                        }
                    viewBackground.forButton(
                        R.id.passbase_congrats_done_action,
                        actionButtonColor
                    )
                }
                is SomethingWrongActivity -> {
                    activity.findViewById<ActionButton>(R.id.passbase_congrats_done_action)
                        .setColor(actionButtonColor)
                }
            }
        }

        fun setLocale(context: Context, language: String) {

            this.language = language

            val dm = context.resources.displayMetrics
            val config = context.resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(Locale(language.toLowerCase()))
            } else {
                config.locale = Locale(language.toLowerCase())
            }
            context.resources.updateConfiguration(config, dm)
        }

        //parse json and get value by field
        fun checkResponse(json: String?, field: String): String? {

            var result: String? = null
            json?.let {
                val jsonObject = JSONObject(it)
                jsonObject.getString(field)?.let { value ->
                    result = value
                } ?: run {
                    result = null
                }
            } ?: run {
                result = null
            }

            return result
        }

        fun sendException(message: Any) {
//
//            val info = Sentry.capture(
//                "user email: ${Passbase.PREFILLUSEREMAIL}\n" +
//                        "api_key: ${Passbase.APIKEY}\n" +
//                        "auth_key: ${Passbase.AUTHKEY}"
//            )
//
//            if (message is String) {
//                println("error $message")
////                Sentry.capture("$message\n$info")
//            }
//
//            if (message is Exception) {
//                println("error ${message.message}")
////                Sentry.capture(message)
////                Sentry.capture("for previous error $info")
//            }
//
//            if (message is IOException) {
//                println("error ${message.message}")
////                Sentry.capture(message)
////                Sentry.capture("for previous error $info")
//            }
        }

        fun loadAnimation(
            activity: AppCompatActivity,
            imageRes: Int,
            buttonRes: Int,
            show: Boolean
        ) {

            val imgView = activity.findViewById<ImageView>(imageRes)
            val nextBtn = activity.findViewById<Button>(buttonRes)

            if (show) {
                Glide.with(activity).asGif().load(R.raw.loadinggif).into(imgView)
                nextBtn.isEnabled = false
                imgView.isEnabled = false
            } else {
                imgView.setImageResource(R.drawable.aic_cancelicon3x)
                Handler().postDelayed({
                    nextBtn.isEnabled = true
                    imgView.isEnabled = true
                }, 1000L)
            }
        }

        fun finish() {

            ChooseCitizenActivity.tryFinish()
            CheckEmailActivity.tryFinish()
            ChooseDocumentType.tryFinish()
            ErrorScreen.tryFinish()
            MoveHeadInstructions.tryFinish()
            PrepareBacksideID.tryFinish()
            PrepareID.tryFinish()
            StartVerification.tryFinish()

            ChooseCountryActivity.tryFinish()
            CongratsDoneActivity.tryFinish()
            EnterEmailActivity.tryFinish()
            ScanBackDocumentActivity.tryFinish()
            ScanFrontDocumentActivity.tryFinish()
            SecurelyDataTransferActivity.tryFinish()
            SelectDocumentActivity.tryFinish()
            StartScreenActivity.tryFinish()
            TakeSelfieVideoActivity.tryFinish()
        }
    }

    private class InputParams(
        val url: String,
        val params: Array<Pair<String, String>>,

        val fieldName: String = "",
        val fileName: String = "",
        val fileType: String = "",
        val filePath: String = "",

        val additionalAttributes: Array<Pair<String, String>> = emptyArray(),
        val header: Array<Pair<String, String>> = emptyArray(),

        val onResult: (String?) -> Unit
    )

    private class SendTask : AsyncTask<InputParams, Void, Void>() {

        override fun doInBackground(vararg params: InputParams): Void? {

            try {

                /*---ADDITIONAL ATTRIBUTES----*/
                if (params[0].additionalAttributes.isNotEmpty()) {

                    val url = URL(params[0].url)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.connectTimeout = 60000
                    conn.readTimeout = 60000
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    conn.connect()

                    println(params[0].url)

                    val jsonObject = JSONObject()
                    params[0].params.forEach {
                        println("${it.first} : ${it.second}")
                        jsonObject.put(it.first, it.second)
                    }


                    val addAttr = JSONObject()
                    params[0].additionalAttributes.forEach {
                        println("${it.first} : ${it.second}")
                        addAttr.put(it.first, it.second)
                    }
                    jsonObject.put("additionalAttributes", addAttr)


                    println("REQUEST add $jsonObject")

                    val wr = DataOutputStream(conn.outputStream)
                    wr.writeBytes(jsonObject.toString())
                    wr.flush()
                    wr.close()

                    println("connection ${conn.responseCode} ${conn.responseMessage}")

                    if (conn.responseCode == HttpsURLConnection.HTTP_OK) {

                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        val sb = StringBuilder()
                        var inputLine = reader.readLine()
                        while (inputLine != null) {
                            sb.append(inputLine)
                            inputLine = reader.readLine()
                        }

                        println("RESPONSE $sb")
                        params[0].onResult(sb.toString())
                        reader.close()
                    } else
                        params[0].onResult(null)

                    /*----SAMPLE REQUEST----*/
                } else {

                    val client = OkHttpClient
                        .Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build()

                    val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)

                    println(params[0].url)

                    //set post params
                    params[0].params.forEach {
                        println("${it.first} : ${it.second}")
                        requestBody.addFormDataPart(it.first, it.second)
                    }

                    //set file if exist
                    if (
                        params[0].filePath.isNotEmpty() &&
                        params[0].fileName.isNotEmpty() &&
                        params[0].fileType.isNotEmpty() &&
                        params[0].fieldName.isNotEmpty()
                    ) {

                        val mediaType = params[0].fileType.toMediaTypeOrNull()
                        requestBody.addFormDataPart(
                            params[0].fieldName,
                            params[0].fileName,
                            RequestBody.create(mediaType, File(params[0].filePath))
                        )
                    }

                    val request = Request.Builder()
                    request.url(params[0].url)
                    request.addHeader("Content-Type", "application/json; charset=utf-8")
                    params[0].header.forEach {
                        println("header $it")
                        request.addHeader(it.first, it.second)
                    }

                    if (params[0].header.isNotEmpty()) {
                        request.get()
                    } else request.post(requestBody.build())

                    val response = client.newCall(request.build()).execute()

                    println("connection ${response.code} ${response.message}")

                    if (response.code == HttpsURLConnection.HTTP_OK)
                        BufferedReader(InputStreamReader(response.body?.byteStream())).use {
                            val responseString = StringBuffer()

                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                responseString.append(inputLine)
                                inputLine = it.readLine()
                            }

                            println("RESPONSE : $responseString")
                            params[0].onResult(responseString.toString())

                            it.close()
                        }
                    else {
                        params[0].onResult(null)
                    }

                }

            } catch (e: Exception) {

                sendException(e)
                params[0].onResult(null)
            }

            return null
        }
    }

    fun createAuthentication(
        apiKey: String,
        additionalAttributes: Array<Pair<String, String>>,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(Pair("api_key", apiKey))
        val url = "$HOST/authentications"

        val inputParams = InputParams(url, params, additionalAttributes = additionalAttributes) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun signupWithEmail(
        apiKey: String,
        authKey: String,
        email: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "signup"),
            Pair("identifier_type", "email"),
            Pair("identifier", email)
        )
        val url = "$HOST/$STEP"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun recordFaceVideo(
        apiKey: String,
        authKey: String,
        filePath: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "record_face_video")
        )

        val url = "$HOST/$FILES"

        val inputParams = InputParams(url, params, "video", "video.mp4", "video/mp4", filePath) {
            completionHandler(it)
        }

        SendTask().execute(inputParams)
    }

    fun checkFaceVideo(
        apiKey: String,
        authKey: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "check_face_video")
        )

        val url = "$HOST/$STEP"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun chooseAuthenticationDocument(
        apiKey: String,
        authKey: String,
        documentId: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "choose_authentication_document"),
            Pair("authentication_method", documentId)
        )

        val url = "$HOST/$STEP"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun recordAuthenticationDocumentPicFRONT(
        apiKey: String,
        authKey: String,
        filePath: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "record_authentication_document_video")
        )

        val url = "$HOST/$FILES"

        val inputParams =
            InputParams(url, params, "picture", "photo_f.jpg", "image/jpg", filePath) {
                completionHandler(it)
            }

        SendTask().execute(inputParams)
    }

    fun checkAuthenticationDocumentPicFRONT(
        apiKey: String,
        authKey: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "check_authentication_document_video")
        )

        val url = "$HOST/$STEP"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun recordAuthenticationDocumentPicBACK(
        apiKey: String,
        authKey: String,
        filePath: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "record_authentication_document_video_back")
        )

        val url = "$HOST/$FILES"

        val inputParams =
            InputParams(url, params, "picture", "photo_b.jpg", "image/jpg", filePath) {
                completionHandler(it)
            }

        SendTask().execute(inputParams)
    }

    fun checkAuthenticationDocumentPicBACK(
        apiKey: String,
        authKey: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "check_authentication_document_video_back")
        )

        val url = "$HOST/$STEP"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun recordCitizenship(
        apiKey: String,
        authKey: String,
        countryCode: String,
        completionHandler: (json: String?) -> Unit
    ) {
        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "citizenship"),
            Pair("country", countryCode)
        )

        val url = "$HOST/$STEP"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun setNationality(
        apiKey: String,
        authKey: String,
        countryCode: String,
        completionHandler: (json: String?) -> Unit
    ) {
        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "set_nationality"),
            Pair("country_code", countryCode)
        )

        val url = "$HOST/$STEP"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun checkAuthenticationDocumentSuccess(
        apiKey: String,
        authKey: String,
        completionHandler: (json: String?) -> Unit
    ) {
        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair("step", "check_authentication_document_video_success")
        )

        val url = "$HOST/$STEP"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }

    fun sendLivenessScore(
        apiKey: String,
        authKey: String,
        livenessAssessment: Float,
        completionHandler: (json: String?) -> Unit
    ) {

        val params = arrayOf(
            Pair("api_key", apiKey),
            Pair("key", authKey),
            Pair(
                "authentication_assessments",
                "{\"token\": \"LIVENESS_FACTOR\",\"value\": \"$livenessAssessment\"}"
            )
        )

        val url = "$HOST/$ASSESSMENTS"

        val inputParams = InputParams(url, params) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)

    }

    fun getDocumentType(
        apiKey: String,
        authKey: String,
        completionHandler: (json: String?) -> Unit
    ) {

        val header = arrayOf(
            Pair("Authorization", "APIKEY $apiKey")
        )

        val url = "$HOST/$DOC_TYPES?key=$authKey"

        val inputParams = InputParams(url, params = emptyArray(), header = header) {
            completionHandler(it)
        }
        SendTask().execute(inputParams)
    }
}