package condom.best.condom.View.Dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import condom.best.condom.R
import condom.best.condom.View.Data.GlideApp
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.View.MainActivity.Companion.storage

class RatingDialog (context: Context, private val count: Int, private val productInfo: ProductInfo, private val userRatingPint : Float) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int { return count }
    override fun getItem(position: Int): Any { return position }
    override fun getItemId(position: Int): Long { return position.toLong() }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var view: View? = convertView

        if (view == null) {
            view = layoutInflater.inflate(R.layout.dialog_rating, parent, false)

            viewHolder = ViewHolder(view!!.findViewById(R.id.prodCompany), view.findViewById(R.id.prodName), view.findViewById(R.id.prodImage), view.findViewById(R.id.prodRating))

            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val context = parent.context

        //상품 데이터 셋
        viewHolder.prodCompany.text = productInfo.prodCompany
        viewHolder.prodName.text = productInfo.prodName
        GlideApp.with(context)
                .load(storage.child(productInfo.prodImage))
                .centerCrop()
                .into(viewHolder.prodImage)

        //유저 평가 점수
        viewHolder.prodRatingBar.rating = userRatingPint

        viewHolder.prodRatingBar.setOnRatingBarChangeListener { ratingBar: RatingBar, fl: Float, b: Boolean ->
            mListener.interfaceClickProd(fl)
        }

        return view
    }

    data class ViewHolder(val prodCompany: TextView,val prodName: TextView,val prodImage: ImageView, val prodRatingBar: RatingBar)

    companion object {
        lateinit var mListener: InterfaceRatingSuccess
        fun productRating(mListener: InterfaceRatingSuccess) { Companion.mListener = mListener }
    }
    interface InterfaceRatingSuccess {
        fun interfaceClickProd(rating : Float)
    }
}