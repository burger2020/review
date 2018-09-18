package condom.best.condom.View.Sign

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.databinding.Observable
import android.databinding.ObservableField
import android.graphics.drawable.Drawable
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import condom.best.condom.R
import dmax.dialog.SpotsDialog

class SignInViewModel(@SuppressLint("StaticFieldLeak") val context : Activity) : ViewModel() {

    val emailText = ObservableField<String>()
    val passText = ObservableField<String>()
    val signInBtnEnable = ObservableField<Boolean>()
    val signInBtnImg = ObservableField<Drawable>()

    var emailCh = false
    var passCh = false
    var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    val dialog = SpotsDialog.Builder()
            .setContext(context)
            .setMessage("로그인 중 입니다.")
            .build()!!

    fun backButton() = context.finish()
    fun signInBtnClick() {
        if(emailCh&&passCh)
            dialog.show()
        mAuth.signInWithEmailAndPassword(emailText.get().toString(), passText.get().toString())
                .addOnCompleteListener(context) { task ->
                    if (task.isSuccessful) {
                        dialog.dismiss()
                        context.finish()
                    } else {
                        dialog.dismiss()
                        Toast.makeText(context, "아이디 또는 비밀번호가 잘못됐습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    init {
        signInBtnEnable.set(false)

        emailText.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                emailCh = emailText.get()!!.isNotEmpty()
                signInButtonEnable(emailCh,passCh)
            }
        })
        passText.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                passCh = passText.get()!!.length>=6
                signInButtonEnable(emailCh,passCh)
            }
        })

    }
    //회원가입 페이지 넘어가기
    fun signUpClick() = context.startActivity(Intent(context, SignUpActivity::class.java))
    //비밀번호 찾기 페이지 넘어가기
    fun forgotPass()  = context.startActivity(Intent(context, PasswordActivity::class.java))

    private fun signInButtonEnable(ch1 : Boolean, ch2 : Boolean){
        if(ch1 && ch2){
            signInBtnEnable.set(true)
            signInBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_button))
        }else{
            signInBtnEnable.set(false)
            signInBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_non_button))
        }
    }
}
