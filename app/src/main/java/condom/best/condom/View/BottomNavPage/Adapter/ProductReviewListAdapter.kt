package condom.best.condom.View.BottomNavPage.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import condom.best.condom.R
import condom.best.condom.View.Data.*
import condom.best.condom.View.MainActivity
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.storage
import kotlinx.android.synthetic.main.adapter_product_review_list.view.*
import kotlinx.android.synthetic.main.fragment_review_detail_header.view.*
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class ProductReviewListAdapter(val context: Context, private val reviewList: ProductReviewData_Like, private val reviewCommentList: ReviewCommentList_Like, private val productInfo: ProductInfo
                               , private val userInfo: UserInfo, private val productReviewData: ProductReviewData, private val divider: Int)
    : RecyclerView.Adapter<ProductReviewListAdapter.ProductReviewViewHolder>(){

    private var headerFlag = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductReviewViewHolder {
        return if(viewType == 0 && divider == 2){//헤더
            headerFlag = true
            val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val mainView = inflater.inflate(R.layout.fragment_review_detail_header,parent,false)
            ProductReviewViewHolder(mainView,headerFlag)
        }else{
            headerFlag = false
            val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val mainView = inflater.inflate(R.layout.adapter_product_review_list,parent,false)
            ProductReviewViewHolder(mainView,headerFlag)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int = if(divider == 1 || divider == 3)
        reviewList.productReviewData.size
    else
        reviewCommentList.reviewCommentData.size

    override fun onBindViewHolder(holder: ProductReviewViewHolder, position: Int) {
        if(divider == 1 || divider == 3)
            holder.bindHolder(context, reviewList, reviewCommentList, divider, productInfo, userInfo, productReviewData, position)
        else
            holder.bindHolder(context, reviewList, reviewCommentList, divider, productInfo, userInfo, productReviewData, position)
    }

    class ProductReviewViewHolder(itemView: View, private val isHeader : Boolean) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindHolder(context: Context, reviewList: ProductReviewData_Like, reviewCommentList: ReviewCommentList_Like, divider: Int, productInfo: ProductInfo, userInfo: UserInfo
                       , productReviewData: ProductReviewData, position: Int) {
            when {
                isHeader -> {
                    userInfoSetting(context,userInfo,productReviewData.profileOpen)
                    prodInfoSetting(productReviewData, productInfo)
                    listStateSetting(productReviewData, reviewCommentList)
                    itemView.likeLayer.setOnClickListener {
                        mListener1.interfaceReviewLike(reviewCommentList.commentLike[0].like)
                    }
                }
                divider == 1  || divider == 3 -> {//리뷰 더보기일 경우
                    val profileOpen = reviewList.productReviewData[position].profileOpen
                    if(profileOpen)
                      setOldProfile(reviewList.reviewerInfo[position],itemView.profileOldText)//프로필 정보
                    itemView.reviewComment.text = reviewList.productReviewData[position].review
                    itemView.reviewerName.text = reviewList.reviewerInfo[position].name
                    userRatingPointSetting(itemView.reviewRating,reviewList.productReviewData[position].rating)
                    //댓글 작성 시간
                    val c = Calendar.getInstance()
                    c.timeInMillis = reviewList.productReviewData[position].date
                    val strNumber = String.format("%04d. %02d. %02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))
                    itemView.reviewDate.text = strNumber
                    //유저프로필사진
                    if(reviewList.reviewerInfo[position].profileUri=="none" || !profileOpen)
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
                    itemView.reviewerProfile.setOnClickListener {// 유저 프사 클릭시
                        if(profileOpen){ // 프로필 공개

                        }
                    }
                    //좋아요버튼
                    if(reviewList.reviewLike[position].like)
                        itemView.likeButton.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_like_ok))
                    else
                        itemView.likeButton.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_like))
                    itemView.likeButtonLayer.setOnClickListener {
                        //좋아요 누르면 인터페이스
                        if(divider == 1)
                            mListener.interfaceLikeClick(reviewList.reviewLike[position].like, reviewList.productReviewData[position], position)
                        else if(divider == 3)
                            mListener3.interfaceLikeClick(reviewList.reviewLike[position].like, reviewList.productReviewData[position], position)
                    }
                    itemView.reviewDetailLayer.setOnClickListener {
                        //리뷰 자세히 보기
                        mListener.interfaceReviewClick(reviewList.reviewLike[position].like, reviewList.productReviewData[position], reviewList.reviewerInfo[position], position)
                    }
                    //좋아요 개수
                    itemView.listNumText.text = reviewList.productReviewData[position].likeNum.toString()

                    //부적절함 버튼 표시
                    if(reviewList.productReviewData[position].userUid == currentUser!!.uid)
                        itemView.improperButton.visibility = View.GONE
                    else
                        itemView.improperButton.visibility = View.VISIBLE

                    itemView.reviewCommentButton.text = "댓글 ${reviewList.productReviewData[position].reReviewNum}" //리뷰텍스트
                }
                divider == 2 -> {//리뷰에 코맨트일 경우
                    itemView.profileOldText.visibility = View.GONE
                    itemView.reviewComment.apply {
                        text = reviewCommentList.reviewCommentData[position].comment
                        ellipsize  = null
                        maxLines = 99999
                    }
                    itemView.reviewerName.text = reviewCommentList.commenterInfo[position].name
                    itemView.reviewRating.visibility = View.GONE
                    //댓글 작성 시간
                    val c = Calendar.getInstance()
                    c.timeInMillis = reviewCommentList.reviewCommentData[position].date
                    val strNumber = String.format("%04d. %02d. %02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))
                    itemView.reviewDate.text = strNumber
                    //유저프로필
                    if(reviewCommentList.commenterInfo[position].profileUri=="none")
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
                    itemView.likeButtonLayer.setOnClickListener{
                        //좋아요 누르면 인터페이스
                        //                    mListener.interfaceReviewStateChange(reviewCommentList.commentLike[position].like,reviewCommentList.reviewCommentData[position],position)
                    }

                    //부적절함 버튼 표시
                    if(reviewCommentList.reviewCommentData[position].userUid == currentUser!!.uid)
                        itemView.improperButton.text = "삭제"
                    else
                        itemView.improperButton.text = "부적절함"

                    //좋아요 개수
                    itemView.listNumText.text = reviewCommentList.reviewCommentData[position].likeNum.toString()
                    itemView.likeButtonLayer.setOnClickListener {// 좋아요
                        mListener1.interfaceCommentLike(reviewCommentList.commentLike[position].like,reviewCommentList.reviewCommentData[position],position)
                    }

                    itemView.improperButton.setOnClickListener {
                        if(itemView.improperButton.text == "삭제"){//삭제
                            mListener1.interfaceCommentRemove(reviewCommentList.reviewCommentData,position)
                        }else{//부적절함 신고
                            mListener1.interfaceCommentImproper()
                        }
                    }
                    itemView.reviewCommentButton.visibility = View.GONE //리뷰텍스트
                }
            }
        }

        private fun userRatingPointSetting(ratingTextView: TextView, rating: Float) { //유저 점수 세팅
            if(rating != 0f)
                ratingTextView.text = rating.toString()
            else
                ratingTextView.text = "-"
        }

        @SuppressLint("SetTextI18n")
        private fun setOldProfile(userInfo: UserInfo, profileText : TextView){ //유저 나이 세팅
            val gender = if(userInfo.gender==1)
                "남"
            else
                "여"
            profileText.visibility = View.VISIBLE
            profileText.text = "${userInfo.getOld()}대.$gender"
        }
        private fun userInfoSetting(context : Context,userInfo: UserInfo,profileOpen : Boolean){//유저 정보
            val index = MainActivity.localDataGet.getInt(context.getString(R.string.userProfileChange),0)
            if(userInfo.profileUri == "none" || !profileOpen)
                GlideApp.with(context)
                        .load(context.resources.getDrawable(R.drawable.ic_user))
                        .apply(RequestOptions().centerCrop())
                        .apply(RequestOptions.circleCropTransform())
                        .apply(RequestOptions.signatureOf(ObjectKey("${context.getString(R.string.userProfileChange)}$index")))
                        .into(itemView.userImage)
            else
                GlideApp.with(itemView.context)
                        .load(storage.child(userInfo.profileUri))
                        .apply(RequestOptions().centerCrop())
                        .apply(RequestOptions.circleCropTransform())
                        .apply(RequestOptions.signatureOf(ObjectKey("${context.getString(R.string.userProfileChange)}$index")))
                        .into(itemView.userImage)
            itemView.userNameText.text = userInfo.name
            if(profileOpen)
                setOldProfile(userInfo,itemView.oldProfileText)
        }
        private fun prodInfoSetting(productReviewData : ProductReviewData,productData: ProductInfo){//제품정보
            val c = Calendar.getInstance()
            itemView.pName.text = productData.prodName
            itemView.pCompany.text = productData.prodCompany
            itemView.reviewCommentText.text = productReviewData.review
            userRatingPointSetting(itemView.userRatingNum,productReviewData.rating)
            c.timeInMillis = productReviewData.date
            val strNumber = String.format("%04d. %02d. %02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))
            itemView.reviewDateText.text = strNumber
        }
        @SuppressLint("SetTextI18n")
        private fun listStateSetting(productReviewData: ProductReviewData, reviewCommentList: ReviewCommentList_Like){//좋아요 상태, 개수, 댓글 개수
            if(reviewCommentList.commentLike[0].like)
                itemView.likeImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_like_ok))
            else
                itemView.likeImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_like))
            itemView.likeNum.text = reviewCommentList.reviewCommentData[0].likeNum.toString()
            itemView.commentNum.text = "댓글 ${productReviewData.reReviewNum}"
        }
        companion object {
            lateinit var mListener: InterfaceReviewLike
            fun reviewLike(mListener: InterfaceReviewLike) { Companion.mListener = mListener }
            lateinit var mListener1: InterfaceCommentLike
            fun commentLike(mListener1: InterfaceCommentLike) { Companion.mListener1 = mListener1 }
            lateinit var mListener3: InterfaceReviewClick
            fun reviewClick(mListener3: InterfaceReviewClick) { Companion.mListener3 = mListener3 }
        }
        interface InterfaceReviewClick {
            fun interfaceLikeClick(likeBool : Boolean, productReviewData: ProductReviewData, position: Int)
        }
        interface InterfaceReviewLike {
            fun interfaceLikeClick(likeBool : Boolean, productReviewData: ProductReviewData, position: Int)
            fun interfaceReviewClick(likeBool: Boolean, productReviewData: ProductReviewData, userInfo: UserInfo, position : Int)
        }
        interface InterfaceCommentLike {
            fun interfaceReviewLike(likeBool: Boolean)
            fun interfaceCommentLike(likeBool: Boolean, productReviewData: ReviewCommentList, position: Int)
            fun interfaceCommentRemove(reviewCommentData: ArrayList<ReviewCommentList>, position: Int)
            fun interfaceCommentImproper()
        }
    }

}
