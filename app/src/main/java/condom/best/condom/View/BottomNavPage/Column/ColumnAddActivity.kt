package condom.best.condom.View.BottomNavPage.Column

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.storage.StorageMetadata
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import condom.best.condom.R
import condom.best.condom.View.Data.ColumnInfo
import condom.best.condom.View.Data.FirebaseConst
import condom.best.condom.View.Data.GlideApp
import condom.best.condom.View.MainActivity.Companion.db
import condom.best.condom.View.MainActivity.Companion.storage
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_column_add.*
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
class ColumnAddActivity : AppCompatActivity() {

    private lateinit var cImagePath : String

    private var imageBool = false
    private var nameBool = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_column_add)

        cImageSelect.setOnClickListener {
            //제품 사진 선택
            val images = arrayListOf<Image>()
            ImagePicker.with(this)                         //  Initialize ImagePicker with activity or fragment context
                    .setToolbarColor("#78aaff")         //  Toolbar color
                    .setStatusBarColor("#4188fe")       //  StatusBar color (works with SDK >= 21  )
                    .setToolbarTextColor("#FFFFFF")     //  Toolbar text color (Title and Done button)
                    .setToolbarIconColor("#FFFFFF")     //  Toolbar icon color (Back and Camera button)
                    .setProgressBarColor("#4CAF50")     //  ProgressBar color
                    .setBackgroundColor("#ffffff")      //  Background color
                    .setCameraOnly(false)               //  Camera mode
                    .setMultipleMode(false)              //  Select multiple images or single image
                    .setFolderMode(false)                //  Folder mode
                    .setShowCamera(true)                //  Show camera button
                    .setFolderTitle(getString(R.string.album))           //  Folder title (works with FolderMode = true)
                    .setImageTitle(getString(R.string.galleries))         //  Image title (works with FolderMode = false)
                    .setDoneTitle(getString(R.string.selectDone))               //  Done button title
                    .setLimitMessage(getString(R.string.limitSelectMessage))    // Selection limit message
                    .setMaxSize(1)                     //  Max images can be selected
                    .setSavePath(getString(R.string.app_name))         //  Image capture folder name
                    .setSelectedImages(images)          //  Selected images
                    .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                    .setKeepScreenOn(true)              //  Keep screen on when selecting images
                    .start()                            //  Start ImagePicker
        }

        cName.addTextChangedListener(object : TextWatcher {
            //제품이름 입력 체크
            override fun afterTextChanged(s: Editable?) {
                nameBool = s!!.length>1
                checkBool(imageBool,nameBool)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        successButtonC.setOnClickListener { _ ->
            val randLikeNum = System.currentTimeMillis()%10+1


            cImage.isDrawingCacheEnabled = true
            cImage.buildDrawingCache()
            val bitmap = BitmapFactory.decodeFile(cImagePath)//경로를 통해 비트맵으로 전환
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val imageData = baos.toByteArray()

            val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()
            cImagePath = "Column_Image/" + cName.text.toString()
            val uploadTask = storage.child(cImagePath).putBytes(imageData, metadata)

            val dialog = SpotsDialog.Builder()
                    .setContext(this)
                    .setMessage("데이터 업로드 중")
                    .build()

            val tagList = arrayListOf<String>()
            val tagString = cTag.text.toString()
            var tag = ""
            for(i in 0 until  tagString.length){
                when {
                    tagString[i] == '.' -> {
                        tagList.add(tag)
                        tag=""
                    }
                    i==tagString.length-1 -> {
                        tag += tagString[i]
                        tagList.add(tag)
                        tag=""
                    }
                    else -> tag += tagString[i]
                }
            }

            dialog.show()

            val productInfo = ColumnInfo(cImagePath,cName.text.toString(),randLikeNum.toInt(),0,tagList)
            db.collection(FirebaseConst.COLUMN_INFO).document(cName.text.toString()).set(productInfo)

            uploadTask.addOnProgressListener {}
                    .addOnPausedListener {
                        dialog.dismiss()
                    }
                    .addOnSuccessListener {
                        //업로드 완료
                        cImage.background = null
                        cName.text = null
                        cTag.text = null
                        cImagePath = ""
                        nameBool = false
                        imageBool = false
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        dialog.dismiss()
                    }

        }
    }

    private fun checkBool(c1 : Boolean,c2 : Boolean){
        if(c1 && c2){
            successButtonC.isEnabled = true
            successButtonC.background = resources.getDrawable(R.drawable.sign_up_success_button)
        }else{
            successButtonC.isEnabled = false
            successButtonC.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            //갤러리에서 사진선택
            cImagePath = data.getParcelableArrayListExtra<Image>(Config.EXTRA_IMAGES)[0].path

            if (cImagePath[cImagePath.length - 1] != 'f') {
                //gif아닌지 확인

                GlideApp.with(this)
                        .load(cImagePath)
                        .apply(RequestOptions().centerCrop())
                        .apply(RequestOptions.placeholderOf(resources.getDrawable(R.drawable.ic_user)))
                        .signature(ObjectKey(System.currentTimeMillis()))
                        .thumbnail(0.1f)
                        .into(cImage)
                imageBool = true
                checkBool(imageBool,nameBool)
            } else {
                Toast.makeText(this, "gif", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
