package com.aspire.baselibrary.network

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class Response<T> {
    var code = 0
        private set
    var msg: String? = null
        private set
    var data: T? = null
        private set

    fun setErrorCode(errorCode: Int) {
        code = errorCode
    }

    fun setErrorMsg(errorMsg: String?) {
        msg = errorMsg
    }

    fun setData(data: T) {
        this.data = data
    }
}

class ParameterizedTypeImpl(val clz: Class<*>) : ParameterizedType {
    override fun getRawType(): Type = List::class.java

    override fun getOwnerType(): Type? = null

    override fun getActualTypeArguments(): Array<Type> = arrayOf(clz)
}
