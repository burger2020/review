package condom.best.condom.View.Sign

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import condom.best.condom.R
import condom.best.condom.databinding.ActivitySignUpBinding


class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivitySignUpBinding>(this,R.layout.activity_sign_up)

        val mAuth = FirebaseAuth.getInstance()

        val signUpViewModel = SignUpViewModel(this, mAuth)
        binding.signUp = signUpViewModel
    }
}
