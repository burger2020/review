package condom.best.condom.Data

import java.io.Serializable

class StringData {
    companion object {
        const val NAME = "NAME"
        const val GENDER = "GENDER"
        const val BIRTH = "BIRTH"

        const val NON_SETTING = "NON_SETTING"
        const val ENTER_SETTING = "ENTER_SETTING"
        const val FIRST_SETTING = "FIRST_SETTING"
        const val SECOND_SETTING = "SECOND_SETTING"
        const val ALL_SETTING = "ALL_SETTING"

        const val ROUTE = "ROUTE"
        const val NEW_SETTING = "NEW_SETTING"

        const val USER_PROFILE = "USER_PROFILE"
        const val USER_GENDER = "USER_GENDER"
        const val USER_NAME = "USER_NAME"
        const val USER_BIRTH = "USER_BIRTH"
        const val USER_TASTE = "USER_TASTE"

        //액티비티 리절트
        //리뷰페이지
        const val REVIEW = "REVIEW"
        const val RATING_POINT = "RATING_POINT"
        const val USER_COMMENT = "USER_COMMENT"

        val COMPANY_CODE = arrayOf("오카모토","듀렉스","사가미","플레이보이","듀오","유니더스","바른생각")

        const val SEARCH_LIST = "SEARCH_LIST"
    }
}
class FirebaseConst{
    companion object {
        const val USER_INFO = "USER_INFO"
        const val USER_ACT_INFO = "USER_ACT_INFO"
        const val USER_RATING_DATA = "USER_RATING_DATA"
        const val USER_WISH_LIST = "USER_WISH_LIST"
        const val PRODUCT_INFO = "PRODUCT_INFO"
        const val PRODUCT_RATING = "PRODUCT_RATING"
        const val PRODUCT_REVIEWS = "PRODUCT_REVIEWS"
        const val COMMENT = "COMMENT"
        const val COMMENT_LIKE = "COMMENT_LIKE"
    }
}

//유저 정보
data class UserInfo(val profileUri:String, val gender : Int, val name : String, val birth : Long) : Serializable {
    constructor() : this("",0,"",0)
}
//유저 활동 정보
data class UserActInfo(var ratingNum : Int, var reviewNum : Int, var wishNum : Int) : Serializable {
    constructor() : this(0,0,0)
}
//제품 정보
data class ProductInfo(val prodImage:String, val prodName:String, val prodPrice:Int,val prodTag:ArrayList<String>, val prodCompany:Int
,val prodPoint : Float, val prodRatingNum : Int) : Serializable {
    constructor() : this("","",0, arrayListOf(),0,0F,0)
}
//제품 별점
data class ProductRating(val ratingData : ArrayList<Int>){
    constructor() : this(arrayListOf(0,0,0,0,0,0,0,0,0,0))
}
//제품 리뷰 데이터
data class ProductReviewData(val review : String, val date : Long, val userUid : String, var likeNum : Long, val gender : Int, val rating : Float, val reReviewNum : Int){
    constructor() : this("",0,"",0,0,0F,0)
}
data class ProductReviewData_Like(val productReviewData: ArrayList<ProductReviewData>, var reviewerInfo: ArrayList<UserInfo>, val reviewLike : ArrayList<ReviewLike>){
    constructor() : this(arrayListOf(), arrayListOf(), arrayListOf())
}
data class ReviewLike(var like : Boolean){
    constructor() : this(false)
}
//유저 제품 평가 데이터
data class UserRatingData(var prodName:String, var ratingPoint : Float, var reviewComment : String, var wishBool:Boolean){
    constructor() : this("",0f,"",false)
}
//유저 위시리스트 등록 데이터
data class UserWishData(var prodName : String, var prodImage : String){
    constructor() : this("","")
}