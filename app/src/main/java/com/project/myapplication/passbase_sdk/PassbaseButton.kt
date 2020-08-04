package com.passbase.passbase_sdk

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.project.myapplication.R

class PassbaseButton : LinearLayout {

    private lateinit var view: View

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        init(context, attrs)

//        val anim = view.findViewById<ImageView>(R.id.button_load)
//        Glide.with(context).asGif().load(R.raw.loadinggif).into(anim)
    }

    constructor(context: Context) : super(context) {

        init(context)
    }

    private fun init(context: Context, attrs: AttributeSet? = null) {

        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.passbase_button_passbase, this, true)

        context.theme.obtainStyledAttributes(attrs, R.styleable.PassbaseButton, 0, 0).apply {
            try {
                val backgroundColor = getColorStateList(R.styleable.PassbaseButton_backgroundColor)
                    ?: ColorStateList.valueOf(Color.WHITE)
                val textColor = getColorStateList(R.styleable.PassbaseButton_textColor)
                    ?: ColorStateList.valueOf(Color.BLACK)
                view.findViewById<CardView>(R.id.passbase_button_background).apply {
                    setCardBackgroundColor(backgroundColor)
                }
                view.findViewById<TextView>(R.id.passbase_button_text).apply {
                    setTextColor(textColor)
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_UP) {

            val anim = view.findViewById<ImageView>(R.id.passbase_button_load)
            Glide.with(context).asGif().load(R.raw.loadinggif).into(anim)
            Handler().postDelayed({
                anim.setImageBitmap(null)
            }, 5000L)
        }

        return super.onTouchEvent(event)
    }

    override fun setBackgroundColor(color: Int) {
        //super.setBackgroundColor(color)

        try {
            view.findViewById<CardView>(R.id.passbase_button_background).apply {
                setCardBackgroundColor(color)
            }
        } finally {

        }
    }

    fun setTextColor(color: Int) {

        try {
            view.findViewById<TextView>(R.id.passbase_button_text).apply {
                setTextColor(color)
            }
        } finally {

        }
    }
}