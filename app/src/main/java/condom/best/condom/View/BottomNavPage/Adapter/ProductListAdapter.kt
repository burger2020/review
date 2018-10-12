package condom.best.condom.View.BottomNavPage.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import condom.best.condom.R
import condom.best.condom.View.Data.GlideApp
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.View.MainActivity.Companion.storage
import kotlinx.android.synthetic.main.adapter_home_list.view.*

class ProductListAdapter(val context:Context, private val pList:ArrayList<ProductInfo>, private val divider : Int) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = inflater.inflate(R.layout.adapter_home_list,parent,false)
        return ViewHolder(mainView)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindHolder(context,pList,divider,position)
    }
    override fun getItemCount(): Int = pList.size
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pName = itemView.productName
        private val pImage = itemView.productImage
        //TODO 가격 표시 보류()
//        private val pPrice = itemView.productPrice
        private val pCompany = itemView.productCompany
        private val productContainer = itemView.productContainer
        @SuppressLint("SetTextI18n")
        fun bindHolder(context: Context, pList: ArrayList<ProductInfo>, divider : Int, position: Int) {

            //상품 이미지
            GlideApp.with(context)
                    .load(storage.child(pList[position].prodImage))
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .centerCrop()
                    .into(pImage)
            //가격
//            pPrice.text = pList[position].prodPrice.toString()+"원"
            //제조사
            pCompany.text = pList[position].prodCompany
            //상품명
            pName.text = pList[position].prodName
            //태그
            val pListAdapter = ProductTagListAdapter(context,pList[position].prodTag,0)
            itemView.productTagList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            itemView.productTagList.adapter = pListAdapter
            //평점
            if(pList[position].prodRatingNum==0) //평가 0명일경우
                itemView.productRatingPoint.text= "0"
            else {//최대 5 ~ 최소 0 오버안되게
                var point = (pList[position].prodPoint / pList[position].prodRatingNum.toFloat())
                if((pList[position].prodPoint / pList[position].prodRatingNum.toFloat())>=5)
                    point = 5f
                else if((pList[position].prodPoint / pList[position].prodRatingNum.toFloat())<=0)
                    point = 0f
                val strNumber = String.format("%.2f", point)
                itemView.productRatingPoint.text = strNumber
            }
            //상품 클릭
            productContainer.setOnClickListener {
                if(divider == 0)
                    mListener.interfaceClickProd(pList[position])
                else
                    mListener1.interfaceClickProdTag(pList[position])
            }
        }
    }
    companion object {
        lateinit var mListener: InterfaceClickProd
        fun productClicked(mListener: InterfaceClickProd) { Companion.mListener = mListener }
        lateinit var mListener1: InterfaceClickProdTag
        fun productClicked(mListener1: InterfaceClickProdTag) { Companion.mListener1 = mListener1 }
    }
    interface InterfaceClickProd {
        fun interfaceClickProd(productData: ProductInfo)
    }
    interface InterfaceClickProdTag {
        fun interfaceClickProdTag(productData: ProductInfo)
    }
}