package condom.best.condom.View.Sign

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import condom.best.condom.R
import condom.best.condom.databinding.ActivitySignInBinding


class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivitySignInBinding>(this,R.layout.activity_sign_in)
        val signInViewModel = SignInViewModel(this)
        binding.signIn = signInViewModel
    }
}
