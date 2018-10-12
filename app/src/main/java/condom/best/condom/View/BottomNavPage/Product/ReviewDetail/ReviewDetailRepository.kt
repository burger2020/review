package condom.best.condom.View.BottomNavPage.Product.ReviewDetail

import com.google.firebase.firestore.Query
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

class ReviewDetailRepository(private val repositoryInterface : ReviewDetailContract.Repository) {
    //내 리뷰 정보 가져오기
    fun getMyReviewData(productData:ProductInfo){
        db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser?.uid.toString()).get().addOnCompleteListener{ it ->
            try {
                var likeState = false
                val productReviewData = (it.result.toObject(ProductReviewData::class.java)!!)
                val likeStateGet = localDataGet.getInt(userReviewLikePath(productData.prodName, currentUser?.uid.toString(), productReviewData.date), REVIEW_LIKE_DEFAULT)
                when(likeStateGet){
                    REVIEW_LIKE_ON->likeState = true
                    REVIEW_LIKE_OFF->likeState = false
                    else->{
                        getLikeState(productData,productReviewData)
                    }
                }
                getCommentData(productData.prodName,productReviewData.userUid)//리뷰의 댓글 데이터 가져오기
                repositoryInterface.callbackReviewData(productReviewData) // 좋아요 상태 콜백
                repositoryInterface.callbackLikeState(likeState) // 좋아요 상태 콜백
            } catch (e: KotlinNullPointerException) { }
        }
    }
    //좋아요 상태 가져오기
    private fun getLikeState(productData:ProductInfo, productReviewData : ProductReviewData ){
        db.collection(PRODUCT_REVIEWS).document(productData.prodName).collection(COMMENT).document(currentUser?.uid.toString())
                .collection(COMMENT_LIKE).document(MainActivity.currentUser!!.uid).get().addOnCompleteListener {
                    //배댓 각각 유저 데이터
                    val likeState = try {
                        localDataPut.putInt(UserLocalDataPath.userReviewLikePath(productData.prodName, productReviewData.userUid, productReviewData.date), FirebaseConst.REVIEW_LIKE_ON)
                        (it.result.toObject(ReviewLike::class.java)!!).like
                    }catch (e:KotlinNullPointerException){
                        localDataPut.putInt(UserLocalDataPath.userReviewLikePath(productData.prodName, productReviewData.userUid, productReviewData.date), FirebaseConst.REVIEW_LIKE_OFF)
                        ReviewLike().like
                    }
                    localDataPut.commit()
                    repositoryInterface.callbackLikeState(likeState) // 좋아요 상태 콜백
                }
    }
    // 리뷰 댓글 가져오기
    fun getCommentData(prodName:String,userUid : String){
        db.collection(PRODUCT_REVIEWS).document(prodName).collection(COMMENT).document(userUid).collection(REVIEW_COMMENT)
                .orderBy("date", Query.Direction.DESCENDING) //내림차순(prodCompany)
                .get().addOnCompleteListener { it ->
                    val commentList = ReviewCommentList_Like()

                    try {
                        it.result.mapTo(commentList.reviewCommentData) { (it.toObject(ReviewCommentList::class.java)) }

                        var settingEnd = commentList.reviewCommentData.size*2
                        if(commentList.reviewCommentData.size == 0){
                            repositoryInterface.adapterRefresh(commentList)
                        }
                        else
                            for(i in 0 until commentList.reviewCommentData.size){
                                db.collection(USER_INFO).document(commentList.reviewCommentData[i].userUid).get().addOnCompleteListener {
                                    try {
                                        commentList.commenterInfo.add(it.result.toObject(UserInfo::class.java)!!)
                                        //마지막 데이터 다들고오면 어뎁터 실행
                                        settingEnd--
                                        if (i == commentList.reviewCommentData.size - 1 && settingEnd == 0) {
                                            repositoryInterface.adapterRefresh(commentList)
                                        }
                                    }catch (e:KotlinNullPointerException){}
                                }
                                //배댓 내가한 좋아요 셋

                                val likeStateGet = localDataGet.getInt(userReviewLikePath(prodName,commentList.reviewCommentData[i].userUid,commentList.reviewCommentData[i].date), REVIEW_LIKE_DEFAULT)
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
                                        db.collection(PRODUCT_REVIEWS).document(prodName).collection(COMMENT).document(userUid).collection(REVIEW_COMMENT)
                                                .document(commentList.reviewCommentData[i].date.toString()).collection(COMMENT_LIKE).document(currentUser!!.uid).get().addOnCompleteListener {
                                                    //배댓 각각 유저 데이터
                                                    try {
                                                        commentList.commentLike.add(it.result.toObject(ReviewLike::class.java)!!)
                                                        localDataPut.putInt(userReviewLikePath(prodName,commentList.reviewCommentData[i].userUid,commentList.reviewCommentData[i].date), REVIEW_LIKE_ON)
                                                    }catch (e:KotlinNullPointerException){
                                                        commentList.commentLike.add(ReviewLike())
                                                        localDataPut.putInt(userReviewLikePath(prodName,commentList.reviewCommentData[i].userUid,commentList.reviewCommentData[i].date), REVIEW_LIKE_OFF)
                                                    }
                                                    localDataPut.commit()
                                                    settingEnd--
                                                    if (i == commentList.reviewCommentData.size - 1 && settingEnd == 0) {
                                                        repositoryInterface.adapterRefresh(commentList)
                                                    }
                                                }
                                    }
                                }
                            }
//                        repositoryInterface.adapterRefresh()
                    }catch (e:NullPointerException){}

                }
    }
}