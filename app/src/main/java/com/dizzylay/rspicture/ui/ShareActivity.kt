package com.dizzylay.rspicture.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.dizzylay.rspicture.databinding.ActivityShareBinding
import com.dizzylay.rspicture.util.ToastUtil
import java.io.File
import java.io.OutputStream


class ShareActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityShareBinding

    companion object {

        private const val IMAGE_PATH = "image_path"
        private const val IMAGE_FOLDER_NAME = "rspicture"

        fun start(context: Context, imagePath: String) {
            val intent = Intent(context, ShareActivity::class.java)
            intent.putExtra(IMAGE_PATH, imagePath)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val path = intent.getStringExtra(IMAGE_PATH)
        if (TextUtils.isEmpty(path)) {
            finish()
            return
        }
        val bitmap = BitmapFactory.decodeFile(path)
        mBinding.imageShare.setImageBitmap(bitmap)
        mBinding.btnSaveImage.setOnClickListener {
            saveImage(path!!)
            ToastUtil.makeShort(this, "成功保存至相册")
        }
        mBinding.btnShareImage.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(
                this,
                "com.dizzylay.rspicture.fileprovider",
                File(path!!)
            )
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            startActivityForResult(Intent.createChooser(shareIntent, "分享到"), 0)
        }
    }

    private fun saveImage(path: String) {
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis()}.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$IMAGE_FOLDER_NAME")
            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = contentResolver.openOutputStream(imageUri!!)
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() +
                    File.separator + IMAGE_FOLDER_NAME
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, "${System.currentTimeMillis()}.jpg")
            fos = image.outputStream()
        }
        if (fos != null) {
            val fis = File(path).inputStream()
            FileUtils.copy(fis, fos)
            fis.close()
            fos.close()
        }
    }
}