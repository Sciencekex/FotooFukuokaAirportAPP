package com.example.fotoofukuokaairportapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 开机自启广播接收器
 * 设备重启后自动启动 MainActivity，确保信息显示板长期运行
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(launchIntent)
        }
    }
}
