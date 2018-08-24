package condom.best.condom.BottomNavPage.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.storage.FirebaseStorage
import condom.best.condom.Data.GlideApp
import condom.best.condom.Data.ProductInfo
import condom.best.condom.Data.StringData.Companion.COMPANY_CODE
import condom.best.condom.R
import kotlinx.android.synthetic.main.adapter_home_list.view.*

class ProductListAdapter(val context:Context,private val pList:ArrayList<ProductInfo>) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = inflater.inflate(R.layout.adapter_home_list,parent,false)
        return ViewHolder(mainView)
    }

    override fun getItemCount(): Int = pList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindHolder(context,pList,position)
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val storage = FirebaseStorage.getInstance("gs://condom-55a91")
        private val storageRef = storage.reference

        private val pName = itemView.productName
        private val pImage = itemView.productImage
        private val pPrice = itemView.productPrice
        private val pCompany = itemView.productCompany
        private val productContainer = itemView.productContainer
        @SuppressLint("SetTextI18n")
        fun bindHolder(context: Context, pList: ArrayList<ProductInfo>, position: Int) {

            //상품 이미지
            GlideApp.with(context)
                    .load(storageRef.child(pList[position].prodImage))
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .centerCrop()
                    .into(pImage)
            //가격
            pPrice.text = pList[position].prodPrice.toString()+"원"
            //제조사
            pCompany.text = COMPANY_CODE[pList[position].prodCompany-1]
            //상품명
            pName.text = pList[position].prodName
            //태그
            val pListAdapter = ProductTagListAdapter(context,pList[position].prodTag)
            itemView.productTagList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemView.productTagList.adapter = pListAdapter
            //평점
            if(pList[position].prodRatingNum==0) //평가 0명일경우
                itemView.productRatingPoint.text= "0"
            else {
                val strNumber = String.format("%.2f", (pList[position].prodPoint / pList[position].prodRatingNum.toFloat()))
                itemView.productRatingPoint.text = strNumber
            }
            //상품 클릭
            productContainer.setOnClickListener {
                mListener.interfaceClickProd(pList[position])
            }
        }
    }
    companion object {
        lateinit var mListener: InterfaceClickProd
        fun productClicked(mListener: InterfaceClickProd) { this.mListener = mListener }
    }
    interface InterfaceClickProd {
        fun interfaceClickProd(productData: ProductInfo)
    }
}