package condom.best.condom.BottomNavPage

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import condom.best.condom.BottomNavPage.Adapter.ProductListAdapter
import condom.best.condom.BottomNavPage.Product.ProductReviewFragment
import condom.best.condom.Data.FirebaseConst.Companion.PRODUCT_INFO
import condom.best.condom.Data.ProductInfo
import condom.best.condom.MainActivity
import condom.best.condom.R
import kotlinx.android.synthetic.main.fragment_home.view.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private val db = FirebaseFirestore.getInstance()

    private var pList = arrayListOf<ProductInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)


        view.addProduct.setOnClickListener {
            startActivity(Intent(context,AddProduct::class.java))
        }

        view.searchProd.setOnClickListener {
            if((activity as MainActivity).searchFragment.isAdded){
//                    actionBar.visibility = View.GONE
                (activity as MainActivity).supportFragmentManager.beginTransaction().hide((activity as MainActivity).currentFragment).show((activity as MainActivity).searchFragment).commit()
                (activity as MainActivity).currentFragment = (activity as MainActivity).searchFragment
            }
        }


        if(pList.size<=0)
            db.collection(PRODUCT_INFO)
                    .orderBy("prodCompany", Query.Direction.DESCENDING) //내림차순(prodCompany)
                    .get()
                    .addOnCompleteListener {
                        //제품 데이터 가져오기 / 리사이클러뷰 갱신
                        try {
                            it.result.mapTo(pList) { (it.toObject(ProductInfo::class.java)) }
                            val pListAdapter = ProductListAdapter(context!!,pList)
                            view.productList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            view.productList.adapter = pListAdapter
                        }catch (e:KotlinNullPointerException){}

                    }
        ProductListAdapter.productClicked(object : ProductListAdapter.InterfaceClickProd{
            override fun interfaceClickProd(productData: ProductInfo) {
//                    actionBar.visibility = View.GONE
                (activity as MainActivity).productReviewFragment = ProductReviewFragment.prodData(productData)
                (activity as MainActivity).supportFragmentManager.beginTransaction()
                        .hide((activity as MainActivity).currentFragment).add(R.id.bottomNavPageContainerHome,(activity as MainActivity).productReviewFragment).commit()
                (activity as MainActivity).currentFragment = (activity as MainActivity).productReviewFragment
            }
        })
        return view
    }
}
