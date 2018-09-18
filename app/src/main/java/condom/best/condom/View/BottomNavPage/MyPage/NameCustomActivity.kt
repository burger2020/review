package condom.best.condom.View.BottomNavPage.MyPage

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import condom.best.condom.R
import kotlinx.android.synthetic.main.activity_name_custom.*

class NameCustomActivity : AppCompatActivity(), NameCustomContract.View {
    private lateinit var presenter : NameCustomPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_custom)

        presenter = NameCustomPresenter().apply {
            view = this@NameCustomActivity
        }

        nameCustomBackBtn.setOnClickListener {
            finish()
        }
        nameCustomSuccessBtn.setOnClickListener {
            val intent = intent
            intent.putExtra("customName",customNameText.text.toString())
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
        customNameText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                if(p0!!.length>=2)
                    nameCustomSuccessBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_success_ok))
                else
                    nameCustomSuccessBtn.setImageDrawable(resources.getDrawable(R.drawable.ic_success))
            }
        })
    }
}
