package com.majidshahbaz.memo

import android.app.Application
import android.content.Context
import java.io.File

class App: Application()  {
    override fun onCreate() {
        super.onCreate()
        ensureModelFolderExists(this)
    }
    fun ensureModelFolderExists(context: Context) {
        val llmFolder = File(context.filesDir, "llm")
        if (!llmFolder.exists()) {
            llmFolder.mkdirs()
        }
    }
}