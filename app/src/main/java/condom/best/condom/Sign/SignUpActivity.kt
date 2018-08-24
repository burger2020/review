package condom.best.condom.Sign

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import condom.best.condom.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.actionbar_activity_log_in.view.*
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity() {
    private lateinit var mAuth : FirebaseAuth
    private lateinit var dialog: android.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setCustomView(R.layout.actionbar_activity_log_in)
        supportActionBar!!.elevation = 10F
        supportActionBar!!.setBackgroundDrawable(resources.getDrawable(R.color.white))
        val actionBar = supportActionBar!!.customView
        actionBar.action_bar_text.text = getString(R.string.signUp)
        actionBar.action_bar_back_key.setOnClickListener { finish() }

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("회원가입 중 입니다")
                .build()

        var emailCh = false
        var passCh = false
        var passConfirmCh = false

        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        userMailText.addTextChangedListener(object :TextWatcher{
            //이메일 유효 검사
            override fun afterTextChanged(s: Editable?) {
                val email = userMailText.text.toString().trim()
                emailCh = if(email.matches(emailPattern.toRegex()) && s!!.isNotEmpty()){
                    userMailText.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.checked),null)
                    true
                }else{
                    userMailText.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)
                    false
                }
                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        userPassText.addTextChangedListener(object : TextWatcher{
            //비밀번호 6자이상 체크
            override fun afterTextChanged(s: Editable?) {
                passCh = s!!.length>=6
                if(passCh)
                    userPassText.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.checked),null)
                else
                    userPassText.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)

                passConfirmCh = userPassConfirmText.text.toString() == s.toString()
                if(passConfirmCh)
                    userPassConfirmText.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.checked),null)
                else
                    userPassConfirmText.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)

                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        userPassConfirmText.addTextChangedListener(object : TextWatcher{
            //비밀번호 재확인 체크
            override fun afterTextChanged(s: Editable?) {
                passConfirmCh = userPassText.text.toString() == s.toString()
                if(passConfirmCh&&s!!.length>=6)
                    userPassConfirmText.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.checked),null)
                else
                    userPassConfirmText.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)
                signUpButtonEnable(emailCh,passCh,passConfirmCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        userSignUpCheck.setOnClickListener {
            //약관 동의
            signUpButtonEnable(emailCh,passCh,passConfirmCh)
        }

        signUpButton.setOnClickListener {
            //가입 완료
            dialog.show()
            mAuth.createUserWithEmailAndPassword(userMailText.text.toString(), userPassText.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            //가입 성공
                            finish()
                            dialog.dismiss()
                        } else {
                            //가입 실패
                            dialog.dismiss()
                            Toast.makeText(this,getString(R.string.alreadySignUpEmail),Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun signUpButtonEnable(ch1 : Boolean,ch2 : Boolean,ch3 : Boolean){
        //가입 완료 버튼 활성 비활성
        if(ch1 && ch2 && ch3 && userSignUpCheck.isChecked){
            signUpButton.isEnabled = true
            signUpButton.background = resources.getDrawable(R.drawable.sign_up_success_button)
        }else{
            signUpButton.isEnabled = false
            signUpButton.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
        }
    }
}
