package condom.best.condom.BottomNavPage.Product

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import condom.best.condom.R
import condom.best.condom.View.BottomNavPage.Adapter.ProductReviewListAdapter
import condom.best.condom.View.Data.*
import condom.best.condom.View.Data.FirebaseConst.Companion.COMMENT
import condom.best.condom.View.Data.FirebaseConst.Companion.COMMENT_LIKE
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_REVIEWS
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.View.MainActivity.Companion.currentUser
import kotlinx.android.synthetic.main.fragment_review_more.view.*


class ReviewMoreFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productData = it.getSerializable(productInfo) as ProductInfo
        }
    }

    private val productInfo = "productInfo"

    private val db = FirebaseFirestore.getInstance()
    private lateinit var productData : ProductInfo
    private val reviewList = ProductReviewData_Like()

    private lateinit var reviewListAdapter : ProductReviewListAdapter
    private lateinit var rootView : View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_review_more, container, false)

        reviewListAdapter = ProductReviewListAdapter(context!!,reviewList, ReviewCommentList_Like(),1)

        //정렬 스피너 셋
        val alineList = rootView.resources.getStringArray(R.array.spinnerArray)
        val adapter = ArrayAdapter<String>(context, R.layout.spinner_item, alineList)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        rootView.reviewAlineSpinner.adapter = adapter
        rootView.reviewAlineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, i: Int, p3: Long) {
                setReviewData(i)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        return rootView
    }
    private fun setReviewData(aline : Int){
        //배댓 가져오기
        val dataAline = when (aline) {
            0 -> db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).orderBy("likeNum", Query.Direction.DESCENDING).limit(20)
            1 -> db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).orderBy("date", Query.Direction.DESCENDING).limit(20)
            else -> db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).orderBy("date", Query.Direction.ASCENDING).limit(20)
        }
        dataAline.get().addOnCompleteListener { it ->
            var adapterSetBool = false
            try {
                reviewList.productReviewData.clear()
                it.result.mapTo(reviewList.productReviewData) {
                    it.toObject(ProductReviewData::class.java)
                }
                for(i in 0 until reviewList.productReviewData.size){
                    db.collection(USER_INFO).document(reviewList.productReviewData[i].userUid).get().addOnCompleteListener {
                        //배댓 유저 데이터
                        try {
                            reviewList.reviewerInfo.add(it.result.toObject(UserInfo::class.java)!!)
                            //마지막 데이터 다들고오면 어뎁터 실행
                            if (i == reviewList.productReviewData.size - 1) {
                                if(!adapterSetBool)
                                    adapterSetBool = true
                                else {
                                    rootView.AllReviewList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                                    rootView.AllReviewList.adapter = reviewListAdapter
                                }
                            }
                        }catch (e:KotlinNullPointerException){}
                    }
                    //배댓 내가한 좋아요 셋
                    db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(reviewList.productReviewData[i].userUid).collection(COMMENT_LIKE)
                            .document(currentUser!!.uid).get().addOnCompleteListener {
                                //배댓 각각 유저 데이터
                                try {
                                    reviewList.reviewLike.add(it.result.toObject(ReviewLike::class.java)!!)
                                }catch (e:KotlinNullPointerException){
                                    reviewList.reviewLike.add(ReviewLike(false))
                                }
                                if (i == reviewList.productReviewData.size - 1) {
                                    if(!adapterSetBool)
                                        adapterSetBool = true
                                    else {
                                        rootView.AllReviewList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                                        rootView.AllReviewList.adapter = reviewListAdapter
                                    }
                                }
                            }
                }
            }catch (e:KotlinNullPointerException){}
        }
    }
    companion object {
        @JvmStatic
        fun newInstance(productData: ProductInfo) =
                ReviewMoreFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(productInfo, productData)
                    }
                }
    }
}
