package condom.best.condom.View.Sign

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import condom.best.condom.R
import condom.best.condom.databinding.ActivityPasswordBinding

class PasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityPasswordBinding>(this,R.layout.activity_password)
        val pass = PasswordViewModel(this)
        binding.pass = pass
    }
}
