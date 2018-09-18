package condom.best.condom.View.BottomNavPage.TagSearch

import condom.best.condom.View.Data.FirebaseConst.Companion.PRODUCT_INFO
import condom.best.condom.View.Data.ProductInfo
import condom.best.condom.View.MainActivity.Companion.db

class TagSearchRepository {
    fun initData(tagList: ArrayList<String>, callback: ListCallBack) : ArrayList<ProductInfo> {//태그 데이터 가져오기
        val data = db.collection(PRODUCT_INFO)
        val searchData = arrayListOf<ProductInfo>()
        val query = data.whereArrayContains("prodTag",tagList[0]) //선택한태그로 필터링
//        for(i in 1 until tagList.size){
//           query = query.whereArrayContains("prodTag",tagList[i])
//        }
        query.get().addOnCompleteListener { it ->
            it.result.mapTo(searchData) { it.toObject(ProductInfo::class.java) }
            val tempData = arrayListOf<ProductInfo>()
            tempData.addAll(searchData)
            if(searchData.size!=0)
                for(i in 1 until tagList.size){
                    for(j in searchData.size-1 downTo 0){
                        if(searchData[j].prodTag.indexOf(tagList[i]) == -1)
                            searchData.removeAt(j)
                        if(searchData.size==0)
                            break
                    }
                    if(searchData.size==0)
                        break
                }
            callback.listCallback(searchData)
        }
        return searchData
    }
}