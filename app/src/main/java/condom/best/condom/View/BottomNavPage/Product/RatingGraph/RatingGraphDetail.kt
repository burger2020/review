package condom.best.condom.View.BottomNavPage.Product.RatingGraph

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import condom.best.condom.R
import condom.best.condom.View.Data.*
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_RATING
import condom.best.condom.View.MainActivity.Companion.db
import kotlinx.android.synthetic.main.fragment_rating_graph_detail.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class RatingGraphDetail : Fragment() {
    private var allUserRatingData : ProductRating? = null
    private var productInfo : ProductInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            allUserRatingData = it.getSerializable(ARG_PARAM1) as ProductRating
            productInfo = it.getSerializable(ARG_PARAM2) as ProductInfo
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_rating_graph_detail, container, false)

        var maleDetailRating = MailDetailRating()

        rootView.progressBar.visibility = View.VISIBLE

        db.collection(PRODUCT_RATING)
                .document(productInfo!!.prodName)
                .get()
                .addOnCompleteListener { //제품 평가 분포도
                    try {
                        maleDetailRating = it.result.toObject(MailDetailRating::class.java)!!
                    }catch (e:KotlinNullPointerException){ }
                    finally {
                        val maleRating = ProductRating()
                        val femaleRating = ProductRating()
                        for(i in 0 until allUserRatingData!!.ratingData.size){
                            maleRating.ratingData[i] = maleDetailRating.mailRatingData[i]
                            femaleRating.ratingData[i] = allUserRatingData!!.ratingData[i] - maleRating.ratingData[i]
                            maleRating.noneZero()
                            femaleRating.noneZero()
                        }
                        ratingSetting(rootView.maleRating,maleRating)
                        ratingSetting(rootView.femaleRating,femaleRating)
                        graphSetting(rootView.maleGraph,maleRating)
                        graphSetting(rootView.femaleGraph,femaleRating)
                        rootView.progressBar.visibility = View.GONE
                    }
                }

        graphSetting(rootView.allUserGraph,allUserRatingData)
        ratingSetting(rootView.allUserRating,allUserRatingData)

        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: ProductRating, param2: ProductInfo) =
                RatingGraphDetail().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PARAM1, param1)
                        putSerializable(ARG_PARAM2, param2)
                    }
                }
    }

    @SuppressLint("SetTextI18n")
    private fun ratingSetting(ratingTextView: TextView, ratingArray: ProductRating?){
        var sum = 0f // 점수
        var num = 0 // 사람수
        for(i in 0 until ratingArray!!.ratingData.size){
            num += ratingArray.ratingData[i]
            sum += (i+1)/2 * ratingArray.ratingData[i]
        }
        var point = if(num==0)
            0F
        else
            sum/num.toFloat()
        if(point>=5)
            point = 5f
        else if(point<=0)
            point = 0f
        val strNumber = String.format("%.2f", point)

        ratingTextView.text = "평점$strNumber(${num}명)"
    }

    private fun graphSetting(graph: BarChart, ratingArray: ProductRating?) { // 별점 그래프 옵션 설정
        graph.apply {
            legend.isEnabled = false
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description?.isEnabled = false
            setMaxVisibleValueCount(60)
            setPinchZoom(false)
            setScaleEnabled(false)
            isDoubleTapToZoomEnabled = false
            setDrawGridBackground(false)
            axisRight?.isEnabled = false
            axisLeft?.isEnabled = false //왼쪽 라벨 안씀
            isHighlightPerTapEnabled = false
            isHighlightPerDragEnabled = false // 드래그시 하이라이트
        }

        val charTextSize = 10f
        val xAxisFormatter = DayAxisValueFormatter()
        val xAxis = graph.xAxis
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(false)
            granularity = 1f
            textColor = Color.GRAY
            textSize = charTextSize
            setDrawLabels(true)
            labelCount = 10
            valueFormatter = xAxisFormatter
        }


        val YAxisL  = graph.axisLeft //y축 좌측
        YAxisL.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setDrawLabels(false)
        }
        val YAxisR  = graph.axisRight //y축 우측
        YAxisR.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setDrawLabels(false)
        }
        graph.setFitBars(true)
        graph.animateY(0) // 그래프 에니메이션 시간 (안하면 바로 안뜸)
        graph.visibility = View.VISIBLE
//        rootView.graphProgressBar.visibility = View.GONE
        setData(10,graph,charTextSize, ratingArray)
    }

    private fun setData(count: Int, graph: BarChart, charTextSize: Float, ratingArray: ProductRating?) {

        val yVals1 = java.util.ArrayList<BarEntry>()
        var max = 0
        ratingArray!!.ratingData.forEach {//최대 값
            if(it>max)
                max = it
        }
        for( i in 0 until count){
            if(ratingArray.ratingData[i] == 0)
                yVals1.add(BarEntry(i.toFloat(), max.toFloat()/50))
            else
                yVals1.add(BarEntry(i.toFloat(), ratingArray.ratingData[i].toFloat()))
        }

        val set1: BarDataSet

        set1 = BarDataSet(yVals1,null)
        set1.valueFormatter = ValueFormatter()
        set1.setDrawIcons(false)
        set1.setColors(Color.argb(255,0x52,0xb3,0xd9)) // 그래프 바 색
        set1.valueTextColor = Color.WHITE

        val dataSets = java.util.ArrayList<IBarDataSet>()
        dataSets.add(set1)

        val data = BarData(dataSets)
        data.setValueTextSize(charTextSize)
        data.barWidth = 0.8f

        graph.data = data
    }
}
