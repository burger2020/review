package condom.best.condom.BottomNavPage.Product

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import condom.best.condom.BottomNavPage.Adapter.SearchListAdapter
import condom.best.condom.Data.StringData.Companion.SEARCH_LIST
import condom.best.condom.MainActivity
import condom.best.condom.R
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

        val pref : SharedPreferences = context!!.getSharedPreferences("pref", AppCompatActivity.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = pref.edit()// editor에 put 하기

        val gson = Gson()
        val json = pref.getString(SEARCH_LIST, "")
        val searchList = arrayListOf<String>()
        if(json.isNotEmpty()){
            val type = object : TypeToken<List<String>>() {}.type
            searchList.addAll(gson.fromJson<ArrayList<String>>(json, type))
        }
        var popularList = arrayListOf<String>()

        val searchListadapter = SearchListAdapter(view.context,searchList)
        view.SearchList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        view.SearchList.adapter = searchListadapter

        view.searchProd.requestFocus()
        view.searchProd.setOnEditorActionListener { textView, i, keyEvent ->
            if(i == EditorInfo.IME_ACTION_SEARCH){
                val st = textView.text.toString()
                if(st.isNotEmpty()) {
                    searchList.remove(st)
                    searchList.add(0, st)
                    val json = gson.toJson(searchList)
                    editor.putString(SEARCH_LIST, json)
                    editor.commit()
                    if (searchList.size >= 10) {
                        searchList.removeAt(searchList.size - 1)
                    }
                    searchListadapter.notifyDataSetChanged()
                    val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)

                }
            }
            return@setOnEditorActionListener false
        }

        view.searchBackBtn.setOnClickListener{
            val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            (activity as MainActivity).onBackPressed()
        }

        return view
    }

}
