package com.passbase.passbase_sdk

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun ProgressBar.setProgressColor(color: Int) {

//    val drawable = this.progressDrawable.mutate()
//    drawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
//    this.progressDrawable = drawable

    this.progressTintList = ColorStateList.valueOf(color)
}

class CViewBackground(private val activity: AppCompatActivity) {

    fun forView(res: Int, color: Int) {

        activity.findViewById<View>(res).setBackgroundColor(color)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun forButton(res: Int, color: Int) {

        activity.findViewById<View>(res).backgroundTintList = ColorStateList.valueOf(color)
    }
}

class CFont {

    companion object {
        fun forLayout(parent: ViewGroup, typeface: Typeface) {

            try {
                for (i in parent.childCount - 1 downTo 0) {
                    val child = parent.getChildAt(i)
                    if (child is ViewGroup) {
                        forLayout(child, typeface)
                    } else
                        when (child) {
                            is TextView -> child.typeface = typeface
                            is Button -> child.typeface = typeface
                            is EditText -> child.typeface = typeface
                        }
                }
            } catch (e: Exception) {
                println(e.message)
                API.sendException(e)
            }

        }
    }

}
