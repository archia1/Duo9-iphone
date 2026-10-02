package com.fold.iphoneduo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto fold detection for Fold 8 Wide
        lifecycleScope.launch {
            WindowInfoTracker.getOrCreate(this@MainActivity)
                .windowLayoutInfo(this@MainActivity)
                .collect { info ->
                    val foldingFeature = info.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
                    val serviceIntent = Intent(this@MainActivity, DuoOverlayService::class.java)
                    if (foldingFeature != null) {
                        when (foldingFeature.state) {
                            FoldingFeature.State.HALF_OPENED -> {
                                // 90도 접힘 - iPhone Duo 보여주기
                                startService(serviceIntent)
                            }
                            FoldingFeature.State.FLAT -> {
                                // 완전히 펼침 - 숨기기
                                stopService(serviceIntent)
                                finish() // 액티비티도 닫기
                            }
                            else -> {}
                        }
                    } else {
                        // FoldingFeature 없으면 (커버 화면 등) 수동 토글
                        if (DuoOverlayService.isRunning) {
                            stopService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }
                        finish()
                    }
                }
        }
    }
}
