package com.aspire.baselibrary.util.record;

import android.content.Context;
import android.os.Environment;

import java.io.File;

//文件管理类
public class FileUtils {
    //获取文件存放根路径

    public static final String ROOT_PATH = "wxr/";
    public static final String RECORD_DIR = "record/";
    public static final String RECORD_PATH = ROOT_PATH + RECORD_DIR;

    public static File getAppDir(Context context) {
        String dirPath = "";
        //SD卡是否存在
        boolean isSdCardExists = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        boolean isRootDirExists = Environment.getExternalStorageDirectory().exists();
        if (isSdCardExists && isRootDirExists) {
            dirPath = String.format("%s/%s/", Environment.getExternalStorageDirectory().getAbsolutePath(), ROOT_PATH);
        } else {
            dirPath = String.format("%s/%s/", context.getApplicationContext().getFilesDir().getAbsolutePath(), ROOT_PATH);
        }

        File appDir = new File(dirPath);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        return appDir;
    }

    //获取录音存放路径
    public static File getAppRecordDir(Context context) {
        File appDir = getAppDir(context);
        File recordDir = new File(appDir.getAbsolutePath(), RECORD_DIR);
        if (!recordDir.exists()) {
            recordDir.mkdir();
        }
        return recordDir;
    }
}
