package condom.best.condom.BottomNavPage.Product

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import condom.best.condom.Data.StringData.Companion.RATING_POINT
import condom.best.condom.Data.StringData.Companion.REVIEW
import condom.best.condom.Data.StringData.Companion.USER_COMMENT
import condom.best.condom.R
import kotlinx.android.synthetic.main.activity_review.*


class ReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        val intent = intent
        val comment = intent.getStringExtra(USER_COMMENT)
        val ratingPoint = intent.getFloatExtra(RATING_POINT,0f)
        //코멘트 수정이면 버튼 이네이블, 텍스트 넣기
        if(comment != null) {
            reviewText.setText(comment)
            reviewSuccess.background = resources.getDrawable(R.drawable.ic_success_ok)
            reviewSuccess.isEnabled = true
        }
        reviewRatingBar.rating = ratingPoint
        reviewText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                //입력 하면 완료버튾 활성화
                if(p0!!.isNotEmpty()&&SpaceCheck(p0.toString())){
                    reviewSuccess.background = resources.getDrawable(R.drawable.ic_success_ok)
                    reviewSuccess.isEnabled = true
                }
                else {
                    reviewSuccess.isEnabled = false
                    reviewSuccess.background = resources.getDrawable(R.drawable.ic_success)
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
        searchBackBtn.setOnClickListener {
            finish()
        }
        //리뷰 달기
        reviewSuccess.setOnClickListener {
            val intent = intent
            intent.putExtra(REVIEW,reviewText.text.toString())
            intent.putExtra(RATING_POINT,reviewRatingBar.rating)
            setResult(123,intent)
            finish()
        }
    }
    private fun SpaceCheck(s : String) : Boolean{
        for (i in 0 until s.length)
            if(s[i]!=' ' && s[i]!='\n')
                return true
        return false
    }
}
