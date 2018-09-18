package condom.best.condom.View.BottomNavPage.MyPage

import condom.best.condom.View.Data.UserDataList
import condom.best.condom.View.BottomNavPage.Adapter.ActListContract

class ActMorePresenter : ActMoreContract.Presenter, RatingListCallback {

    private lateinit var actMoreRepository: ActMoreRepository
    override lateinit var view: ActMoreFragment
    override var adapterModel: ActListContract.Model? = null
        set(value){
            field = value
            field?.onClick = { view.adapterItemClick(it)}
            actMoreRepository = ActMoreRepository(this)
        }

    override fun getSortData(divider : String,sort : Int): UserDataList? {//정렬데이터 가져오기
        val ratingList = actMoreRepository.getDataList(divider, sort)
        return if(ratingList != null) { // 로컬 데이터 가져오기 성공
            ratingList
        }
        else{ //로컬에 데이터 없음
            actMoreRepository.getRatingRemoteDataList(divider, sort)
            null
        }
    }
    override fun listCallBack(ratingList: UserDataList) {
        view.setServerRatingList(ratingList)
    }
}

interface RatingListCallback{
    fun listCallBack(ratingList: UserDataList)
}