package com.dizzylay.rspicture.ui

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dizzylay.rspicture.databinding.ActivityMainBinding
import com.dizzylay.rspicture.util.FileUtil
import com.dizzylay.rspicture.util.ToastUtil
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var photoPath: String
    private lateinit var photoUri: Uri
    private lateinit var photoOutputPath: String
    private lateinit var mBinding: ActivityMainBinding

    private companion object {
        // Used to load the 'native-lib' library on application startup.
//        init {
//            System.loadLibrary("native-lib")
//        }

        const val ALBUM_REQUEST_CODE: Int = 0
        const val SHOOT_REQUEST_CODE: Int = 1

        const val TAKE_PHOTO_PERMISSION_REQUEST_CODE: Int = 0
        const val WRITE_SDCARD_PERMISSION_REQUEST_CODE: Int = 1
        const val CROP_PHOTO_REQUEST_CODE: Int = 2

        const val FILE_PROVIDER_AUTHORITY = "com.dizzylay.rspicture.fileprovider"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Example of a call to a native method
//        mBinding.sampleText.text = stringFromJNI()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 申请读写内存卡内容的权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_SDCARD_PERMISSION_REQUEST_CODE
            )
        }

        mBinding.cardViewAlbum.setOnClickListener(albumClickListener)
        mBinding.cardViewShoot.setOnClickListener(shootClickListener)
        mBinding.cardViewDraw.setOnClickListener(drawClickListener)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    private external fun stringFromJNI(): String

    private val albumClickListener = View.OnClickListener {
        val choiceFromAlbumIntent = Intent(Intent.ACTION_GET_CONTENT)
        choiceFromAlbumIntent.type = "image/*"
        startActivityForResult(choiceFromAlbumIntent, ALBUM_REQUEST_CODE)
    }

    private val shootClickListener = View.OnClickListener {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA), TAKE_PHOTO_PERMISSION_REQUEST_CODE
            )
        } else {
            startCamera()
        }
    }

    private val drawClickListener = View.OnClickListener {
        startDraw()
    }

    private fun startCamera() {
        val file = FileUtil.createFile(externalCacheDir, "_tmp_image.jpg")
        photoPath = file.path

        photoUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
        } else {
            Uri.fromFile(file)
        }
        // 打开系统相机的 Action
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 设置拍照所得照片的输出目录
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(takePhotoIntent, SHOOT_REQUEST_CODE)
    }

    private fun startDraw() {
        // TODO("Not yet implemented")
        PictureActivity.start(this, "")
    }

    private fun cropPhoto(inputUri: Uri) {
        val file = File(Environment.getExternalStorageDirectory().path, "_crop_image.jpg")
//        val file = File(externalCacheDir, "_crop_image.jpg")
        photoOutputPath = file.path
        if (file.exists()) {
            file.delete()
        }

        val outputUri = Uri.fromFile(file)
//        val outputUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file)
        val cropPhotoIntent = Intent("com.android.camera.action.CROP")
        cropPhotoIntent.setDataAndType(inputUri, "image/*")
        cropPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        cropPhotoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        cropPhotoIntent.putExtra("crop", "true")
        cropPhotoIntent.putExtra("scale", true)
        cropPhotoIntent.putExtra("scaleUpIfNeeded", true)
        cropPhotoIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        cropPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        startActivityForResult(cropPhotoIntent, CROP_PHOTO_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            TAKE_PHOTO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    ToastUtil.makeShort(this, "拍照权限被拒绝")
                }
            }
            WRITE_SDCARD_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ToastUtil.makeShort(this, "读写内存卡内容权限被拒绝")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            ToastUtil.makeShort(this, "未选择照片")
            return
        }
        var imagePath: String? = null
        when (requestCode) {
            ALBUM_REQUEST_CODE -> {
                data?.data?.let {
                    cropPhoto(it)
                    imagePath = FileUtil.getPathFromUri(this, it)
                }
            }
            SHOOT_REQUEST_CODE -> {
                cropPhoto(photoUri)
                imagePath = photoPath
            }
            CROP_PHOTO_REQUEST_CODE -> {
                val file = File(photoOutputPath)
                if (file.exists()) {
                    imagePath = file.path
                    PictureActivity.start(this, imagePath!!)
                } else {
                    ToastUtil.makeShort(this, "找不到照片")
                }
            }
        }
//        if (imagePath != null) {
//            PictureActivity.start(this, imagePath!!)
//        } else {
//            ToastUtil.makeShort(this, "error")
//        }
    }
}