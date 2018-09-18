package condom.best.condom.View.BottomNavPage

import condom.best.condom.View.BottomNavPage.Adapter.TagAdapterContract

class TagPresenter : TagContract.Model{
    override lateinit var view: TagFragment
    override var adapterModel: TagAdapterContract.Model? = null
        set(value) {
            field = value
            field?.onClick = {position,idx,text ->tagListClick(position,idx,text)}
        }

    private fun tagListClick(position: Int, idx : Int, text: String) {
        view.tagClick(position, idx, text)
    }
    fun tagListRefresh(){
        for(i in 0 until view.tagList.size)
            for(j in 0 until view.tagList[i].tagColor.size)
                view.tagList[i].tagColor[j] = view.basicColor
        view.selectTagList.clear()
    }
}