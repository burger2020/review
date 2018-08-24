package condom.best.condom.Sign

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import condom.best.condom.Data.StringData.Companion.NON_SETTING
import condom.best.condom.R
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_log_in.*
import java.util.*


class LogInActivity : AppCompatActivity() {
    private lateinit var callbackManager : CallbackManager
    private lateinit var mAuth: FirebaseAuth
    private lateinit var dialog: android.app.AlertDialog

    private val SIGNIN_REQUEST = 1002
    private val SIGNUP_REQUEST = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        callbackManager = CallbackManager.Factory.create()
        mAuth = FirebaseAuth.getInstance()

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("로그인 중 입니다.")
                .build()

//        facebookSignUnButton.setReadPermissions("email")
        facebookSignUnButton.setOnClickListener {
            //페이스북 로글인 접근 권한 받기
            dialog.show()
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"))
        }
        // Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            //페이스북 로그인 콜백
            override fun onSuccess(loginResult: LoginResult) {
                dialog.show()
                handleFacebookAccessToken(loginResult.accessToken)
            }
            override fun onCancel() {
                dialog.dismiss()
            }
            override fun onError(exception: FacebookException) {
                dialog.dismiss()
            }
        })

        signIpButton.setOnClickListener {
            startActivityForResult(Intent(this,SignInActivity::class.java),SIGNIN_REQUEST)
        }
        signUpButton.setOnClickListener {
            startActivityForResult(Intent(this,SignUpActivity::class.java),SIGNUP_REQUEST)
        }

    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        dialog.dismiss()
                        updateUI(user)
                    } else {
                        updateUI(null)
                    }
                }
    }
    private fun updateUI(user : FirebaseUser?){
        if(user != null){
            val pref : SharedPreferences = getSharedPreferences("pref", MODE_PRIVATE)
            val editor = pref.edit()// editor에 put 하기
            editor.putString(user.uid+getString(R.string.firstUser),NON_SETTING)
            editor.commit()
            finish()
        }else{}
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            SIGNIN_REQUEST->{
                //로그인 리절트
                if(resultCode==123){
                    startActivityForResult(Intent(this,SignUpActivity::class.java),SIGNUP_REQUEST)
                }
                else {
                    mAuth = FirebaseAuth.getInstance()
                    updateUI(mAuth.currentUser)
                }
            }
            SIGNUP_REQUEST->{
                //회원가입 리절트
                mAuth = FirebaseAuth.getInstance()
                updateUI(mAuth.currentUser)
            }
            else->{
                //페북 로그인/가입 리절트
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}