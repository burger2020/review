package condom.best.condom.View.BottomNavPage

import condom.best.condom.View.BottomNavPage.Adapter.ColumnAdapterContract

interface ColumnContract {
    interface View {

    }
    interface Presenter {
        var view : ColumnFragment?
        var adapterContract : ColumnAdapterContract.Moder?
    }
}