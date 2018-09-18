package condom.best.condom.View.Sign

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.graphics.drawable.Drawable
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import condom.best.condom.R
import dmax.dialog.SpotsDialog

class PasswordViewModel(@SuppressLint("StaticFieldLeak") val context: Activity) : ViewModel(){

    val emailInputText = ObservableField<String>()      // 이메일칸 입력 텍스트
    val passSendBtnImg = ObservableField<Drawable>()    // 비밀번호 보내기 버튼 이미지
    val passSendBtnEnable = ObservableBoolean()  // 비밀번호 보내기 버튼 활성
    val emailCheckImg = ObservableField<Drawable>()     // 이메일 체크 이미지

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private var  dialog = SpotsDialog.Builder()
            .setContext(context)
            .setMessage("메일 전송중 입니다.")
            .build()

    init {
        passSendBtnEnable.set(false) // 비밀번호 버튼 보내기 활성화
        passSendBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_non_button)) // 비밀번호 보내기 버튼 이미지
        emailCheckImg.set(null) // 이메일 입력칸 체크 이미지
        emailInputText.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            //이메일 변화 리스너
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val email = emailInputText.get() //입력 이메일
                val emailCh = if(email!!.matches(emailPattern.toRegex()) && emailInputText.get()!!.isNotEmpty()){ //이메일 패턴 체크
                    emailCheckImg.set(context.resources.getDrawable(R.drawable.checked)) //이메일 체크 이미지 활성화
                    true
                }else{
                    emailCheckImg.set(null) //이메일 체크 이미지 비활성화
                    false
                }
                if(emailCh){
                    passSendBtnEnable.set(true) //비밀번호 보내기 버튼 활성화
                    passSendBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_button)) // 비밀번호 보내기 버튼 활성화 이미지
                }else{
                    passSendBtnEnable.set(false) //비밀번호 보내기 버튼 비활성화
                    passSendBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_non_button)) //비밀번호 보내기 버튼 비활성화 이미지
                }
            }
        })
    }

    //액션바 백버튼
    fun backButton() = context.finish()
    //비밀번호 전송 버튼
    fun passSendBtn(){
        val auth = FirebaseAuth.getInstance()
        val emailAddress = emailInputText.get() //이메일주소

        //보내기 결과 다이얼로그
        dialog.show()
        auth.sendPasswordResetEmail(emailAddress!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context,context.getString(R.string.passwordResetMailSend), Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(context,context.getString(R.string.passwordResetMailNonSend), Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
                }

    }
}