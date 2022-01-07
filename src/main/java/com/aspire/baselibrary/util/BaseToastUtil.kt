package com.aspire.baselibrary.util

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.aspire.baselibrary.base.BaseApplication
import com.aspire.baselibrary.expand.baseEmpty
import com.aspire.baselibrary.expand.baseGetString

object BaseToastUtil {
    var handler = Handler(Looper.getMainLooper())
    var toast: Toast? = null

    fun showToastShort(msg: String="", res: Int = -1) {
        Handler(Looper.getMainLooper()).post {
            if (!msg.baseEmpty()) {
                toast = Toast.makeText(
                    BaseApplication.INSTANCE.applicationContext,
                    msg,
                    Toast.LENGTH_SHORT
                )
                toast?.show()
            } else if (res != -1) {
                toast = Toast.makeText(
                    BaseApplication.INSTANCE.applicationContext,
                    baseGetString(res), Toast.LENGTH_SHORT
                )
                toast?.show()
            }
        }

    }
}