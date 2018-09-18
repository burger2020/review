package condom.best.condom.BottomNavPage.Product

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.R
import kotlinx.android.synthetic.main.fragment_product_info_more.view.*

class ProductInfoMore : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productData = it.getSerializable(productInfo) as ProductInfo
        }
    }

    private val productInfo = "productInfo"

    private val db = FirebaseFirestore.getInstance()

    private lateinit var productData : ProductInfo


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_product_info_more, container, false)

        rootView.pMoreName.text = productData.prodName
        rootView.pMoreCompany.text = productData.prodCompany
        rootView.pMoreCountry.text = productData.prodName
        rootView.pMoreTag.text = productData.prodTag.toString()
        rootView.pMoreUnit.text = productData.sellUnit.toString()
        rootView.pMoreFeature.text = productData.prodFeature
        rootView.pMoreIngredient.text = productData.prodIngredient

        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance(productData: ProductInfo) =
                ProductInfoMore().apply {
                    arguments = Bundle().apply {
                        putSerializable(productInfo, productData)
                    }
                }
    }
}
