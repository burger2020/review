package condom.best.condom.BottomNavPage.Product

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.instacart.library.truetime.TrueTimeRx
import com.orhanobut.dialogplus.DialogPlus
import condom.best.condom.BottomNavPage.Adapter.ProductReviewListAdapter
import condom.best.condom.BottomNavPage.Adapter.ProductTagListAdapter
import condom.best.condom.Data.*
import condom.best.condom.Data.FirebaseConst.Companion.COMMENT
import condom.best.condom.Data.FirebaseConst.Companion.COMMENT_LIKE
import condom.best.condom.Data.FirebaseConst.Companion.PRODUCT_INFO
import condom.best.condom.Data.FirebaseConst.Companion.PRODUCT_RATING
import condom.best.condom.Data.FirebaseConst.Companion.PRODUCT_REVIEWS
import condom.best.condom.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.Data.FirebaseConst.Companion.USER_RATING_DATA
import condom.best.condom.Data.FirebaseConst.Companion.USER_WISH_LIST
import condom.best.condom.Data.StringData.Companion.COMPANY_CODE
import condom.best.condom.Data.StringData.Companion.RATING_POINT
import condom.best.condom.Data.StringData.Companion.REVIEW
import condom.best.condom.Data.StringData.Companion.USER_COMMENT
import condom.best.condom.Dialog.RatingDialog
import condom.best.condom.MainActivity.Companion.currentUser
import condom.best.condom.MainActivity.Companion.userActInfo
import condom.best.condom.MainActivity.Companion.userInfo
import condom.best.condom.R
import kotlinx.android.synthetic.main.fragment_product_review.view.*


class ProductReviewFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productData = it.getSerializable("productData") as ProductInfo
        }
    }
    private val COMMENT_SET_RESULT = 1952
    private val COMMENT_UPDATE_RESULT = 1953

    private val storage = FirebaseStorage.getInstance("gs://condom-55a91").reference
    private val db = FirebaseFirestore.getInstance()

    private lateinit var productData: ProductInfo
    //트랜잭션 안겹치게
    private var transactionBool = true
    private var oldRating = 0F
    //유저 등록 코맨트
    private var userRatingData = UserRatingData()
    //제품평가 어레이
    private var ratingArray = ProductRating()

    private lateinit var rootView : View

    private val reviewList = ProductReviewData_Like()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_product_review, container, false)

        val reviewListAdapter = ProductReviewListAdapter(context!!,reviewList)

        db.collection(PRODUCT_RATING)
                .document(productData.prodName)
                .get()
                .addOnCompleteListener {
                    //제품 데이터 가져오기 / 리사이클러뷰 갱신
                    try {
                        ratingArray = it.result.toObject(ProductRating::class.java)!!
                    }catch (e:KotlinNullPointerException){}
                }
        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                .get()
                .addOnCompleteListener {
                    //유저 제품 기록 데이터
                    try {
                        userRatingData = it.result.toObject(UserRatingData::class.java)!!
                        userRatingData.prodName = productData.prodName
                        //유저제품기록 세팅
                        if(userRatingData.ratingPoint>0){//평가했을시 레이팅바 비지블
                            rootView.ratingPointText.visibility = View.VISIBLE
                            rootView.ratingIcon.visibility = View.GONE
                            rootView.ratingSetText.visibility = View.GONE
                            rootView.ratingPointText.text = userRatingData.ratingPoint.toString()
//                            rootView.ratingBar.rating = userRatingData.ratingPoint
                            oldRating = userRatingData.ratingPoint
                        }else{
                            rootView.ratingPointText.visibility = View.GONE
                            rootView.ratingIcon.visibility = View.VISIBLE
                            rootView.ratingSetText.visibility = View.VISIBLE
                        }
                        //코맨트창, 아이콘
                        if(userRatingData.reviewComment.isNotEmpty()){
                            commentView()
                        }
                        //위시아이콘
                        updateWish()
                    }catch (e:KotlinNullPointerException){}
                }
        //배댓 가져오기
        db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).orderBy("likeNum", Query.Direction.DESCENDING).limit(3)
                .get()
                .addOnCompleteListener {
                    var adapterSetBool = false
                    //제품 데이터 가져오기 / 리사이클러뷰 갱신
                    try {
                        //배댓 데이터
                        it.result.mapTo(reviewList.productReviewData) { (it.toObject(ProductReviewData::class.java)) }
                        for(i in 0 until reviewList.productReviewData.size){
                            db.collection(USER_INFO).document(reviewList.productReviewData[i].userUid).get().addOnCompleteListener {
                                //배댓 각각 유저 데이터
                                try {
                                    reviewList.reviewerInfo.add(it.result.toObject(UserInfo::class.java)!!)
                                    //마지막 데이터 다들고오면 어뎁터 실행
                                    if (i == reviewList.productReviewData.size - 1) {
                                        if(!adapterSetBool)
                                            adapterSetBool = true
                                        else {
                                            rootView.ReviewList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                                            rootView.ReviewList.adapter = reviewListAdapter
                                        }
                                    }
                                }catch (e:KotlinNullPointerException){}
                            }
                            //배댓 내가한 좋아요 셋
                            db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(reviewList.productReviewData[i].userUid).collection(COMMENT_LIKE)
                                    .document(currentUser!!.uid).get().addOnCompleteListener {
                                        //배댓 각각 유저 데이터
                                        try {
                                            reviewList.reviewLike.add(it.result.toObject(ReviewLike::class.java)!!)
                                        }catch (e:KotlinNullPointerException){
                                            reviewList.reviewLike.add(ReviewLike(false))
                                        }
                                        if (i == reviewList.productReviewData.size - 1) {
                                            if(!adapterSetBool)
                                                adapterSetBool = true
                                            else {
                                                rootView.ReviewList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                                                rootView.ReviewList.adapter = reviewListAdapter
                                            }
                                        }
                                    }
                        }
                    }catch (e:KotlinNullPointerException){}
                }
        //노이즈,상품사진
        GlideApp.with(rootView.context)
                .load(storage.child(productData.prodImage))
                .centerCrop()
                .into(rootView.prodImage)
        GlideApp.with(rootView.context)
                .load(storage.child(productData.prodImage))
                .transform(MultiTransformation(
                        CenterCrop(), BlurTransformation(rootView.context))
                )
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(rootView.prodImageSecondary)
        //태그
        val pListAdapter = ProductTagListAdapter(rootView.context,productData.prodTag)
        rootView.proTagList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rootView.proTagList.adapter = pListAdapter
        //품명,제조사
        rootView.prodName.text = productData.prodName
        rootView.prodCompany.text = COMPANY_CODE[productData.prodCompany-1]
        //평점
        val point = if(productData.prodRatingNum==0)
            0F
        else
            productData.prodPoint/productData.prodRatingNum.toFloat()
        val strNumber = String.format("%.2f", point)
        rootView.prodRatingPoint.text = getString(R.string.ratingComment1)+" "+strNumber+" ("+productData.prodRatingNum+getString(R.string.ratingComment2)
        rootView.prodRating.rating = point
        //평가 다이얼로그
        val adapter = RatingDialog(rootView.context, 1,productData)
        val dialog = DialogPlus.newDialog(rootView.context)
                .setAdapter(adapter)
                .setExpanded(false, 600)
                .setOnCancelListener {
                    transactionBool = true
                } //다이얼로그 외부 클릭시 종료
                .create()
        //평가하기
        rootView.ratingButton.setOnClickListener {
            if(transactionBool) {
                dialog.show()
                transactionBool = false
            }
        }
        //인터페이스. 평가완료
        RatingDialog.productRating(object : RatingDialog.InterfaceRatingSuccess{
            override fun interfaceClickProd(rating: Float) {
                dialog.dismiss()
                ratingPointUpdate(rating)
            }
        })
        //리뷰 달기
        rootView.reviewButton.setOnClickListener {
            if(userRatingData.reviewComment.isNotEmpty()) {
                //리뷰수정
                val intent = Intent(activity, ReviewActivity::class.java)
                intent.putExtra(RATING_POINT, userRatingData.ratingPoint)
                intent.putExtra(USER_COMMENT, userRatingData.reviewComment)
                startActivityForResult(intent, COMMENT_UPDATE_RESULT)
            }else {
                val intent = Intent(activity, ReviewActivity::class.java)
                intent.putExtra(RATING_POINT, userRatingData.ratingPoint)
                startActivityForResult(intent, COMMENT_SET_RESULT)
            }
        }
        //리뷰 수정
        rootView.reviewCustom.setOnClickListener {
            val intent = Intent(activity, ReviewActivity::class.java)
            intent.putExtra(RATING_POINT, userRatingData.ratingPoint)
            intent.putExtra(USER_COMMENT, userRatingData.reviewComment)
            startActivityForResult(intent, COMMENT_UPDATE_RESULT)
        }
        //리뷰삭제
        rootView.reviewRemove.setOnClickListener {
            userRatingData.reviewComment = ""
            commentView()
            //제품 리뷰 데이터
            db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid).delete()
            //내 리뷰 데이터
            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName).delete()
            //유저 활동 데이터 업데이트
            userActInfo.reviewNum--
            db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("reviewNum",userActInfo.reviewNum)
        }
        //위시리스트
        var wishCount = 0
        rootView.wishButton.setOnClickListener {
            userRatingData.wishBool = !userRatingData.wishBool
            updateWish()
            wishCount++
            val mHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    wishCount--
                    if(wishCount==0) {
                        if (userRatingData.wishBool) {
                            //위시 등록
                            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                    .update("wishBool", userRatingData.wishBool)
                                    .addOnFailureListener {
                                        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                                .set(userRatingData)
                                    }
                            //위시리스트 개인 저장
                            db.collection(USER_RATING_DATA).document(USER_WISH_LIST).collection(currentUser!!.uid).document(productData.prodName)
                                    .set(UserWishData(productData.prodName, productData.prodImage))
                            //유저 평가, 리뷰, 위시 상황
                            db.collection(USER_RATING_DATA).document(USER_WISH_LIST).collection(currentUser!!.uid).document(productData.prodName)
                                    .set(UserWishData(productData.prodName, productData.prodImage))
                            userActInfo.wishNum++
                            db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("wishNum",userActInfo.wishNum)
                        } else {
                            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                    .update("wishBool", userRatingData.wishBool)
                                    .addOnFailureListener {
                                        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                                .set(userRatingData)
                                    }
                            //위시리스트 개인 저장
                            db.collection(USER_RATING_DATA).document(USER_WISH_LIST).collection(currentUser!!.uid).document(productData.prodName)
                                    .delete()
                            userActInfo.wishNum--
                            db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("wishNum",userActInfo.wishNum)
                        }
                    }
                }
            }
            mHandler.sendEmptyMessageDelayed(0, 1000)
        }
        //제품 정보 더보기
        rootView.product_info_more.setOnClickListener {
            startActivity(Intent(activity,ProductInfoMore::class.java))
        }
        var likeClickBool = true
        //리뷰좋아요
        ProductReviewListAdapter.ProductReviewViewHolder.reviewLike(object : ProductReviewListAdapter.ProductReviewViewHolder.InterfaceReviewLike{
            override fun interfaceLikeClick(likeBool: Boolean,position : Int) {
                if(likeClickBool) {
                    likeClickBool = false
                    reviewList.reviewLike[position].like = !likeBool
                    if (likeBool)
                        reviewList.productReviewData[position].likeNum--
                    else
                        reviewList.productReviewData[position].likeNum++

                    val sfDocRef = db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(reviewList.productReviewData[position].userUid)
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(sfDocRef)
                        val likeNum = if (reviewList.reviewLike[position].like)
                            snapshot.getLong("likeNum")!! + 1
                        else
                            snapshot.getLong("likeNum")!! - 1
                        transaction.update(sfDocRef, "likeNum", likeNum)
                        likeClickBool = true
                        likeNum
                    }.addOnSuccessListener {}
                            .addOnFailureListener { e -> Log.w("asdasd", "Transaction failure.", e) }

                    reviewListAdapter.notifyDataSetChanged()
                }
            }
        })
        return rootView
    }

    private fun updateWish() {
        if(userRatingData.wishBool) {
            rootView.wishIcon.background = rootView.resources.getDrawable(R.drawable.ic_wish_ok)
            rootView.wishText.visibility = View.GONE
        }
        else {
            rootView.wishIcon.background = rootView.resources.getDrawable(R.drawable.ic_wish)
            rootView.wishText.visibility = View.VISIBLE
        }
    }

    //코멘트뷰 비지블
    private fun commentView() {
        if(userRatingData.reviewComment.isNotEmpty()) {
            rootView.userCommentView.expand()
            rootView.userCommentText.text = userRatingData.reviewComment
            rootView.userCommentView2.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            rootView.reviewSetText.visibility = View.GONE
            rootView.reviewRegitIcon.background = rootView.resources.getDrawable(R.drawable.ic_review_ok)
            rootView.reviewRegitIcon.isActivated = false
        }else{
            rootView.userCommentView.collapse()
            rootView.reviewSetText.visibility = View.VISIBLE
            rootView.reviewRegitIcon.background = rootView.resources.getDrawable(R.drawable.ic_review)
            rootView.reviewRegitIcon.isActivated = true
        }
    }

    @SuppressLint("SetTextI18n")
    //평가, 댓글달고 반영
    private fun update(rating: Float, bool: Boolean, view: View, prodPintArr: ProductRating, userPoint: Float){
        oldRating = rating
        transactionBool=bool
        //평점
        val point = if(productData.prodRatingNum==0)
            0F
        else
            productData.prodPoint/productData.prodRatingNum.toFloat()
        val strNumber = String.format("%.2f", point)
        rootView.prodRatingPoint.text = getString(R.string.ratingComment1)+" "+strNumber+" ("+productData.prodRatingNum+getString(R.string.ratingComment2)
        rootView.prodRating.rating = point
        //유저 개인 레이팅바
        rootView.ratingPointText.text = userPoint.toString()
        userRatingData.ratingPoint = userPoint

        //유저 개인 평가 데이터
        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                .update("ratingPoint",userRatingData.ratingPoint)
                .addOnSuccessListener {  }
                .addOnFailureListener {
                    db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                            .set(userRatingData)
                }
        if(userRatingData.reviewComment.isNotEmpty())
            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                    .update("reviewComment",userRatingData.reviewComment)
                    .addOnSuccessListener {  }
                    .addOnFailureListener {
                        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                .set(userRatingData)
                    }
        commentView()
        Snackbar.make(view,getString(R.string.rating_reflect),Snackbar.LENGTH_SHORT).show()
    }
    private fun ratingPointUpdate(rating : Float){
        if(rating==0.0f) {
            //평가 취소
            rootView.ratingPointText.visibility = View.GONE
            rootView.ratingIcon.visibility = View.VISIBLE
            rootView.ratingSetText.visibility = View.VISIBLE
            var overlap = 0

            var prodPointArr = ProductRating()
            var prodPoint = 0.0
            var prodNum = 0L

            update(0F, true, rootView, prodPointArr, rating)

            val sfDocRef = db.collection(PRODUCT_RATING).document(productData.prodName)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(sfDocRef)
                prodPointArr = (snapshot.toObject(ProductRating::class.java))!!
                prodPointArr.ratingData[((oldRating - 0.5) * 2).toInt()]--
                val arrayList = prodPointArr.ratingData
                transaction.update(sfDocRef, "ratingData", arrayList)
                prodPointArr
            }.addOnSuccessListener {}.addOnFailureListener {}

            val sfDocRef2 = db.collection(PRODUCT_INFO).document(productData.prodName)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(sfDocRef2)
                prodPoint = snapshot.getDouble("prodPoint")!!
                prodNum = snapshot.getLong("prodRatingNum")!!
                prodPoint -= oldRating
                prodNum--
                transaction.update(sfDocRef2, "prodPoint", prodPoint)
                transaction.update(sfDocRef2, "prodRatingNum", prodNum)
                //평가점수 바꾸면 정정되게
                prodPoint
            }.addOnSuccessListener {}.addOnFailureListener {}
        }else{
            //평가
            rootView.ratingPointText.visibility = View.VISIBLE
            rootView.ratingPointText.text = rating.toString()
            rootView.ratingIcon.visibility = View.GONE
            rootView.ratingSetText.visibility = View.GONE
            var overlap = 0
            //트랜잭션
            var prodPintArr = ProductRating()
            var prodPoint = 0.0
            var prodNum = 0L

            //평점
            val point = if(productData.prodRatingNum==0)
                0F
            else
                productData.prodPoint/productData.prodRatingNum.toFloat()
            val strNumber = String.format("%.2f", point)
            rootView.prodRatingPoint.text = getString(R.string.ratingComment1)+" "+strNumber+" ("+productData.prodRatingNum+getString(R.string.ratingComment2)
            rootView.prodRating.rating = point

            //평가점수 업데이트
            update(rating, true, rootView, prodPintArr, rating)

            val sfDocRef = db.collection(PRODUCT_RATING).document(productData.prodName)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(sfDocRef)
                prodPintArr = (snapshot.toObject(ProductRating::class.java))!!
                if(oldRating==0F)//정상평가
                    prodPintArr.ratingData[((rating-0.5)*2).toInt()]++
                else {//평가정정
                    prodPintArr.ratingData[((rating - 0.5) * 2).toInt()]++
                    prodPintArr.ratingData[((oldRating-0.5)*2).toInt()]--
                }
                val arrayList = prodPintArr.ratingData
                transaction.update(sfDocRef,"ratingData", arrayList)
                prodPintArr
            }.addOnSuccessListener {}.addOnFailureListener {}

            val sfDocRef2 = db.collection(PRODUCT_INFO).document(productData.prodName)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(sfDocRef2)
                prodPoint = snapshot.getDouble("prodPoint")!!
                prodNum = snapshot.getLong("prodRatingNum")!!
                prodPoint += if(oldRating==0F) {
                    prodNum++
                    rating
                } else {//평가변경
                    -oldRating+rating
                }
                transaction.update(sfDocRef2,"prodPoint", prodPoint)
                transaction.update(sfDocRef2,"prodRatingNum", prodNum)
                //평가점수 바꾸면 정정되게
                prodPoint
            }.addOnSuccessListener {}.addOnFailureListener {}

        }}

    companion object {
        @JvmStatic
        fun prodData(productData: ProductInfo) =
                ProductReviewFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("productData", productData)
                    }
                }
    }
    //TODO 내리뷰 보이게 && 베스트리뷰 보이게
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == COMMENT_SET_RESULT){
            //리뷰 셋
            if(resultCode == 123){
                val rating = data!!.getFloatExtra(RATING_POINT,0f)
                val review = data.getStringExtra(REVIEW)
                userRatingData.reviewComment = review
                commentView()
                val trueTime = try {
                    TrueTimeRx.now().time
                }catch (e : IllegalStateException){
                    System.currentTimeMillis()
                }
                db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid)
                        .set(ProductReviewData(review,trueTime, currentUser!!.uid, 0, userInfo.gender,rating,0))
                ratingPointUpdate(rating)
                //유저 활동 데이터 업데이트
                userActInfo.reviewNum++
                userActInfo.ratingNum++
                db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("reviewNum",userActInfo.reviewNum)
                db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("ratingNum",userActInfo.ratingNum)
            }
        }else if(requestCode == COMMENT_UPDATE_RESULT){
            //리뷰 업데이트
            if(resultCode == 123){
                val rating = data!!.getFloatExtra(RATING_POINT,0f)
                val review = data.getStringExtra(REVIEW)
                if(userRatingData.reviewComment != review) {
                    //리뷰수정시
                    db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid)
                            .update("review", review)
                    //유저 개인 리뷰
                    if(userRatingData.reviewComment.isNotEmpty())
                        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                .update("reviewComment",userRatingData.reviewComment)
                                .addOnSuccessListener {  }
                                .addOnFailureListener {
                                    db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                            .set(userRatingData)
                                }
                    userRatingData.reviewComment = review
                    commentView()
                }
                if(userRatingData.ratingPoint != rating) {
                    db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid)
                            .update("rating", rating)
                    ratingPointUpdate(rating)
                }
            }
        }
    }
}
