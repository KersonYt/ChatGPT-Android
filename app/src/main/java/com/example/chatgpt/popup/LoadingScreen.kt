package com.example.chatgpt.popup

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import com.example.chatgpt.R

class LoadingScreen(private val activity: Activity): AsyncTask<Void,Void,Void>() {

    var dialog = Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar)

    override fun onPreExecute() {
        val view = activity.layoutInflater.inflate(R.layout.loading, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.show()
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        Thread.sleep(30000)
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        dialog.dismiss()
    }

    fun isDismiss() {
        dialog.dismiss()
    }

}