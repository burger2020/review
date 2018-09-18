package condom.best.condom.View.BottomNavPage.Product

import android.annotation.SuppressLint
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
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
import condom.best.condom.View.MainActivity.Companion.localDataGet
import condom.best.condom.View.MainActivity.Companion.localDataPut
import kotlinx.android.synthetic.main.fragment_product_review.view.*
import kotlinx.android.synthetic.main.fragment_review_detail.view.*
import java.util.*


@Suppress("DEPRECATION")
class ReviewDetailFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            divider = it.getInt(dividerParam)
            productData = it.getSerializable(productInfo) as ProductInfo
            likeState.like = it.getBoolean(likeStateParams)
            if(divider==1)
                userRatingData = it.getSerializable(userRating) as UserRatingData
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

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance("gs://condom-55a91").reference

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
                                                listStateSetting(likeState) //좋아요 상태, 개수
                                            }
                                }
                            }
                            getCommentData()//리뷰의 댓글 데이터 가져오기
                            listStateSetting(likeState) //좋아요 상태, 개수
                        } catch (e: KotlinNullPointerException) { }
                    }

            userInfoSetting(currentUser?.displayName.toString(), currentUser?.photoUrl.toString())//유저 정보
            prodInfoSetting()//제품정보
        }
        else if(divider == 2){//다른유저 댓글
            userInfoSetting(userInfo.name,userInfo.profileUri)//유저 정보
            prodInfoSetting() //제품정보
            listStateSetting(likeState.like) //좋아요 상태, 개수
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

        userCommentAdapter = ProductReviewListAdapter(rootView.context, ProductReviewData_Like(),commentList,2)

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
        val trueTime = try { TrueTimeRx.now().time }
        catch (e : IllegalStateException){ System.currentTimeMillis() }
        val reviewCommentList = ReviewCommentList(comment, trueTime, currentUser?.uid.toString(),0)
        db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid).collection(REVIEW_COMMENT)
                .document(trueTime.toString()).set(reviewCommentList)

        val sfDocRef = db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid)

        db.runTransaction { transaction ->//평가리스트 로컬 저장
            val snapshot = transaction.get(sfDocRef)
            val reReviewNum = snapshot.getLong("reReviewNum")!! + 1
            transaction.update(sfDocRef, "reReviewNum", reReviewNum)
            reReviewNum
        }.addOnSuccessListener {}.addOnFailureListener {}

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(rootView.commentText.windowToken, 0)

        rootView.commentText.setText("")
        rootView.sendComment.isEnabled = false
        rootView.sendComment.setTextColor(rootView.context.resources.getColor(R.color.gray1))

        commentList.reviewCommentData.add(0,reviewCommentList)
        userCommentAdapter.notifyItemInserted(0)
    }
    private fun userInfoSetting(name : String, profileUrl : String){//유저 정보
        val index = MainActivity.localDataGet.getInt(getString(R.string.userProfileChange),0)
        if(profileUrl == "null")
            GlideApp.with(this)
                    .load(context!!.resources.getDrawable(R.drawable.ic_user))
                    .apply(RequestOptions().centerCrop())
                    .apply(RequestOptions.circleCropTransform())
                    .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}$index")))
                    .into(rootView.userCommentViewProfile)
        else
            GlideApp.with(rootView.context)
                    .load(storage.child(profileUrl))
                    .apply(RequestOptions().centerCrop())
                    .apply(RequestOptions.circleCropTransform())
                    .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}$index")))
                    .into(rootView.userImage)
        rootView.userNameText.text = name
    }
    private fun prodInfoSetting(){//제품정보
        val c = Calendar.getInstance()
        rootView.pName.text = productData.prodName
        rootView.pCompany.text = productData.prodCompany
        if(divider == 1) {
            rootView.reviewCommentText.text = userRatingData.reviewComment
            rootView.userRatingNum.text = userRatingData.ratingPoint.toString()
            c.timeInMillis = userRatingData.reviewDate
        }
        else {
            rootView.reviewCommentText.text = productReviewData.review
            rootView.userRatingNum.text = productReviewData.rating.toString()
            c.timeInMillis = productReviewData.date
        }
        val strNumber = String.format("%04d. %02d. %02d", c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))
        rootView.reviewDateText.text = strNumber
    }
    @SuppressLint("SetTextI18n")
    private fun listStateSetting(listState: Boolean){//좋아요 상태, 개수, 댓글 개수
        if(listState)
            rootView.likeImage.setImageDrawable(rootView.resources.getDrawable(R.drawable.ic_like_ok))
        else
            rootView.likeImage.setImageDrawable(rootView.resources.getDrawable(R.drawable.ic_like))
        rootView.likeNum.text = productReviewData.likeNum.toString()
        rootView.commentNum.text = "댓글 ${productReviewData.reReviewNum}"
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
