package condom.best.condom.View.BottomNavPage.TagSearch

interface TagSearchContract {
    interface View{

    }
    interface Presenter{
        var view : TagSearchFragment
        fun init(tagList: ArrayList<String>)
    }
}