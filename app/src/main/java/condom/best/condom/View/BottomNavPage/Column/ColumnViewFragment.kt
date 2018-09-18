package condom.best.condom.View.BottomNavPage.Column

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import condom.best.condom.R
import condom.best.condom.View.Data.ColumnInfo
import condom.best.condom.View.Data.GlideApp
import condom.best.condom.View.MainActivity.Companion.storage
import kotlinx.android.synthetic.main.fragment_column_view.view.*

private const val ARG_PARAM1 = "param1"

class ColumnViewFragment : Fragment() {
    private var columnData: ColumnInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            columnData = it.getSerializable(ARG_PARAM1) as ColumnInfo
        }
    }

    lateinit var rootView : View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_column_view, container, false)

        rootView.columnTitle.text = columnData?.columnTitle
        GlideApp.with(rootView.context)
                .load(storage.child(columnData?.columnImage.toString()))
                .into(rootView.columnMainImage)
        rootView.columnMainImage.adjustViewBounds = true
        rootView.columnContent.text = "컬럼 내용???"
        rootView.columnLayer.layoutParams = FrameLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT,CoordinatorLayout.LayoutParams.WRAP_CONTENT)


        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: ColumnInfo) =
                ColumnViewFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ARG_PARAM1, param1)
                    }
                }
    }
}
