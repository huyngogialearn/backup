package com.passbase.passbase_sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.project.myapplication.R

class ChooseDocumentType : AppCompatActivity() {

    //for finish ChooseDocumentType from any activity
    companion object {
        var tryFinish: (() -> Unit) = {}
    }

    private var selectedIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_document_type_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        API.setCustomizeUI(this)

        setupListeners()

        initDocList()

        tryFinish = {
            finish()
        }
    }

    private fun setupListeners() {

        val nextButtonDocumentChose = findViewById<Button>(R.id.nextButtonDocumentChose)
        val exitButtonChooseDocument = findViewById<ImageButton>(R.id.passbase_exitButtonChooseDocument)

        nextButtonDocumentChose.setOnClickListener {

            CVibtation(this).vibrate()
            API.loadAnimation(this, R.id.passbase_exitButtonChooseDocument, R.id.nextButtonDocumentChose, true)
            chooseDocumentAndSendToBackend()
        }
        exitButtonChooseDocument.setOnClickListener {
            exitView()
        }
    }

    // Table
    private fun initDocList() {

        val parentLay = findViewById<LinearLayout>(R.id.passbase_choose_doc_layout)
        val inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //array for control border visible
        var arrViewDocBorder = emptyArray<ImageView>()


        API.dynamicDocument.forEachIndexed { index, doc ->

            val itemLay = inflater.inflate(R.layout.document_item_passbase, null)
            parentLay.addView(itemLay)
            val docName = itemLay.findViewById<TextView>(R.id.passbase_adapter_doc_name)
            val docImage = itemLay.findViewById<ImageView>(R.id.passbase_adapter_doc_image)
            val docBorder = itemLay.findViewById<ImageView>(R.id.passbase_adapter_doc_border)

            arrViewDocBorder = arrViewDocBorder.plus(docBorder)

            docName.text = doc.name
            API.font?.let {
                docName.typeface = it
            }
            docImage.setImageResource(doc.iconRes)
            docBorder.visibility = View.GONE

            itemLay.setOnClickListener {
                arrViewDocBorder.forEach { border ->
                    border.visibility = View.GONE
                }
                docBorder.visibility = View.VISIBLE
                selectedIndex = index
                CVibtation(this).vibrate()
            }
        }

    }

    private fun chooseDocumentAndSendToBackend() {

        //if (selectedIndex >= 0 && selectedIndex < API.documents.size) {
        if (selectedIndex >= 0 && selectedIndex < API.dynamicDocument.size) {

            goToNextView()

            //var doc = API.documents[selectedIndex].id
            val doc = API.dynamicDocument[selectedIndex].id
            //doc = ""

            API.build.chooseAuthenticationDocument(
                Passbase.APIKEY,
                Passbase.AUTHKEY,
                doc
            ) { json ->
                API.checkResponse(json, "next_step")?.let {
                    //goToNextView()
                } ?: run {
                    //API.sendException("ChooseDocumentType : chooseDocumentAndSendToBackend $json")
                    //Show error
                    //goToErrorScreen()
                }
                runOnUiThread {
                    API.loadAnimation(this, R.id.passbase_exitButtonChooseDocument, R.id.nextButtonDocumentChose, false)
                }
            }
        } else {
            Toast.makeText(this, "Please choose document.", Toast.LENGTH_LONG).show()
        }


    }

    // Navigation
    private fun goToNextView() {

        val intent = Intent(this, PrepareID::class.java)
        intent.putExtra("documentIndex", selectedIndex)
        startActivity(intent)
    }

    private fun goToErrorScreen() {

        val intent = Intent(this, ErrorScreen::class.java)
        startActivity(intent)
    }

    private fun exitView() {

        ChooseCitizenActivity.tryFinish()
        CheckEmailActivity.tryFinish()
        ErrorScreen.tryFinish()
        MoveHeadInstructions.tryFinish()
        PrepareBacksideID.tryFinish()
        PrepareID.tryFinish()
        StartVerification.tryFinish()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
    }

    override fun onDestroy() {
        super.onDestroy()

        tryFinish = {}
    }
}
