package condom.best.condom.View.BottomNavPage.Adapter

interface TagAdapterContract {
    interface Model{
        var onClick : ((Int, Int, String)-> Unit)?
    }
}