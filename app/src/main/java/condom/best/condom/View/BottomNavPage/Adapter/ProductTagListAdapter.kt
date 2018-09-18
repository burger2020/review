package condom.best.condom.View.BottomNavPage.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import condom.best.condom.R
import kotlinx.android.synthetic.main.adapter_tag_list.view.*

class ProductTagListAdapter(val context:Context, private val pTagList: ArrayList<String>, private val divider : Int) : RecyclerView.Adapter<ProductTagListAdapter.PTagViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PTagViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = if(divider==0)
            inflater.inflate(R.layout.adapter_tag_list,parent,false)
        else
            inflater.inflate(R.layout.adapter_tag_list_large,parent,false)
        return PTagViewHolder(mainView)
    }

    override fun getItemCount(): Int = pTagList.size

    override fun onBindViewHolder(holder: PTagViewHolder, position: Int) {
        holder.bindHolder(pTagList,position)
    }
    class PTagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pName = itemView.PTagText!!
        fun bindHolder(pTagList: ArrayList<String>,position: Int) {
            pName.text = pTagList[position]
        }
    }
}