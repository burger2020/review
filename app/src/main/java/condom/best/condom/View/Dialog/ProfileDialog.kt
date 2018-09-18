package condom.best.condom.View.Dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import condom.best.condom.R

@Suppress("UNREACHABLE_CODE")
class ProfileDialog(context: Context, private val count: Int) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int { return count }
    override fun getItem(position: Int): Any { return position }

    override fun getItemId(position: Int): Long { return position.toLong() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        var view: View? = convertView

        if (view == null) {
            view = layoutInflater.inflate(R.layout.dialog_profile, parent, false)

            viewHolder = ViewHolder(view!!.findViewById(R.id.text_view), view.findViewById(R.id.view_divider))

            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val context = parent.context
        when (position) {
            0 -> {
                viewHolder.textView.text = context.getString(R.string.profileDialogText)
                viewHolder.view.visibility = View.VISIBLE

            }
            1 -> {
                viewHolder.textView.text = context.getString(R.string.profileDialogText1)
                viewHolder.view.visibility = View.GONE
            }
            2 -> {
                viewHolder.textView.text = context.getString(R.string.profileDialogText2)
                viewHolder.view.visibility = View.GONE
            }
            3 -> {
                viewHolder.textView.text = context.getString(R.string.profileDialogText3)
                viewHolder.view.visibility = View.GONE
            }
            4 -> {
                viewHolder.textView.text = context.getString(R.string.profileDialogText4)
                viewHolder.view.visibility = View.GONE
            }
            else -> {
                viewHolder.textView.text = context.getString(R.string.profileDialogText5)
                viewHolder.view.visibility = View.GONE
            }
        }

        return view
    }

    data class ViewHolder(val textView: TextView,val view : View)
}