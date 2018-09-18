package condom.best.condom.View.BottomNavPage.MyPage

import condom.best.condom.View.Data.UserDataList
import condom.best.condom.View.BottomNavPage.Adapter.ActListContract

interface ActMoreContract {
    interface View{
        fun adapterItemClick(prodName : String)
    }
    interface Presenter{
        var view : ActMoreFragment
        var adapterModel : ActListContract.Model?
        fun getSortData(divider : String, sort : Int): UserDataList?
    }
}