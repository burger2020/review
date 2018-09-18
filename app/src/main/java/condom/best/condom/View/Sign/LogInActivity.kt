package condom.best.condom.View.Sign

import android.annotation.SuppressLint
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import condom.best.condom.View.LogInConst
import condom.best.condom.View.Data.StringData
import condom.best.condom.R
import condom.best.condom.View.MainActivity
import condom.best.condom.databinding.ActivityLogInBinding


//뷰
@SuppressLint("Registered")
class LogInActivity : AppCompatActivity() , LogInConst {
    override fun startSignIn() = startActivityForResult(Intent(this, SignInActivity::class.java), SIGNUP_REQUEST)
    override fun startSignUp() = startActivityForResult(Intent(this, SignUpActivity::class.java), SIGNUP_REQUEST)
    override fun finishLogIn(user: FirebaseUser) {
        MainActivity.localDataPut.putString(user.uid+getString(R.string.firstUser), StringData.NON_SETTING).apply()
        finish()
    }
    private lateinit var loginViewModel : LogInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding = DataBindingUtil.setContentView<ActivityLogInBinding>(this,R.layout.activity_log_in)
        loginViewModel = LogInViewModel(this, this)
        dataBinding.login = loginViewModel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            SIGNIN_REQUEST ->{
                //로그인 리절트
                if(resultCode==123) startActivityForResult(Intent(this, SignUpActivity::class.java), SIGNUP_REQUEST)
                else loginViewModel.signIn()
            }
            SIGNUP_REQUEST -> loginViewModel.signIn() //회원가입 리절트
            else -> loginViewModel.facebookCallback(requestCode, resultCode, data) //페북 로그인/가입 리절트
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val SIGNIN_REQUEST = 1002
        private const val SIGNUP_REQUEST = 1003
    }

}