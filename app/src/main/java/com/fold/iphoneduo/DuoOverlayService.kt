package com.fold.iphoneduo

import android.app.Service
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.Toast

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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
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

    private fun launchApp(intentAction: String, uri: String? = null) {
        try {
            val intent = if (uri != null) Intent(intentAction, Uri.parse(uri)) else Intent(intentAction)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            stopSelf()
        } catch(e:Exception) {
            try {
                Toast.makeText(this, "앱을 열 수 없어요: $intentAction", Toast.LENGTH_SHORT).show()
            } catch(_:Exception){}
        }
    }

    private fun createDuoView(): LinearLayout {
        val ctx = this
        val outer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            // iOS 18 wallpaper style gradient
            val outerBg = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#1C1C1E"), Color.parseColor("#2C2C2E"), Color.parseColor("#000000")))
            background = outerBg
        }

        val label = TextView(ctx).apply {
            text = "Fold 8 Wide - Auto iPhone Duo - Half Open to Show"
            setTextColor(Color.WHITE)
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(24), dp(16), dp(12))
            alpha = 0.8f
        }
        outer.addView(label)

        val duoRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        duoRow.addView(createIPhone(true))
        val hinge = View(ctx)
        hinge.layoutParams = LinearLayout.LayoutParams(dp(12), dp(400)).apply { setMargins(dp(6),0,dp(6),0) }
        hinge.setBackgroundColor(Color.parseColor("#3A3A3C"))
        hinge.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(6).toFloat()
            setColor(Color.parseColor("#3A3A3C"))
        }
        duoRow.addView(hinge)
        duoRow.addView(createIPhone(false))

        outer.addView(duoRow)

        val hint = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(24))
        }
        hint.addView(TextView(ctx).apply {
            text = "아이콘 터치하면 실제 앱 실행됨 • 배경 터치하면 닫힘"
            setTextColor(Color.LTGRAY)
            textSize = 11f
            gravity = Gravity.CENTER
        })
        hint.addView(TextView(ctx).apply {
            text = "펼치면 자동으로 사라짐 (Auto Fold Detection)"
            setTextColor(Color.parseColor("#0A84FF"))
            textSize = 10f
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, 0)
        })
        outer.addView(hint)

        outer.setOnClickListener { stopSelf() }

        return outer
    }

    data class AppIcon(val emoji: String, val name: String, val action: String, val uri: String? = null)

    private fun createIPhone(isLeft: Boolean): FrameLayout {
        val ctx = this
        val icons = if (isLeft) {
            listOf(
                AppIcon("📞","Phone", Intent.ACTION_DIAL, "tel:"),
                AppIcon("💬","Message", Intent.ACTION_SENDTO, "smsto:"),
                AppIcon("📷","Camera", MediaStore.ACTION_IMAGE_CAPTURE),
                AppIcon("🎵","Music", Intent.ACTION_VIEW, "https://music.youtube.com"),
                AppIcon("📸","Photos", Intent.ACTION_VIEW, "content://media/internal/images/media"),
                AppIcon("🗒️","Notes", Intent.ACTION_MAIN),
                AppIcon("⚙️","Settings", android.provider.Settings.ACTION_SETTINGS),
                AppIcon("🗺️","Map", Intent.ACTION_VIEW, "geo:0,0?q=Pohang")
            )
        } else {
            listOf(
                AppIcon("📧","Mail", Intent.ACTION_SENDTO, "mailto:"),
                AppIcon("🌐","Browser", Intent.ACTION_VIEW, "https://www.google.com"),
                AppIcon("🎬","Video", Intent.ACTION_VIEW, "https://youtube.com"),
                AppIcon("📚","Books", Intent.ACTION_VIEW, "https://books.google.com"),
                AppIcon("💡","Idea", Intent.ACTION_VIEW, "https://keep.google.com"),
                AppIcon("🛒","Shop", Intent.ACTION_VIEW, "https://shopping.google.com"),
                AppIcon("🏠","Home", Intent.ACTION_MAIN),
                AppIcon("🎮","Game", Intent.ACTION_MAIN)
            )
        }

        val frame = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(172), dp(384))
        }
        val bg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(36).toFloat()
            setColor(Color.WHITE)
            setStroke(dp(3), Color.BLACK)
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
                cornerRadius = dp(14).toFloat()
                setColor(Color.BLACK)
            }
            background = notchBg
            layoutParams = LinearLayout.LayoutParams(dp(72), dp(20)).apply { gravity = Gravity.CENTER_HORIZONTAL }
        }
        inner.addView(notch)

        val title = TextView(ctx).apply {
            text = if (isLeft) "Left iPhone" else "Right iPhone"
            textSize = 10f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(6))
        }
        inner.addView(title)

        val grid = GridLayout(ctx).apply {
            columnCount = 4
            rowCount = 2
            alignmentMode = GridLayout.ALIGN_BOUNDS
            setPadding(dp(4),0,dp(4),0)
        }
        icons.forEach { app ->
            val btn = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(36)
                    height = dp(52)
                    setMargins(dp(2), dp(2), dp(2), dp(2))
                }
                isClickable = true
                isFocusable = true
            }
            val iconTv = TextView(ctx).apply {
                text = app.emoji
                textSize = 22f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor("#F2F2F7"))
                }
            }
            val nameTv = TextView(ctx).apply {
                text = app.name
                textSize = 6f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
            }
            btn.addView(iconTv)
            btn.addView(nameTv)
            btn.setOnClickListener { launchApp(app.action, app.uri) }
            grid.addView(btn)
        }
        inner.addView(grid)

        val dock = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)).apply {
                topMargin = dp(12)
                leftMargin = dp(8)
                rightMargin = dp(8)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(16).toFloat()
                setColor(Color.parseColor("#E5E5EA"))
            }
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        listOf("📞","🌐","💬","🎵").forEach { emoji ->
            val tv = TextView(ctx).apply {
                text = emoji
                textSize = 16f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(32), dp(32)).apply { setMargins(dp(2),0,dp(2),0) }
            }
            dock.addView(tv)
        }
        inner.addView(dock)

        val home = View(ctx)
        home.layoutParams = LinearLayout.LayoutParams(dp(60), dp(4)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = dp(10)
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
