package com.aspire.baselibrary.network

import android.app.Activity
import android.graphics.Color
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.aspire.baselibrary.R
import com.aspire.baselibrary.base.BaseActivityStackManager
import com.aspire.baselibrary.expand.*
import com.aspire.baselibrary.network.NetWorkConfig.failDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import java.io.IOException
import java.lang.reflect.Type


/***
 * 定义和配置一些网路请求基本使用内容
 */
object NetWorkConfig {
    const val successCode = 200
    const val expiredCode = 2002
    var expiredDialog: MaterialDialog? = null
    var failDialog: MaterialDialog? = null
}

@Parser(name = "Response")
open class ResponseParser<T> : AbstractParser<T> {

    protected constructor() : super()
    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): T {
        try {
            var result = response.body!!.string().replace("\n", "").replace("\t", "")
            val gson = Gson()
            var resultObj: JsonObject? = null
            val request = response.request
            var requestString = baseRequestBodyToString(
                request.body
            )
            if (result.length > 4000) {

                try {
                    resultObj = gson.fromJson<JsonObject>(result, JsonObject::class.java)
                    var caches = resultObj.get("data").toString()
                    if (caches.startsWith("[")) {
                        val arr =
                            Gson().fromJson<ArrayList<*>>(caches, java.util.ArrayList::class.java)
                        var strs = arr.toArray().slice(0..arr.size / 4).toString()
                        Log.e("network", strs)
                    }
                } catch (e: java.lang.Exception) {
                    throw ParseException("-1", result, response)
                }


            }
            if (request.method == "GET") {
                if (!request.url.toString().contains("notclose")) {
                    baseLoadingDismiss()
                }
            } else {
                try {
                    var responseJson = JSONObject(requestString)
                    if (!responseJson.getBoolean("notclose")) {
                        baseLoadingDismiss()
                    }
                } catch (e: java.lang.Exception) {
                    baseLoadingDismiss()
                }
            }


            baseAddLog(
                "HEAD:" + request.headers.toString() + "\nMETHOD: " + request.method + "\nURL" + request.url +
                        "\nPARAM: **JSON" + requestString + "JSON**\n" + "RESULT:**JSON" + result + "JSON**\n原串：" + result,
                "NETWORK"
            )

            try {
                resultObj = gson.fromJson<JsonObject>(result, JsonObject::class.java)
            } catch (e: java.lang.Exception) {
                throw ParseException("-1", result, response)
            }
            var code = resultObj!!.get("code").asInt
            var resultData: T? = null
            if (code == NetWorkConfig.successCode) {
                try {
                    resultData = gson.fromJson<T>(resultObj.get("data").toString(), mType)
                } catch (e: java.lang.Exception) {
                    throw Exception(e.message)
                }
            }

            var message = resultObj.get("msg").toString().baseGetValue()

            if (code == NetWorkConfig.successCode) {
                return resultData!!
            } else if (code == NetWorkConfig.expiredCode) {
                baseLoadingDismiss()
                //过期 终结所有act,打开选择用户act
                try {

                    if (BaseActivityStackManager.nowActivity != null) {
                        android.os.Handler(Looper.getMainLooper()).post {
                            if (NetWorkConfig.expiredDialog == null) {
                                NetWorkConfig.expiredDialog =
                                    MaterialDialog(BaseActivityStackManager.nowActivity!!)
                            }
                            if (!NetWorkConfig.expiredDialog!!.isShowing) {
                                NetWorkConfig.expiredDialog!!.show {
                                    title(res = R.string.com_expired_waring)
                                    message(res = R.string.com_login_is_expired)
                                    negativeButton(
                                        res = R.string.com_checkin_sure,
                                        click = object : DialogCallback {
                                            override fun invoke(p1: MaterialDialog) {
                                                go(
                                                    "/login/chooseUser",
                                                    mapOf("double" to true),
                                                    true
                                                )
                                            }
                                        })
                                    cancelable(false)
                                    lifecycleOwner(BaseActivityStackManager.nowActivity!!)
                                }
                            }

                        }

                    }
                } catch (e: java.lang.Exception) {
                    throw Exception(e.message)
                }


            } else {
                baseLoadingDismiss()
                var str = message
                if (str.baseEmpty()) {
                    str = "请求失败，请稍后再试"
                }
                if (str.startsWith("\"")) {
                    str = str.substring(1, str.length)
                }
                if (str.endsWith("\"")) {
                    str = str.substring(0, str.length - 1)
                }
                if (BaseActivityStackManager.nowActivity != null) {
                    GlobalScope.launch(Dispatchers.Main) {
                        if (failDialog != null && failDialog!!?.isShowing) {
                            return@launch
                        }
                        if (failDialog != null && failDialog!!.isShowing) {
                            failDialog!!.dismiss()
                        }
                        failDialog = MaterialDialog(BaseActivityStackManager.nowActivity!!).show {
                            message(text = str)
//                            title(text = "请求失败")
                            positiveButton(text = "确定")
                            getActionButton(WhichButton.POSITIVE).updateTextColor(Color.parseColor("#77B0FF"))
                        }
                        if (!(BaseActivityStackManager.nowActivity!! as AppCompatActivity).isFinishing) {
                            try {
                                failDialog!!.show()
                            } catch (e: java.lang.Exception) {
                            }
                        }

                    }

                }
            }
        } catch (outWrong: java.lang.Exception) {
            throw Exception(outWrong.message)
        }
        throw Exception("")
    }
}
