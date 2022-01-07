package com.aspire.baselibrary.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import java.io.*

class BaseImageUtil {

    companion object {
        /***
         * 图片质量压缩
         */
        fun compressImageQuality(image: Bitmap): Bitmap? {
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            var options = 90
            val length = baos.toByteArray().size / 1024
            if (length > 5000) {
                //重置baos即清空baos
                baos.reset()
                //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
                image.compress(Bitmap.CompressFormat.JPEG, 10, baos)
            } else if (length > 4000) {
                baos.reset()
                image.compress(Bitmap.CompressFormat.JPEG, 20, baos)
            } else if (length > 3000) {
                baos.reset()
                image.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            } else if (length > 2000) {
                baos.reset()
                image.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            }
            //循环判断如果压缩后图片是否大于1M,大于继续压缩
            while (baos.toByteArray().size / 1024 > 1024) {
                //重置baos即清空baos
                baos.reset()
                //这里压缩options%，把压缩后的数据存放到baos中
                image.compress(Bitmap.CompressFormat.JPEG, options, baos)
                //每次都减少10
                options -= 10
            }
            //把压缩后的数据baos存放到ByteArrayInputStream中
            val isBm = ByteArrayInputStream(baos.toByteArray())
            //把ByteArrayInputStream数据生成图片
            return BitmapFactory.decodeStream(isBm, null, null)
        }

        /***
         * 图片压缩算法
         * @param path 图片路径 二选一
         * @param bitmapRes 要压缩的图片bitmap 二选一
         * @param savePath 压缩后的保存路径
         */
        fun compressImage(path: String = "", bitmapRes: Bitmap? = null, savePath: String) {
            var bitmap: Bitmap? = null
            if (bitmapRes != null) {
                bitmap = bitmapRes
            } else {
                bitmap = BitmapFactory.decodeFile(path)
            }
            var baos = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            if (baos.toByteArray().size / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
                baos.reset()//重置baos即清空baos
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)//这里压缩50%，把压缩后的数据存放到baos中
            }
            var isBm = ByteArrayInputStream(baos.toByteArray())
            var newOpts = BitmapFactory.Options()
            //开始读入图片，此时把options.inJustDecodeBounds 设回true了
            newOpts.inJustDecodeBounds = true
            var bitmapResult = BitmapFactory.decodeStream(isBm, null, newOpts)
            newOpts.inJustDecodeBounds = false
            var w = newOpts.outWidth
            var h = newOpts.outHeight
            //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
            var hh = 800f//这里设置高度为800f
            var ww = 480f//这里设置宽度为480f
            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            var be = 1//be=1表示不缩放
            if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
                be = (newOpts.outWidth / ww).toInt()
            } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
                be = (newOpts.outHeight / hh).toInt()
            }
            if (be <= 0)
                be = 1
            newOpts.inSampleSize = be//设置缩放比例
            //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
            isBm = ByteArrayInputStream(baos.toByteArray())
            bitmapResult = BitmapFactory.decodeStream(isBm, null, newOpts)
            bitmapResult = compressImageQuality(bitmapResult!!)
            saveBitmap(bitmapResult!!, savePath)
        }


        /**
         * 保存bitmap到本地
         * @param context the context
         * @param mBitmap the m bitmap
         * @return string
         */
        fun saveBitmap(
            mBitmap: Bitmap,
            path: String
        ): String? {
            val filePic: File
            try {
                filePic = File(path)
                if (!filePic.exists()) {
                    filePic.getParentFile().mkdirs()
                    filePic.createNewFile()
                }
                val fos = FileOutputStream(filePic)
                //不压缩，保存本地
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
            return filePic.getAbsolutePath()
        }
    }

}