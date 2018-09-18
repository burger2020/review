package condom.best.condom.View.BottomNavPage.MyPage

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_INFO
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.View.Data.StringData.Companion.RATING
import condom.best.condom.View.Data.StringData.Companion.REVIEW
import condom.best.condom.View.Data.StringData.Companion.WISH
import condom.best.condom.View.Data.UserDataList
import condom.best.condom.View.BottomNavPage.Adapter.ActListAdapter
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BottomNavPage.Product.ProductReviewFragment
import condom.best.condom.View.MainActivity
import condom.best.condom.View.MainActivity.Companion.db
import kotlinx.android.synthetic.main.fragment_act_more.view.*


class   ActMoreFragment : Fragment(), ActMoreContract.View{

    val DIVIDER = "DIVIDER"

    lateinit var divider : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            divider = it.getString(DIVIDER)
        }
    }

    private lateinit var actListAdapter : ActListAdapter
    lateinit var rootView : View
    private lateinit var presenter : ActMorePresenter
    private var ratingDataList = UserDataList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
         rootView = inflater.inflate(R.layout.fragment_act_more, container, false)

        actListAdapter = ActListAdapter(rootView.context, ratingDataList,divider)

        presenter = ActMorePresenter().apply {
            view = this@ActMoreFragment
            adapterModel = actListAdapter //인터페이스 넘겨주기
        }

        rootView.actList.let {
            it.adapter = actListAdapter
            it.layoutManager = GridLayoutManager(context, 2)
        }

        //스피너 세팅
        val alineList = when (divider) {
            RATING ->{
                rootView.toolbarTitle.text = getString(R.string.ratingList)
                rootView.resources.getStringArray(R.array.ratingListSortArray)
            }
            WISH -> {
                rootView.toolbarTitle.text = getString(R.string.wishList)
                rootView.resources.getStringArray(R.array.wishListSortArray)
            }
            else -> {
                rootView.toolbarTitle.text = getString(R.string.reviewList)
                rootView.resources.getStringArray(R.array.reviewListSortArray)
            }
        }
        val adapter = ArrayAdapter<String>(context, R.layout.spinner_item, alineList)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        rootView.ratingListSortSpinner.adapter = adapter
        rootView.ratingListSortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, i: Int, p3: Long) {
                val ratingList = presenter.getSortData(divider,i)//정렬 데이터
                if(ratingList !=null) // 로컬에서 가져온 데이터 세팅
                    actDataSetting(ratingList)
            }override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        return rootView
    }
    fun setServerRatingList(ratingList: UserDataList){ //서버에서 가져온 데이터 세팅
        actDataSetting(ratingList)
    }
    fun actDataSetting(ratingList : UserDataList){
        when(divider){
            RATING->{
                ratingDataList.userRatingData.clear()
                ratingDataList.userRatingData.addAll(ratingList.userRatingData)
            }
            WISH->{
                ratingDataList.userWishData.clear()
                ratingDataList.userWishData.addAll(ratingList.userWishData)
            }
            REVIEW->{
                ratingDataList.userReviewData.clear()
                ratingDataList.userReviewData.addAll(ratingList.userReviewData)
            }
        }
        actListAdapter.notifyDataSetChanged()
        if(actListAdapter.itemCount == 0) {
            rootView.emptyView.visibility = View.VISIBLE
            rootView.actList.visibility = View.GONE
        }else{
            rootView.emptyView.visibility = View.GONE
            rootView.actList.visibility = View.VISIBLE
        }
    }
    override fun adapterItemClick(prodName: String) {
        db.collection(PRODUCT_INFO).document(prodName).get()
                .addOnSuccessListener{ it ->
                    //제품 데이터 가져오기 / 리사이클러뷰 갱신
                    try {
                        val prodData = it.toObject(ProductInfo::class.java)
                        (activity as MainActivity).productReviewFragment = ProductReviewFragment.prodData(prodData!! )
                        FragmentUtil.fragmentAddChanger((activity as MainActivity),(activity as MainActivity).productReviewFragment,"MY")
                    }catch (e:KotlinNullPointerException){}
                }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
                ActMoreFragment().apply {
                    arguments = Bundle().apply {
                        putString(DIVIDER, param1)
                    }
                }
    }
}
