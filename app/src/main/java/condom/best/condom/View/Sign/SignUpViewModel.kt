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

class SignUpViewModel(@SuppressLint("StaticFieldLeak") val context: Activity, private val mAuth: FirebaseAuth) : ViewModel() {

    val emailText = ObservableField<String>()
    val emailTextChImg = ObservableField<Drawable>()
    val passText = ObservableField<String>()
    val passTextChImg = ObservableField<Drawable>()
    val passCText = ObservableField<String>()
    val passCChImg = ObservableField<Drawable>()
    val signUpIsCheck = ObservableBoolean()
    val signUpBtnEnable = ObservableBoolean()
    val signUpBtnImg = ObservableField<Drawable>()

    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    var emailCh = false
    var passCh = false
    var passConfirmCh = false
    val dialog = SpotsDialog.Builder()
            .setContext(context)
            .setMessage("회원가입 중 입니다")
            .build()!!

    fun backButton() = context.finish()
    fun signUpCheck() = signUpButtonEnable(emailCh,passCh,passConfirmCh)
    fun signUpBtnClick(){
        //가입 완료
        dialog.show()
        mAuth.createUserWithEmailAndPassword(emailText.get()!!, passText.get()!!)
                .addOnCompleteListener(context) { task ->
                    if (task.isSuccessful) {
                        //가입 성공
                        context.finish()
                        dialog.dismiss()
                    } else {
                        //가입 실패
                        dialog.dismiss()
                        Toast.makeText(context,context.getString(R.string.alreadySignUpEmail), Toast.LENGTH_SHORT).show()
                    }
                }
    }
    init {
        signUpBtnEnable.set(false)
        signUpBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_non_button))

        emailText.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                val email = emailText.get()!!.trim()
                emailCh = if(email.matches(emailPattern.toRegex()) && email.isNotEmpty()){
                    emailTextChImg.set(context.resources.getDrawable(R.drawable.checked))
                    true
                }else{
                    emailTextChImg.set(null)
                    false
                }
                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
        })
        passText.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                //비밀번호 6자이상 체크
                passCh = passText.get()!!.length>=6
                if(passCh)
                    passTextChImg.set(context.resources.getDrawable(R.drawable.checked))
                else
                    passTextChImg.set(null)

                passConfirmCh = passCText.get() == passText.get()
                if(passConfirmCh)
                    passCChImg.set(context.resources.getDrawable(R.drawable.checked))
                else
                    passCChImg.set(null)

                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
        })
        passCText.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                passConfirmCh = passText.get() == passCText.get()
                if(passConfirmCh&&passCText.get()!!.length>=6)
                    passCChImg.set(context.resources.getDrawable(R.drawable.checked))
                else
                    passCChImg.set(null)
                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
        })
    }
    private fun signUpButtonEnable(ch1 : Boolean,ch2 : Boolean,ch3 : Boolean){
        //가입 완료 버튼 활성 비활성
        if(ch1 && ch2 && ch3 && signUpIsCheck.get()){
            signUpBtnEnable.set(true)
            signUpBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_button))
        }else{
            signUpBtnEnable.set(false)
            signUpBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_non_button))
        }
    }
}