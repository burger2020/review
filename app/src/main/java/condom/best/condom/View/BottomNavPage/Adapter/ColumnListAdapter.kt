package condom.best.condom.View.BottomNavPage.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import condom.best.condom.R
import condom.best.condom.View.Data.ColumnInfo
import condom.best.condom.View.Data.GlideApp
import condom.best.condom.View.MainActivity.Companion.storage
import kotlinx.android.synthetic.main.adapter_column_list.view.*

class ColumnListAdapter(val context:Context,private val columnList : ArrayList<ColumnInfo>) : RecyclerView.Adapter<ColumnListAdapter.ColumnListViewHolder>(), ColumnAdapterContract.Moder {
    override var onClick: ((ColumnInfo) -> Unit)? = null

    override fun getItemCount(): Int = columnList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColumnListViewHolder {
        val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val mainView = inflater.inflate(R.layout.adapter_column_list,parent,false)
        return ColumnListViewHolder(mainView)
    }

    override fun onBindViewHolder(holder: ColumnListViewHolder, position: Int) {
        holder.bind(context,columnList,position,onClick)
    }

    class ColumnListViewHolder(view : View) : RecyclerView.ViewHolder(view){
        private val columnImg = view.columnImage
        private val columnTitle = view.columnTitleText
        private val columnHeart = view.columnHeartImg
        private val columnHeartNum = view.columnHeartNumTxt
        private val columnComment = view.columnCommentImg
        private val columnCommentNum = view.columnCommentNumTxt
        private val columnListLayer = view.columnListLayer
        fun bind(context: Context, columnList: ArrayList<ColumnInfo>, position: Int, onClick: ((ColumnInfo) -> Unit)?){
            GlideApp.with(context)
                    .load(storage.child(columnList[position].columnImage))
                    .apply(RequestOptions().centerCrop())
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(columnImg)
            columnTitle.text = columnList[position].columnTitle
            columnHeartNum.text = columnList[position].columnLikeNum.toString()
            columnCommentNum.text = columnList[position].columnCommentNum.toString()

            columnListLayer.setOnClickListener { onClick?.invoke(columnList[position]) }
        }
    }
}