package condom.best.condom.View.BottomNavPage.MyPage

import com.google.firebase.firestore.Query
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_ACT_INFO
import condom.best.condom.View.Data.StringData.Companion.RATING
import condom.best.condom.View.Data.StringData.Companion.REVIEW
import condom.best.condom.View.Data.StringData.Companion.WISH
import condom.best.condom.View.Data.UserDataList
import condom.best.condom.View.Data.UserLocalDataPath.Companion.USER_RATING_LIST_PATH
import condom.best.condom.View.Data.UserLocalDataPath.Companion.USER_REVIEW_LIST_PATH
import condom.best.condom.View.Data.UserLocalDataPath.Companion.USER_WISH_LIST_PATH
import condom.best.condom.View.Data.UserRatingDataList
import condom.best.condom.View.Data.UserReviewDataList
import condom.best.condom.View.Data.UserWishDataList
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.db
import condom.best.condom.View.MainActivity.Companion.localDataGet
import condom.best.condom.View.MainActivity.Companion.localDataPut
import java.util.*
import kotlin.Comparator

class ActMoreRepository(private val callback: RatingListCallback){
    ////////////////Local///////////////
    fun getDataList(divider:String, sort:Int): UserDataList? {
        val ratingDataJson = localDataGet.getString(currentUser!!.uid+"USER_$divider"+"_LIST_PATH", "none")
        return if(ratingDataJson != "none") {//평가제품데이터 로컬에서 가져오기
            val gson = GsonBuilder().create()
            val ratingList = UserDataList()
            when(divider){
                RATING->{
                    val listType = object : TypeToken<ArrayList<UserRatingDataList>>(){}.type
                    ratingList.userRatingData.addAll(gson.fromJson(ratingDataJson, listType))
                    ratingList.userRatingData.sortWith(Comparator { o1: UserRatingDataList, o2: UserRatingDataList ->
                        when(sort){
                            0->o2.time.compareTo(o1.time)
                            1->o1.time.compareTo(o2.time)
                            2->o2.ratingPoint.compareTo(o1.ratingPoint)
                            else ->o1.ratingPoint.compareTo(o2.ratingPoint)
                        }
                    })
                }
                WISH->{
                    val listType = object : TypeToken<ArrayList<UserWishDataList>>(){}.type
                    ratingList.userWishData.addAll(gson.fromJson(ratingDataJson, listType))
                    ratingList.userWishData.sortWith(Comparator { o1: UserWishDataList, o2: UserWishDataList ->
                        when(sort){
                            0->o2.time.compareTo(o1.time)
                            else->o1.time.compareTo(o2.time)
                        }
                    })
                }
                else->{
                    val listType = object : TypeToken<ArrayList<UserReviewDataList>>(){}.type
                    ratingList.userReviewData.addAll(gson.fromJson(ratingDataJson, listType))
                    ratingList.userReviewData.sortWith(Comparator { o1: UserReviewDataList, o2: UserReviewDataList ->
                        when(sort){
                            0->o2.time.compareTo(o1.time)
                            else->o1.time.compareTo(o2.time)
                        }
                    })
                }
            }
            ratingList
        } else{//로컬에 없으면 서버에서 가져오기
            null
        }
    }
    ////////////////Server//////////////
    fun getRatingRemoteDataList(divider: String, sort:Int){
        val field : String
        val direction : Query.Direction
        when(sort){
            0->{
                field = "time"
                direction = Query.Direction.DESCENDING
            }
            1->{
                field = "time"
                direction = Query.Direction.ASCENDING
            }
            2->{
                field = "ratingPoint"
                direction = Query.Direction.DESCENDING
            }
            else->{
                field = "ratingPoint"
                direction = Query.Direction.ASCENDING
            }
        }
        db.collection(USER_ACT_INFO).document("USER_$divider"+"_LIST").collection(currentUser!!.uid).orderBy(field, direction).get().addOnCompleteListener { it ->
            try {
                val dataList = UserDataList()
                val gson = GsonBuilder().create()
                when(divider){
                    RATING->{
                        val data = arrayListOf<UserRatingDataList>()
                         it.result.mapTo(data) { it.toObject(UserRatingDataList::class.java) }
                        dataList.userRatingData.addAll(data)
                        val listType = object : TypeToken<ArrayList<UserRatingDataList>>(){}.type
                        val strContact = gson.toJson(data, listType)
                        localDataPut.putString(USER_RATING_LIST_PATH, strContact) //로컬에 전체 저장
                    }
                    WISH->{
                        val data = arrayListOf<UserWishDataList>()
                        it.result.mapTo(data) { it.toObject(UserWishDataList::class.java) }
                        dataList.userWishData.addAll(data)
                        val listType = object : TypeToken<ArrayList<UserWishDataList>>(){}.type
                        val strContact = gson.toJson(data, listType)
                        localDataPut.putString(USER_WISH_LIST_PATH, strContact) //로컬에 전체 저장
                    }
                    REVIEW->{
                        val data = arrayListOf<UserReviewDataList>()
                        it.result.mapTo(data) { it.toObject(UserReviewDataList::class.java) }
                        dataList.userReviewData.addAll(data)
                        val listType = object : TypeToken<ArrayList<UserReviewDataList>>(){}.type
                        val strContact = gson.toJson(data, listType)
                        localDataPut.putString(USER_REVIEW_LIST_PATH, strContact) //로컬에 전체 저장
                    }
                }
                localDataPut.commit()
                callback.listCallBack(dataList)
            } catch (e: KotlinNullPointerException) {
            }
        }
    }
}