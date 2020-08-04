package com.passbase.passbase_sdk.new_design

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.passbase.passbase_sdk.*
import com.project.myapplication.R
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class ChooseCountryActivity : AppCompatActivity() {

    companion object {
        var tryFinish = {}
    }

    private class Country(val name: String, val code: String, var iconRes: Int = 0) {
        override fun toString(): String {
            return name
        }
    }

    private var countryList: MutableList<Country> = mutableListOf()
    private var currentCountryIndex = 0
    private var currentLocale = "de"
    private var isCountryListOpen = false

    //views
    private lateinit var countryIconView: ImageView
    private lateinit var countryTxtView: TextView
    private lateinit var actionBtn: ActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_country_passbase)

        API.setCustomizeUI(this)

        currentLocale = Locale.getDefault().country.toString().toLowerCase()

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

        countryIconView = findViewById(R.id.passbase_select_document_icon)
        countryTxtView = findViewById(R.id.passbase_select_document_txt)
        actionBtn = findViewById(R.id.passbase_choose_country_action)

        actionBtn.setOnClick {
            CVibtation(this@ChooseCountryActivity).vibrate()
            actionBtn.startLoading()
            sendCountry()
        }

        findViewById<View>(R.id.passbase_choose_country_title).apply {
            setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.passbase.com"))
                startActivity(browserIntent)
            }
        }

        findViewById<View>(R.id.passbase_choose_country_selector_parent).setOnClickListener {
            setViewCountryDialog(!isCountryListOpen)
        }

        findViewById<ProgressBar>(R.id.passbase_choose_country_progress).apply {
            progress = 10
            val a = ObjectAnimator.ofInt(this, "progress", 20)
            a.duration = 600
            a.repeatCount = 0
            a.startDelay = 1000
            a.start()
        }

        findViewById<View>(R.id.passbase_choose_country_back).apply {
            setOnClickListener {
                finish()
                overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out)
            }
        }

        readCountryList()
    }

    //dialog handlers
    private fun readCountryList() {

        val stream = resources.openRawResource(R.raw.countries)

        var byteStream: ByteArrayOutputStream? = null
        try {
            val buffer = ByteArray(stream.available())
            stream.read(buffer)
            byteStream = ByteArrayOutputStream()
            byteStream.write(buffer)
            byteStream.close()
            stream.close()
        } catch (e: IOException) {
            API.sendException(e)
            e.printStackTrace()
        }

        val json = byteStream.toString()
        try {
            val jsonArray = JSONArray(json)
            if (jsonArray.length() > 0) {
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val nameCap = name.substring(0, 1).toUpperCase() + name.substring(1)
                    val code = obj.getString("code")

                    this.countryList.add(Country(nameCap, code))
                    //this.countryNamesRes.add(Country(nameCap, code))
                }
            }
        } catch (e: IOException) {
            API.sendException(e)
        }

        /* AUTO SELECT COUNTRY*/
        currentCountryIndex = 0 //"de"
        val defaultC = currentLocale
        countryList.forEachIndexed { index, pair ->
            if (defaultC.toLowerCase() == pair.code.toLowerCase()) {
                currentCountryIndex = index
            }
        }

        //important: only in this part use read icon
        countryList.forEach {

            var code = it.code
            //"do_1" is not a valid resource name (reserved Java keyword)
            if (it.code == "do") code = "do_1"

            val res = resources.getIdentifier(code, "drawable", packageName)
            it.iconRes = res
        }

        Handler().postDelayed({
            try {
                setList(countryList)
            } catch (e: Exception) {
            }
        }, 400L)

        setCountry(currentCountryIndex)
    }

    private fun setList(list: MutableList<Country>) {

        val parentLay = findViewById<LinearLayout>(R.id.passbase_choose_country_parent_list)
        parentLay.removeAllViews()
        val inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val listItemLen = list.size

        try {
            for (i in 0 until listItemLen) {

                val itemLay = inflater.inflate(R.layout.country_item_passbase, null)
                parentLay.addView(itemLay)
                val cImg = itemLay.findViewById<ImageView>(R.id.passbase_country_item_icon)
                val cName = itemLay.findViewById<TextView>(R.id.passbase_country_item_text)

                Glide.with(this).asBitmap().load(list[i].iconRes).into(cImg)

                cName.text = list[i].name
                API.font?.let {
                    cName.typeface = it
                }
                itemLay.setOnClickListener {
                    setCountry(list[i].code)
                    setViewCountryDialog(false)
                }
            }
        } catch (e: Exception) {

            API.sendException(e)
        }


        val scrollView = findViewById<ScrollView>(R.id.passbase_choose_country_scroll)
        scrollView.post {
            scrollView.smoothScrollBy(0, 0)
        }
    }

    private fun setCountry(index: Int) {

        if (currentCountryIndex < 0) return

        currentCountryIndex = index

        try {
            this.countryIconView.setImageResource(countryList[index].iconRes)
            this.countryTxtView.text = countryList[index].name
        } catch (e: Exception) {
            API.sendException(e)
        }
    }

    private fun setCountry(code: String) {

        countryList.forEachIndexed { index, country ->
            if (country.code.contains(code, true)) {
                currentCountryIndex = index
            }

        }

        setCountry(currentCountryIndex)
    }

    private fun setViewCountryDialog(open: Boolean) {

        isCountryListOpen = open

        val duration = 300L

        findViewById<View>(R.id.passbase_select_document_open_arrow).apply {
            val r = if (open) 180f else 0f
            animate().setDuration(duration).rotation(r).start()
        }

        findViewById<View>(R.id.passbase_choose_country_scroll).apply {

            val max = if (getAspectRatio()) dpToPx(170) else dpToPx(100)

            val va = if (open) ValueAnimator.ofInt(0, max) else ValueAnimator.ofInt(max, 0)
            va.duration = duration
            va.addUpdateListener {
                val v = it.animatedValue as Int
                val lp = layoutParams
                lp.height = v
                layoutParams = lp
            }
            va.start()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private fun getAspectRatio(): Boolean {

        val display = Point()
        windowManager.defaultDisplay.getSize(display)

        var r = PointF(1f, 1f)

        val aspectRatio = arrayOf(
            PointF(9f, 21f),
            PointF(9f, 20f),
            PointF(9f, 19f),
            PointF(9f, 19.5f),
            PointF(9f, 18f),
            PointF(9f, 17f),
            PointF(9f, 16f)
        )

        var min = Double.MAX_VALUE
        aspectRatio.forEach {
            val d =
                Math.abs((display.x.toDouble() / display.y.toDouble()) - (it.x.toDouble() / it.y.toDouble()))
            if (d < min) {
                min = d
                r = PointF(it.x, it.y)
            }
        }

        return r.y / r.x >= 2
    }

    private fun sendCountry() {

        if (API.DEBUG) {
            goToNextView()
            return
        }

        if (currentCountryIndex < 0) return

        API.build.setNationality(
            Passbase.APIKEY,
            Passbase.AUTHKEY,
            countryList[currentCountryIndex].code
        ) { json ->

            API.checkResponse(json, "next_step")?.let {
                checkIfEmailPrefilled()
            } ?: run {
                API.sendException("ChooseCitizenActivity : sendCountry $json")
                goToErrorScreen()
            }
        }
    }

    private fun checkIfEmailPrefilled() {

        if (Passbase.PREFILLUSEREMAIL.isNotEmpty() && Passbase.APIKEY.isNotEmpty() && Passbase.AUTHKEY.isNotEmpty()) {

            API.build.signupWithEmail(Passbase.APIKEY, Passbase.AUTHKEY, Passbase.PREFILLUSEREMAIL) { json ->

                API.checkResponse(json, "next_step")?.let {
                    skipNextView()
                } ?: run {
                    goToNextView()
                }
            }
        } else {
            goToNextView()
        }
    }

    private fun goToErrorScreen() {

        runOnUiThread {
            Handler().postDelayed({
                actionBtn.stopLoading()
            }, 400)
            val intent = Intent(this, SomethingWrongActivity::class.java)
            startActivity(intent)
        }
    }

    private fun skipNextView() {

        runOnUiThread {
            Handler().postDelayed({
                actionBtn.stopLoading()
            }, 400)
            startActivity(Intent(this, TakeSelfieVideoActivity::class.java))
        }
    }

    private fun goToNextView() {

        runOnUiThread {
            Handler().postDelayed({
                actionBtn.stopLoading()
            }, 400)
            startActivity(Intent(this, EnterEmailActivity::class.java))
        }
    }


}