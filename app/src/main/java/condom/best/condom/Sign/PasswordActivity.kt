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
import kotlinx.android.synthetic.main.activity_password.*

class PasswordActivity : AppCompatActivity() {
    private lateinit var dialog: android.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setCustomView(R.layout.actionbar_activity_log_in)
        supportActionBar!!.elevation = 10F
        supportActionBar!!.setBackgroundDrawable(resources.getDrawable(R.color.white))
        val actionBar = supportActionBar!!.customView
        actionBar.action_bar_text.text = getString(R.string.passwordReset)
        actionBar.action_bar_back_key.setOnClickListener { finish() }

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("메일 전송중 입니다.")
                .build()

        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        var emailCh: Boolean

        userMailChText.addTextChangedListener(object : TextWatcher {
            //이메일 유효 검사
            override fun afterTextChanged(s: Editable?) {
                val email = userMailChText.text.toString().trim()
                emailCh = if(email.matches(emailPattern.toRegex()) && s!!.isNotEmpty()){
                    userMailChText.setCompoundDrawablesWithIntrinsicBounds(null,null,resources.getDrawable(R.drawable.checked),null)
                    true
                }else{
                    userMailChText.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null)
                    false
                }
                if(emailCh){
                    passwordSendButton.isEnabled = true
                    passwordSendButton.background = resources.getDrawable(R.drawable.sign_up_success_button)
                }else{
                    passwordSendButton.isEnabled = false
                    passwordSendButton.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        passwordSendButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val emailAddress = userMailChText.text.toString()

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
}
