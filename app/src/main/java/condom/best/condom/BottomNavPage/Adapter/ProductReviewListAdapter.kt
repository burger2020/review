package condom.best.condom.BottomNavPage.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import condom.best.condom.Data.GlideApp
import condom.best.condom.Data.ProductReviewData_Like
import condom.best.condom.R
import kotlinx.android.synthetic.main.adapter_product_review_list.view.*
import java.util.*

class ProductReviewListAdapter(val context: Context, private val reviewList: ProductReviewData_Like) : RecyclerView.Adapter<ProductReviewListAdapter.ProductReviewViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductReviewListAdapter.ProductReviewViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = inflater.inflate(R.layout.adapter_product_review_list,parent,false)
        return ProductReviewViewHolder(mainView)
    }
    override fun getItemCount(): Int = reviewList.productReviewData.size
    override fun onBindViewHolder(holder: ProductReviewListAdapter.ProductReviewViewHolder, position: Int) {
        holder.bindHolder(context,reviewList,position)
    }

    class ProductReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val storage = FirebaseStorage.getInstance("gs://condom-55a91").reference
        fun bindHolder(context:Context,reviewList: ProductReviewData_Like,position: Int) {
            itemView.reviewComment.text = reviewList.productReviewData[position].review
            itemView.reviewerName.text = reviewList.reviewerInfo[position].name
            itemView.reviewRating.text = reviewList.productReviewData[position].rating.toString()
            //댓글 작성 시간
            val c = Calendar.getInstance()
            c.timeInMillis = reviewList.productReviewData[position].date
            val strNumber = String.format("%04d. %02d. %02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))
            itemView.reviewDate.text = strNumber
            //유저프로필
            GlideApp.with(context)
                    .load(storage.child(reviewList.reviewerInfo[position].profileUri))
                    .apply(RequestOptions.circleCropTransform())
                    .apply(RequestOptions.placeholderOf(context.resources.getDrawable(R.drawable.ic_user)))
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .centerCrop()
                    .into(itemView.reviewerProfile)
            //좋아요버튼
            if(reviewList.reviewLike[position].like)
                GlideApp.with(context)
                        .load(itemView.resources.getDrawable(R.drawable.ic_like_ok))
                        .into(itemView.likeButton)
            else
                GlideApp.with(context)
                        .load(itemView.resources.getDrawable(R.drawable.ic_like))
                        .into(itemView.likeButton)
            itemView.likeButton.setOnClickListener{
                //좋아요 누르면 인터페이스
                mListener.interfaceLikeClick(reviewList.reviewLike[position].like,position)
            }
            //좋아요 개수
            itemView.listNumText.text = reviewList.productReviewData[position].likeNum.toString()
        }
        companion object {
            lateinit var mListener: InterfaceReviewLike
            fun reviewLike(mListener: InterfaceReviewLike) { this.mListener = mListener }
        }
        interface InterfaceReviewLike {
            fun interfaceLikeClick(likeBool : Boolean,position: Int)
        }
    }

}
