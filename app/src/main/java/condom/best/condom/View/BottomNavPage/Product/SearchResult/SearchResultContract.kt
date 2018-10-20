package condom.best.condom.View.BottomNavPage.Product.SearchResult

interface SearchResultContract {
    interface View
    interface Presenter{
        var view : SearchResultFragment
        fun init(searchText: String)
    }
}