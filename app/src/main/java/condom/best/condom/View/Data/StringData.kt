package condom.best.condom.View.Data

import android.content.Context
import com.bumptech.glide.signature.ObjectKey
import condom.best.condom.R
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.localDataGet
import java.io.Serializable
import java.util.*

class StringData {
    companion object {
        const val NAME = "NAME"
        const val GENDER = "GENDER"
        const val BIRTH = "BIRTH"

        const val HOME = "HOME"
        const val TAG = "TAG"
        const val COLUMN = "COLUMN"
        const val MY = "MY"


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
        const val WISH = "WISH"
        const val RATING = "RATING"
        const val REVIEW = "REVIEW"
        const val RATING_POINT = "RATING_POINT"
        const val USER_COMMENT = "USER_COMMENT"
        const val PROFILE_OPEN_CHECKED = "PROFILE_OPEN_CHECKED"

        const val SEARCH_LIST = "SEARCH_LIST"

        const val LIKE_NUM = "LIKE_NUM"
        const val LIKE_STATE = "LIKE_STATE"
        const val COMMENT_NUM = "COMMENT_NUM"
        const val REVIEW_UID = "REVIEW_UID"
    }
}
class GetSignatureKey {
    fun getKey(context: Context) : ObjectKey {
        return ObjectKey((System.currentTimeMillis() / 1000 / 60 / 60) + localDataGet.getInt(context.getString(R.string.userProfileChange), 0))
    }
}
class FirebaseConst{
    companion object {
        const val USER_INFO = "USER_INFO"
        const val USER_ACT_INFO = "USER_ACT_INFO"
        const val USER_RATING_DATA = "USER_RATING_DATA"
        const val USER_WISH_LIST = "USER_WISH_LIST"
        const val USER_RATING_LIST = "USER_RATING_LIST"
        const val USER_REVIEW_LIST = "USER_REVIEW_LIST"
        const val PRODUCT_INFO = "PRODUCT_INFO"
        const val PRODUCT_RATING = "PRODUCT_RATING"
        const val COLUMN_INFO = "COLUMN_INFO"
        const val PRODUCT_REVIEWS = "PRODUCT_REVIEWS"
        const val COMMENT = "COMMENT"
        const val COMMENT_LIKE = "COMMENT_LIKE"
        const val REVIEW_COMMENT = "REVIEW_COMMENT"


        const val REVIEW_LIKE_ON = 1
        const val REVIEW_LIKE_OFF = 2
        const val REVIEW_LIKE_DEFAULT = -1
    }
}
class UserLocalDataPath{
    companion object {
        val USER_RATING_LIST_PATH = "${currentUser!!.uid}USER_RATING_LIST_PATH" // 유저 제품 평가 부분 기록
        val USER_REVIEW_LIST_PATH = "${currentUser!!.uid}USER_REVIEW_LIST_PATH" // 유저 제품 평가 부분 기록
        val USER_WISH_LIST_PATH = "${currentUser!!.uid}USER_WISH_LIST_PATH" // 유저 제품 위시 부분 기록

        private val USER_REVIEW_LIKE_PATH = "${currentUser!!.uid}USER_REVIEW_LIKE_PATH" // 유저 리뷰 좋아요 기록 (+ 제품이름 + 유저이름)
        fun userReviewLikePath(prodName: String, userId : String, date : Long) = USER_REVIEW_LIKE_PATH + prodName + userId + date

        private val USER_PRODUCT_ACT_PATH = "${currentUser!!.uid}USER_PRODUCT_ACT_PATH" // 유저 제품 기록 전체 (+ 제품이름)
        fun userProductActPath(prodName:String) = USER_PRODUCT_ACT_PATH + prodName

        val USER_ACT_INFO_PATH = "${currentUser!!.uid}USER_ACT_INFO_PATH" // 유저 활동 기록

        val USER_INFO_PATH = "${currentUser!!.uid}USER_INFO_PATH" // 유저 정보 기록


    }
}
//유저 정보
class UserInfo(var profileUri:String, var gender : Int, var name : String, val birth : Long) : Serializable {
    constructor() : this("none",0,"",0)
    fun getOld() : Int{
        val c = Calendar.getInstance()
        val c_ = Calendar.getInstance()
        c_.timeInMillis = birth
        val old = c.get(Calendar.YEAR) - c_.get(Calendar.YEAR)
        return old/10*10
    }
}
//유저 활동 정보
data class UserActInfo(var ratingNum : Int, var reviewNum : Int, var wishNum : Int) : Serializable {
    constructor() : this(0,0,0)
}
//제품 정보
data class ProductInfo(val prodImage:String, val prodName:String, val prodPrice:ArrayList<Int>, val prodTag:ArrayList<String>, val prodCompany:String, val prodPoint : Float, val prodRatingNum : Int,
                       val sellUnit: ArrayList<Int>, val prodFeature : String, val prodIngredient : String) : Serializable {
    constructor() : this("","",arrayListOf(), arrayListOf(),"",0F,0, arrayListOf(),"","")

}
//제품 별점
data class ProductRating(val ratingData : ArrayList<Int>) : Serializable{
    constructor() : this(arrayListOf(0,0,0,0,0,0,0,0,0,0))

    fun noneZero(){
        for(i in 0 until ratingData.size){
            if(ratingData[i]<0)
                ratingData[i] = 0
        }
    }
}
data class MailDetailRating(val mailRatingData : ArrayList<Int>){
    constructor() : this(arrayListOf(0,0,0,0,0,0,0,0,0,0))
}
//제품 리뷰 데이터
data class ProductReviewData(val review : String, val date : Long, val userUid : String, var likeNum : Long, val rating : Float, var reReviewNum : Int, var profileOpen : Boolean) : Serializable{
    constructor() : this("",0,"",0,0F,0, true)
}
data class ProductReviewData_Like(val productReviewData: ArrayList<ProductReviewData>, var reviewerInfo: ArrayList<UserInfo>, val reviewLike : ArrayList<ReviewLike>){
    //제품 리뷰 데이터 + 좋아요
    constructor() : this(arrayListOf(), arrayListOf(), arrayListOf())
}
data class ReviewCommentList(val comment : String, val date : Long, val userUid : String, var likeNum: Int) : Serializable{
    //리뷰에 코멘트 데이터
    constructor() : this("",0L,"",0)
}
data class ReviewCommentList_Like(val reviewCommentData : ArrayList<ReviewCommentList>, var commenterInfo : ArrayList<UserInfo>, val commentLike : ArrayList<ReviewLike>) : Serializable{
    constructor() : this(arrayListOf(),arrayListOf(),arrayListOf())
}


data class ReviewLike(var like : Boolean) : Serializable{
    constructor() : this(false)
}
//칼럼 정보
data class ColumnInfo(val columnImage:String, val columnTitle:String, val columnLikeNum:Int, val columnCommentNum : Int,val columnTag : ArrayList<String>) : Serializable {
    constructor() : this("","",0,0, arrayListOf())
}
//유저 제품 평가 데이터
data class UserRatingData(var prodName:String, var ratingPoint : Float, var reviewComment : String, var wishBool:Boolean,var reviewDate : Long, var profileOpen : Boolean) : Serializable {
    constructor() : this("",0f,"",false,0,true)
}
data class UserWishDataList(var prodName : String, var prodCompany : String,var prodImageUrl:String,var time : Long){//유저 위시리스트 등록 데이터
constructor() : this("","","",0L)
}
data class UserRatingDataList(var prodName : String, var ratingPoint : Float, var prodCompany : String,var prodImageUrl:String,var time : Long){//유저 평가리스트 등록 데이터
constructor() : this("",0F,"","",0L)
}
data class UserReviewDataList(var prodName : String, var prodCompany : String,var prodImageUrl:String,var time : Long) {
    //유저 평가리스트 등록 데이터
constructor() : this("","","",0L)
}
data class UserDataList(var userRatingData: ArrayList<UserRatingDataList>, var userWishData: ArrayList<UserWishDataList>, var userReviewData: ArrayList<UserReviewDataList>){
    //유저 평가리스트 등록 데이터
constructor() : this(arrayListOf(),arrayListOf(),arrayListOf())
}
data class TagList(var tagTitle : String,var tagArr : ArrayList<String>,var tagColor : ArrayList<IntArray>){
    //태그검색시 검색된 태그 리스트
    constructor() : this("",arrayListOf(), arrayListOf())
}