package condom.best.condom.View.Sign

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import condom.best.condom.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity() {

    private var emailCh = false
    private var passCh = false
    private var mAuth : FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var dialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("로그인 중 입니다.")
                .build()!!

        backBtn.setOnClickListener { finish() }
        emailText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                emailCh = s!!.isNotEmpty()
                signInButtonEnable(emailCh,passCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        passText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                passCh = s!!.length>=6
                signInButtonEnable(emailCh,passCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        signInBtn.setOnClickListener { signInBtnClick() }
        signUpBtn.setOnClickListener { startActivity(Intent(this, SignUpActivity::class.java)) } //회원가입 페이지 넘어가기
        forgotPassBtn.setOnClickListener { startActivity(Intent(this, PasswordActivity::class.java)) } //비밀번호 찾기 페이지 넘어가기
    }

    private fun signInBtnClick() { //로그인 버튼
        if(emailCh&&passCh)
            dialog.show()
        mAuth.signInWithEmailAndPassword(emailText.text.toString(), passText.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        dialog.dismiss()
                        finish()
                    } else {
                        dialog.dismiss()
                        Toast.makeText(this, "아이디 또는 비밀번호가 잘못됐습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
    }
    private fun signInButtonEnable(ch1 : Boolean, ch2 : Boolean){
        if(ch1 && ch2){
            signInBtn.isEnabled = true
            signInBtn.background = resources.getDrawable(R.drawable.sign_up_success_button)
        }else{
            signInBtn.isEnabled = false
            signInBtn.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
        }
    }
}
