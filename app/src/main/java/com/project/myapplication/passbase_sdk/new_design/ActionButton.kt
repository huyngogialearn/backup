package com.passbase.passbase_sdk.new_design

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.project.myapplication.R
import kotlin.math.roundToInt

class ActionButton(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    companion object {
        private const val ANIMATION_SPEED = 300L
    }

    private val view: View = inflate(context, R.layout.action_button_passbase, this)
    private var loadChecker = false
    private var colorEnable =
        ContextCompat.getColorStateList(context, R.color.passbase_blue)
    private var colorDisable =
        ContextCompat.getColorStateList(context, R.color.passbase_blue_disable)
    val isLoading: Boolean
        get() = loadChecker

    init {

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.ActionButton)
        when (attributes.getBoolean(
            R.styleable.ActionButton_loading,
            false
        )) {
            false -> stopLoading(false)
            true -> startLoading(false)
        }
        attributes.recycle()

    }

    fun startLoading(animate: Boolean = true) {

        loadChecker = true
        changeShape(true, animate)
    }

    fun stopLoading(animate: Boolean = true) {

        loadChecker = false
        changeShape(false, animate)
    }

    private fun changeShape(loading: Boolean, animate: Boolean) {

        val animationSpeed = if (animate) ANIMATION_SPEED else 2

        var oldRadius = 28
        var newRadius = 8

        var oldWidth = 56
        var newWidth = 120

        if (loading) {
            oldRadius = 8
            newRadius = 28

            oldWidth = 120
            newWidth = 56
        }

        view.findViewById<CardView>(R.id.passbase_action_btn_parent).apply {

            radius = dpToPx(oldRadius).toFloat()

            val a = ObjectAnimator.ofFloat(this, "radius", dpToPx(newRadius).toFloat())
            a.duration = animationSpeed
            a.repeatCount = 0
            a.start()
            val lp = this.layoutParams
            val vA = ValueAnimator.ofInt(oldWidth, newWidth)
            vA.duration = animationSpeed
            vA.addUpdateListener {
                val v = it.animatedValue as Int
                lp.width = dpToPx(v)
                //this.translationX = v - 120f
                this.layoutParams = lp
            }
            vA.start()
        }

        view.findViewById<View>(R.id.passbase_action_btn_text).apply {
            val v = if (!loading) 1f else 0f
            val oa = ObjectAnimator.ofFloat(this, "alpha", v)
            oa.duration = 100
            if (!loading)
                oa.startDelay = animationSpeed / 2
            oa.start()
        }

        view.findViewById<View>(R.id.passbase_action_btn_progress).apply {
            val v = if (!loading) 0f else 1f
            val oa = ObjectAnimator.ofFloat(this, "alpha", v)
            oa.duration = animationSpeed
            oa.start()
        }
    }

    fun setEnable(enable: Boolean) {

        view.isEnabled = enable
        view.findViewById<View>(R.id.passbase_action_btn_background).apply {
            backgroundTintList = if (enable) colorEnable else colorDisable
        }
    }

    fun setOnClick(action: () -> Unit) {

        view.setOnClickListener {
            if (!isLoading)
                action()
        }
    }

    fun setColor(color: Int) {

        colorEnable = ColorStateList.valueOf(color)
        val hsv = FloatArray(3) { 0f }
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv)
        println("HSV ${hsv[0]} ${hsv[1]} ${hsv[2]}")
        hsv[0] -= 8f
        hsv[1] -= 0.74f
        hsv[2] += 0.50f

        val newColor = Color.HSVToColor(hsv)
        colorDisable = ColorStateList.valueOf(newColor)

        view.findViewById<View>(R.id.passbase_action_btn_background).apply {
            backgroundTintList = if (isEnabled) colorEnable else colorDisable
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }
}
