package co.kr.shlim.cameratest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.java.simpleName

    /**
     * 권한
     */
    val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    lateinit var textView : TextView
    lateinit var imageView : ImageView

    lateinit var photoURI : Uri
    lateinit var albumURI : Uri
    lateinit var imageUri : Uri
    lateinit var mCurrnetPhotoPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = textview
        imageView = imageview

        camera.setOnClickListener(View.OnClickListener {
            captureCamera()
        })

        checkPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_TAKE_PHOTO -> {
                if(Activity.RESULT_OK == resultCode) {
                    imageView.setImageURI(imageUri)

                    val albumFile = createImageFile()
                    albumURI = Uri.fromFile(albumFile)
                    cropImage()
                }else {
                    Toast.makeText(this, "사진 찍기 취소" , Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_IMAGE_CROP -> {
                if(Activity.RESULT_OK == resultCode) {
                    imageView.setImageURI(albumURI)
                }
            }
        }
    }

    /**
     * 권한 획득 결과
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        textView.text = ""
        var idx = 0
        for(idx in grantResults.indices) {
            if(grantResults[idx] == PackageManager.PERMISSION_GRANTED) {
                textView.append("${permissions[idx]} : 허용함\n")
            }else {
                textView.append("${permissions[idx]} : 허용하지않음\n")
            }
        }
    }


    /**
     * 권한 체크
     */
    fun checkPermission() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        for(permission : String in permissions) {
            var chk = checkCallingOrSelfPermission(permission)

            if(chk == PackageManager.PERMISSION_DENIED) {
                requestPermissions(permissions, 0)
                break
            }
        }
    }

    /**
     * 카메라를 켠다
     */
    fun captureCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val photoFile = createImageFile()

        val  providerUri = FileProvider.getUriForFile(this, packageName, photoFile)

        imageUri = providerUri

        photoURI = providerUri

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerUri)
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
    }

    /**
     * 카메라 및 Crop에 필요한파일을 생성한다.
     * JPEG_239023092.jpg 형태
     */
    fun createImageFile() : File{
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + ".jpg"
        val dir = Environment.getExternalStorageDirectory().absolutePath
        val storageDir = File(dir + "/Pictures", "gyeom")

        if(!storageDir.exists()) {
            Log.d(TAG, "${storageDir.absolutePath}")
            storageDir.mkdirs()
        }

        val imageFile = File(storageDir, imageFileName)
        mCurrnetPhotoPath = imageFile.absolutePath

        return imageFile
    }

    /**
     * crop image 수행
     */
    fun cropImage() {
        Log.d(TAG, "cropImage call")
        Log.d(TAG, "cropImage photoURI : ${photoURI}  " + "/ albumURI : ${albumURI}")

        val cropIntent = Intent("com.android.camera.action.CROP").apply {
            setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(photoURI, "image/*")
            putExtra("aspectX", 1)
            putExtra("aspectY", 1)
            putExtra("scale", true)
            putExtra("output", albumURI)
        }
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP)
    }

}
