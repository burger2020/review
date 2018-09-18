package condom.best.condom.View

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import condom.best.condom.View.Data.StringData
import condom.best.condom.View.Data.StringData.Companion.ALL_SETTING
import condom.best.condom.View.Data.StringData.Companion.ENTER_SETTING
import condom.best.condom.View.Data.StringData.Companion.FIRST_SETTING
import condom.best.condom.View.Data.StringData.Companion.NEW_SETTING
import condom.best.condom.View.Data.StringData.Companion.NON_SETTING
import condom.best.condom.View.Data.StringData.Companion.SECOND_SETTING
import condom.best.condom.View.Data.StringData.Companion.USER_TASTE
import condom.best.condom.R
import condom.best.condom.View.MainActivity.Companion.localDataGet
import condom.best.condom.View.MainActivity.Companion.localDataPut
import kotlinx.android.synthetic.main.actionbar_activity_info.view.*
import kotlinx.android.synthetic.main.activity_second_setting.*

@Suppress("DEPRECATION")
class SecondSettingActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_setting)

        val intent = intent
        val route = intent.getStringExtra(StringData.ROUTE)

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        supportActionBar!!.setCustomView(R.layout.actionbar_activity_info)
        supportActionBar!!.elevation = 10F
        supportActionBar!!.setBackgroundDrawable(resources.getDrawable(R.color.white))

        val actionBar = supportActionBar!!.customView

        actionBar.action_bar_text.text = getString(R.string.ignore)
        actionBar.action_bar_back_key.visibility = View.GONE
        actionBar.action_bar_text.setOnClickListener {
            //다음에하기 옵션
            finish()
        }

        val rangeArray = Array(7){0}

        rangeBar1.setRangePinsByValue(0F, 0F)
        rangeBar2.setRangePinsByValue(0F, 0F)
        rangeBar3.setRangePinsByValue(0F, 0F)
        rangeBar4.setRangePinsByValue(0F, 0F)
        rangeBar5.setRangePinsByValue(0F, 0F)
        rangeBar6.setRangePinsByValue(0F, 0F)
        rangeBar7.setRangePinsByValue(0F, 0F)

        rangeBar1.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            rangeChange(rangeArray,0,rangeText1,rightPinValue.toInt())
        }
        rangeBar2.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            rangeChange(rangeArray,1,rangeText2,rightPinValue.toInt())
        }
        rangeBar3.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            rangeChange(rangeArray,2,rangeText3,rightPinValue.toInt())
        }
        rangeBar4.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            rangeChange(rangeArray,3,rangeText4,rightPinValue.toInt())
        }
        rangeBar5.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            rangeChange(rangeArray,4,rangeText5,rightPinValue.toInt())
        }
        rangeBar6.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            rangeChange(rangeArray,5,rangeText6,rightPinValue.toInt())
        }
        rangeBar7.setOnRangeBarChangeListener { _, _, _, _, rightPinValue ->
            rangeChange(rangeArray,6,rangeText7,rightPinValue.toInt())
        }

        successButton.setOnClickListener {
            //완료 버튼
            val state = localDataGet.getString(MainActivity.currentUser!!.uid+getString(R.string.firstUser),NON_SETTING)
            //취향, 정보 입력 현황 데이터 저장
            if(state == ENTER_SETTING)
                localDataPut.putString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), SECOND_SETTING)
            else if(state == FIRST_SETTING)
                localDataPut.putString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), ALL_SETTING)
            localDataPut.commit()

            intent.putExtra(USER_TASTE,rangeArray)

            if(route == NEW_SETTING){
                //처음 가입시 들어와서 세팅 완료
                setResult(1001,intent)
                finish()
            }else{
                //차후 변경으로

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun rangeChange(rangeArray: Array<Int>, idx: Int, rangeText: TextView, pinValue:Int){
        rangeText.text = " = $pinValue"
        rangeArray[idx] = pinValue
        var sum = 0
        for(i in 0 until rangeArray.size)
            sum += rangeArray[i]
        totalPoint.text = "$sum / 100"
        when {
            sum==100 -> {
                totalPoint.setTextColor(resources.getColor(R.color.colorPrimary))
                totalPoint.setTypeface(null,Typeface.BOLD)
                successButton.isEnabled = true
                successButton.background = resources.getDrawable(R.drawable.sign_up_success_button)
            }
            sum>100 -> {
                totalPoint.setTextColor(resources.getColor(R.color.red))
                totalPoint.setTypeface(null,Typeface.BOLD)
                successButton.isEnabled = false
                successButton.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
            }
            else -> {
                totalPoint.setTextColor(resources.getColor(R.color.gray5))
                totalPoint.setTypeface(null,Typeface.NORMAL)
                successButton.isEnabled = false
                successButton.background = resources.getDrawable(R.drawable.sign_up_success_non_button)
            }
        }
    }

    override fun onBackPressed() {}

    private fun rangeBarActionUp(rangeArray: Array<Int>, beforeRangeArray: Array<Int>, idx:Int, rangeBar: com.appyvet.materialrangebar.RangeBar){
        rangeArray[idx] = rangeBar.rightPinValue.toInt()
        if(!rangeCheck(rangeArray)){
            val mHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    rangeBar.setRangePinsByValue(0f,beforeRangeArray[idx].toFloat())
                }
            }
            mHandler.sendEmptyMessageDelayed(0, 100)
        }
        beforeRangeArray[idx] = rangeArray[idx]
    }
    private fun rangeCheck(rangeArray: Array<Int>):Boolean{
        var sum = 0
        for(i in 0 until rangeArray.size)
            sum += rangeArray[i]

        return sum < 50
    }
}
