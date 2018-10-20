package condom.best.condom.View.Dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import condom.best.condom.R
import condom.best.condom.View.Data.ReviewCommentList
import java.util.*

class DeleteDialog (context: Context, private val divider: Int, private val reviewCommentData: ArrayList<ReviewCommentList>, private val position: Int) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int { return 1 }
    override fun getItem(position: Int): Any { return position }
    override fun getItemId(position: Int): Long { return position.toLong() }
    override fun getView(p: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var view: View? = convertView

        if (view == null) {
            view = layoutInflater.inflate(R.layout.dialog_review_delete, parent, false)

            viewHolder = ViewHolder(view!!.findViewById(R.id.deleteTitle), view.findViewById(R.id.cancelButton), view.findViewById(R.id.deleteButton))

            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        viewHolder.title.text = "리뷰를 삭제하시겠습니까?"

        viewHolder.deleteBtn.setOnClickListener {
            when(divider){
                1->{//리뷰
                    mListener2.interfaceReviewDelete()
                }
                2->{//댓글
                    mListener.interfaceCommentDelete(reviewCommentData, position)
                }
            }
        }

        return view
    }

    data class ViewHolder(val title: TextView,val cancelBtn: TextView,val deleteBtn: TextView)

    companion object {
        lateinit var mListener: InterfaceCommentDelete
        lateinit var mListener2: InterfaceReviewDelete
        fun commentDelete(mListener: InterfaceCommentDelete) { Companion.mListener = mListener }
        fun reviewDelete(mListener2: InterfaceReviewDelete) { Companion.mListener2 = mListener2 }
    }
    interface InterfaceCommentDelete {
        fun interfaceCommentDelete(reviewCommentData: ArrayList<ReviewCommentList>, position: Int)
    }
    interface InterfaceReviewDelete {
        fun interfaceReviewDelete()
    }
}