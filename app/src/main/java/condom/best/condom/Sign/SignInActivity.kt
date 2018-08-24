package condom.best.condom.Sign

import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AppCompatActivity() {
    lateinit var mAuth : FirebaseAuth

    private lateinit var dialog: android.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("로그인 중 입니다.")
                .build()

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setCustomView(R.layout.actionbar_activity_log_in)
        supportActionBar!!.elevation = 10F
        supportActionBar!!.setBackgroundDrawable(resources.getDrawable(R.color.white))
        val actionBar = supportActionBar!!.customView
        actionBar.action_bar_text.text = getString(R.string.signIn)
        actionBar.action_bar_back_key.setOnClickListener { finish() }

        var emailCh = false
        var passCh = false

        userMailText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                emailCh = s!!.isNotEmpty()
                signInButtonEnable(emailCh,passCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        userPassText.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                passCh = s!!.length>=6
                signInButtonEnable(emailCh,passCh)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        signInButton.setOnClickListener {
            if(emailCh&&passCh)
                dialog.show()
                mAuth.signInWithEmailAndPassword(userMailText.text.toString(), userPassText.text.toString())
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
        signUpButton.setOnClickListener {
            //회원가입 페이지 넘어가기
            setResult(123)
            finish()
        }
        forgotPassword.setOnClickListener {
            startActivity(Intent(this,PasswordActivity::class.java))
        }
    }
    private fun signInButtonEnable(ch1 : Boolean, ch2 : Boolean){
        if(ch1 && ch2){
            signInButton.isEnabled = true
            signInButton.background = resources.getDrawable(R.drawable.sign_up_success_button)
        }else{
            signInButton.isEnabled = false
            signInButton.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
        }
    }
}
