package condom.best.condom.View.BottomNavPage.TagSearch

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import condom.best.condom.View.BottomNavPage.Adapter.ProductTagListAdapter
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BottomNavPage.Adapter.ProductListAdapter
import condom.best.condom.View.BottomNavPage.Product.ProductReviewFragment
import condom.best.condom.View.MainActivity
import kotlinx.android.synthetic.main.fragment_tag_search.view.*

private const val ARG_PARAM1 = "param1"

class TagSearchFragment : Fragment(), TagSearchContract.View{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tagList = it.getStringArrayList(ARG_PARAM1)
        }
    }

    private var tagList: ArrayList<String> = arrayListOf()
    private lateinit var presenter : TagSearchPresenter
    private lateinit var rootView : View
    private var prodList = arrayListOf<ProductInfo>()
    private lateinit var adapter : ProductListAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_tag_search, container, false)

        presenter = TagSearchPresenter().apply {
            view = this@TagSearchFragment
            init(tagList)
        }

        // 제품리스트
        adapter = ProductListAdapter(rootView.context,prodList,1) //divider 1
        rootView.productList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rootView.productList.adapter = adapter


        ProductListAdapter.productClicked(object : ProductListAdapter.InterfaceClickProdTag{ //제품 클릭 -> 제품 리뷰 페이지
            override fun interfaceClickProdTag(productData: ProductInfo) {
                (activity as MainActivity).productReviewFragment = ProductReviewFragment.prodData(productData)
                FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).productReviewFragment,"TAG")
            }
        })

        //태그리스트
        val prodTag = ProductTagListAdapter(rootView.context,tagList,1)// tag size large
        rootView.productTagList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rootView.productTagList.adapter = prodTag

        return rootView
    }
    private fun emptyViewSetting(size : Int){ //엠티뷰 셋
        if(size == 0){
            rootView.productList.visibility = View.GONE
            rootView.emptyView.visibility = View.VISIBLE
        }else{
            rootView.productList.visibility = View.VISIBLE
            rootView.emptyView.visibility = View.GONE
        }
        rootView.progressBar.visibility = View.GONE
    }
    fun prodListSetting(searchData: ArrayList<ProductInfo>) { // 제품 리스트 데이터 셋
        prodList.clear()
        prodList.addAll(searchData)
        emptyViewSetting(prodList.size)
        adapter.notifyDataSetChanged()
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: ArrayList<String>) =
                TagSearchFragment().apply {
                    arguments = Bundle().apply {
                        putStringArrayList(ARG_PARAM1, param1)
                    }
                }
    }
}
