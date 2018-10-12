package condom.best.condom.View.BottomNavPage.Product.ReviewDetail

import condom.best.condom.View.Data.ProductReviewData
import condom.best.condom.View.Data.ReviewCommentList_Like

interface ReviewDetailContract {
    interface View{

    }
    interface Presenter{
        var view : ReviewDetailActivity?
        fun getCommentData(prodName: String, userId: String)
    }
    interface Repository{
        fun callbackLikeState(likeState : Boolean)
        fun callbackReviewData(productReviewData : ProductReviewData)
        fun adapterRefresh(commentList: ReviewCommentList_Like)
    }
}