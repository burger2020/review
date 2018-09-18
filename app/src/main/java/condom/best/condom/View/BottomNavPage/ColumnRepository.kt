package condom.best.condom.View.BottomNavPage

import condom.best.condom.View.Data.ColumnInfo
import condom.best.condom.View.Data.FirebaseConst
import condom.best.condom.View.MainActivity

class ColumnRepository(private val callBack: DataCallBack) {
    fun columnListGet(){
        MainActivity.db.collection(FirebaseConst.COLUMN_INFO).get().addOnCompleteListener { it ->
            val columnListData = arrayListOf<ColumnInfo>()
            it.result.mapTo(columnListData){it.toObject(ColumnInfo::class.java)}
            callBack.callBack(columnListData)
        }
    }
}
