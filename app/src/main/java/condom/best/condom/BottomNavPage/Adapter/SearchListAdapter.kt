package condom.best.condom.BottomNavPage.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import condom.best.condom.R
import kotlinx.android.synthetic.main.adapter_search_list.view.*

class SearchListAdapter(val context: Context, private val searchList: ArrayList<String>) : RecyclerView.Adapter<SearchListAdapter.SearchListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListAdapter.SearchListViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = inflater.inflate(R.layout.adapter_search_list,parent,false)
        return SearchListViewHolder(mainView)
    }
    override fun getItemCount(): Int = searchList.size
    override fun onBindViewHolder(holder: SearchListAdapter.SearchListViewHolder, position: Int) {
        holder.bindHolder(searchList,position)
    }
    class SearchListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pName = itemView.searchListText!!
        fun bindHolder(searchList: ArrayList<String>,position: Int) {
            pName.text = searchList[position]
        }
    }
}
