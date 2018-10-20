package condom.best.condom.View.BottomNavPage.Product.SearchResult

import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_INFO
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.View.MainActivity.Companion.db

class SearchResultRepository {
    fun initData(searchString: String, callback: SearchListCallBack) : ArrayList<ProductInfo> {//태그 데이터 가져오기
        val searchData = arrayListOf<ProductInfo>()

        db.collection(PRODUCT_INFO).get().addOnCompleteListener { it ->
            it.result.mapTo(searchData) { it.toObject(ProductInfo::class.java) }
            searchString.toLowerCase()
            for(i in searchData.size-1 downTo 0){ // 검색어로 필터링
                if(!searchData[i].prodName.contains(searchString))
                    searchData.removeAt(i)
            }
            callback.listCallback(searchData)
        }
        return searchData
    }
}