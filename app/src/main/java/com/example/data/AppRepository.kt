package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

class AppRepository(private val context: Context) {
    fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val apps = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        return apps.map { resolveInfo ->
            AppInfo(
                label = resolveInfo.loadLabel(pm).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                icon = resolveInfo.loadIcon(pm)
            )
        }.sortedBy { it.label }
    }
    
    fun launchApp(packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        }
    }
}
