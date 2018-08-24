package condom.best.condom

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import com.orhanobut.dialogplus.DialogPlus
import condom.best.condom.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.Data.GlideApp
import condom.best.condom.Data.StringData.Companion.BIRTH
import condom.best.condom.Data.StringData.Companion.FIRST_SETTING
import condom.best.condom.Data.StringData.Companion.GENDER
import condom.best.condom.Data.StringData.Companion.NAME
import condom.best.condom.Data.StringData.Companion.NEW_SETTING
import condom.best.condom.Data.StringData.Companion.ROUTE
import condom.best.condom.Data.UserInfo
import condom.best.condom.Dialog.ProfileDialog
import condom.best.condom.MainActivity.Companion.currentUser
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.actionbar_activity_log_in.view.*
import kotlinx.android.synthetic.main.activity_first_setting.*
import java.io.ByteArrayOutputStream
import java.util.*


@Suppress("DEPRECATION")
class FirstSettingActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var pref : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val REQUEST_IMAGE_CAPTURE = 10942

    private var profileUri = "/defaultProfileImage/ic_user.png"

    @SuppressLint("SetTextI18n", "CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_setting)

        val intent = intent
        val route = intent.getStringExtra(ROUTE)

        mAuth = FirebaseAuth.getInstance()

        pref = getSharedPreferences("pref", MODE_PRIVATE)
        editor = pref.edit()// editor에 put 하기

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setCustomView(R.layout.actionbar_activity_info)
        supportActionBar!!.elevation = 10F
        supportActionBar!!.setBackgroundDrawable(resources.getDrawable(R.color.white))
        val actionBar = supportActionBar!!.customView
        actionBar.action_bar_text.text = getString(R.string.ignore)
        actionBar.action_bar_back_key.visibility = View.GONE
        actionBar.action_bar_text.setOnClickListener {
            //다음에하기 옵션
//            startActivity(Intent(this,SecondSettingActivity::class.java))
            finish()
        }

        var name = false
        var birth = false
        var gender = 0

        userNameText.addTextChangedListener(object : TextWatcher {
            //유저이름 길이 검사
            override fun afterTextChanged(s: Editable?) {
                if(s.toString().length in 2..8){
                    checkName.visibility = View.VISIBLE
                    name = true
                    checkNext(gender,name,birth)
                }else {
                    checkName.visibility = View.INVISIBLE
                    name = false
                    checkNext(gender,name,birth)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        val c = Calendar.getInstance()
        var userBirthYear = c.get(Calendar.YEAR)-19
        var userBirthMonth = c.get(Calendar.MONTH)
        var userBirthDay = c.get(Calendar.DAY_OF_MONTH)
        val listener = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            //데이트피커 셋 이벤트
            userBirthYear = year
            userBirthMonth = monthOfYear
            userBirthDay = dayOfMonth
            userBirth.setText("$userBirthYear. ${userBirthMonth+1}. $userBirthDay")
            checkBirth.visibility = View.VISIBLE
            birth = true
            checkNext(gender,name,birth)
        }
        var datePickerBool = true
        userBirth.setOnClickListener {
            //생일 선택
            if(datePickerBool) {
                datePickerBool = false
                val dialog = DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, listener, userBirthYear, userBirthMonth, userBirthDay)
                dialog.datePicker.calendarViewShown = false
                dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.datePicker.maxDate = c.timeInMillis
                dialog.show()
                val mHandler = @SuppressLint("HandlerLeak")
                object : Handler() {
                    override fun handleMessage(msg: Message) {
                        datePickerBool = true
                    }
                }
                mHandler.sendEmptyMessageDelayed(0, 500)
            }
        }

        //성별 선택
        userGenderM.setOnClickListener {
            userGenderW.isChecked = false
            gender = 1
            checkNext(gender,name,birth)
        }
        userGenderW.setOnClickListener {
            userGenderM.isChecked = false
            gender = 2
            checkNext(gender,name,birth)
        }

        userProfileImg.setOnClickListener {
            //프사 선택
            val adapter = ProfileDialog(this, 3)
            var overlapClick = true
            val dialog = DialogPlus.newDialog(this)
                    .setAdapter(adapter)
                    .setExpanded(false, 600)
                    .setOnItemClickListener { dialog, _, _, position ->
                        if(overlapClick) {
                            overlapClick = false
                            if (position == 1) {
                                //사진 찍어 변경
                                val permissionlistener = object : PermissionListener {
                                    override fun onPermissionGranted() {
                                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                        if (takePictureIntent.resolveActivity(packageManager) != null) {
                                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                                        }                      //  Start ImagePicker
                                    }
                                    override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                                    }
                                }
                                TedPermission.with(this)
                                        .setPermissionListener(permissionlistener)
                                        .setRationaleMessage("사진을 찍기위해 권한이 필요합니다.")
                                        .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                                        .setPermissions(Manifest.permission.CAMERA)
                                        .check()
                            } else if (position == 2) {
                                //갤러리에서 변경
                                val permissionlistener = object : PermissionListener {
                                    override fun onPermissionGranted() {
                                        val images = arrayListOf<Image>()
                                        ImagePicker.with(this@FirstSettingActivity)                         //  Initialize ImagePicker with activity or fragment context
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

                                    override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {

                                    }
                                }
                                TedPermission.with(this)
                                        .setPermissionListener(permissionlistener)
                                        .setRationaleMessage("앨범에서 사진을 가져오기위해 권한이 필요합니다.")
                                        .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                                        .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        .check()
                            }
                            dialog.dismiss()
                            overlapClick = true
                        }
                    }
                    .create()
            dialog.show()

        }

        //다음 버튼
        nextButton.setOnClickListener { it ->
            //            val state = pref.getString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), StringData.NON_SETTING)
            //취향, 정보 입력 현황 데이터 저장
//            if(state == SECOND_SETTING){
//                editor.putString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), ALL_SETTING)
//            }else
            editor.putString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), FIRST_SETTING)
            editor.commit()

            val cal = Calendar.getInstance()
            cal.set(userBirthYear,userBirthMonth,userBirthDay)

            val userInfo = UserInfo(profileUri,gender,userNameText.text.toString(),cal.timeInMillis)

            val firestore = db.collection(USER_INFO)
            firestore.document(currentUser!!.uid).set(userInfo)

            //사용자 데이터 업데이트
            val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(userNameText.text.toString())
                    .setPhotoUri(Uri.parse(profileUri))
                    .build()
            mAuth.currentUser!!.updateProfile(profileUpdates)

            if(route == NEW_SETTING) {
                //처음 가입시 들어와서 세팅 완료
                intent.putExtra(NAME,name)
                intent.putExtra(BIRTH,birth)
                intent.putExtra(GENDER,gender)
                setResult(1001,intent)
                finish()
            }else{
                //차후 변경으로

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            //갤러리에서 사진선택
            val images = data.getParcelableArrayListExtra<Image>(Config.EXTRA_IMAGES)[0].path

            if(images[images.length-1]!='f'){
                //gif아닌지 확인
                userProfileImageView.isDrawingCacheEnabled = true
                userProfileImageView.buildDrawingCache()
                val bitmap = BitmapFactory.decodeFile(images)//경로를 통해 비트맵으로 전환
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val imageData = baos.toByteArray()

                val metadata = StorageMetadata.Builder()
                        .setContentType("image/jpeg")
                        .build()

                val storageRef = storage.reference
                val uploadTask = storageRef.child("profile/"+currentUser!!.uid).putBytes(imageData, metadata)

                val dialog = SpotsDialog.Builder()
                        .setContext(this)
                        .setMessage("사진 변경 중 입니다")
                        .build()

                uploadTask.addOnProgressListener {
                    dialog.show()
                }
                        .addOnPausedListener {
                            dialog.dismiss()
                        }
                        .addOnSuccessListener {
                            //업로드 완료
                            dialog.dismiss()
                            profileUri = "profile/"+currentUser!!.uid
                            GlideApp.with(this)
                                    .load(storageRef.child(profileUri))
                                    .apply(RequestOptions().centerCrop())
                                    .apply(RequestOptions.circleCropTransform())
                                    .apply(RequestOptions.placeholderOf(resources.getDrawable(R.drawable.ic_user)))
                                    .signature(ObjectKey(System.currentTimeMillis()))
                                    .thumbnail(0.1f)
                                    .into(userProfileImageView)
                        }
                        .addOnFailureListener{
                            dialog.dismiss()
                        }

            }else{
                Toast.makeText(this,"gif",Toast.LENGTH_SHORT).show()
            }
        }else if(requestCode == REQUEST_IMAGE_CAPTURE&& resultCode == RESULT_OK){
            val extras = data!!.extras
            val imageBitmap = extras.get("data")

            val baos = ByteArrayOutputStream()
            (imageBitmap as Bitmap).compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val imageData = baos.toByteArray()

            val metadata = StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build()

            val storageRef = storage.reference
            val uploadTask = storageRef.child("profile/"+currentUser!!.uid).putBytes(imageData, metadata)

            val dialog = SpotsDialog.Builder()
                    .setContext(this)
                    .setMessage("사진 변경 중 입니다")
                    .build()

            uploadTask.addOnProgressListener {
                dialog.show()
            }
                    .addOnPausedListener {
                        dialog.dismiss()
                    }
                    .addOnSuccessListener {
                        //업로드 완료
                        dialog.dismiss()
                        profileUri = "profile/"+currentUser!!.uid
                        GlideApp.with(this)
                                .load(storageRef.child(profileUri))
                                .apply(RequestOptions().centerCrop())
                                .apply(RequestOptions.circleCropTransform())
                                .apply(RequestOptions.placeholderOf(resources.getDrawable(R.drawable.ic_user)))
                                .signature(ObjectKey(System.currentTimeMillis()))
                                .thumbnail(0.1f)
                                .into(userProfileImageView)
                    }
                    .addOnFailureListener{
                        dialog.dismiss()
                    }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    override fun onBackPressed() {}

    private fun checkNext(gender : Int, name:Boolean, birth:Boolean){
        if(birth && name && gender>0) {
            nextButton.isEnabled = true
            nextButton.background = resources.getDrawable(R.drawable.sign_up_success_button)
        }else{
            nextButton.isEnabled = false
            nextButton.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
        }
    }
}