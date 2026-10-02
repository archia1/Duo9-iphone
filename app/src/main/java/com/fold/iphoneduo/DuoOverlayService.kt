package com.fold.iphoneduo

import android.app.Service
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*

class DuoOverlayService : Service() {
    companion object { var isRunning = false }
    private var windowManager: WindowManager? = null
    private var rootView: LinearLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        rootView = createDuoView()
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        try { windowManager?.addView(rootView, params) } catch(e:Exception){}
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try { rootView?.let { windowManager?.removeView(it) } } catch(e:Exception){}
    }

    private fun dp(v: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun createDuoView(): LinearLayout {
        val ctx = this
        val outer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.argb(200, 0, 0, 0))
        }

        val label = TextView(ctx).apply {
            text = "Fold 8 Wide - iPhone Duo Mode"
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(16), dp(16), dp(8))
        }
        outer.addView(label)

        val duoRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(16))
        }

        duoRow.addView(createIPhone("Left iPhone", arrayOf("📞","💬","📷","🎵","📸","🗒️","⚙️","🗺️")))

        val hinge = View(ctx)
        hinge.layoutParams = LinearLayout.LayoutParams(dp(8), dp(380)).apply { setMargins(dp(4),0,dp(4),0) }
        hinge.setBackgroundColor(Color.DKGRAY)
        duoRow.addView(hinge)

        duoRow.addView(createIPhone("Right iPhone", arrayOf("📧","🌐","🎬","📚","💡","🛒","🏠","🎮")))

        outer.addView(duoRow)

        val hint = TextView(ctx).apply {
            text = "Tap background to close overlay"
            setTextColor(Color.LTGRAY)
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0,0,0,dp(24))
        }
        outer.addView(hint)

        outer.setOnClickListener { stopSelf() }

        return outer
    }

    private fun createIPhone(label: String, icons: Array<String>): FrameLayout {
        val ctx = this
        val frame = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(168), dp(360))
        }
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(32).toFloat()
            setColor(Color.WHITE)
            setStroke(dp(4), Color.BLACK)
        }
        frame.background = bg

        val inner = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setPadding(dp(8), dp(6), dp(8), dp(8))
        }

        val notch = TextView(ctx).apply {
            val notchBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(12).toFloat()
                setColor(Color.BLACK)
            }
            background = notchBg
            layoutParams = LinearLayout.LayoutParams(dp(70), dp(18)).apply { gravity = Gravity.CENTER_HORIZONTAL }
        }
        inner.addView(notch)

        val title = TextView(ctx).apply {
            text = label
            textSize = 10f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
        }
        inner.addView(title)

        val grid = GridLayout(ctx).apply {
            columnCount = 4
            rowCount = 3
            alignmentMode = GridLayout.ALIGN_BOUNDS
        }
        icons.forEach { emoji ->
            val tv = TextView(ctx).apply {
                text = emoji
                textSize = 22f
                gravity = Gravity.CENTER
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(36)
                    height = dp(42)
                    setMargins(dp(2), dp(2), dp(2), dp(2))
                }
                val iconBg = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#F0F0F0"))
                }
                background = iconBg
            }
            grid.addView(tv)
        }
        inner.addView(grid)

        val home = View(ctx)
        home.layoutParams = LinearLayout.LayoutParams(dp(60), dp(4)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = dp(12)
        }
        home.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(2).toFloat()
            setColor(Color.BLACK)
        }
        inner.addView(home)

        frame.addView(inner)
        return frame
    }
}
