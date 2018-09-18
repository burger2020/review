package condom.best.condom.View.BottomNavPage

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BottomNavPage.Adapter.ColumnListAdapter
import condom.best.condom.View.BottomNavPage.Column.ColumnAddActivity
import condom.best.condom.View.BottomNavPage.Column.ColumnViewFragment
import condom.best.condom.View.Data.ColumnInfo
import condom.best.condom.View.Data.StringData.Companion.COLUMN
import condom.best.condom.View.MainActivity
import kotlinx.android.synthetic.main.fragment_column.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ColumnFragment : Fragment(), ColumnContract.View {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private val columnListData = arrayListOf<ColumnInfo>()
    private lateinit var columnAdapter : ColumnListAdapter
    private lateinit var rootView : View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_column, container, false)
        rootView.addProduct.setOnClickListener {//칼럼 추가
            rootView.context.startActivity(Intent(context,ColumnAddActivity::class.java))
        }

        columnAdapter = ColumnListAdapter(rootView.context,columnListData)
        rootView.columnList.adapter = columnAdapter
        rootView.columnList.layoutManager = GridLayoutManager(context, 2)

        val presenter = ColumnPresenter().apply {
            view = this@ColumnFragment
            adapterContract = columnAdapter
        }

        return rootView
    }

    fun columnListGet(columnListGet: ArrayList<ColumnInfo>) { // 칼럼리스트 처음 세팅
        columnListData.clear()
        columnListData.addAll(columnListGet)
        columnAdapter.notifyDataSetChanged()
    }

    fun columnOnClick(it: ColumnInfo) {//칼럼 클릭 이벤트
        (activity as MainActivity).columnViewFragment = ColumnViewFragment.newInstance(it)
        FragmentUtil.fragmentAddChanger((activity as MainActivity),(activity as MainActivity).columnViewFragment,COLUMN)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ColumnFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
