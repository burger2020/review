package condom.best.condom.View.BottomNavPage.Product.SearchResult

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BottomNavPage.Adapter.ProductListAdapter
import condom.best.condom.View.BottomNavPage.Product.ProductReviewFragment
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.View.Data.StringData.Companion.HOME
import condom.best.condom.View.MainActivity
import kotlinx.android.synthetic.main.fragment_search_result.view.*

private const val ARG_PARAM1 = "param1"

class SearchResultFragment : Fragment(), SearchResultContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchText = it.getString(ARG_PARAM1)
        }
    }

    private lateinit var searchText: String
    private lateinit var presenter : SearchResultPresenter
    private lateinit var rootView : View
    private var prodList = arrayListOf<ProductInfo>()
    private lateinit var adapter : ProductListAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_result, container, false)

        presenter = SearchResultPresenter().apply {
            view = this@SearchResultFragment
            init(searchText)
        }
        rootView.searchText.setText(searchText) //검색착 검색 단어 입력
        rootView.searchText.clearFocus() // 검색창     포커스 해제
        rootView.searchText.setOnFocusChangeListener { _, hasFocus -> // 검색창 포커스시 검색 페이지로
            if(hasFocus)
                FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).searchFragment)
        }
        // 제품리스트
        adapter = ProductListAdapter(rootView.context,prodList,1) //divider 1
        rootView.searchProductList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rootView.searchProductList.adapter = adapter


        ProductListAdapter.productClicked(object : ProductListAdapter.InterfaceClickProdTag{ //제품 클릭 -> 제품 리뷰 페이지
            override fun interfaceClickProdTag(productData: ProductInfo) {
                (activity as MainActivity).productReviewFragment = ProductReviewFragment.prodData(productData)
                FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).productReviewFragment, HOME)
            }
        })
        return rootView
    }
    private fun emptyViewSetting(size : Int) { //엠티뷰 셋
        if(size == 0) {
            rootView.searchProductList.visibility = View.GONE
            rootView.emptyView.visibility = View.VISIBLE
        } else {
            rootView.searchProductList.visibility = View.VISIBLE
            rootView.emptyView.visibility = View.GONE
        }
        rootView.progressBar.visibility = View. GONE
    }
    fun prodListSetting(searchData: ArrayList<ProductInfo>) { // 제품 리스트 데이터 셋
        prodList.clear()
        prodList.addAll(searchData)
        emptyViewSetting(prodList.size)
        adapter.notifyDataSetChanged()
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
                SearchResultFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }
}
