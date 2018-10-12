package condom.best.condom.View.Sign

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
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
import condom.best.condom.R
import condom.best.condom.View.Data.StringData
import condom.best.condom.View.MainActivity
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_log_in.*
import java.util.*

@SuppressLint("Registered")
class LogInActivity : AppCompatActivity(){
    private lateinit var mAuth: FirebaseAuth
    private var callbackManager : CallbackManager = CallbackManager.Factory.create()

    private lateinit var dialog : android.app.AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        dialog = SpotsDialog.Builder()
                .setContext(this)
                .setMessage("로그인 중 입니다.")
                .build()

        mAuth = FirebaseAuth.getInstance()

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
        facebookSignInBtn.setOnClickListener { facebookLoginClick() }
        signUpButton.setOnClickListener { startSignUp() }
        signInBtn.setOnClickListener { startSignIn() }
    }

    private fun startSignIn() = startActivityForResult(Intent(this, SignInActivity::class.java), SIGNUP_REQUEST) //이메일로 로그인
    private fun startSignUp() = startActivityForResult(Intent(this, SignUpActivity::class.java), SIGNUP_REQUEST)//이메일로 회원가입
    private fun finishLogIn(user: FirebaseUser) {
        MainActivity.localDataPut.putString(user.uid+getString(R.string.firstUser), StringData.NON_SETTING).apply()
        finish()
    }
    private fun facebookCallback(requestCode: Int, resultCode: Int, data: Intent?) = callbackManager.onActivityResult(requestCode, resultCode, data)
    private fun facebookLoginClick() { //페이스북 로그인 접근 권한 받기
        dialog.show()
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"))
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        dialog.dismiss()
                        finishLogIn(user!!)
                    } else {//null
                    }
                }
    }

    private fun updateUI(user : FirebaseUser?){
        if(user != null){
            finish()
        }else{}
    }

    private fun signIn(){
        mAuth = FirebaseAuth.getInstance()
        updateUI(mAuth.currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            SIGNIN_REQUEST ->{
                //로그인 리절트
                if(resultCode==123) startActivityForResult(Intent(this, SignUpActivity::class.java), SIGNUP_REQUEST)
                else signIn()
            }
            SIGNUP_REQUEST -> signIn() //회원가입 리절트
            else -> facebookCallback(requestCode, resultCode, data) //페북 로그인/가입 리절트
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val SIGNIN_REQUEST = 1002
        private const val SIGNUP_REQUEST = 1003
    }

}