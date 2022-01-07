package com.aspire.baselibrary.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.aspire.baselibrary.expand.baseLoadingDismiss
import com.aspire.baselibrary.update.App
import java.util.*

/***
 ** create by: NotCoder
 ** time: 2020/5/10
 ** des: activity栈维护
 **/

object BaseActivityStackManager : Application.ActivityLifecycleCallbacks {
    var stack = Stack<Activity>()
    var nowActivity: AppCompatActivity? = null

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        stack.remove(activity)
        if (!stack.empty()) {
            nowActivity = stack.peek() as AppCompatActivity
        }
        baseLoadingDismiss()
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityPostDestroyed(activity: Activity) {
        super.onActivityPostDestroyed(activity)

    }

    override fun onActivityPreDestroyed(activity: Activity) {
        super.onActivityPreDestroyed(activity)
        var ss = ""
        ss += ""

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        nowActivity = activity as AppCompatActivity
        stack.add(nowActivity)
    }


    override fun onActivityResumed(activity: Activity) {


    }

    fun register(application: BaseApplication) {
        application.registerActivityLifecycleCallbacks(this)
    }

    fun unRegister(application: BaseApplication) {
        application.unregisterActivityLifecycleCallbacks(this)
    }

    fun finishAll() {
        while (!stack.isEmpty()) {
            stack.pop()?.finish()
        }
    }

//    fun getFirst(): Activity? {
//        if (stack.empty())
//            return null
//        return nowActivity
//    }
}