package com.aspire.baselibrary.base

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.aspire.baselibrary.expand.baseIsDebug
import com.aspire.baselibrary.expand.baseSetDebug
import java.io.Serializable
import java.lang.Exception
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import javax.net.ssl.*
import javax.security.cert.X509Certificate


open class BaseApplication : Application() {
    companion object {
        lateinit var INSTANCE: BaseApplication

    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        baseSetDebug(true)
        BaseActivityStackManager.register(this)
        //初始化一些东西
        //arouter
        if (baseIsDebug()) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);
        handleSSLHandshake()
    }

    open fun handleSSLHandshake() {
        try {
            val trustAllCerts: Array<TrustManager> =
                arrayOf<TrustManager>(object : X509TrustManager {
                    val acceptedIssuers: Array<Any?>?
                        get() = arrayOfNulls(0)

                    override fun checkClientTrusted(
                        chain: Array<out java.security.cert.X509Certificate>?,
                        authType: String?
                    ) {

                    }

                    override fun checkServerTrusted(
                        chain: Array<out java.security.cert.X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {

                        return arrayOf(CertificateFactory.getInstance("X.509") as java.security.cert.X509Certificate);
                    }
                })
            val sc: SSLContext = SSLContext.getInstance("TLS")
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(object : HostnameVerifier {
                override fun verify(hostname: String?, session: SSLSession?): Boolean {
                    return true
                }
            })
        } catch (ignored: Exception) {
        }
    }

}