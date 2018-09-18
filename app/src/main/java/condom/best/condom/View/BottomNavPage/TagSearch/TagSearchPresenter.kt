package condom.best.condom.View.BottomNavPage.TagSearch

import condom.best.condom.View.Data.ProductInfo

class TagSearchPresenter: TagSearchContract.Presenter,ListCallBack {

    private lateinit var repository : TagSearchRepository
    override fun init(tagList: ArrayList<String>) {
        repository = TagSearchRepository()
        getProdLst(tagList)
    }
    fun getProdLst(tagList: ArrayList<String>) {
        repository.initData(tagList,this)
    }
    override lateinit var view: TagSearchFragment

    override fun listCallback(searchData: ArrayList<ProductInfo>) {
        view.prodListSetting(searchData)
    }


}
interface ListCallBack{
    fun listCallback(searchData: ArrayList<ProductInfo>)
}