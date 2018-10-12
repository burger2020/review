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
import kotlinx.android.synthetic.main.activity_password.*

@Suppress("DEPRECATION")
class PasswordActivity : AppCompatActivity() {

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private lateinit var dialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("메일 전송중 입니다.")
                .build()

        backBtn.setOnClickListener { finish() }
        userMailChText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val emailCh = if(s!!.matches(emailPattern.toRegex()) && s.isNotEmpty()){ //이메일 패턴 체크
                    userMailChText.setCompoundDrawables(null,null,resources.getDrawable(R.drawable.checked),null)
                    true
                }else{
                    userMailChText.setCompoundDrawables(null,null,null,null)
                    false
                }
                if(emailCh){
                    passSendBtn.isEnabled = true //비밀번호 보내기 버튼 활성화
                    passSendBtn.background = resources.getDrawable(R.drawable.sign_up_success_button) // 비밀번호 보내기 버튼 활성화 이미지
                }else{
                    passSendBtn.isEnabled = false //비밀번호 보내기 버튼 비활성화
                    passSendBtn.background = resources.getDrawable(R.drawable.sign_up_success_non_button) //비밀번호 보내기 버튼 비활성화 이미지
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        passSendBtn.setOnClickListener { passSendBtn() }
    }

    //비밀번호 전송 버튼
    private fun passSendBtn(){
        val auth = FirebaseAuth.getInstance()
        val emailAddress = userMailChText.text.toString() //이메일주소

        //보내기 결과 다이얼로그
        dialog.show()
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,getString(R.string.passwordResetMailSend), Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this,getString(R.string.passwordResetMailNonSend), Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
                }

    }
}
