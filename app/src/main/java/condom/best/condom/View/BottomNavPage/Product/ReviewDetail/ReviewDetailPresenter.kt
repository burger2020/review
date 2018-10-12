package condom.best.condom.View.BottomNavPage.Product.ReviewDetail

import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.View.Data.ProductReviewData
import condom.best.condom.View.Data.ReviewCommentList_Like

class ReviewDetailPresenter : ReviewDetailContract.Presenter, ReviewDetailContract.Repository {


    override var view: ReviewDetailActivity? = null
    var repository = ReviewDetailRepository(this)

    fun getMyReviewData(productInfo: ProductInfo){//내리뷰 데이터
        repository.getMyReviewData(productInfo)
    }
    override fun getCommentData(prodName : String,userId : String) { // 코멘트 데이터
        repository.getCommentData(prodName,userId)
    }
    //    -------------- 위 프레젠터 / 아래 레퍼시토리 ---------
    override fun callbackReviewData(productReviewData: ProductReviewData) {
        view?.reviewDataSetting(productReviewData)
    }
    override fun callbackLikeState(likeState: Boolean) {
        view?.listStateSetting(likeState)
    }
    override fun adapterRefresh(commentList: ReviewCommentList_Like) {
        view?.adapterRefresh(commentList)
    }
}