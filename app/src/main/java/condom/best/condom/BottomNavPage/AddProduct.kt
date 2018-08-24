package condom.best.condom.BottomNavPage

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import condom.best.condom.Data.FirebaseConst
import condom.best.condom.Data.GlideApp
import condom.best.condom.Data.ProductInfo
import condom.best.condom.Data.ProductRating
import condom.best.condom.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_add_product.*
import java.io.ByteArrayOutputStream

class AddProduct : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance("gs://condom-55a91")
    private lateinit var pImagePath : String

    private var imageBool = false
    private var nameBool = false
    private var priceBool = false
    private var tagBool = false
    private var companyBool = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        pImageSelect.setOnClickListener {
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

        pName.addTextChangedListener(object : TextWatcher{
            //제품이름 입력 체크
            override fun afterTextChanged(s: Editable?) {
                nameBool = s!!.length>1
                checkBool(imageBool,nameBool,priceBool,tagBool,companyBool)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        pPrice.addTextChangedListener(object : TextWatcher{
            //제품이름 입력 체크
            override fun afterTextChanged(s: Editable?) {
                priceBool = s!!.length>2
                checkBool(imageBool,nameBool,priceBool,tagBool,companyBool)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        pTag.addTextChangedListener(object : TextWatcher{
            //제품이름 입력 체크
            override fun afterTextChanged(s: Editable?) {
                tagBool = s!!.isNotEmpty()
                checkBool(imageBool,nameBool,priceBool,tagBool,companyBool)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        pCompany.addTextChangedListener(object : TextWatcher{
            //제품이름 입력 체크
            override fun afterTextChanged(s: Editable?) {
                companyBool = s!!.isNotEmpty()
                checkBool(imageBool,nameBool,priceBool,tagBool,companyBool)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        successButton.setOnClickListener {
            //제품 등록

            //사진 업로드
            pImage.isDrawingCacheEnabled = true
            pImage.buildDrawingCache()
            val bitmap = BitmapFactory.decodeFile(pImagePath)//경로를 통해 비트맵으로 전환
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val imageData = baos.toByteArray()

            val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()
            pImagePath = "Product_Image/" + pName.text.toString()
            val storageRef = storage.reference
            val uploadTask = storageRef.child(pImagePath).putBytes(imageData, metadata)

            val dialog = SpotsDialog.Builder()
                    .setContext(this)
                    .setMessage("데이터 업로드 중")
                    .build()
            val tagList = arrayListOf<String>()
            val tagString = pTag.text.toString()
            var tag = ""
            for(i in 0 until  tagString.length){
                if(tagString[i] == '.'||i==tagString.length-1){
                    tagList.add(tag)
                    tag=""
                }else{
                    tag += tagString[i]
                }
            }

            dialog.show()

            val productInfo = ProductInfo(pImagePath,pName.text.toString(),pPrice.text.toString().toInt(),tagList,pCompany.text.toString().toInt(),0F,0)

            db.collection(FirebaseConst.PRODUCT_INFO).document(pName.text.toString()).set(productInfo)
            db.collection(FirebaseConst.PRODUCT_RATING).document(pName.text.toString()).set(ProductRating())

            uploadTask.addOnProgressListener {
                dialog.show()
            }
                    .addOnPausedListener {
                        dialog.dismiss()
                    }
                    .addOnSuccessListener {
                        //업로드 완료
                        pImage.background = resources.getDrawable(R.drawable.ic_user)
                        pName.text = null
                        pPrice.text = null
                        pTag.text = null
                        pCompany.text = null
                        pImagePath = ""
                        priceBool = false
                        nameBool = false
                        companyBool = false
                        imageBool = false
                        tagBool = false
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        dialog.dismiss()
                    }
        }
    }

    private fun checkBool(c1 : Boolean,c2 : Boolean,c3 : Boolean,c4 : Boolean,c5 : Boolean){
        if(c1 && c2 && c3 && c4 && c5){
            successButton.isEnabled = true
            successButton.background = resources.getDrawable(R.drawable.sign_up_success_button)
        }else{
            successButton.isEnabled = false
            successButton.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            //갤러리에서 사진선택
            pImagePath = data.getParcelableArrayListExtra<Image>(Config.EXTRA_IMAGES)[0].path

            if (pImagePath[pImagePath.length - 1] != 'f') {
                //gif아닌지 확인

                GlideApp.with(this)
                        .load(pImagePath)
                        .apply(RequestOptions().centerCrop())
                        .apply(RequestOptions.placeholderOf(resources.getDrawable(R.drawable.ic_user)))
                        .signature(ObjectKey(System.currentTimeMillis()))
                        .thumbnail(0.1f)
                        .into(pImage)
                imageBool = true
                checkBool(imageBool,nameBool,priceBool,tagBool,companyBool)
            } else {
                Toast.makeText(this, "gif", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
