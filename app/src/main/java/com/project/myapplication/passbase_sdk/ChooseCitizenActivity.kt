package com.passbase.passbase_sdk

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.bumptech.glide.Glide
import com.project.myapplication.R
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class ChooseCitizenActivity : AppCompatActivity() {

    //for finish ChooseCitizenActivity from any activity
    companion object {
        var tryFinish: (() -> Unit) = {}
    }

    private class Country(val name: String, val code: String, var iconRes: Int = 0) {
        override fun toString(): String {
            return name
        }
    }

    private var countryList: MutableList<Country> = mutableListOf()
    private var currentCountryIndex = 0
    private var currentLocale = "de"

    private lateinit var countryTxtView: TextView
    private lateinit var countryIconView: ImageView
    private lateinit var countryIconDialog: ImageView


    private var dialogStateSmall = true


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        API.setLocale(this, API.language)

        setContentView(R.layout.activity_citizes_passbase)
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        API.setCustomizeUI(this)

        currentLocale = Locale.getDefault().country.toString().toLowerCase()

        val infoText = findViewById<TextView>(R.id.passbase_country_info)
        infoText.movementMethod = LinkMovementMethod.getInstance()

        setupListeners()
        readCountryList()

        tryFinish = {
            exitView()
        }


    }

    override fun onBackPressed() {

        if (!dialogStateSmall) {
            setViewCountryDialog(true)
        } else {
            super.onBackPressed()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setupListeners() {

        countryIconView = findViewById(R.id.passbase_country_icon)
        countryTxtView = findViewById(R.id.passbase_country_txt)
        countryIconDialog = findViewById(R.id.passbase_country_dialog_icon)

        findViewById<LinearLayout>(R.id.passbase_country_parent).setOnClickListener {

            dialogStateSmall = !dialogStateSmall
            setViewCountryDialog(dialogStateSmall)
        }

        findViewById<View>(R.id.passbase_country_img1).setOnClickListener {

            dialogStateSmall = !dialogStateSmall
            setViewCountryDialog(dialogStateSmall)
        }

        findViewById<View>(R.id.passbase_country_dialog_arrow).setOnClickListener {

            dialogStateSmall = !dialogStateSmall
            setViewCountryDialog(dialogStateSmall)
        }

        findViewById<ImageView>(R.id.passbase_exitButtonCountry).setOnClickListener {

            exitView()
        }

        findViewById<Button>(R.id.passbase_startButton_country).setOnClickListener {

            CVibtation(this).vibrate()
            API.loadAnimation(this, R.id.passbase_exitButtonCountry, R.id.passbase_startButton_country, true)
            sendCountry()
        }

        val dialogFindEditText = findViewById<EditText>(R.id.passbase_country_dialog_edit)
        dialogFindEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                findInCountryDialog(dialogFindEditText.text.toString())

            }
        })

    }

    override fun onResume() {
        super.onResume()

        hideKeyboard()
    }

    //dialog handlers
    @RequiresApi(Build.VERSION_CODES.KITKAT)
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

        //important: only in this part call readCountryIcon()
        readCountryIcon()

        setCountry(currentCountryIndex)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun readCountryIcon() {

        countryList.forEach {

            var code = it.code
            //"do_1" is not a valid resource name (reserved Java keyword)
            if (it.code == "do") code = "do_1"

            val res = resources.getIdentifier(code, "drawable", packageName)
            it.iconRes = res
        }

        setList(countryList)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setList(list: MutableList<Country>) {

        val parentLay = findViewById<LinearLayout>(R.id.passbase_country_dialog_parent)
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
                    setViewCountryDialog(true)
                }
            }
        } catch (e: Exception) {

            API.sendException(e)
        }


        val scrollView = findViewById<ScrollView>(R.id.passbase_country_dialog_scroll)
        scrollView.post {
            scrollView.smoothScrollBy(0, 0)
        }
    }

    private fun findInCountryDialog(name: String) {

        countryList.filter {
            it.name.contains(name, true) ||
                    it.code.contains(name, true)
        }.also {
            setList(it.toMutableList())
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun setViewCountryDialog(state: Boolean) {

        dialogStateSmall = state
        setList(countryList)

        this.currentFocus?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.let { it.hideSoftInputFromWindow(v.windowToken, 0) }
        }

        //val countryDialogSearchLayout = findViewById<View>(R.id.country_layout2)
        val dialogFindEditText = findViewById<EditText>(R.id.passbase_country_dialog_edit)
        dialogFindEditText.setText("")
        val arrow = findViewById<View>(R.id.passbase_country_img1)

        val id: Int = if (state) {
            //countryDialogSearchLayout.visibility = View.GONE
            //countryDialogSearchLayout.animate().setDuration(300).alpha(0f).scaleY(0f).start()
            arrow.animate().rotation(0f).setDuration(300).start()
            R.layout.activity_citizes_passbase
        } else {
            //countryDialogSearchLayout.visibility = View.VISIBLE
            //countryDialogSearchLayout.animate().setDuration(300).alpha(1f).scaleY(0f).start()
            arrow.animate().rotation(180f).setDuration(300).start()
            R.layout.activity_citizen_dialog_small_passbase
        }

//        val id: Int = when (state) {
//            0 -> {
//                countryDialogSearchLayout.visibility = View.GONE
//                countryDialogSearchLayout.animate().setDuration(300).alpha(0f).scaleY(0f).start()
//                arrow.animate().rotation(0f).setDuration(300).start()
//                arrow.rotation = 0f
//                R.layout.activity_citizes_passbase
//            }
//            1 -> {
//                //countryDialogSearchLayout.animate().setDuration(300).alpha(0f).scaleY(0f).start()
//                countryDialogSearchLayout.visibility = View.VISIBLE
//                arrow.animate().rotation(180f).setDuration(300).start()
//                R.layout.activity_citizen_dialog_small_passbase
//            }
//            2 -> {
//                countryDialogSearchLayout.visibility = View.VISIBLE
//                countryDialogSearchLayout.animate().setDuration(500).alpha(1f).scaleY(1f).start()
//                arrow.animate().rotation(0f).setDuration(300).start()
//                R.layout.activity_citizen_dialog_full_passbase
//            }
//            else -> {
//                countryDialogSearchLayout.visibility = View.GONE
//                countryDialogSearchLayout.animate().setDuration(300).alpha(0f).scaleY(0f).start()
//                arrow.animate().rotation(0f).setDuration(300).start()
//                R.layout.activity_citizes_passbase
//            }
//        }

        //  ANIMATION
        val root = findViewById<ConstraintLayout>(R.id.passbase_country_parent_constraint)
        val newConstraintSet = ConstraintSet()
        newConstraintSet.clone(this, id)
        newConstraintSet.applyTo(root)
        val transition = ChangeBounds()
        transition.interpolator = android.view.animation.AnticipateOvershootInterpolator()
        TransitionManager.beginDelayedTransition(root, transition)
    }

    private fun setCountry(index: Int) {

        if (currentCountryIndex < 0) return

        currentCountryIndex = index

        try {
            this.countryIconView.setImageResource(countryList[index].iconRes)
            this.countryIconDialog.setImageResource(countryList[index].iconRes)
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

        if (currentCountryIndex < 0) return

        try {
            this.countryIconView.setImageResource(countryList[currentCountryIndex].iconRes)
            this.countryIconDialog.setImageResource(countryList[currentCountryIndex].iconRes)
            this.countryTxtView.text = countryList[currentCountryIndex].name
        } catch (e: Exception) {
            API.sendException(e)
        }

    }

    private fun sendCountry() {

        if (currentCountryIndex < 0) return

        API.build.setNationality(
            Passbase.APIKEY,
            Passbase.AUTHKEY,
            countryList[currentCountryIndex].code
        ) { json ->

            API.checkResponse(json, "next_step")?.let {
                //goToNextView()
                checkIfEmailPrefilled()
            } ?: run {
                API.sendException("ChooseCitizenActivity : sendCountry $json")
                goToErrorScreen()
            }

            runOnUiThread {
                API.loadAnimation(this, R.id.passbase_exitButtonCountry, R.id.passbase_startButton_country, false)
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


    //navigation
    private fun goToNextView() {

        val intent = Intent(this, CheckEmailActivity::class.java)
        startActivity(intent)
    }

    private fun skipNextView() {

        val intent = Intent(this, StartVerification::class.java)
        startActivity(intent)
    }

    private fun goToErrorScreen() {

        val intent = Intent(this, ErrorScreen::class.java)
        startActivity(intent)
    }

    private fun exitView() {

        Passbase.onCancelPassbase()

        API.finish()
        finish()
    }

    private fun hideKeyboard() {
        //val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        var view = this.currentFocus
        if (view == null) {
            view = View(this)
        }
        view.clearFocus()
    }
}
