package condom.best.condom.View.BottomNavPage

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import condom.best.condom.View.Data.TagList
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BottomNavPage.Adapter.TagAdapter
import condom.best.condom.View.BottomNavPage.TagSearch.TagSearchFragment
import condom.best.condom.View.MainActivity
import kotlinx.android.synthetic.main.fragment_hash_tag.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@Suppress("DEPRECATION")
class TagFragment : Fragment(), TagContract.View {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private lateinit var presenter: TagPresenter

    val tagList = arrayListOf<TagList>()

    private val shapeTagList = arrayListOf("굴곡","돌출","나선","복합")
    private val charTagList = arrayListOf("향","색","윤활유","사정지연")
    private val thickTagList = arrayListOf("초박")
    private val diaTagList = arrayListOf("넓음","좁음")
    private val lengthTagList  = arrayListOf("롱","숏")
    private val brandTagList = arrayListOf("오카모토","사가미","플레이보이","듀렉스","바른생각","유니더스")

    lateinit var basicColor : IntArray
    private lateinit var selectColors : IntArray

    val selectTagList = arrayListOf<String>()

    lateinit var rootView : View
    private lateinit var tagListAdapter : TagAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_hash_tag, container, false)

        basicColor = intArrayOf(context!!.resources.getColor(R.color.white)
                ,context!!.resources.getColor(R.color.colorPrimary)
                ,context!!.resources.getColor(R.color.black))
        selectColors = intArrayOf(context!!.resources.getColor(R.color.colorPrimary2)
                ,context!!.resources.getColor(R.color.colorPrimary)
                ,context!!.resources.getColor(R.color.white))

        val colors = ArrayList<IntArray>()
        colors.add(basicColor)
        colors.add(basicColor)
        colors.add(basicColor)
        colors.add(basicColor)
        tagList.add(TagList("형태",shapeTagList,colors))

        val colors1 = ArrayList<IntArray>()
        colors1.add(basicColor)
        colors1.add(basicColor)
        colors1.add(basicColor)
        colors1.add(basicColor)
        tagList.add(TagList("특징",charTagList,colors1))

        val colors2 = ArrayList<IntArray>()
        colors2.add(basicColor)
        tagList.add(TagList("두께",thickTagList,colors2))

        val colors3 = ArrayList<IntArray>()
        colors3.add(basicColor)
        colors3.add(basicColor)
        tagList.add(TagList("지름",diaTagList,colors3))

        val colors4 = ArrayList<IntArray>()
        colors4.add(basicColor)
        colors4.add(basicColor)
        tagList.add(TagList("길이",lengthTagList,colors4))

        val colors5 = ArrayList<IntArray>()
        for(i in 0 until brandTagList.size)
            colors5.add(basicColor)
        tagList.add(TagList("브랜드",brandTagList,colors5))

        tagListAdapter = TagAdapter(rootView.context,tagList)
        rootView.tagListView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rootView.tagListView.adapter = tagListAdapter

        presenter = TagPresenter().apply {
            view = this@TagFragment
            adapterModel = tagListAdapter
        }

        rootView.tagListRefresh.setOnClickListener {//태그선택 초기화
            presenter.tagListRefresh()//태그클릭 초기화
            tagListAdapter.notifyDataSetChanged()
            btnSetting(false)
        }
        rootView.tagListSearch.setOnClickListener {//태그 검색
            (activity as MainActivity).tagSearchResultFragment = TagSearchFragment.newInstance(selectTagList)
            FragmentUtil.fragmentAddChanger((activity as MainActivity),(activity as MainActivity).tagSearchResultFragment,"TAG")
            tagListAdapter.notifyDataSetChanged()
        }

        return rootView
    }
    var overlapBool = true
    fun tagClick(position: Int, idx : Int, text: String) {
        if(overlapBool) {
            overlapBool = false
            if(tagList[position].tagColor[idx].contentEquals(basicColor)) {//선택
                tagList[position].tagColor[idx] = selectColors
                selectTagList.add(text)
                btnSetting(true)
            }
            else {//선택취소
                tagList[position].tagColor[idx] = basicColor
                selectTagList.remove(text)
            }
            tagListAdapter.notifyItemChanged(position)
            if(selectTagList.size==0) {
                btnSetting(false)
            }


            val mHandler = @SuppressLint("HandlerLeak")
            object : Handler() { override fun handleMessage(msg: Message) { overlapBool = true } }
            mHandler.sendEmptyMessageDelayed(0, 500)
        }
    }

    private fun btnSetting(set : Boolean){
        if(set){
            rootView.tagListSearch.isEnabled = true
            rootView.tagListRefresh.isEnabled = true
            rootView.tagListSearch.setTextColor(rootView.context.resources.getColor(R.color.colorPrimary))
            rootView.tagListRefresh.setTextColor(rootView.context.resources.getColor(R.color.colorPrimary))
        }else{
            rootView.tagListSearch.isEnabled = false
            rootView.tagListRefresh.isEnabled = false
            rootView.tagListSearch.setTextColor(rootView.context.resources.getColor(R.color.gray1))
            rootView.tagListRefresh.setTextColor(rootView.context.resources.getColor(R.color.gray1))
            selectTagList.clear()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                TagFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}