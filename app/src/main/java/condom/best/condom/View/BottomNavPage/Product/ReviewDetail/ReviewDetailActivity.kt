package condom.best.condom.View.BottomNavPage.Product.ReviewDetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.instacart.library.truetime.TrueTimeRx
import com.orhanobut.dialogplus.DialogPlus
import condom.best.condom.R
import condom.best.condom.View.BottomNavPage.Adapter.ProductReviewListAdapter
import condom.best.condom.View.Data.*
import condom.best.condom.View.Data.FirebaseConst.Companion.COMMENT
import condom.best.condom.View.Data.FirebaseConst.Companion.COMMENT_LIKE
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_REVIEWS
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_COMMENT
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_LIKE_OFF
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_LIKE_ON
import condom.best.condom.View.Data.UserLocalDataPath.Companion.userReviewLikePath
import condom.best.condom.View.Dialog.DeleteDialog
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.db
import condom.best.condom.View.MainActivity.Companion.localDataPut
import kotlinx.android.synthetic.main.activity_review_detail.*
import java.lang.IndexOutOfBoundsException
import java.util.*

@Suppress("DEPRECATION")
class ReviewDetailActivity : AppCompatActivity(), ReviewDetailContract.View {

    lateinit var productReviewData : ProductReviewData
    private var divider : Int = 1
    private lateinit var productInfo : ProductInfo
    private var commentList = ReviewCommentList_Like() // 리뷰 댓글 리스트
    private lateinit var userCommentAdapter : ProductReviewListAdapter // 리뷰 댓글 어댑터
    private lateinit var userRatingData : UserRatingData

    private lateinit var presenter: ReviewDetailPresenter

    private var likeOverlap = true
    private var reviewLikeOverlap = true

    private var likeState = false
    private lateinit var userInfo : UserInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_detail)

        val intent = intent
        productInfo = intent.getSerializableExtra("PRODUCT_DATA") as ProductInfo
        userInfo = intent.getSerializableExtra("USER_INFO") as condom.best.condom.View.Data.UserInfo
        divider = intent.getIntExtra("DIVIDER",1)
        lateinit var adapter : DeleteDialog
        lateinit var dialog : DialogPlus
        presenter = ReviewDetailPresenter().apply {
            view = this@ReviewDetailActivity
        }

        if(divider == 1){// 내 댓글
            userRatingData = intent.getSerializableExtra("RATING_DATA") as UserRatingData
            presenter.getMyReviewData(productInfo)//내 리뷰 좋아요 및 데이
            productReviewData = ProductReviewData()
        }else{//다른유저 댓글
            val likeBool = intent.getBooleanExtra("LIkE_BOOL",false)
            productReviewData = intent.getSerializableExtra("REVIEW_DATA") as ProductReviewData
            listStateSetting(likeBool) //좋아요 상태, 개수
            presenter.getCommentData(productInfo.prodName,productReviewData.userUid)//리뷰의 댓글 데이터 가져오기
        }

        //키보드 보내기 버튼
        commentText.setOnEditorActionListener { v, _, _ ->
            if(v.text.isNotEmpty()){
                sendComment(v.text.toString())
            }
            return@setOnEditorActionListener true
        }
        //등록버튼  눌러 댓글 남기기
        sendComment.setOnClickListener { sendComment(commentText.text.toString()) }
        //댓글 등록 버튼 활성화
        commentText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isNotEmpty()){
                    sendComment.isEnabled = true
                    sendComment.setTextColor(resources.getColor(R.color.colorPrimary))
                }else{
                    sendComment.isEnabled = false
                    sendComment.setTextColor(resources.getColor(R.color.gray1))
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        DeleteDialog.commentDelete(object : DeleteDialog.InterfaceCommentDelete{
            override fun interfaceCommentDelete(reviewCommentData: ArrayList<ReviewCommentList>, position: Int) {//댓글삭제 다이얼로그 인터페이스
                deleteComment(reviewCommentData,position)
                dialog.dismiss()
            }
        })
        ProductReviewListAdapter.ProductReviewViewHolder.commentLike(object : ProductReviewListAdapter.ProductReviewViewHolder.InterfaceCommentLike{
            override fun interfaceReviewLike(likeBool: Boolean) {//리뷰 좋아요
                if(reviewLikeOverlap) {
                    reviewLikeOverlap = false
                    reviewLikeUpdate()
                }
            }
            override fun interfaceCommentLike(likeBool: Boolean, productReviewData: ReviewCommentList, position: Int) { // 코멘트 좋아요
                if(likeOverlap){
                    likeOverlap = false
                    commentLikeUpdate(productReviewData,likeBool)
                    commentList.commentLike[position].like = !likeBool
                    if(likeBool)
                        commentList.reviewCommentData[position].likeNum--
                    else
                        commentList.reviewCommentData[position].likeNum++
                    userCommentAdapter.notifyItemChanged(position)
                }
            }
            override fun interfaceCommentRemove(reviewCommentData: ArrayList<ReviewCommentList>, position: Int) { //코멘트 삭제
                adapter = DeleteDialog(this@ReviewDetailActivity, 2,reviewCommentData,position)
                dialog = DialogPlus.newDialog(this@ReviewDetailActivity)
                        .setAdapter(adapter)
                        .setExpanded(false, 300)
                        .setOnItemClickListener { dialog, _, _, _ ->
                            dialog.dismiss()
                        }
                        .create()
                dialog.show()
            }
            override fun interfaceCommentImproper() { //코멘트 신고
            }
        })
    }

    private fun deleteComment(reviewCommentData: ArrayList<ReviewCommentList>, position: Int) { // 댓글 데이터 삭제
        try {
            db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                    .document(reviewCommentData[position].date.toString()).delete()
            db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                    .document(reviewCommentData[position].date.toString()).collection(COMMENT_LIKE).get().addOnCompleteListener { it ->
                        // 좋아요 데이터 삭제
                        it.result.forEach{ it.reference.delete() }
                    }

            commentListUpdate(2)

            commentList.reviewCommentData.removeAt(position)
            commentList.commentLike.removeAt(position)
            commentList.commenterInfo.removeAt(position)
            userCommentAdapter.notifyDataSetChanged()
//                userCommentAdapter.notifyItemRemoved(position)
        }catch (e:IndexOutOfBoundsException){}
    }
    private fun adapterDataChange(){
        progressBar.visibility = View.GONE
        userCommentAdapter = ProductReviewListAdapter(this, ProductReviewData_Like(), commentList, productInfo, userInfo, productReviewData, 2)
        userCommentList.adapter = userCommentAdapter
        userCommentAdapter.notifyDataSetChanged()
    }
    //어뎁터 초기화
    fun adapterRefresh(commentList: ReviewCommentList_Like) {
        this.commentList.reviewCommentData.clear()
        this.commentList.commentLike.clear()
        this.commentList.commenterInfo.clear()
        this.commentList.reviewCommentData.add(ReviewCommentList(productReviewData.review,productReviewData.date,productReviewData.userUid, productReviewData.likeNum.toInt()))
        this.commentList.commentLike.add(ReviewLike(likeState))
        this.commentList.commenterInfo.add(userInfo)
        this.commentList.reviewCommentData.addAll(commentList.reviewCommentData)
        this.commentList.commentLike.addAll(commentList.commentLike)
        this.commentList.commenterInfo.addAll(commentList.commenterInfo)
        userCommentList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapterDataChange()
//                emptyViewVisible()
    }

    //댓글 남기기
    private fun sendComment(comment : String){
        val trueTime = try {
            TrueTimeRx.now().time
        } catch (e: IllegalStateException) {
            System.currentTimeMillis()
        }
        val reviewCommentList = ReviewCommentList(comment, trueTime, currentUser?.uid.toString(), 0)
        db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                .document(trueTime.toString()).set(reviewCommentList)
        commentListUpdate(1)// 코맨트 카운트
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(commentText.windowToken, 0)

        commentText.setText("")
        sendComment.isEnabled = false
        sendComment.setTextColor(resources.getColor(R.color.gray1))

        commentList.reviewCommentData.add(1, reviewCommentList)
        commentList.commentLike.add(1, ReviewLike(false))
        commentList.commenterInfo.add(1, userInfo)
//        if(commentList.reviewCommentData.size == 1){
//            userCommentList.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
//            userCommentList.adapter = userCommentAdapter
//        }
//        userCommentAdapter.notifyItemInserted(1)
        userCommentAdapter.notifyDataSetChanged()
    }
    private fun commentListUpdate(state: Int){
        val sfDocRef = db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid)

        if(state==1)
            productReviewData.reReviewNum++
        else
            productReviewData.reReviewNum--
        reviewStateChange()
        db.runTransaction { transaction ->
            //평가리스트 로컬 저장
            val snapshot = transaction.get(sfDocRef)
            val reReviewNum =
                    if(state==1) { snapshot.getLong("reReviewNum")!! + 1 }
                    else{ snapshot.getLong("reReviewNum")!! - 1 }
            transaction.update(sfDocRef, "reReviewNum", reReviewNum)

            reReviewNum
        }.addOnSuccessListener {}.addOnFailureListener {}
    }
    private fun reviewStateChange(){
        mListener.interfaceReviewStateChange(commentList.reviewCommentData[0].likeNum,commentList.commentLike[0].like,productReviewData.reReviewNum,productReviewData.userUid)
    }
    //리뷰 좋아요 업데이트
    fun reviewLikeUpdate(){
        commentList.commentLike[0].like = !commentList.commentLike[0].like
        if(commentList.commentLike[0].like)
            commentList.reviewCommentData[0].likeNum++
        else
            commentList.reviewCommentData[0].likeNum--
        reviewStateChange()
//                adapterDataChange()
        userCommentAdapter.notifyItemChanged(0)
        val sfDocRef = db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(sfDocRef)
            val likeNum = if (commentList.commentLike[0].like) {//좋아요 ok
                //평가리스트 로컬 저장
                localDataPut.putInt(userReviewLikePath(productInfo.prodName,productReviewData.userUid,productReviewData.date), REVIEW_LIKE_ON).commit()
                db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid).collection(COMMENT_LIKE)
                        .document(currentUser!!.uid).set(ReviewLike(true))
                snapshot.getLong("likeNum")!! + 1
            }
            else {//좋아요 취소
                localDataPut.putInt(userReviewLikePath(productInfo.prodName,productReviewData.userUid,productReviewData.date), REVIEW_LIKE_OFF).commit()
                db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid).collection(COMMENT_LIKE)
                        .document(currentUser!!.uid).set(ReviewLike(false))
                snapshot.getLong("likeNum")!! - 1
            }
            transaction.update(sfDocRef, "likeNum", likeNum)
            reviewLikeOverlap = true
        }.addOnSuccessListener {}.addOnFailureListener {}
    }

    fun commentLikeUpdate(reviewCommentData: ReviewCommentList, likeBool: Boolean){//코멘트 좋아요
        val sfDocRef = db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid)
                .collection(REVIEW_COMMENT).document(reviewCommentData.date.toString())
        db.collection(PRODUCT_REVIEWS).document(productInfo.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                .document(reviewCommentData.date.toString()).collection(COMMENT_LIKE).document(currentUser!!.uid).set(ReviewLike(!likeBool))
        if(likeBool)
            localDataPut.putInt(userReviewLikePath(productInfo.prodName,productReviewData.userUid,reviewCommentData.date), REVIEW_LIKE_OFF).commit()
        else
            localDataPut.putInt(userReviewLikePath(productInfo.prodName,productReviewData.userUid,reviewCommentData.date), REVIEW_LIKE_ON).commit()
        db.runTransaction { transaction ->
            //평가리스트 로컬 저장
            val snapshot = transaction.get(sfDocRef)
            val reReviewNum =
                    if(!likeBool) { snapshot.getLong("likeNum")!! + 1 }
                    else{ snapshot.getLong("likeNum")!! - 1 }
            transaction.update(sfDocRef, "likeNum", reReviewNum)
            likeOverlap = true
        }.addOnSuccessListener {}.addOnFailureListener {}
    }
    //데이터 셋팅
    fun reviewDataSetting(productReviewData: ProductReviewData) {
        this.productReviewData = productReviewData
    }
    //좋아요 상태, 개수, 댓글 개수
    @SuppressLint("SetTextI18n")
    fun listStateSetting(likeState: Boolean){
        this.likeState = likeState
    }
    companion object {
        lateinit var mListener: InterfaceReviewStateChange
        fun reviewLike(mListener: InterfaceReviewStateChange) { Companion.mListener = mListener }
    }
    interface InterfaceReviewStateChange {
        fun interfaceReviewStateChange(likeNum: Int, likeState: Boolean, commentNum: Int, userUid: String)
    }
}
