package condom.best.condom.View.BottomNavPage.Product

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
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.reflect.TypeToken
import com.instacart.library.truetime.TrueTimeRx
import com.orhanobut.dialogplus.DialogPlus
import condom.best.condom.BottomNavPage.Product.ProductInfoMore
import condom.best.condom.BottomNavPage.Product.ReviewActivity
import condom.best.condom.BottomNavPage.Product.ReviewMoreFragment
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BottomNavPage.Adapter.ProductReviewListAdapter
import condom.best.condom.View.BottomNavPage.Adapter.ProductTagListAdapter
import condom.best.condom.View.BottomNavPage.HomeFragment.Companion.pList
import condom.best.condom.View.Data.*
import condom.best.condom.View.Data.FirebaseConst.Companion.COMMENT
import condom.best.condom.View.Data.FirebaseConst.Companion.COMMENT_LIKE
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_INFO
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_RATING
import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_REVIEWS
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_LIKE_DEFAULT
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_LIKE_OFF
import condom.best.condom.View.Data.FirebaseConst.Companion.REVIEW_LIKE_ON
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_ACT_INFO
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_RATING_DATA
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_RATING_LIST
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_REVIEW_LIST
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_WISH_LIST
import condom.best.condom.View.Data.StringData.Companion.RATING_POINT
import condom.best.condom.View.Data.StringData.Companion.REVIEW
import condom.best.condom.View.Data.StringData.Companion.USER_COMMENT
import condom.best.condom.View.Data.UserLocalDataPath.Companion.USER_ACT_INFO_PATH
import condom.best.condom.View.Data.UserLocalDataPath.Companion.userProductActPath
import condom.best.condom.View.Data.UserLocalDataPath.Companion.userReviewLikePath
import condom.best.condom.View.Dialog.RatingDialog
import condom.best.condom.View.MainActivity
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.gson
import condom.best.condom.View.MainActivity.Companion.localDataGet
import condom.best.condom.View.MainActivity.Companion.localDataPut
import condom.best.condom.View.MainActivity.Companion.userActInfo
import kotlinx.android.synthetic.main.fragment_product_review.view.*

@Suppress("DEPRECATION")
class ProductReviewFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productData = it.getSerializable("productData") as ProductInfo
            productIndex =pList.indexOf(productData)
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

    private var productIndex = 0
    lateinit var reviewListAdapter : ProductReviewListAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_product_review, container, false)

        reviewListAdapter = ProductReviewListAdapter(context!!,reviewList,ReviewCommentList_Like(),1)

        db.collection(PRODUCT_RATING)
                .document(productData.prodName)
                .get()
                .addOnCompleteListener { //제품 평가 데이터
                    try {
                        ratingArray = it.result.toObject(ProductRating::class.java)!!
                    }catch (e:KotlinNullPointerException){}
                }

        //배댓 가져오기
        db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).orderBy("likeNum", Query.Direction.DESCENDING).limit(3).get()
                .addOnCompleteListener { it ->
                    var adapterSetBool = false
                    fun adapterRefresh(){
                        if(!adapterSetBool)
                            adapterSetBool = true
                        else {
                            rootView.ReviewList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                            rootView.ReviewList.adapter = reviewListAdapter
                        }
                        emptyViewVisible()
                    }
                    //제품 데이터 가져오기 / 리사이클러뷰 갱신
                    try {
                        //배댓 데이터
                        it.result.mapTo(reviewList.productReviewData) { it.toObject(ProductReviewData::class.java) }
                        var settingEnd = reviewList.productReviewData.size*2
                        for(i in 0 until reviewList.productReviewData.size){
                            db.collection(USER_INFO).document(reviewList.productReviewData[i].userUid).get().addOnCompleteListener {
                                //배댓 유저 데이터
                                try {
                                    reviewList.reviewerInfo.add(it.result.toObject(UserInfo::class.java)!!)
                                    //마지막 데이터 다들고오면 어뎁터 실행
                                    settingEnd--
                                    if (i == reviewList.productReviewData.size - 1 && settingEnd == 0) {
                                        adapterRefresh()
                                    }
                                }catch (e:KotlinNullPointerException){}
                            }
                            //배댓 내가한 좋아요 셋

                            val likeStateGet = localDataGet.getInt(userReviewLikePath(productData.prodName,reviewList.productReviewData[i].userUid,reviewList.productReviewData[i].date), REVIEW_LIKE_DEFAULT)
                            when(likeStateGet){
                                REVIEW_LIKE_ON-> {
                                    reviewList.reviewLike.add(ReviewLike(true))
                                    settingEnd--
                                }
                                REVIEW_LIKE_OFF-> {
                                    reviewList.reviewLike.add(ReviewLike(false))
                                    settingEnd--
                                }
                                else->{
                                    db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(reviewList.productReviewData[i].userUid)
                                            .collection(COMMENT_LIKE).document(currentUser!!.uid).get().addOnCompleteListener {
                                                //배댓 각각 유저 데이터
                                                try {
                                                    reviewList.reviewLike.add(it.result.toObject(ReviewLike::class.java)!!)
                                                    localDataPut.putInt(userReviewLikePath(productData.prodName,reviewList.productReviewData[i].userUid,reviewList.productReviewData[i].date), REVIEW_LIKE_ON)
                                                }catch (e:KotlinNullPointerException){
                                                    reviewList.reviewLike.add(ReviewLike())
                                                    localDataPut.putInt(userReviewLikePath(productData.prodName,reviewList.productReviewData[i].userUid,reviewList.productReviewData[i].date), REVIEW_LIKE_OFF)
                                                }
                                                localDataPut.commit()
                                                settingEnd--
                                                if (i == reviewList.productReviewData.size - 1 && settingEnd == 0) {
                                                    adapterRefresh()
                                                }
                                            }
                                }
                            }
                        }
                        adapterRefresh()
                    }catch (e:KotlinNullPointerException){}
                }
        val userRatingDataJson = localDataGet.getString(userProductActPath(productData.prodName),"none")
        if(userRatingDataJson!="none"){ //유저 제품 기록 데이터
            val mHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    userRatingData = gson.fromJson(userRatingDataJson, UserRatingData::class.java)
                    userRatingDataSetting()
                }
            }
            mHandler.sendEmptyMessageDelayed(0, 50)
        }else
            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName).get()
                    .addOnCompleteListener {
                        try {//데이터 로컬에 저장
                            userRatingData = it.result.toObject(UserRatingData::class.java)!!
                            userRatingDataSetting()
                            val strContact = gson.toJson(userRatingData, UserRatingData::class.java)
                            localDataPut.putString(userProductActPath(productData.prodName),strContact)
                            localDataPut.commit()
                        }catch (e:KotlinNullPointerException){}
                    }

        //노이즈,상품사진
        GlideApp.with(rootView.context)
                .load(storage.child(productData.prodImage))
                .centerCrop()
                .into(rootView.prodImage)
        GlideApp.with(rootView.context)
                .load(storage.child(productData.prodImage))
                .transform(MultiTransformation(CenterCrop(), BlurTransformation(rootView.context)))
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(rootView.prodImageSecondary)
        //태그
        val pListAdapter = ProductTagListAdapter(rootView.context,productData.prodTag,0)// tag size normal
        rootView.proTagList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rootView.proTagList.adapter = pListAdapter
        //품명,제조사
        rootView.prodName.text = productData.prodName
        rootView.prodCompany.text = productData.prodCompany
        //평점
        ratingUiUpdate(productData.prodPoint,productData.prodRatingNum)
        //평가 다이얼로그
        var adapter = RatingDialog(rootView.context, 1,productData,userRatingData.ratingPoint)
        var dialog = DialogPlus.newDialog(rootView.context)
                .setAdapter(adapter)
                .setExpanded(false, 600)
                .setOnCancelListener {
                    transactionBool = true
                } //다이얼로그 외부 클릭시 종료
                .create()
        //평가하기
        rootView.ratingButton.setOnClickListener { _ ->
            if(transactionBool) {
                adapter = RatingDialog(rootView.context, 1,productData,userRatingData.ratingPoint)
                dialog = DialogPlus.newDialog(rootView.context)
                        .setAdapter(adapter)
                        .setExpanded(false, 600)
                        .setOnCancelListener {
                            transactionBool = true
                        } //다이얼로그 외부 클릭시 종료
                        .create()
                dialog.show()
                transactionBool = false
            }
        }
        //인터페이스. 다이얼로그 평가완료
        RatingDialog.productRating(object : RatingDialog.InterfaceRatingSuccess{
            override fun interfaceClickProd(rating: Float) {
                dialog.dismiss()
                userRatingData.ratingPoint = rating
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

            val trueTime = try { TrueTimeRx.now().time }
            catch (e : IllegalStateException){ System.currentTimeMillis() }

            val ratingData = UserReviewDataList(productData.prodName,productData.prodCompany, productData.prodImage,trueTime) //개인 저장 데이터

            val ratingDataJson = localDataGet.getString(UserLocalDataPath.USER_REVIEW_LIST_PATH, "none")
            val listType = object : TypeToken<ArrayList<UserReviewDataList>>() {}.type

            //위시리스트 개인 db 삭제
            db.collection(USER_ACT_INFO).document(USER_REVIEW_LIST).collection(currentUser!!.uid).document(productData.prodName).delete()
            //평가리스트 로컬 저장
            if(ratingDataJson != "none") {
                val ratingList: ArrayList<UserReviewDataList> = gson.fromJson(ratingDataJson, listType)
                for(i in 0 until ratingList.size){
                    if(ratingList[i].prodName == ratingData.prodName) {
                        ratingList.removeAt(i)
                        break
                    }
                }
                val strContact = gson.toJson(ratingList, listType)
                localDataPut.putString(UserLocalDataPath.USER_REVIEW_LIST_PATH, strContact) //로컬에 전체 저장
                localDataPut.commit()
            }
            //제품 리뷰 데이터
            db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid).delete()
            //내 리뷰 데이터
            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName).delete()
            //유저 활동 데이터 업데이트
            userActInfo.reviewNum--
            db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("reviewNum",userActInfo.reviewNum)
            localDataSave()
        }
        //위시리스트 클릭
        var wishCount = 0
        var wishOverlap = true
        rootView.wishButton.setOnClickListener { _ ->
            if(wishOverlap) {
                wishOverlap = false
                userRatingData.wishBool = !userRatingData.wishBool
                updateWish()
                wishCount++
                val mHandler = @SuppressLint("HandlerLeak")
                object : Handler() {
                    override fun handleMessage(msg: Message) {
                        wishCount--
                        if (wishCount == 0) {
                            val trueTime = try {
                                TrueTimeRx.now().time
                            }catch (e : IllegalStateException){
                                System.currentTimeMillis()
                            }
                            val wishList = UserWishDataList(productData.prodName, productData.prodCompany,productData.prodImage,trueTime)

                            if (userRatingData.wishBool) {
                                //위시 등록
                                db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                        .update("wishBool", userRatingData.wishBool)
                                        .addOnFailureListener {
                                            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName).set(userRatingData)
                                        }

                                //위시리스트 개인 db 저장
                                db.collection(USER_ACT_INFO).document(USER_WISH_LIST).collection(currentUser!!.uid).document(productData.prodName).set(wishList)
                                //위시리스트 로컬 저장
                                val ratingDataJson = localDataGet.getString(UserLocalDataPath.USER_WISH_LIST_PATH, "none")
                                val listType = object : TypeToken<ArrayList<UserWishDataList>>() {}.type
                                val ratingList: ArrayList<UserWishDataList> = if(ratingDataJson != "none") {
                                    gson.fromJson(ratingDataJson, listType)
                                }else {
                                    arrayListOf()
                                }
                                ratingList.add(wishList)
                                val strContact = gson.toJson(ratingList, listType)

                                localDataPut.putString(UserLocalDataPath.USER_WISH_LIST_PATH, strContact) //로컬에 전체 저장
                                localDataPut.commit()
                                //유저 평가, 리뷰, 위시 상황
                                userActInfo.wishNum++
                                db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("wishNum", userActInfo.wishNum)
                            } else {
                                db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                                        .update("wishBool", userRatingData.wishBool)
                                        .addOnFailureListener {
                                            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName).set(userRatingData)
                                        }
                                //위시리스트 개인 db 삭제
                                db.collection(USER_ACT_INFO).document(USER_WISH_LIST).collection(currentUser!!.uid).document(productData.prodName).delete()
                                //위시리스트 로컬 저장
                                val ratingDataJson = localDataGet.getString(UserLocalDataPath.USER_WISH_LIST_PATH, "none")
                                if(ratingDataJson != "none") {
                                    val listType = object : TypeToken<ArrayList<UserWishDataList>>() {}.type
                                    val ratingList: ArrayList<UserWishDataList> = gson.fromJson(ratingDataJson, listType)
                                    for(i in 0 until ratingList.size){
                                        if(ratingList[i].prodName == wishList.prodName) {
                                            ratingList.removeAt(i)
                                            break
                                        }
                                    }
                                    val strContact = gson.toJson(ratingList, listType)
                                    localDataPut.putString(UserLocalDataPath.USER_WISH_LIST_PATH, strContact) //로컬에 전체 저장
                                    localDataPut.commit()
                                }
                                userActInfo.wishNum--
                                db.collection(USER_ACT_INFO).document(currentUser!!.uid).update("wishNum", userActInfo.wishNum)
                            }
                            db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("reviewNum",userActInfo.reviewNum)
//                            editor.putString(currentUser!!.uid+USER_RATING_DATA+productData.prodName,"none")  //뭐지?;
//                            editor.commit()
                            wishOverlap = true
                            localDataSave()
                        }
                    }
                }
                mHandler.sendEmptyMessageDelayed(0, 1000)
            }
        }
        //제품 단위
        var unitText = ""
        for(i in 0 until productData.sellUnit.size) {
            unitText += "${productData.sellUnit[i]}p"
            if(i!=productData.sellUnit.size-1)
                unitText += "/"
        }
        rootView.unitText.text = "판매 단위 - $unitText"
        //제품 설명
        rootView.featureText.text = "제품 설명 - ${productData.prodFeature}"
        //제품 성분
        rootView.ingredientText.text = "전성분 - ${productData.prodIngredient}"

        rootView.userCommentView1.setOnClickListener { //내 리뷰 클릭시 리뷰자세히보기 페이지이동
            (activity as MainActivity).reviewDetailFragment= ReviewDetailFragment.newInstance(productData,userRatingData, ProductReviewData(), UserInfo(),false,1)
            FragmentUtil.fragmentAddChanger((activity as MainActivity),(activity as MainActivity).reviewDetailFragment,"HOME")
        }

        rootView.product_info_more.setOnClickListener { //제품 정보 더보기
            (activity as MainActivity).productInfoMoreFragment = ProductInfoMore.newInstance(productData)
            FragmentUtil.fragmentAddChanger((activity as MainActivity),(activity as MainActivity).productInfoMoreFragment,"HOME")
        }

        rootView.product_review_more.setOnClickListener { //리뷰 더보기
            (activity as MainActivity).reviewMoreFragment = ReviewMoreFragment.newInstance(productData)
            FragmentUtil.fragmentAddChanger((activity as MainActivity),(activity as MainActivity).reviewMoreFragment,"HOME")
        }
        var likeClickBool = true
        //리뷰 자세히보기 화면 이동, 리뷰 좋아요
        ProductReviewListAdapter.ProductReviewViewHolder.reviewLike(object : ProductReviewListAdapter.ProductReviewViewHolder.InterfaceReviewLike{
            override fun interfaceReviewClick(likeBool: Boolean, productReviewData: ProductReviewData, uesrInfo: UserInfo, position: Int) {//리뷰 자세히보기
                (activity as MainActivity).reviewDetailFragment= ReviewDetailFragment.newInstance(productData, UserRatingData(),productReviewData,uesrInfo,likeBool,2)
                FragmentUtil.fragmentAddChanger((activity as MainActivity),(activity as MainActivity).reviewDetailFragment,"HOME")
            }
            override fun interfaceLikeClick(likeBool: Boolean, productReviewData: ProductReviewData, position: Int) {//리뷰 좋아요
                if(likeClickBool) {
                    likeClickBool = false
                    reviewList.reviewLike[position].like = !likeBool
                    if (likeBool)
                        reviewList.productReviewData[position].likeNum--
                    else
                        reviewList.productReviewData[position].likeNum++

                    val sfDocRef = db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid)

                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(sfDocRef)
                        val likeNum = if (reviewList.reviewLike[position].like) {//좋아요 ok
                            //평가리스트 로컬 저장
                            localDataPut.putInt(userReviewLikePath(productData.prodName,productReviewData.userUid,productReviewData.date), REVIEW_LIKE_ON)
                            db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid).collection(COMMENT_LIKE)
                                    .document(currentUser!!.uid).set(ReviewLike(true))
                            snapshot.getLong("likeNum")!! + 1
                        }
                        else {//좋아요 취소
                            localDataPut.putInt(userReviewLikePath(productData.prodName,productReviewData.userUid,productReviewData.date), REVIEW_LIKE_OFF)
                            db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(productReviewData.userUid).collection(COMMENT_LIKE)
                                    .document(currentUser!!.uid).set(ReviewLike(false))
                            snapshot.getLong("likeNum")!! - 1
                        }
                        localDataPut.commit()
                        transaction.update(sfDocRef, "likeNum", likeNum)
                        likeClickBool = true
                        likeNum
                    }.addOnSuccessListener {}.addOnFailureListener {}
                    reviewListAdapter.notifyDataSetChanged()
                    localDataSave()
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
        localDataSave()
    }

    //내 코멘트뷰 비지블
    private fun commentView() {
        if(userRatingData.reviewComment.isNotEmpty()) {
            val index = localDataGet.getInt(getString(R.string.userProfileChange),0)
            if(currentUser!!.photoUrl.toString() == "null")
                GlideApp.with(this)
                        .load(context!!.resources.getDrawable(R.drawable.ic_user))
                        .apply(RequestOptions().centerCrop())
                        .apply(RequestOptions.circleCropTransform())
                        .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}$index")))
                        .into(rootView.userCommentViewProfile)
            else
                GlideApp.with(this)
                        .load(storage.child(currentUser!!.photoUrl.toString()))
                        .apply(RequestOptions().centerCrop())
                        .apply(RequestOptions.circleCropTransform())
                        .apply(RequestOptions.placeholderOf(context!!.resources.getDrawable(R.drawable.ic_user)))
                        .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}$index")))
                        .transition(DrawableTransitionOptions.withCrossFade(100))
                        .into(rootView.userCommentViewProfile)

            rootView.userCommentText.text = userRatingData.reviewComment
            rootView.reviewSetText.visibility = View.GONE
            rootView.reviewRegitIcon.background = rootView.resources.getDrawable(R.drawable.ic_review_ok)
            rootView.reviewRegitIcon.isActivated = false
            rootView.userCommentView.expand()
        }else{
            rootView.userCommentView.collapse()
            rootView.reviewSetText.visibility = View.VISIBLE
            rootView.reviewRegitIcon.background = rootView.resources.getDrawable(R.drawable.ic_review)
            rootView.reviewRegitIcon.isActivated = true
        }
    }

    @SuppressLint("SetTextI18n")
    //평가, 댓글달고 반영
    private fun update(rating: Float, bool: Boolean, view: View, prodPintArr: ProductRating, prodPoint: Double, prodNum: Long, userPoint : Float){
        oldRating = rating
        transactionBool=bool
        //평점
        ratingUiUpdate(prodPoint.toFloat(),prodNum.toInt())
        //유저 개인 별점
//        rootView.ratingBar.rating = userPoint
        rootView.ratingPointText.text = userPoint.toString()
        userRatingData.ratingPoint = userPoint

        //유저 개인 평가 데이터
        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                .update("ratingPoint",userRatingData.ratingPoint)
                .addOnSuccessListener {  }
                .addOnFailureListener {
                    db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName).set(userRatingData)
                }
        if(userRatingData.reviewComment.isNotEmpty())
            db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
                    .update("reviewComment", userRatingData.reviewComment)
                    .addOnSuccessListener { }
                    .addOnFailureListener {
                        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName).set(userRatingData)
                    }
        localDataSave()
        commentView()
        Snackbar.make(view,getString(R.string.rating_reflect),Snackbar.LENGTH_SHORT).show()
    }
    @SuppressLint("SetTextI18n")
    private fun ratingUiUpdate(rating : Float, ratingNum : Int){//제품 점수, 레이팅바 업데이트
        var point = if(ratingNum==0)
            0F
        else
            rating/ratingNum.toFloat()
        if(point>=5)
            point = 5f
        else if(point<=0)
            point = 0f
        val strNumber = String.format("%.2f", point)
        rootView.prodRatingPoint.text = getString(R.string.ratingComment1)+" "+strNumber+" ("+ratingNum+getString(R.string.ratingComment2)
        rootView.prodRating.rating = point
    }
    private fun ratingPointUpdate(rating : Float){
        val sfDocRef = db.collection(PRODUCT_RATING).document(productData.prodName)
        val sfDocRef2 = db.collection(PRODUCT_INFO).document(productData.prodName)

        var overlap = 0
        //트랜잭션
        var prodPointArr = ProductRating()
        var prodPoint = 0.0
        var prodNum = 0L

        val trueTime = try { TrueTimeRx.now().time }
        catch (e : IllegalStateException){ System.currentTimeMillis() }

        val ratingData = UserRatingDataList(productData.prodName,rating,productData.prodCompany, productData.prodImage,trueTime) //개인 저장 데이터

        db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid).update("rating", rating)

        val ratingDataJson = localDataGet.getString(UserLocalDataPath.USER_RATING_LIST_PATH, "none")
        val listType = object : TypeToken<ArrayList<UserRatingDataList>>() {}.type

        if(rating==0.0f) { //평가 취소
            rootView.ratingPointText.visibility = View.GONE
            rootView.ratingIcon.visibility = View.VISIBLE
            rootView.ratingSetText.visibility = View.VISIBLE
            //유저 활동 데이터 업데이트
            userActInfo.ratingNum--
            db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("ratingNum",userActInfo.ratingNum)
            //위시리스트 개인 db 삭제
            db.collection(USER_ACT_INFO).document(USER_RATING_LIST).collection(currentUser!!.uid).document(productData.prodName).delete()
            //평가리스트 로컬 저장
            if(ratingDataJson != "none") {
                val ratingList: ArrayList<UserRatingDataList> = gson.fromJson(ratingDataJson, listType)
                for(i in 0 until ratingList.size){
                    if(ratingList[i].prodName == ratingData.prodName) {
                        ratingList.removeAt(i)
                        break
                    }
                }
                val strContact = gson.toJson(ratingList, listType)
                localDataPut.putString(UserLocalDataPath.USER_RATING_LIST_PATH, strContact) //로컬에 전체 저장
                localDataPut.commit()
            }
            //평점 그래프 수치 업데이트
            db.runTransaction { transaction ->
                val snapshot = transaction.get(sfDocRef)
                prodPointArr = (snapshot.toObject(ProductRating::class.java))!!
                prodPointArr.ratingData[((oldRating - 0.5) * 2).toInt()]--
                val arrayList = prodPointArr.ratingData
                transaction.update(sfDocRef, "ratingData", arrayList)
                localDataSave()
                prodPointArr
            }.addOnSuccessListener {
                overlap++
                if (overlap >= 2)
                    update(0F, true, rootView, prodPointArr, prodPoint, prodNum,rating)
            }.addOnFailureListener { e -> Log.w("asdasd", "Transaction failure.", e) }

            db.runTransaction { transaction -> //제품평점 업데이트
                val snapshot = transaction.get(sfDocRef2)
                prodPoint = snapshot.getDouble("prodPoint")!!
                prodNum = snapshot.getLong("prodRatingNum")!!
                prodPoint -= oldRating
                prodNum--
                transaction.update(sfDocRef2, "prodPoint", prodPoint)
                transaction.update(sfDocRef2, "prodRatingNum", prodNum)
                localDataSave()
                //평가점수 바꾸면 정정되게
                prodPoint
            }.addOnSuccessListener {
                overlap++
                if (overlap >= 2)
                    update(0F, true, rootView, prodPointArr, prodPoint, prodNum,rating)
            }.addOnFailureListener { e -> Log.w("asdasd", "Transaction failure.", e) }
        }else{ //평가함
            rootView.ratingPointText.visibility = View.VISIBLE
            rootView.ratingPointText.text = rating.toString()
            rootView.ratingIcon.visibility = View.GONE
            rootView.ratingSetText.visibility = View.GONE

            //평가리스트 개인 db 저장
            db.collection(USER_ACT_INFO).document(USER_RATING_LIST).collection(currentUser!!.uid).document(productData.prodName).set(ratingData)
            //평가리스트 로컬 저장
            val ratingList: ArrayList<UserRatingDataList> = if(ratingDataJson != "none") {
                gson.fromJson(ratingDataJson, listType)
            }else { arrayListOf() }
            for(i in 0 until ratingList.size){
                if(ratingList[i].prodName == ratingData.prodName) {
                    userActInfo.ratingNum--
                    ratingList.removeAt(i)
                    break
                }
            }
            //유저 활동 데이터 업데이트
            userActInfo.ratingNum++
            db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("ratingNum",userActInfo.ratingNum)

            ratingList.add(ratingData)
            val strContact = gson.toJson(ratingList, listType)
            localDataPut.putString(UserLocalDataPath.USER_RATING_LIST_PATH, strContact) //로컬에 전체 저장
            localDataPut.commit()
            //평점 그래프 수치 업데이트
            db.runTransaction { transaction ->
                val snapshot = transaction.get(sfDocRef)
                prodPointArr = (snapshot.toObject(ProductRating::class.java))!!
                if(oldRating==0F) {//정상평가
                    prodPointArr.ratingData[((rating - 0.5) * 2).toInt()]++
                } else {//평가정정
                    prodPointArr.ratingData[((rating - 0.5) * 2).toInt()]++
                    prodPointArr.ratingData[((oldRating-0.5)*2).toInt()]--
                }
                val arrayList = prodPointArr.ratingData
                transaction.update(sfDocRef,"ratingData", arrayList)
                localDataSave()
                prodPointArr
            }.addOnSuccessListener {
                overlap++
                //평가점수 바꾸면 정정되게
                if (overlap >= 2) update(rating,true,rootView,prodPointArr, prodPoint,prodNum,rating)
            }.addOnFailureListener { e -> Log.w("asdasd", "Transaction failure.", e) }

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
                localDataSave()
                //평가점수 바꾸면 정정되게
                prodPoint
            }.addOnSuccessListener {
                overlap++
                //평가점수 업데이트
                if (overlap >= 2) update(rating,true,rootView,prodPointArr, prodPoint,prodNum,rating)
            }.addOnFailureListener { e -> Log.w("asdasd", "Transaction failure.", e) }
        }
    }

    companion object {
        @JvmStatic
        fun prodData(productData: ProductInfo) =
                ProductReviewFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("productData", productData)
                    }
                }
    }
    private fun emptyViewVisible(){
        if(reviewList.productReviewData.size==0){
            rootView.emptyView.visibility = View.VISIBLE
            rootView.ReviewList.visibility = View.GONE
            rootView.product_review_more.visibility = View.GONE
        }else{
            rootView.emptyView.visibility = View.GONE
            rootView.ReviewList.visibility = View.VISIBLE
            rootView.product_review_more.visibility = View.VISIBLE
        }
    }
    private fun userRatingDataSetting(){
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
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == 123){
            //리뷰
            if(requestCode == COMMENT_SET_RESULT){
                //리뷰 저장
                val rating = data!!.getFloatExtra(RATING_POINT,0f)
                val review = data.getStringExtra(REVIEW)

                val trueTime = try { TrueTimeRx.now().time }
                catch (e : IllegalStateException){ System.currentTimeMillis() }

                val ratingData = UserReviewDataList(productData.prodName,productData.prodCompany, productData.prodImage,trueTime) //개인 저장 데이터

                val ratingDataJson = localDataGet.getString(UserLocalDataPath.USER_REVIEW_LIST_PATH, "none")
                val listType = object : TypeToken<ArrayList<UserReviewDataList>>() {}.type
                //평가리스트 개인 db 저장
                db.collection(USER_ACT_INFO).document(USER_REVIEW_LIST).collection(currentUser!!.uid).document(productData.prodName).set(ratingData)
                //평가리스트 로컬 저장
                val ratingList: ArrayList<UserReviewDataList> = if(ratingDataJson != "none") {
                    gson.fromJson(ratingDataJson, listType)
                }else { arrayListOf() }
                for(i in 0 until ratingList.size){
                    if(ratingList[i].prodName == ratingData.prodName) {
                        ratingList.removeAt(i)
                        break
                    }
                }
                ratingList.add(ratingData)
                val strContact = gson.toJson(ratingList, listType)
                localDataPut.putString(UserLocalDataPath.USER_REVIEW_LIST_PATH, strContact) //로컬에 전체 저장
                localDataPut.commit()

                userRatingData.reviewDate = trueTime
                userRatingData.reviewComment = review
                commentView()
                val reviewData = ProductReviewData(review,trueTime, currentUser!!.uid, 0 ,rating,0)
                db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid).set(reviewData)
                ratingPointUpdate(rating)
                //유저 활동 데이터 업데이트
                userActInfo.reviewNum++
                db.collection(FirebaseConst.USER_ACT_INFO).document(currentUser!!.uid).update("reviewNum",userActInfo.reviewNum)
                //유저 댓글 날짜 데이터
                db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName).set(userRatingData)
                localDataSave()
            }else if(requestCode == COMMENT_UPDATE_RESULT){ //리뷰 업데이트
                val rating = data!!.getFloatExtra(RATING_POINT,0f)
                val review = data.getStringExtra(REVIEW)
                if(userRatingData.reviewComment != review) {//리뷰수정시
//                    //유저 개인 리뷰
//                    if(userRatingData.reviewComment.isNotEmpty())
//                        db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
//                                .update("reviewComment",userRatingData.reviewComment)
//                                .addOnSuccessListener {  }
//                                .addOnFailureListener {
//                                    db.collection(USER_RATING_DATA).document(USER_RATING_DATA).collection(currentUser!!.uid).document(productData.prodName)
//                                            .set(userRatingData)
//                                }

                    db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid).update("review",review)
                    userRatingData.reviewComment = review
                    commentView()
                }
                if(userRatingData.ratingPoint != rating) { // 점수만 수정시
                    db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser!!.uid).update("rating", rating)
                    ratingPointUpdate(rating)
                }
                localDataSave()
            }
        }
    }
    private fun localDataSave(){
        val strContact = gson.toJson(userRatingData, UserRatingData::class.java)
        localDataPut.putString(userProductActPath(productData.prodName),strContact)//유저 제품 기록 전체 데이터
        val strContact2 = gson.toJson(userActInfo, UserActInfo::class.java)
        localDataPut.putString(USER_ACT_INFO_PATH,strContact2)//유저 활동 기록
        localDataPut.commit()
    }
}
