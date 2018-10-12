package condom.best.condom.View.Sign

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import condom.best.condom.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_sign_up.*

@Suppress("DEPRECATION")
class SignUpActivity : AppCompatActivity() {

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private var emailCh = false
    private var passCh = false
    private var passConfirmCh = false

    private lateinit var dialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("회원가입 중 입니다")
                .build()!!

        val mAuth = FirebaseAuth.getInstance()

        //백버튼
        backBtn.setOnClickListener { finish() }
        //약관 동의 체크박스
        signUpCheckBox.setOnClickListener { signUpButtonEnable(emailCh,passCh,passConfirmCh) }
        //이메일 텍스트 입력 변경시
        emailText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                emailCh = if(email.matches(emailPattern.toRegex()) && email.isNotEmpty()){
                    emailText.setCompoundDrawables(null,null,resources.getDrawable(R.drawable.checked),null)
                    true
                }else{
                    emailText.setCompoundDrawables(null,null,null,null)
                    false
                }
                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        passText.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                //비밀번호 6자이상 체크
                passCh = s!!.length>=6
                if(passCh)
                    passText.setCompoundDrawables(null,null,resources.getDrawable(R.drawable.checked),null)
                else
                    passText.setCompoundDrawables(null,null,null,null)

                passConfirmCh = passCText.text == passText.text
                if(passConfirmCh)
                    passCText.setCompoundDrawables(null,null,resources.getDrawable(R.drawable.checked),null)
                else
                    passCText.setCompoundDrawables(null,null,null,null)

                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        passCText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                passConfirmCh = passText.text == passCText.text
                if(passConfirmCh&&passCText.text!!.length>=6)
                    passCText.setCompoundDrawables(null,null,resources.getDrawable(R.drawable.checked),null)
                else
                    passCText.setCompoundDrawables(null,null,null,null)
                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        signUpBtn.setOnClickListener {
            //가입 완료
            dialog.show()
            mAuth.createUserWithEmailAndPassword(emailText.text.toString(), passText.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            //가입 성공
                            finish()
                            dialog.dismiss()
                        } else {
                            //가입 실패
                            dialog.dismiss()
                            Toast.makeText(this,getString(R.string.alreadySignUpEmail), Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }
    private fun signUpButtonEnable(ch1 : Boolean,ch2 : Boolean,ch3 : Boolean){
        //가입 완료 버튼 활성 비활성
        if(ch1 && ch2 && ch3 && signUpCheckBox.isChecked){
            signUpBtn.isEnabled = true
            signUpBtn.background = resources.getDrawable(R.drawable.sign_up_success_button)
        }else{
            signUpBtn.isEnabled = false
            signUpBtn.background = resources.getDrawable(R.drawable.sign_up_success_button)
        }
    }
}
