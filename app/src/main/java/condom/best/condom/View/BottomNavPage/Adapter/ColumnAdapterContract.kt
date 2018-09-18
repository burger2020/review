package condom.best.condom.View.BottomNavPage.Adapter

import condom.best.condom.View.Data.ColumnInfo

interface ColumnAdapterContract {
    interface Moder{
        var onClick : ((ColumnInfo) -> Unit)?
    }
}