package com.deepinnovations.call_history

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PopupOverlay {
    private var rootView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var ticker: Runnable? = null

    fun dismiss(ctx: Context) {
        ticker?.let { handler.removeCallbacks(it) }
        ticker = null
        val v = rootView ?: return
        try {
            val wm = ctx.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(v)
        } catch (_: Exception) {
        }
        rootView = null
    }

    private fun fmt(sec: Long): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m ${s}s"
    }

    @Suppress("DEPRECATION")
    fun show(ctx: Context, number: String?, type: String, durSec: Long, today: CallStore.Today) {
        val app = ctx.applicationContext
        if (!Settings.canDrawOverlays(app)) return
        dismiss(app)

        val dp = app.resources.displayMetrics.density
        fun px(v: Int) = (v * dp).toInt()
        fun tv(t: String, sp: Float, bold: Boolean, color: String): TextView {
            val x = TextView(app)
            x.text = t
            x.textSize = sp
            x.setTextColor(Color.parseColor(color))
            if (bold) x.setTypeface(x.typeface, Typeface.BOLD)
            return x
        }

        val shownNumber = if (number.isNullOrBlank()) "Unknown number" else number
        val date = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())

        val card = LinearLayout(app)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(px(18), px(16), px(18), px(16))
        val bg = GradientDrawable()
        bg.setColor(Color.WHITE)
        bg.cornerRadius = px(20).toFloat()
        card.background = bg
        card.elevation = px(8).toFloat()

        card.addView(tv("CALL ENDED", 12f, true, "#5B6B82"))
        card.addView(tv(shownNumber, 24f, true, "#15233D"))
        card.addView(tv("$type  •  ${fmt(durSec)}  •  $date", 14f, false, "#44546A"))

        // Today's report
        val row = LinearLayout(app)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, px(14), 0, px(6))
        fun stat(value: String, label: String) {
            val col = LinearLayout(app)
            col.orientation = LinearLayout.VERTICAL
            col.gravity = Gravity.CENTER_HORIZONTAL
            col.addView(tv(value, 20f, true, "#15233D"))
            col.addView(tv(label, 12f, false, "#5B6B82"))
            row.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        stat("${today.count}", "Calls today")
        stat("${today.missed}", "Missed")
        stat(fmt(today.totalSec), "Total talk")
        card.addView(row)

        val timer = tv("⏱ 5s", 13f, true, "#C2410C")
        timer.setPadding(0, px(6), 0, px(6))
        card.addView(timer)

        // Ad placeholder (replace with a real ad later if policy allows)
        val ad = tv("Ad space", 13f, false, "#8A94A6")
        ad.gravity = Gravity.CENTER
        val adBg = GradientDrawable()
        adBg.setColor(Color.parseColor("#EEF1F6"))
        adBg.cornerRadius = px(8).toFloat()
        ad.background = adBg
        card.addView(ad, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, px(50)))

        val btns = LinearLayout(app)
        btns.orientation = LinearLayout.HORIZONTAL
        btns.setPadding(0, px(12), 0, 0)
        val close = Button(app)
        close.text = "Close"
        close.setOnClickListener { dismiss(app) }
        val callBack = Button(app)
        callBack.text = "Call Back"
        callBack.setOnClickListener {
            dismiss(app)
            if (!number.isNullOrBlank()) {
                val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number)))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                app.startActivity(i)
            }
        }
        val lp1 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp1.marginEnd = px(6)
        val lp2 = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        lp2.marginStart = px(6)
        btns.addView(close, lp1)
        btns.addView(callBack, lp2)
        card.addView(btns)

        val root = FrameLayout(app)
        root.setPadding(px(12), px(12), px(12), px(24))
        root.addView(card, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val type2 = if (Build.VERSION.SDK_INT >= 26)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type2,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.BOTTOM

        try {
            val wm = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.addView(root, params)
            rootView = root
        } catch (_: Exception) {
            return
        }

        // 5-second visual timer. Popup does NOT auto-close.
        var left = 5
        val r = object : Runnable {
            override fun run() {
                left--
                if (left > 0) {
                    timer.text = "⏱ ${left}s"
                    handler.postDelayed(this, 1000)
                } else {
                    timer.text = "⏱ 0s - tap Close to dismiss"
                }
            }
        }
        ticker = r
        handler.postDelayed(r, 1000)
    }
}
