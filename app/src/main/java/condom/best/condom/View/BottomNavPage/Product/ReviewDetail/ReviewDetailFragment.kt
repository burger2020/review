package condom.best.condom.View.BottomNavPage.Product.ReviewDetail

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.instacart.library.truetime.TrueTimeRx
import condom.best.condom.R
import condom.best.condom.View.BottomNavPage.Adapter.ProductReviewListAdapter
import condom.best.condom.View.Data.*
import condom.best.condom.View.Data.FirebaseConst.Companion.COMMENT
import condom.best.condom.View.Data.FirebaseConst.Companion.COMMENT_LIKE
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_REVIEWS
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_COMMENT
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_LIKE_DEFAULT
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_LIKE_OFF
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_LIKE_ON
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.View.Data.UserLocalDataPath.Companion.userReviewLikePath
import condom.best.condom.View.MainActivity
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.db
import condom.best.condom.View.MainActivity.Companion.localDataGet
import condom.best.condom.View.MainActivity.Companion.localDataPut
import kotlinx.android.synthetic.main.fragment_review_detail.view.*
import java.util.*

//TODO 지워도 될듯
@Suppress("DEPRECATION")
class ReviewDetailFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            divider = it.getInt(dividerParam)
            productData = it.getSerializable(productInfo) as ProductInfo
            likeState.like = it.getBoolean(likeStateParams)
            if(divider==1) {
                productReviewData = ProductReviewData()
                userRatingData = it.getSerializable(userRating) as UserRatingData
                userInfo = MainActivity.userInfo
            }
            else {
                productReviewData = it.getSerializable(productReviewDataParams) as ProductReviewData
                userInfo = it.getSerializable(userInfoParam) as UserInfo
            }
        }
    }

    private val productInfo = "productInfo"
    private val userRating = "userRating"
    private val productReviewDataParams = "productReviewDataParams"
    private val userInfoParam = "userInfoParam"
    private val dividerParam = "dividerParam"
    private val likeStateParams = "likeStateParams"

    private lateinit var productData : ProductInfo
    private lateinit var userRatingData : UserRatingData
    private lateinit var productReviewData : ProductReviewData
    private lateinit var userInfo: UserInfo
    private var divider: Int = 0
    private var likeState = ReviewLike() // 리뷰 좋아요 상태
    private var commentList = ReviewCommentList_Like() // 리뷰 댓글 리스트
    private lateinit var userCommentAdapter : ProductReviewListAdapter // 리뷰 댓글 어댑터
    private lateinit var rootView : View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_review_detail, container, false)
//        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        if(divider==1) { //내 댓글
            //배댓 내가한 좋아요 셋
            db.collection(FirebaseConst.PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser?.uid.toString())
                    .get().addOnCompleteListener{ it ->
                        try {
                            val likeStateGet = MainActivity.localDataGet
                                    .getInt(UserLocalDataPath.userReviewLikePath(productData.prodName, currentUser?.uid.toString(), userRatingData.reviewDate), FirebaseConst.REVIEW_LIKE_DEFAULT)
                            var likeState = false

                            productReviewData = (it.result.toObject(ProductReviewData::class.java)!!)
                            when(likeStateGet){
                                REVIEW_LIKE_ON->likeState = true
                                REVIEW_LIKE_OFF->likeState = false
                                else->{
                                    db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser?.uid.toString())
                                            .collection(COMMENT_LIKE).document(currentUser!!.uid).get().addOnCompleteListener {
                                                //배댓 각각 유저 데이터
                                                try {
                                                    likeState = (it.result.toObject(ReviewLike::class.java)!!).like
                                                    localDataPut.putInt(userReviewLikePath(productData.prodName,productReviewData.userUid,productReviewData.date), REVIEW_LIKE_ON)
                                                }catch (e:KotlinNullPointerException){
                                                    likeState = ReviewLike().like
                                                    localDataPut.putInt(userReviewLikePath(productData.prodName,productReviewData.userUid,productReviewData.date), REVIEW_LIKE_OFF)
                                                }
                                                localDataPut.commit()
//                                                listStateSetting(likeState) //좋아요 상태, 개수
                                            }
                                }
                            }
                            getCommentData()//리뷰의 댓글 데이터 가져오기
//                            listStateSetting(likeState) //좋아요 상태, 개수
                        } catch (e: KotlinNullPointerException) { }
                    }

//            userInfoSetting(currentUser?.displayName.toString(), currentUser?.photoUrl.toString())//유저 정보
//            prodInfoSetting()//제품정보
        }
        else if(divider == 2){//다른유저 댓글
//            userInfoSetting(userInfo.name,userInfo.profileUri)//유저 정보
//            prodInfoSetting() //제품정보
//            listStateSetting(likeState.like) //좋아요 상태, 개수
            getCommentData()//리뷰의 댓글 데이터 가져오기
        }

        rootView.commentText.setOnEditorActionListener { v, _, _ ->
            if(v.text.isNotEmpty()){
                sendComment(v.text.toString())
            }
            return@setOnEditorActionListener true
        }
        rootView.commentText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(s!!.isNotEmpty()){
                    rootView.sendComment.isEnabled = true
                    rootView.sendComment.setTextColor(rootView.context.resources.getColor(R.color.colorPrimary))
                }else{
                    rootView.sendComment.isEnabled = false
                    rootView.sendComment.setTextColor(rootView.context.resources.getColor(R.color.gray1))
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        rootView.sendComment.setOnClickListener { sendComment(rootView.commentText.text.toString()) } //댓글 남기기

        ProductReviewListAdapter.ProductReviewViewHolder.commentLike(object : ProductReviewListAdapter.ProductReviewViewHolder.InterfaceCommentLike{
            override fun interfaceReviewLike(likeBool: Boolean) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun interfaceCommentLike(likeBool: Boolean, productReviewData: ReviewCommentList, position: Int) { // 코멘트 좋아요

            }
            override fun interfaceCommentRemove(reviewCommentData: ArrayList<ReviewCommentList>, position: Int) { //코멘트 삭제
                db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                        .document(reviewCommentData[position].date.toString()).delete()
                commentListUpdate(2)

                commentList.reviewCommentData.removeAt(position)
                commentList.commentLike.removeAt(position)
                commentList.commenterInfo.removeAt(position)
//                userCommentAdapter.notifyItemRemoved(position)
                userCommentAdapter.notifyDataSetChanged()
            }
            override fun interfaceCommentImproper() { //코멘트 신고
            }
        })

        userCommentAdapter = ProductReviewListAdapter(rootView.context, ProductReviewData_Like(), commentList, productData, userInfo, productReviewData, 2)
        return rootView
    }
    private fun getCommentData(){ // 리뷰 댓글 가져오기
        db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                .get().addOnCompleteListener { it ->
                    var adapterSetBool = false
                    fun adapterRefresh(){
                        if(!adapterSetBool)
                            adapterSetBool = true
                        else {
                            rootView.userCommentList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            rootView.userCommentList.adapter = userCommentAdapter
                        }
//                emptyViewVisible()
                    }
                    try {
                        it.result.mapTo(commentList.reviewCommentData) { (it.toObject(ReviewCommentList::class.java)) }

                        var settingEnd = commentList.reviewCommentData.size*2
                        for(i in 0 until commentList.reviewCommentData.size){
                            db.collection(USER_INFO).document(commentList.reviewCommentData[i].userUid).get().addOnCompleteListener {
                                //배댓 유저 데이터
                                try {
                                    commentList.commenterInfo.add(it.result.toObject(UserInfo::class.java)!!)
                                    //마지막 데이터 다들고오면 어뎁터 실행
                                    settingEnd--
                                    if (i == commentList.reviewCommentData.size - 1 && settingEnd == 0) {
                                        adapterRefresh()
                                    }
                                }catch (e:KotlinNullPointerException){}
                            }
                            //배댓 내가한 좋아요 셋

                            val likeStateGet = localDataGet.getInt(userReviewLikePath(productData.prodName,commentList.reviewCommentData[i].userUid,commentList.reviewCommentData[i].date), REVIEW_LIKE_DEFAULT)
                            when(likeStateGet){
                                REVIEW_LIKE_ON-> {
                                    commentList.commentLike.add(ReviewLike(true))
                                    settingEnd--
                                }
                                REVIEW_LIKE_OFF-> {
                                    commentList.commentLike.add(ReviewLike(false))
                                    settingEnd--
                                }
                                else->{
                                    db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                                            .document(commentList.reviewCommentData[i].userUid).collection(COMMENT_LIKE).document(currentUser!!.uid).get().addOnCompleteListener {
                                                //배댓 각각 유저 데이터
                                                try {
                                                    commentList.commentLike.add(it.result.toObject(ReviewLike::class.java)!!)
                                                    localDataPut.putInt(userReviewLikePath(productData.prodName,commentList.reviewCommentData[i].userUid,commentList.reviewCommentData[i].date), REVIEW_LIKE_ON)
                                                }catch (e:KotlinNullPointerException){
                                                    commentList.commentLike.add(ReviewLike())
                                                    localDataPut.putInt(userReviewLikePath(productData.prodName,commentList.reviewCommentData[i].userUid,commentList.reviewCommentData[i].date), REVIEW_LIKE_OFF)
                                                }
                                                localDataPut.commit()
                                                settingEnd--
                                                if (i == commentList.reviewCommentData.size - 1 && settingEnd == 0) {
                                                    adapterRefresh()
                                                }
                                            }
                                }
                            }
                        }
                        adapterRefresh()
                    }catch (e:NullPointerException){}

                }
    }
    private fun sendComment(comment : String){ //댓글 남기기
        val trueTime = try {
            TrueTimeRx.now().time
        } catch (e: IllegalStateException) {
            System.currentTimeMillis()
        }
        val reviewCommentList = ReviewCommentList(comment, trueTime, currentUser?.uid.toString(), 0)
        db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                .document(trueTime.toString()).set(reviewCommentList)
        commentListUpdate(1)// 코맨트 카운트
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(rootView.commentText.windowToken, 0)

        rootView.commentText.setText("")
        rootView.sendComment.isEnabled = false
        rootView.sendComment.setTextColor(rootView.context.resources.getColor(R.color.gray1))

        commentList.reviewCommentData.add(0, reviewCommentList)
        commentList.commentLike.add(0, ReviewLike(false))
        commentList.commenterInfo.add(0, MainActivity.userInfo)
        if(commentList.reviewCommentData.size == 1){
            rootView.userCommentList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            rootView.userCommentList.adapter = userCommentAdapter
        }
        userCommentAdapter.notifyDataSetChanged()
//        userCommentAdapter.notifyDataSetChanged()

    }
    fun commentListUpdate(state: Int){
        val sfDocRef = db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid)
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

    companion object {
        @JvmStatic
        fun newInstance(productData: ProductInfo, userRatingData: UserRatingData, productReviewData: ProductReviewData, userInfo: UserInfo, likeBool : Boolean, divider : Int) =
                ReviewDetailFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(productInfo, productData)
                        if(divider == 1)
                            putSerializable(userRating, userRatingData)
                        else {
                            putSerializable(productReviewDataParams, productReviewData)
                            putSerializable(userInfoParam, userInfo)
                        }
                        putBoolean(likeStateParams,likeBool)
                        putSerializable(dividerParam, divider)
                    }
                }
    }
}
