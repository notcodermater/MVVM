@file:Suppress("NOTHING_TO_INLINE")

package com.vector.appupdatedemo.ext

import android.app.Activity
import com.aspire.baselibrary.update.util.CProgressDialogUtils

/**
 * Created by Vector
 * on 2017/7/18 0018.
 */
inline fun Activity.showProgressDialog() {
    CProgressDialogUtils.showProgressDialog(this)
}

inline fun Activity.cancelProgressDialog() {
    CProgressDialogUtils.cancelProgressDialog(this)
}