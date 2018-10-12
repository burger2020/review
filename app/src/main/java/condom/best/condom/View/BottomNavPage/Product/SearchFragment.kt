package condom.best.condom.BottomNavPage.Product

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.google.common.reflect.TypeToken
import condom.best.condom.BottomNavPage.Adapter.SearchListAdapter
import condom.best.condom.View.Data.StringData.Companion.SEARCH_LIST
import condom.best.condom.View.MainActivity
import condom.best.condom.R
import condom.best.condom.View.BindingAdapter.FragmentUtil
import condom.best.condom.View.BottomNavPage.Product.SearchResult.SearchResultFragment
import condom.best.condom.View.MainActivity.Companion.gson
import condom.best.condom.View.MainActivity.Companion.localDataGet
import condom.best.condom.View.MainActivity.Companion.localDataPut
import kotlinx.android.synthetic.main.fragment_search.view.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)


        val searchListJson = localDataGet.getString(SEARCH_LIST, "")
        val searchList = arrayListOf<String>() // 최근 검색 제품 리스트
        if(searchListJson.isNotEmpty()){
            val type = object : TypeToken<List<String>>() {}.type
            searchList.addAll(gson.fromJson<ArrayList<String>>(searchListJson, type))
        }else{ // 검색리스트 없을경우
            view.searchListLayer.visibility = View.GONE
        }
        var popularList = arrayListOf<String>() // 인기 검색 제품 리스트

        val searchListAdapter = SearchListAdapter(view.context,searchList)
        view.SearchList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.SearchList.adapter = searchListAdapter

        view.searchProd.isFocusableInTouchMode = true
        view.searchProd.requestFocus()

        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        SearchListAdapter.listClick(object : SearchListAdapter.SearchListClick{
            override fun searchLickClick(searchString: String) {
                sts(view,searchList,searchString,searchListAdapter,imm)
            }
        })

        view.searchProd.setOnEditorActionListener { textView, i, _ ->
            if(i == EditorInfo.IME_ACTION_SEARCH){
                val st = textView.text.toString()
                if(st.isNotEmpty()) { // 입력 돼있으면 검색
                    sts(view,searchList,st,searchListAdapter,imm)
                }
            }
            return@setOnEditorActionListener false
        }

        view.searchBackBtn.setOnClickListener{
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            (activity as MainActivity).onBackPressed()
        }

        return view
    }
    fun sts(view : View, searchList : ArrayList<String>,st : String, searchListAdapter: SearchListAdapter,imm : InputMethodManager){
        searchList.remove(st)  // 검색 기록 저장
        searchList.add(0, st)
        val json = gson.toJson(searchList)
        localDataPut.putString(SEARCH_LIST, json)
        localDataPut.commit()
        if (searchList.size >= 8) { // 리스트 최대 8개까지(초과되면 삭제)
            searchList.removeAt(searchList.size - 1)
        }
        searchListAdapter.notifyDataSetChanged()
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.searchListLayer.visibility = View.VISIBLE
        (activity as MainActivity).searchResultFragment = SearchResultFragment.newInstance(st)
        FragmentUtil.fragmentChanger((activity as MainActivity),(activity as MainActivity).searchResultFragment,"HOME")
    }
}
