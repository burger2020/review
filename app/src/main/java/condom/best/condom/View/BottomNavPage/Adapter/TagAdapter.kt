package condom.best.condom.View.BottomNavPage.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.lujun.androidtagview.TagView
import condom.best.condom.View.Data.TagList
import condom.best.condom.R
import kotlinx.android.synthetic.main.adapter_serach_tag_list.view.*

class TagAdapter(val context : Context, private val tagList: ArrayList<TagList>) : RecyclerView.Adapter<TagAdapter.TagViewHolder>(), TagAdapterContract.Model {
    override var onClick: ((Int ,Int, String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = inflater.inflate(R.layout.adapter_serach_tag_list,parent,false)
        return TagViewHolder(mainView)
    }

    override fun getItemCount(): Int = tagList.size

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tagList,position, onClick)
    }

    class TagViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val tagContainer = view.tagList
        private val tagTitle = view.tagListName
        fun bind(tagList: ArrayList<TagList>, position: Int, clickListener: ((Int, Int, String) -> Unit)?) {
            tagTitle.text = tagList[position].tagTitle
            tagContainer.setTags(tagList[position].tagArr,tagList[position].tagColor)
            tagContainer.setOnTagClickListener(object : TagView.OnTagClickListener{
                override fun onTagLongClick(idx: Int, text: String?) {}
                override fun onTagClick(idx: Int, text: String?) {
                    clickListener?.invoke(position, idx, text.toString())
                }
                override fun onTagCrossClick(position: Int) {}

            })
        }
    }
}