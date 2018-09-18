package condom.best.condom.View.BottomNavPage.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import condom.best.condom.R
import condom.best.condom.View.Data.*
import condom.best.condom.View.MainActivity.Companion.currentUser
import kotlinx.android.synthetic.main.adapter_product_review_list.view.*
import java.util.*

@Suppress("DEPRECATION")
class ProductReviewListAdapter(val context: Context, private val reviewList: ProductReviewData_Like, private val reviewCommentList: ReviewCommentList_Like, private val divider : Int)
    : RecyclerView.Adapter<ProductReviewListAdapter.ProductReviewViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductReviewViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = inflater.inflate(R.layout.adapter_product_review_list,parent,false)
        return ProductReviewViewHolder(mainView)
    }
    override fun getItemCount(): Int = if(divider == 1)
        reviewList.productReviewData.size
    else
        reviewCommentList.reviewCommentData.size

    override fun onBindViewHolder(holder: ProductReviewViewHolder, position: Int) {
        holder.bindHolder(context,reviewList,reviewCommentList,divider,position)
    }

    class ProductReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val storage = FirebaseStorage.getInstance("gs://condom-55a91").reference
        @SuppressLint("SetTextI18n")
        fun bindHolder(context:Context, reviewList: ProductReviewData_Like, reviewCommentList: ReviewCommentList_Like, divider: Int, position: Int) {
            if(divider == 1){//리뷰 목록일 경우
                itemView.reviewComment.text = reviewList.productReviewData[position].review
                itemView.reviewerName.text = reviewList.reviewerInfo[position].name
                itemView.reviewRating.text = reviewList.productReviewData[position].rating.toString()
                //댓글 작성 시간
                val c = Calendar.getInstance()
                c.timeInMillis = reviewList.productReviewData[position].date
                val strNumber = String.format("%04d. %02d. %02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))
                itemView.reviewDate.text = strNumber
                //유저프로필
                if(reviewList.reviewerInfo[position].profileUri=="null")
                    GlideApp.with(context)
                            .load(context.resources.getDrawable(R.drawable.ic_user))
                            .apply(RequestOptions.circleCropTransform())
                            .apply(RequestOptions.signatureOf(GetSignatureKey().getKey(context)))
                            .into(itemView.reviewerProfile)
                else
                    GlideApp.with(context)
                            .load(storage.child(reviewList.reviewerInfo[position].profileUri))
                            .apply(RequestOptions().centerCrop())
                            .apply(RequestOptions.circleCropTransform())
                            .apply(RequestOptions.placeholderOf(context.resources.getDrawable(R.drawable.ic_user)))
                            .apply(RequestOptions.signatureOf(GetSignatureKey().getKey(context)))
                            .transition(DrawableTransitionOptions.withCrossFade(100))
                            .into(itemView.reviewerProfile)
                //좋아요버튼
                if(reviewList.reviewLike[position].like)
                    itemView.likeButton.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_like_ok))
                else
                    itemView.likeButton.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_like))
                itemView.likeButton.setOnClickListener{
                    //좋아요 누르면 인터페이스
                    mListener.interfaceLikeClick(reviewList.reviewLike[position].like,reviewList.productReviewData[position],position)
                }
                itemView.reviewDetailLayer.setOnClickListener {
                    //리뷰 자세히 보기
                    mListener.interfaceReviewClick(reviewList.reviewLike[position].like,reviewList.productReviewData[position],reviewList.reviewerInfo[position],position)
                }
                //좋아요 개수
                itemView.listNumText.text = reviewList.productReviewData[position].likeNum.toString()

                //부적절함 버튼 표시
                if(reviewList.productReviewData[position].userUid == currentUser!!.uid)
                    itemView.improperButton.visibility = View.GONE
                else
                    itemView.improperButton.visibility = View.VISIBLE

                itemView.reviewCommentButton.text = "댓글 ${reviewList.productReviewData[position].reReviewNum}" //리뷰텍스트
            }else if(divider == 2){//리뷰에 코맨트일 경우
                itemView.reviewComment.text = reviewCommentList.reviewCommentData[position].comment
                itemView.reviewerName.text = reviewCommentList.commenterInfo[position].name
                itemView.reviewRating.visibility = View.GONE
                //댓글 작성 시간
                val c = Calendar.getInstance()
                c.timeInMillis = reviewCommentList.reviewCommentData[position].date
                val strNumber = String.format("%04d. %02d. %02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))
                itemView.reviewDate.text = strNumber
                //유저프로필
                if(reviewCommentList.commenterInfo[position].profileUri=="null")
                    GlideApp.with(context)
                            .load(context.resources.getDrawable(R.drawable.ic_user))
                            .apply(RequestOptions.circleCropTransform())
                            .apply(RequestOptions.signatureOf(GetSignatureKey().getKey(context)))
                            .into(itemView.reviewerProfile)
                else
                    GlideApp.with(context)
                            .load(storage.child(reviewCommentList.commenterInfo[position].profileUri))
                            .apply(RequestOptions().centerCrop())
                            .apply(RequestOptions.circleCropTransform())
                            .apply(RequestOptions.placeholderOf(context.resources.getDrawable(R.drawable.ic_user)))
                            .apply(RequestOptions.signatureOf(GetSignatureKey().getKey(context)))
                            .transition(DrawableTransitionOptions.withCrossFade(100))
                            .into(itemView.reviewerProfile)
                //좋아요버튼
                if(reviewCommentList.commentLike[position].like)
                    itemView.likeButton.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_like_ok))
                else
                    itemView.likeButton.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_like))
                itemView.likeButton.setOnClickListener{
                    //좋아요 누르면 인터페이스
//                    mListener.interfaceLikeClick(reviewCommentList.commentLike[position].like,reviewCommentList.reviewCommentData[position],position)
                }
                itemView.reviewDetailLayer.setOnClickListener {
                    //리뷰 자세히 보기
//                    mListener.interfaceReviewClick(reviewList.reviewLike[position].like,reviewList.productReviewData[position],reviewList.reviewerInfo[position],position)
                }
                //좋아요 개수
                itemView.listNumText.text = reviewCommentList.reviewCommentData[position].likeNum.toString()

                //부적절함 버튼 표시
                if(reviewCommentList.reviewCommentData[position].userUid == currentUser!!.uid)
                    itemView.improperButton.visibility = View.GONE
                else
                    itemView.improperButton.visibility = View.VISIBLE

                itemView.reviewCommentButton.visibility = View.GONE //리뷰텍스트
            }
        }
        companion object {
            lateinit var mListener: InterfaceReviewLike
            fun reviewLike(mListener: InterfaceReviewLike) { Companion.mListener = mListener }
        }
        interface InterfaceReviewLike {
            fun interfaceLikeClick(likeBool : Boolean, productReviewData: ProductReviewData, position: Int)
            fun interfaceReviewClick(likeBool: Boolean, productReviewData: ProductReviewData, uesrInfo: UserInfo, position : Int)
        }
    }

}
