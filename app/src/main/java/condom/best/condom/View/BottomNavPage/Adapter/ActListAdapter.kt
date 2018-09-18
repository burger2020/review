package condom.best.condom.View.BottomNavPage.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import condom.best.condom.View.Data.StringData.Companion.RATING
import condom.best.condom.View.Data.StringData.Companion.WISH
import condom.best.condom.View.Data.UserDataList
import condom.best.condom.R
import condom.best.condom.View.MainActivity.Companion.storage
import kotlinx.android.synthetic.main.adapter_act_list.view.*
import condom.best.condom.View.Data.GlideApp

class ActListAdapter(val context : Context, private val actList : UserDataList, private val divider:String) : RecyclerView.Adapter<ActListAdapter.ViewHolder>(), ActListContract.Model {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActListAdapter.ViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = inflater.inflate(R.layout.adapter_act_list,parent,false)
        return ViewHolder(mainView)
    }
    override fun getItemCount(): Int
            = when(divider){
        RATING-> actList.userRatingData.size
        WISH-> actList.userWishData.size
        else-> actList.userReviewData.size
    }
    override var onClick: ((String) -> Unit)? = null
    override fun onBindViewHolder(holder: ActListAdapter.ViewHolder, position: Int) = holder.bindHolder(context,actList,divider,position,onClick)

    class ViewHolder(view : View)
        : RecyclerView.ViewHolder(view) {
        private val actImage = view.actListImage
        private val actName = view.actNameText
        private val actCompany = view.actCompanyText
        private val actPoint = view.actRatingPoint
        private val actStart = view.actRatingStar
        fun bindHolder(context: Context, actList: UserDataList, divider:String, position: Int, onClick : ((String) -> Unit)?){
            val prodName :String
            val prodCompany : String
            val prodImgUrl : String
            when(divider){
                RATING -> {
                    actStart?.visibility = View.VISIBLE
                    actPoint?.visibility = View.VISIBLE
                    actPoint?.text = actList.userRatingData[position].ratingPoint.toString()
                    prodName = actList.userRatingData[position].prodName
                    prodCompany = actList.userRatingData[position].prodCompany
                    prodImgUrl = actList.userRatingData[position].prodImageUrl
                }
                WISH -> {
                    actStart?.visibility = View.GONE
                    actPoint?.visibility = View.GONE
                    prodName = actList.userWishData[position].prodName
                    prodCompany = actList.userWishData[position].prodCompany
                    prodImgUrl = actList.userWishData[position].prodImageUrl
                }
                else -> {
                    actStart?.visibility = View.GONE
                    actPoint?.visibility = View.GONE
                    prodName = actList.userReviewData[position].prodName
                    prodCompany = actList.userReviewData[position].prodCompany
                    prodImgUrl = actList.userReviewData[position].prodImageUrl
                }
            }
            actName?.text = prodName
            actCompany?.text = prodCompany
            actImage?.let {
                GlideApp.with(context)
                        .load(storage.child(prodImgUrl))
                        .apply(RequestOptions().centerCrop())
                        .transition(DrawableTransitionOptions.withCrossFade(100))
                        .into(it)
            }
            actImage?.setOnClickListener{
                onClick?.invoke(prodName)
            }
        }
    }
}