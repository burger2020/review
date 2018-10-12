package condom.best.condom.View.BottomNavPage

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.StorageMetadata
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.model.Image
import com.orhanobut.dialogplus.DialogPlus
import condom.best.condom.BottomNavPage.Product.PhotoViewActivity
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.View.Data.StringData.Companion.RATING
import condom.best.condom.View.Data.StringData.Companion.REVIEW
import condom.best.condom.View.Data.StringData.Companion.WISH
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BindingAdapter.ProfileOption
import condom.best.condom.View.BottomNavPage.MyPage.ActMoreFragment
import condom.best.condom.View.BottomNavPage.MyPage.NameCustomActivity
import condom.best.condom.View.Data.GlideApp
import condom.best.condom.View.Data.UserInfo
import condom.best.condom.View.Data.UserLocalDataPath.Companion.USER_INFO_PATH
import condom.best.condom.View.Dialog.ProfileDialog
import condom.best.condom.View.MainActivity
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.db
import condom.best.condom.View.MainActivity.Companion.gson
import condom.best.condom.View.MainActivity.Companion.localDataGet
import condom.best.condom.View.MainActivity.Companion.localDataPut
import condom.best.condom.View.MainActivity.Companion.storage
import condom.best.condom.View.MainActivity.Companion.userActInfo
import condom.best.condom.View.MainActivity.Companion.userInfo
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_mypage.view.*
import java.io.ByteArrayOutputStream

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Suppress("DEPRECATION")
class MyPageFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private var profileUri = "/defaultProfileImage/ic_user.png"

    private val REQUEST_NAME_CUSTOM = 12341
    private val REQUEST_IMAGE_CAPTURE = 10943

    lateinit var rootView : View
    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_mypage, container, false)

        rootView.userNameText.text = currentUser!!.displayName

        val index = localDataGet.getInt(getString(R.string.userProfileChange),0)
        if(currentUser!!.photoUrl.toString()=="null")
            GlideApp.with(context!!)
                    .load(context!!.resources.getDrawable(R.drawable.ic_user))
                    .apply(RequestOptions.circleCropTransform())
                    .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}$index")))
                    .into(rootView.userProfileImg)
        else
            GlideApp.with(context!!)
                    .load(storage.child(currentUser!!.photoUrl.toString()))
                    .apply(RequestOptions().centerCrop())
                    .apply(RequestOptions.circleCropTransform())
                    .apply(RequestOptions.placeholderOf(context!!.resources.getDrawable(R.drawable.ic_user)))
                    .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}$index")))
                    .transition(DrawableTransitionOptions.withCrossFade(100))
                    .into(rootView.userProfileImg)

        rootView.userProfileLayer.setOnClickListener {
            //프사 선택
            val adapter = ProfileDialog(context!!, 6)
            var overlapClick = true
            val dialog = DialogPlus.newDialog(rootView.context)
                    .setAdapter(adapter)
                    .setExpanded(false, 600)
                    .setOnItemClickListener { dialog, _, _, position ->
                        if(overlapClick) {
                            overlapClick = false
                            when (position) {
                                1 -> { //사진 찍어 변경
                                    ProfileOption.imageCapture(this,REQUEST_IMAGE_CAPTURE)
                                }
                                2 -> { //갤러리에서 변경
                                    ProfileOption.gallerySelect(this)
                                }
                                3 -> { //기본 이미지로 변경
                                    val index = localDataGet.getInt(getString(R.string.userProfileChange),0)+1
                                    userInfo.profileUri = "none"
                                    val strContact = gson.toJson(userInfo, UserInfo::class.java)
                                    localDataPut.putString(USER_INFO_PATH, strContact)
                                    localDataPut.putInt(getString(R.string.userProfileChange),index)
                                    localDataPut.commit()
                                    GlideApp.with(context!!)
                                            .load(context!!.resources.getDrawable(R.drawable.ic_user))
                                            .apply(RequestOptions.circleCropTransform())
                                            .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}$index")))
                                            .into(rootView.userProfileImg)//프로필사진 세팅

                                    //사용자 데이터 업데이트
                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setPhotoUri(Uri.parse("none"))
                                            .build()
                                    currentUser!!.updateProfile(profileUpdates)

                                    storage.child("profile/"+currentUser!!.uid).delete()

                                    db.collection(USER_INFO).document(currentUser!!.uid).update("profileUri","none")
                                }
                                4->{ //이미지 크게보기
                                    val intent = Intent(rootView.context,PhotoViewActivity::class.java)
                                    startActivity(intent)
                                }
                                5->{ //이름 변경
                                    startActivityForResult(Intent(rootView.context, NameCustomActivity::class.java),REQUEST_NAME_CUSTOM)
                                }
                            }
                            dialog.dismiss()
                            overlapClick = true
                        }
                    }
                    .create()
            dialog.show()
        }

        rootView.ratingListMore.setOnClickListener {//평가
            (activity as MainActivity).actMoreFragment = ActMoreFragment.newInstance(RATING)
            FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).actMoreFragment,"MY")
        }
        rootView.wishListMore.setOnClickListener {//위시
            (activity as MainActivity).actMoreFragment = ActMoreFragment.newInstance(WISH)
            FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).actMoreFragment,"MY")
        }
        rootView.reviewMore.setOnClickListener {//리뷰 리스트
            (activity as MainActivity).actMoreFragment = ActMoreFragment.newInstance(REVIEW)
            FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).actMoreFragment,"MY")
        }
        return rootView
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == AppCompatActivity.RESULT_OK && data != null) { //갤러리에서 사진선택
            val images = data.getParcelableArrayListExtra<Image>(Config.EXTRA_IMAGES)[0].path
            if(images[images.length-1]!='f'){
                //gif아닌지 확인
                val bitmap = BitmapFactory.decodeFile(images)//경로를 통해 비트맵으로 전환
                profileImgUpload(bitmap)
            }else{ Toast.makeText(context!!,"gif", Toast.LENGTH_SHORT).show() }
        }
        else if(requestCode == REQUEST_IMAGE_CAPTURE&& resultCode == AppCompatActivity.RESULT_OK){ //사진 찍어서
            val extras = data!!.extras
            val imageBitmap = extras.get("data")
            profileImgUpload((imageBitmap as Bitmap))
        }
        else if(requestCode == REQUEST_NAME_CUSTOM&& resultCode == AppCompatActivity.RESULT_OK){//이름 바꾸기
            val name = data!!.getStringExtra("customName")
            val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
            userInfo.name = name
            val strContact = gson.toJson(userInfo, UserInfo::class.java)
            localDataPut.putString(USER_INFO_PATH, strContact)
            currentUser!!.updateProfile(profileUpdates)
            db.collection(USER_INFO).document(currentUser!!.uid).update("name", name)
            rootView.userNameText.text = name
        }
    }
    //프로필 사진 서버에 업데이트
    private fun profileImgUpload(bitmap: Bitmap){ //비트맵파일 서버에 저장
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

        val uploadTask = storage.child("profile/"+currentUser!!.uid).putBytes(imageData, metadata)

        val dialog = SpotsDialog.Builder()
                .setContext(context!!)
                .setMessage("사진 변경 중 입니다")
                .build()

        uploadTask.addOnProgressListener { dialog.show() }
                .addOnPausedListener { dialog.dismiss() }
                .addOnSuccessListener {
                    //업로드 완료
                    val index = localDataGet.getInt(getString(R.string.userProfileChange),0)+1
                    profileUri = "profile/"+currentUser!!.uid
                    userInfo.profileUri = profileUri
                    val strContact = gson.toJson(userInfo, UserInfo::class.java)
                    localDataPut.putString(USER_INFO_PATH, strContact)
                    localDataPut.putInt(getString(R.string.userProfileChange),index)
                    localDataPut.commit()

                    GlideApp.with(context!!)
                            .load(storage.child(currentUser!!.photoUrl.toString()))
                            .apply(RequestOptions().centerCrop())
                            .apply(RequestOptions.circleCropTransform())
                            .apply(RequestOptions.placeholderOf(context!!.resources.getDrawable(R.drawable.ic_user)))
                            .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}$index")))
                            .transition(DrawableTransitionOptions.withCrossFade(100))
                            .into(rootView.userProfileImg)

                    if(currentUser!!.photoUrl.toString()=="none") { //사용자 데이터 업데이트
                        val profileUpdates = UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(profileUri))
                                .build()
                        currentUser!!.updateProfile(profileUpdates)

                        db.collection(USER_INFO).document(currentUser!!.uid).update("profileUri", profileUri)
                    }

                    dialog.dismiss()
                }
                .addOnFailureListener{ dialog.dismiss() }}

    fun setData(){
        rootView.userUseNum.text = userActInfo.ratingNum.toString()
        rootView.userWishNum.text = userActInfo.wishNum.toString()
        rootView.userReviewNum.text = userActInfo.reviewNum.toString()
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                MyPageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
