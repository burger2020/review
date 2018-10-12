package condom.best.condom.View

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.instacart.library.truetime.TrueTimeRx
import condom.best.condom.BottomNavPage.Product.ProductInfoMore
import condom.best.condom.BottomNavPage.Product.SearchFragment
import condom.best.condom.R
import condom.best.condom.View.BottomNavPage.Column.ColumnViewFragment
import condom.best.condom.View.BottomNavPage.ColumnFragment
import condom.best.condom.View.BottomNavPage.HomeFragment
import condom.best.condom.View.BottomNavPage.MyPage.ActMoreFragment
import condom.best.condom.View.BottomNavPage.MyPageFragment
import condom.best.condom.View.BottomNavPage.Product.ProductReviewFragment
import condom.best.condom.View.BottomNavPage.Product.RatingGraph.RatingGraphDetail
import condom.best.condom.View.BottomNavPage.Product.ReviewMoreFragment
import condom.best.condom.View.BottomNavPage.TagFragment
import condom.best.condom.View.BottomNavPage.Product.SearchResult.SearchResultFragment
import condom.best.condom.View.BottomNavPage.TagSearch.TagSearchFragment
import condom.best.condom.View.Data.*
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_ACT_INFO
import condom.best.condom.View.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.View.Data.StringData.Companion.COLUMN
import condom.best.condom.View.Data.StringData.Companion.ENTER_SETTING
import condom.best.condom.View.Data.StringData.Companion.FIRST_SETTING
import condom.best.condom.View.Data.StringData.Companion.HOME
import condom.best.condom.View.Data.StringData.Companion.MY
import condom.best.condom.View.Data.StringData.Companion.NEW_SETTING
import condom.best.condom.View.Data.StringData.Companion.NON_SETTING
import condom.best.condom.View.Data.StringData.Companion.ROUTE
import condom.best.condom.View.Data.StringData.Companion.TAG
import condom.best.condom.View.Data.UserLocalDataPath.Companion.USER_ACT_INFO_PATH
import condom.best.condom.View.Data.UserLocalDataPath.Companion.USER_INFO_PATH
import condom.best.condom.View.Sign.LogInActivity
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlin.concurrent.thread

@SuppressLint("Registered")
class MainActivity : AppCompatActivity(){

    private lateinit var mAuth: FirebaseAuth
    companion object {
        val storage = FirebaseStorage.getInstance("gs://condom_storage").reference
        @SuppressLint("StaticFieldLeak")
        val db = FirebaseFirestore.getInstance()

        lateinit var localDataGet : SharedPreferences
        lateinit var localDataPut : SharedPreferences.Editor

        val gson : Gson = GsonBuilder().create()

        var currentUser : FirebaseUser? = null
        lateinit var userInfo : UserInfo
        var userActInfo = UserActInfo()
    }

    private val RESULT_FIRST_SETTING = 1258
    private val RESULT_SECOND_SETTING = 1259

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                changePage(1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tag -> {
                changePage(2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                changePage(3)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myPage-> {
                changePage(4)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private var homeFragment = HomeFragment()
    private var tagFragment = TagFragment()
    private var columnFragment = ColumnFragment()
    private var myPageFragment = MyPageFragment()
    var searchFragment = SearchFragment()
    var tagSearchResultFragment = TagSearchFragment.newInstance(arrayListOf())
    var productReviewFragment = ProductReviewFragment.prodData(ProductInfo())
    var reviewMoreFragment = ReviewMoreFragment.newInstance(ProductInfo())
    var productInfoMoreFragment = ProductInfoMore.newInstance(ProductInfo())
    var columnViewFragment = ColumnViewFragment.newInstance(ColumnInfo())
    var searchResultFragment = SearchResultFragment.newInstance("")
    var graphFragment = RatingGraphDetail.newInstance(ProductRating(),ProductInfo())
    var actMoreFragment = ActMoreFragment()

    var beforeSearchResultFragment : Boolean = false
    var currentHomeFragment : Fragment? = null
    var currentTagFragment : Fragment? = null
    var currentColumnFragment : Fragment? = null
    var currentMyFragment : Fragment? = null

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser

        localDataGet = getSharedPreferences("localDataGet", MODE_PRIVATE)
        localDataPut = localDataGet.edit()// editor에 put 하기

        if(currentUser==null){
            startActivity(Intent(this, LogInActivity::class.java))
        } else if(currentUser != null) {
            bottom_Navigation.onNavigationItemSelectedListener = mOnNavigationItemSelectedListener
            bottom_Navigation.enableAnimation(false)
            bottom_Navigation.enableItemShiftingMode(false)
            bottom_Navigation.enableShiftingMode(false)

//        supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerHome,productReviewFragment).hide(productReviewFragment).commit()
            supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerHome,searchFragment).hide(searchFragment).commit()
            supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerHome,homeFragment).hide(homeFragment).commit()
            supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerTag,tagFragment).commit()
            supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerColumn,columnFragment).commit()
            supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerMy,myPageFragment).commit()
            supportFragmentManager.beginTransaction().show(homeFragment).commit()
            currentHomeFragment = homeFragment
            currentTagFragment = tagFragment
            currentColumnFragment = columnFragment
            currentMyFragment = myPageFragment

            thread{ //truetime 초기화
                TrueTimeRx.build()
                        .initializeRx("time.google.com")
                        .subscribeOn(Schedulers.io())
                        .subscribe({ date -> Log.v("TAG!", "TrueTime was initialized and we have a time: $date") }) { throwable -> throwable.printStackTrace() }
            }

            //테스트용
//            reset.setOnClickListener {
//                localDataPut.putString(currentUser!!.uid+getString(R.string.firstUser),NON_SETTING)
//                localDataPut.apply()
//                val intent = Intent(this,FirstSettingActivity::class.java)
//                intent.putExtra(ROUTE,NEW_SETTING)
//                startActivityForResult(intent,RESULT_FIRST_SETTING)
//            }

            if (localDataGet.getString(currentUser!!.uid + getString(R.string.firstUser), NON_SETTING) == NON_SETTING) {//유저정보 셋팅안했으면 설정 페이지로
                //유저 첫접속시 이름, 프사 등 기본   데이터 저장
                db.collection(USER_INFO).document(currentUser!!.uid).get().addOnCompleteListener { //유저데이터 저장되어있는지 확인
                    try { //삭제후 로그인, 로컬에 유저정보 저장
                        userInfo = it.result.toObject(UserInfo::class.java)!!
                        val strContact = gson.toJson(userInfo, UserInfo::class.java)
                        localDataPut.putString(USER_INFO_PATH, strContact)
                        localDataPut.putString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), FIRST_SETTING).commit()
                    } catch (e: KotlinNullPointerException) { //첫 가입
                        userInfoSetting()
                    }
                }
            } else {//유저정보 셋팅 했을시
                val userInfoJson = localDataGet.getString(USER_INFO_PATH, "none")
                val userActInfoJson = localDataGet.getString(USER_ACT_INFO_PATH, "none")
                if (userInfoJson != "none") {//유저 정보 로컬에서 가져오기
                    userInfo = gson.fromJson(userInfoJson, UserInfo::class.java)
                } else //유저 정보 로컬에 없을시 서버에서 가져오기
                    db.collection(USER_INFO).document(currentUser!!.uid).get().addOnCompleteListener {
                        try { //
                            userInfo = it.result.toObject(UserInfo::class.java)!!
                            val strContact = gson.toJson(userInfo, UserInfo::class.java)
                            localDataPut.putString(USER_INFO_PATH, strContact).commit()
                        } catch (e: KotlinNullPointerException) {//유저 정보 없을시 설정
                            userInfoSetting()
                        }
                    }
                if (userActInfoJson != "none") { // 유저활동정보 로컬에 있을시 가져오기
                    userActInfo = gson.fromJson(userActInfoJson, UserActInfo::class.java)
                } else //유저 활동 정보 로컬에 없을시 서버에서 가져오기
                    db.collection(USER_ACT_INFO).document(currentUser!!.uid).get().addOnCompleteListener {
                        try { // 서버에서 데이터가져와서 로컬에 저장
                            userActInfo = it.result.toObject(UserActInfo::class.java)!!
                            val strContact = gson.toJson(userActInfo, UserActInfo::class.java)
                            localDataPut.putString(USER_ACT_INFO_PATH, strContact).commit()
                        } catch (e: KotlinNullPointerException) { // 서버에 데이터 없을시 0,0,0 으로 초기데이터 저장
                            db.collection(USER_ACT_INFO).document(currentUser!!.uid).set(userActInfo)
                            val strContact = gson.toJson(userActInfo, UserActInfo::class.java)
                            localDataPut.putString(USER_ACT_INFO_PATH, strContact).commit()
                        }
                    }
            }
        }
    }

    private fun changePage(position: Int){
        when(position){
            1->{
                bottomNavPageContainerHome.visibility = View.VISIBLE
                bottomNavPageContainerTag.visibility = View.GONE
                bottomNavPageContainerColumn.visibility = View.GONE
                bottomNavPageContainerMy.visibility = View.GONE
                if(homeFragment.isHidden && bottom_Navigation.selectedItemId == R.id.navigation_home)  //홈 프래그면 안보이는상태에 바텀네비 누르면 홈으로
                    fragmentBackPress(homeFragment,"HOME")
            }
            2->{
                bottomNavPageContainerTag.visibility = View.VISIBLE
                bottomNavPageContainerHome.visibility = View.GONE
                bottomNavPageContainerColumn.visibility = View.GONE
                bottomNavPageContainerMy.visibility = View.GONE
                if(tagFragment.isHidden && bottom_Navigation.selectedItemId == R.id.navigation_tag)  //홈 프래그면 안보이는상태에 바텀네비 누르면 홈으로
                    fragmentBackPress(tagFragment,"TAG")
            }
            3->{
                bottomNavPageContainerColumn.visibility = View.VISIBLE
                bottomNavPageContainerHome.visibility = View.GONE
                bottomNavPageContainerTag.visibility = View.GONE
                bottomNavPageContainerMy.visibility = View.GONE
            }
            4->{
                myPageFragment.setData()
                bottomNavPageContainerMy.visibility = View.VISIBLE
                bottomNavPageContainerHome.visibility = View.GONE
                bottomNavPageContainerTag.visibility = View.GONE
                bottomNavPageContainerColumn.visibility = View.GONE
                if(myPageFragment.isHidden && bottom_Navigation.selectedItemId == R.id.navigation_myPage)  //홈 프래그면 안보이는상태에 바텀네비 누르면 홈으로
                    fragmentBackPress(myPageFragment,"MY")
            }
        }
    }

    private fun userInfoSetting(){
        localDataPut.putString(currentUser!!.uid + getString(R.string.firstUser), ENTER_SETTING).commit()
        val intent = Intent(this, FirstSettingActivity::class.java)
        intent.putExtra(ROUTE, NEW_SETTING)
        startActivityForResult(intent, RESULT_FIRST_SETTING)
    }
    private fun fragmentBackPress(fragment : Fragment,container : String) {
        when(container){
            HOME->{
                supportFragmentManager.beginTransaction().hide(currentHomeFragment).show(fragment).commit()
                currentHomeFragment = fragment
            }
            TAG->{
                supportFragmentManager.beginTransaction().hide(currentTagFragment).show(fragment).commit()
                currentTagFragment = fragment
            }
            COLUMN->{
                supportFragmentManager.beginTransaction().hide(currentColumnFragment).show(fragment).commit()
                currentColumnFragment = fragment
            }
            MY->{
                supportFragmentManager.beginTransaction().hide(currentMyFragment).show(fragment).commit()
                currentMyFragment = fragment
            }
        }
    }

    override fun onBackPressed() {
        if(bottomNavPageContainerHome.visibility == View.VISIBLE) {
            //홈 페이지 켜져있을 때
            if(!homeFragment.isHidden) { //홈 프래그면 종료
                super.onBackPressed()
            } else if (!searchFragment.isHidden) { //검색화면에서 홈
                fragmentBackPress(homeFragment,HOME)
                searchFragment.searchProd.setText("")
            } else if (!productReviewFragment.isHidden) { //제품리뷰에서 홈 또는 검색 결과
                if(beforeSearchResultFragment)
                    fragmentBackPress(searchResultFragment,HOME)
                else
                    fragmentBackPress(homeFragment, HOME)
            } else if (!productInfoMoreFragment.isHidden) { //정보 더보기 에서 제품리뷰
                fragmentBackPress(productReviewFragment,HOME)
            } else if (!searchResultFragment.isHidden) { //검색결과 에서 제품리뷰
                fragmentBackPress(searchFragment,HOME)
            } else if (!reviewMoreFragment.isHidden) { //리뷰터보기에서 제품리뷰
                fragmentBackPress(productReviewFragment,HOME)
            }
        }else if(bottomNavPageContainerTag.visibility == View.VISIBLE){   //태그검색에서 홈
            if(!tagFragment.isHidden) //  마이페이지에서 홈
                bottom_Navigation.selectedItemId = R.id.navigation_home
            else if(!tagSearchResultFragment.isHidden)  //활동 리스트 전체보기에서 마이페이지
                fragmentBackPress(tagFragment, TAG)

        }else if(bottomNavPageContainerColumn.visibility == View.VISIBLE){//     팁에서 홈
            if(!columnFragment.isHidden) //  마이페이지에서 홈
                bottom_Navigation.selectedItemId = R.id.navigation_home
            else if(!columnViewFragment.isHidden)  //활동 리스트 전체보기에서 마이페이지
                fragmentBackPress(columnFragment, COLUMN)

        }else if(bottomNavPageContainerMy.visibility == View.VISIBLE){//        내정보에서 홈
            if(!myPageFragment.isHidden) //  마이페이지에서 홈
                bottom_Navigation.selectedItemId = R.id.navigation_home
            else if(!actMoreFragment.isHidden)  //활동 리스트 전체보기에서 마이페이지
                fragmentBackPress(myPageFragment,MY)
            else if(!productReviewFragment.isHidden) //평가리스트 제품리뷰에서 마이페이지
                fragmentBackPress(actMoreFragment, MY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==RESULT_FIRST_SETTING){
            if(resultCode==1001) {
                //가입시 세팅 완료 리절트
//                val intent = Intent(this, SecondSettingActivity::class.java)
//                intent.putExtra(ROUTE, NEW_SETTING)
//                startActivityForResult(intent, RESULT_SECOND_SETTING)
            }
        }else if(requestCode == RESULT_SECOND_SETTING){
            if(resultCode==1001) {
                //가입시 세팅 완료 리절트
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
