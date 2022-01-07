package com.aspire.baselibrary.expand

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.util.*

//class GsonDeserialier :JsonDeserializer<List<Any>>{
//    override fun deserialize(
//        json: JsonElement?,
//        typeOfT: Type?,
//        context: JsonDeserializationContext?
//    ): List<Any> {
//        if(json==null || !json.isJsonArray){
//            return  Collections.emptyList()
//        }
////        return Gson().fromJson()
////        if(json.isJsonArray)
//    }
//}