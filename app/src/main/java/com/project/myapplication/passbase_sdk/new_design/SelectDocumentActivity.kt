package com.passbase.passbase_sdk.new_design

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.passbase.passbase_sdk.API
import com.passbase.passbase_sdk.CVibtation
import com.passbase.passbase_sdk.Passbase
import com.project.myapplication.R

class SelectDocumentActivity : AppCompatActivity() {

    companion object {
        var tryFinish = {}
    }

    private var selectedIndex = 0
    private val docList = if (API.DEBUG) API.documents else API.dynamicDocument

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_document_passbase)

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

        findViewById<View>(R.id.passbase_select_document_title).apply {
            setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<ProgressBar>(R.id.passbase_select_document_progress).apply {
            progress = 40
            val a = ObjectAnimator.ofInt(this, "progress", 50)
            a.duration = 600
            a.repeatCount = 0
            a.startDelay = 1000
            a.start()
        }

        findViewById<ActionButton>(R.id.passbase_select_document_action).apply {
            setOnClickListener {
                CVibtation(this@SelectDocumentActivity).vibrate()
                sendToBackend()
                goToNextView()
            }
        }

        findViewById<View>(R.id.passbase_select_document_back).apply {
            setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
            }
        }

        findViewById<View>(R.id.passbase_select_document_selector_parent).apply {
            setOnClickListener {
                showDockList(true)
            }
        }

        initDocList()
    }

    private fun initDocList() {

        val parentLay = findViewById<LinearLayout>(R.id.passbase_select_document_table)
        val inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        //array for control border visible
        var arrViewDocBorder = emptyArray<ImageView>()

        docList.forEachIndexed { index, doc ->

            val itemLay = inflater.inflate(R.layout.document_item_passbase, null)
            parentLay.addView(itemLay)
            val docName = itemLay.findViewById<TextView>(R.id.passbase_adapter_doc_name)
            val docImage = itemLay.findViewById<ImageView>(R.id.passbase_adapter_doc_image)
            val docBorder = itemLay.findViewById<ImageView>(R.id.passbase_adapter_doc_border)

            arrViewDocBorder = arrViewDocBorder.plus(docBorder)

            docName.text = doc.name
            docImage.setImageResource(API.documents[index].iconRes)
            docBorder.visibility = View.GONE
            if (index == selectedIndex) {
                docBorder.visibility = View.VISIBLE
            }

            itemLay.setOnClickListener {
                arrViewDocBorder.forEach { border ->
                    border.visibility = View.GONE
                }
                docBorder.visibility = View.VISIBLE
                selectedIndex = index

                showDockList(false)
            }
        }

        findViewById<ImageView>(R.id.passbase_select_document_icon).apply {
            setImageResource(docList[selectedIndex].iconRes)
        }
        findViewById<TextView>(R.id.passbase_select_document_txt).apply {
            text = docList[selectedIndex].name
        }
    }

    private fun showDockList(show: Boolean) {

        findViewById<ImageView>(R.id.passbase_select_document_icon).apply {
            setImageResource(docList[selectedIndex].iconRes)
        }
        findViewById<TextView>(R.id.passbase_select_document_txt).apply {
            text = docList[selectedIndex].name
        }

        findViewById<View>(R.id.passbase_select_document_table).apply {

            val va2 = if (show) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f,0f)
            va2.duration = if(show) 300L else 200L
            va2.startDelay = if(show) 100L else 0L
            va2.addUpdateListener {
                val v = it.animatedValue as Float
                alpha = v
            }
            va2.start()

            val lp = layoutParams
            val h = 192
            val va = if (show) ValueAnimator.ofInt(0, dpToPx(h)) else ValueAnimator.ofInt(dpToPx(h), 0)
            va.duration = 300L
            va.addUpdateListener {
                val v = it.animatedValue as Int
                lp.height = v
                layoutParams = lp
            }
            va.start()
        }

        findViewById<View>(R.id.passbase_select_document_selector_parent).apply {
            val a = if (show) 0f else 1f
            animate().setDuration(300L).alpha(a).start()
        }
    }

    private fun sendToBackend() {

        if (API.DEBUG) {
            return
        }

        //if (selectedIndex >= 0 && selectedIndex < API.documents.size) {
        if (selectedIndex >= 0 && selectedIndex < docList.size) {

            //var doc = API.documents[selectedIndex].id
            val doc = docList[selectedIndex].id
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
                    //API.loadAnimation(this, R.id.exitButtonChooseDocument, R.id.nextButtonDocumentChose, false)
                }
            }
        } else {
            //.makeText(this, "Please choose document.", Toast.LENGTH_LONG).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private fun goToNextView() {

        val intent = Intent(this, ScanFrontDocumentActivity::class.java)
        intent.putExtra("documentIndex", selectedIndex)
        startActivity(intent)
    }
}