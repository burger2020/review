package condom.best.condom.View.BottomNavPage

import condom.best.condom.View.BottomNavPage.Adapter.ColumnAdapterContract
import condom.best.condom.View.Data.ColumnInfo

class ColumnPresenter : ColumnContract.Presenter,DataCallBack {
    private lateinit var repository: ColumnRepository
    override var view : ColumnFragment? = null
        set(value) {
            field = value
            repository = ColumnRepository(this)
            repository.columnListGet()
        }
    override var adapterContract: ColumnAdapterContract.Moder? = null
        set(value){
            field = value
            field?.onClick = { view?.columnOnClick(it) }
        }

    override fun callBack(columnListData: ArrayList<ColumnInfo>) {//칼럼리스트 db에서 가져온거 뷰로 콜백넘겨주기
        view?.columnListGet(columnListData)
    }
}
interface DataCallBack{
    fun callBack(columnListData: ArrayList<ColumnInfo>)
}