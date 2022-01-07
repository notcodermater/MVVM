package com.aspire.baselibrary.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/***
 ** create by: NotCoder
 ** time: 2020/4/23
 ** des:
 **/

class BaseMDBHelper: SQLiteOpenHelper {
    constructor(
        context: Context?,
        name: String?,
        factory: SQLiteDatabase.CursorFactory?,
        version: Int
    ) : super(context, name, factory, version)


    override fun onCreate(db: SQLiteDatabase?) {
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}