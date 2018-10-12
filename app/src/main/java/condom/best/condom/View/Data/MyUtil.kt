package condom.best.condom.View.Data

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import condom.best.condom.View.Data.UserLocalDataPath.Companion.userProductActPath
import condom.best.condom.View.MainActivity
import condom.best.condom.View.MainActivity.Companion.gson
import java.text.DecimalFormat

class MyUtil {
    fun userRatingDataLocalSave(userRatingData: UserRatingData,productName : String){
        val strContact = gson.toJson(userRatingData, UserRatingData::class.java)
        MainActivity.localDataPut.putString(userProductActPath(productName),strContact)//유저 제품 기록 전체 데이터
        val strContact2 = gson.toJson(MainActivity.userActInfo, UserActInfo::class.java)
        MainActivity.localDataPut.putString(UserLocalDataPath.USER_ACT_INFO_PATH,strContact2)//유저 활동 기록
        MainActivity.localDataPut.commit()
    }
}

class DayAxisValueFormatter : IAxisValueFormatter {
    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        //        하단 단위
        return if(value.toInt()%2==1) ((value+1) /2).toInt().toString()
        else ""
    }
}

class ValueFormatter : IValueFormatter{
    private var mFormat : DecimalFormat = DecimalFormat("###,###.#")
    override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?) = mFormat.format(value)!!
}
