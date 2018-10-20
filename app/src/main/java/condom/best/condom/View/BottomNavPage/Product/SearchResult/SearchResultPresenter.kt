package condom.best.condom.View.BottomNavPage.Product.SearchResult

import condom.best.condom.View.Data.ProductInfo

class SearchResultPresenter: SearchResultContract.Presenter, SearchListCallBack {

    private lateinit var resultRepository : SearchResultRepository
    override fun init(searchText: String) {
        resultRepository = SearchResultRepository()
        getProdLst(searchText)
    }
    fun getProdLst(searchText: String) {
        resultRepository.initData(searchText,this)
    }
    override lateinit var view: SearchResultFragment

    override fun listCallback(searchData: ArrayList<ProductInfo>) {
        view.prodListSetting(searchData)
    }


}
interface SearchListCallBack{
    fun listCallback(searchData: ArrayList<ProductInfo>)
}