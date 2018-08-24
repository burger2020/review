package condom.best.condom.Dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.storage.FirebaseStorage
import condom.best.condom.Data.GlideApp
import condom.best.condom.Data.ProductInfo
import condom.best.condom.Data.StringData.Companion.COMPANY_CODE
import condom.best.condom.R

class RatingDialog (context: Context, private val count: Int,private val productInfo:ProductInfo) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int { return count }
    override fun getItem(position: Int): Any { return position }
    override fun getItemId(position: Int): Long { return position.toLong() }

    private val storage = FirebaseStorage.getInstance("gs://condom-55a91").reference

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

        viewHolder.prodCompany.text = COMPANY_CODE[productInfo.prodCompany-1]
        viewHolder.prodName.text = productInfo.prodName
        GlideApp.with(context)
                .load(storage.child(productInfo.prodImage))
                .centerCrop()
                .into(viewHolder.prodImage)

        viewHolder.prodRatingBar.setOnRatingBarChangeListener { ratingBar: RatingBar, fl: Float, b: Boolean ->
            mListener.interfaceClickProd(fl)
        }

        return view
    }

    data class ViewHolder(val prodCompany: TextView,val prodName: TextView,val prodImage: ImageView, val prodRatingBar: RatingBar)

    companion object {
        lateinit var mListener: InterfaceRatingSuccess
        fun productRating(mListener: InterfaceRatingSuccess) { this.mListener = mListener }
    }
    interface InterfaceRatingSuccess {
        fun interfaceClickProd(rating : Float)
    }
}