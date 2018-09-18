package condom.best.condom.View.Sign

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModel
import android.content.Intent
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import condom.best.condom.View.LogInConst
import dmax.dialog.SpotsDialog
import java.util.*

//뷰모델
class LogInViewModel(@SuppressLint("StaticFieldLeak") val context: Activity, private val logInConst : LogInConst) : ViewModel() {
    private lateinit var mAuth: FirebaseAuth

    private var callbackManager : CallbackManager = CallbackManager.Factory.create()

    init {
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
    }

    private val dialog= SpotsDialog.Builder()
            .setContext(context)
            .setMessage("로그인 중 입니다.")
            .build()

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(context) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        dialog.dismiss()
                        logInConst.finishLogIn(user!!)
                    } else {//null
                    }
                }
    }

    private fun updateUI(user : FirebaseUser?){
        if(user != null){
            context.finish()
        }else{}
    }

    fun facebookLoginClick() { //페이스북 로그인 접근 권한 받기
        dialog.show()
        LoginManager.getInstance().logInWithReadPermissions(context, Arrays.asList("email"))
    }
    //로그인
    fun signInClick() = logInConst.startSignIn()
    //회원가입
    fun signUpClick() = logInConst.startSignUp()

    fun signIn(){
        mAuth = FirebaseAuth.getInstance()
        updateUI(mAuth.currentUser)
    }

    fun facebookCallback(requestCode: Int, resultCode: Int, data: Intent?) = callbackManager.onActivityResult(requestCode, resultCode, data)
}