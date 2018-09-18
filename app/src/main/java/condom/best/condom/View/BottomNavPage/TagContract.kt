package condom.best.condom.View.BottomNavPage

import condom.best.condom.View.BottomNavPage.Adapter.TagAdapterContract
import condom.best.condom.View.BottomNavPage.TagFragment

interface TagContract {
    interface View{

    }
    interface Model{
        var adapterModel : TagAdapterContract.Model?
        val view : TagFragment
    }
}