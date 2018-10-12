package condom.best.condom.View

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.StorageMetadata
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.orhanobut.dialogplus.DialogPlus
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.ProfileOption
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.View.Data.GlideApp
import condom.best.condom.View.Data.StringData.Companion.BIRTH
import condom.best.condom.View.Data.StringData.Companion.FIRST_SETTING
import condom.best.condom.View.Data.StringData.Companion.GENDER
import condom.best.condom.View.Data.StringData.Companion.NAME
import condom.best.condom.View.Data.UserInfo
import condom.best.condom.View.Dialog.ProfileDialog
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.db
import condom.best.condom.View.MainActivity.Companion.localDataPut
import condom.best.condom.View.MainActivity.Companion.storage
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_first_setting.*
import java.io.ByteArrayOutputStream
import java.util.*


@Suppress("DEPRECATION")
class FirstSettingActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private val REQUEST_IMAGE_CAPTURE = 10942

    private var name = false
    private var birth = false
    private var gender = 0
    private var profileUri = "none"

    private var datePickerBool = true
    private val c = Calendar.getInstance()
    private var userBirthYear = c.get(Calendar.YEAR)-19
    private var userBirthMonth = c.get(Calendar.MONTH)
    private var userBirthDay = c.get(Calendar.DAY_OF_MONTH)
    @SuppressLint("SetTextI18n")
    val listener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth -> //데이트피커 셋 이벤트
        userBirthYear = year
        userBirthMonth = monthOfYear
        userBirthDay = dayOfMonth
        setBirth.setText("$userBirthYear. ${userBirthMonth + 1}. $userBirthDay")
        checkBirth.visibility = View.VISIBLE
        birth = true
        checkNext(gender, name, birth)
    }
    @SuppressLint("SetTextI18n", "CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_setting)

        mAuth = FirebaseAuth.getInstance()

        userNameText.addTextChangedListener(object : TextWatcher{
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
        setBirth.setOnClickListener { setBirth() }
        maleCheckBox.setOnClickListener { maleSet() }
        femaleCheckBox.setOnClickListener { femaleSet() }
        nextBtn.setOnClickListener { successBtnClick() }
        userProfileLayer.setOnClickListener { profileChange() }
    }
    private fun profileChange() { //프사 선택
        val adapter = ProfileDialog(this, 4)
        var overlapClick = true
        val dialog = DialogPlus.newDialog(this)
                .setAdapter(adapter)
                .setExpanded(false, 600)
                .setOnItemClickListener { dialog, _, _, position ->
                    if(overlapClick) {
                        overlapClick = false
                        profileImageSetting(position)
                        dialog.dismiss()
                        overlapClick = true
                    }
                }
                .create()
        dialog.show()
    }
    //다음 버튼
    private fun successBtnClick(){
        val cal = Calendar.getInstance()
        cal.set(userBirthYear,userBirthMonth,userBirthDay)
        successSetting(UserInfo(profileUri,gender,userNameText.text.toString(),cal.timeInMillis))
        //            val state = pref.getString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), StringData.NON_SETTING)
        // 취향, 정보 입력 현황 데이터 저장
        //            if(state == SECOND_SETTING){
        //                editor.putString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), ALL_SETTING)
        //            }else
    }
    private fun femaleSet(){//여자 선택
        maleCheckBox.isChecked = false
        gender = 2
        checkNext(gender,name,birth)
    }
    private fun maleSet(){//남자 선택
        femaleCheckBox.isChecked = false
        gender = 1
        checkNext(gender,name,birth)
    }
    private fun setBirth(){ //생일 선택
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

    private fun checkNext(gender : Int, name:Boolean, birth:Boolean){//입력 다 됐는지 확인
        if(birth && name && gender>0) {
            nextBtn.isEnabled = true
            resources.getDrawable(R.drawable.sign_up_success_button)
        }else{
            nextBtn.isEnabled = false
            nextBtn.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
        }
    }

    private fun profileImageSetting(sect: Int) {
        when (sect) {
            1 -> { //사진 찍어 변경
                ProfileOption.imageCapture(this,REQUEST_IMAGE_CAPTURE)
            }
            2 -> { //갤러리에서 변경
                ProfileOption.gallerySelect(this)
            }
            3->{ //기본이미지 설정
                storage.child("profile/"+currentUser!!.uid).delete()
                profileUri = "none"
                GlideApp.with(this)
                        .load(resources.getDrawable(R.drawable.ic_user))
                        .apply(RequestOptions().centerCrop())
                        .apply(RequestOptions.circleCropTransform())
                        .signature(ObjectKey(System.currentTimeMillis()))
                        .into(profileImg)
            }
        }
    }

    private fun successSetting(userInfo: UserInfo) { //세팅 완료후 데이터 저장
        localDataPut.putString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), FIRST_SETTING)
        localDataPut.commit()

        val firestore = db.collection(USER_INFO)
        firestore.document(currentUser!!.uid).set(userInfo)

        //사용자 데이터 업데이트
        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userInfo.name)
                .setPhotoUri(Uri.parse(userInfo.profileUri))
                .build()
        mAuth.currentUser!!.updateProfile(profileUpdates)

        //처음 가입시 들어와서 세팅 완료
        intent.putExtra(NAME,userInfo.name)
        intent.putExtra(BIRTH,userInfo.birth)
        intent.putExtra(GENDER,userInfo.birth)
        setResult(1001,intent)
        finish()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) { //갤러리에서 사진선택
            val images = data.getParcelableArrayListExtra<Image>(Config.EXTRA_IMAGES)[0].path
            if(images[images.length-1]!='f'){ //gif아닌지 확인
//                userProfileImageView.isDrawingCacheEnabled = true
//                userProfileImageView.buildDrawingCache()
                val bitmap = BitmapFactory.decodeFile(images)//경로를 통해 비트맵으로 전환
                profileImgUpload((bitmap as Bitmap))
            }else{ Toast.makeText(this,"gif",Toast.LENGTH_SHORT).show() }
        }else if(requestCode == REQUEST_IMAGE_CAPTURE&& resultCode == RESULT_OK){
            val extras = data!!.extras
            val imageBitmap = extras.get("data")
            profileImgUpload((imageBitmap as Bitmap))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun profileImgUpload(bitmap: Bitmap){ //비트맵파일 서버에 저장
        val dialog: AlertDialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("사진 변경 중 입니다")
                .build()
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val imageData = baos.toByteArray()
        val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()
        val uploadTask = storage.child("profile/"+currentUser!!.uid).putBytes(imageData, metadata)

        uploadTask.addOnProgressListener { dialog.show() }
                .addOnPausedListener { dialog.dismiss() }
                .addOnSuccessListener { //업로드 완료
                    dialog.dismiss()
                    val profileUri = "profile/"+currentUser!!.uid
                    GlideApp.with(this)
                            .load(storage.child(profileUri))
                            .apply(RequestOptions().centerCrop())
                            .apply(RequestOptions.circleCropTransform())
                            .signature(ObjectKey(System.currentTimeMillis()))
                            .apply(RequestOptions.placeholderOf(resources.getDrawable(R.drawable.ic_user)))
                            .transition(DrawableTransitionOptions.withCrossFade(100))
                            .into(profileImg)
                }.addOnFailureListener{ dialog.dismiss() }
    }
    override fun onBackPressed() {}

}