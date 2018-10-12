package condom.best.condom.View.BottomNavPage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BottomNavPage.Product.ProductReviewFragment
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_INFO
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.R
import condom.best.condom.View.BottomNavPage.Adapter.ProductListAdapter
import condom.best.condom.View.MainActivity
import kotlinx.android.synthetic.main.fragment_home.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val db = FirebaseFirestore.getInstance()
    companion object {
        var pList = arrayListOf<ProductInfo>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var pListAdapter : ProductListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        Log.d("adsdafasdf","adsdafasdf")

        pListAdapter = ProductListAdapter(context!!,pList,0)// divider 0

        view.addProduct.setOnClickListener {// 테스트용
            startActivity(Intent(context, AddProduct::class.java))
        }

        view.searchProd.setOnClickListener {// 검색창 넘어가기
            FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).searchFragment)
            //키보드 바로 올라오게
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }

        if(pList.size<=0)
            db.collection(PRODUCT_INFO)
                    .orderBy("prodCompany", Query.Direction.DESCENDING) //내림차순(prodCompany)
                    .get()
                    .addOnCompleteListener { it ->
                        //제품 데이터 가져오기 / 리사이클러뷰 갱신
                        try {
                            it.result.mapTo(pList) { (it.toObject(ProductInfo::class.java)) }
                            view.productList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            view.productList.adapter = pListAdapter
                        }catch (e:KotlinNullPointerException){}
                    }
        else{
            view.productList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            view.productList.adapter = pListAdapter
        }
        ProductListAdapter.productClicked(object : ProductListAdapter.InterfaceClickProd{
            override fun interfaceClickProd(productData: ProductInfo) {
                (activity as MainActivity).productReviewFragment = ProductReviewFragment.prodData(productData)
                FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).productReviewFragment,"HOME")
            }
        })
        return view
    }

}