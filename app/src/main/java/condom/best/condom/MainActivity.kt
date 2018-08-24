package condom.best.condom

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
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.instacart.library.truetime.TrueTimeRx
import condom.best.condom.BottomNavPage.*
import condom.best.condom.BottomNavPage.Product.ProductReviewFragment
import condom.best.condom.BottomNavPage.Product.SearchFragment
import condom.best.condom.Data.FirebaseConst.Companion.USER_ACT_INFO
import condom.best.condom.Data.FirebaseConst.Companion.USER_INFO
import condom.best.condom.Data.ProductInfo
import condom.best.condom.Data.ProductRating
import condom.best.condom.Data.StringData.Companion.ENTER_SETTING
import condom.best.condom.Data.StringData.Companion.NEW_SETTING
import condom.best.condom.Data.StringData.Companion.NON_SETTING
import condom.best.condom.Data.StringData.Companion.ROUTE
import condom.best.condom.Data.UserActInfo
import condom.best.condom.Sign.LogInActivity
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(){

    private lateinit var mAuth: FirebaseAuth
    companion object {
        var currentUser : FirebaseUser? = null
        lateinit var userInfo : condom.best.condom.Data.UserInfo
        var userActInfo = UserActInfo()
    }
    lateinit var pref : SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    private val db = FirebaseFirestore.getInstance()

    val RESULT_FIRST_SETTING = 1258
    val RESULT_SECOND_SETTING = 1259

    override fun onStart() {
        super.onStart()

        pref = getSharedPreferences("pref", MODE_PRIVATE)
        editor = pref.edit()// editor에 put 하기
        if(currentUser!=null&&pref.getString(currentUser!!.uid+getString(R.string.firstUser),NON_SETTING)==NON_SETTING){
            //유저 첫접속시 이름, 프사 등 기본 데이터 저장
            editor.putString(currentUser!!.uid+getString(R.string.firstUser), ENTER_SETTING)
            editor.commit()
            val intent = Intent(this,FirstSettingActivity::class.java)
            intent.putExtra(ROUTE,NEW_SETTING)
            startActivityForResult(intent,RESULT_FIRST_SETTING)
        }
        db.collection(USER_INFO).document(currentUser!!.uid).get().addOnCompleteListener {
            try {
                userInfo = it.result.toObject(condom.best.condom.Data.UserInfo::class.java)!!
            }catch (e:KotlinNullPointerException){}
        }
        db.collection(USER_ACT_INFO).document(currentUser!!.uid).get().addOnCompleteListener {
            try {
                userActInfo = it.result.toObject(UserActInfo::class.java)!!
            }catch (e:KotlinNullPointerException){
                db.collection(USER_ACT_INFO).document(currentUser!!.uid).set(userActInfo)
            }
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                changePage(1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
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

    var homeFragment = HomeFragment()
    var dashFragment = DashboardFragment()
    var alarmFragment = AlarmFragment()
    var mypageFragment = MyPageFragment()
    var searchFragment = SearchFragment()
    var productReviewFragment = ProductReviewFragment.prodData(ProductInfo())

    var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser

        if(currentUser==null){
            startActivity(Intent(this, LogInActivity::class.java))
        }

        bottom_Navigation.onNavigationItemSelectedListener = mOnNavigationItemSelectedListener
        bottom_Navigation.enableAnimation(false)
        bottom_Navigation.enableItemShiftingMode(false)
        bottom_Navigation.enableShiftingMode(false)

//        supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerHome,productReviewFragment).hide(productReviewFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerHome,searchFragment).hide(searchFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerHome,homeFragment).hide(homeFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerDashboard,dashFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerAlarm,alarmFragment).commit()
        supportFragmentManager.beginTransaction().add(R.id.bottomNavPageContainerMy,mypageFragment).commit()
        supportFragmentManager.beginTransaction().show(homeFragment).commit()
        currentFragment = homeFragment

        thread{ //truetime 초기화
            TrueTimeRx.build()
                    .initializeRx("time.google.com")
                    .subscribeOn(Schedulers.io())
                    .subscribe({ date -> Log.v("TAG!", "TrueTime was initialized and we have a time: $date") }) { throwable -> throwable.printStackTrace() }
        }

        reset.setOnClickListener {
            editor.putString(currentUser!!.uid+getString(R.string.firstUser),NON_SETTING)
            editor.commit()
            val intent = Intent(this,FirstSettingActivity::class.java)
            intent.putExtra(ROUTE,NEW_SETTING)
            startActivityForResult(intent,RESULT_FIRST_SETTING)
        }
    }

    private fun changePage(position: Int){
        when(position){
            1->{
                bottomNavPageContainerHome.visibility = View.VISIBLE
                bottomNavPageContainerDashboard.visibility = View.GONE
                bottomNavPageContainerAlarm.visibility = View.GONE
                bottomNavPageContainerMy.visibility = View.GONE
            }
            2->{
                bottomNavPageContainerDashboard.visibility = View.VISIBLE
                bottomNavPageContainerHome.visibility = View.GONE
                bottomNavPageContainerAlarm.visibility = View.GONE
                bottomNavPageContainerMy.visibility = View.GONE
            }
            3->{
                bottomNavPageContainerAlarm.visibility = View.VISIBLE
                bottomNavPageContainerHome.visibility = View.GONE
                bottomNavPageContainerDashboard.visibility = View.GONE
                bottomNavPageContainerMy.visibility = View.GONE
            }
            4->{
                bottomNavPageContainerMy.visibility = View.VISIBLE
                bottomNavPageContainerHome.visibility = View.GONE
                bottomNavPageContainerDashboard.visibility = View.GONE
                bottomNavPageContainerAlarm.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
        if(!searchFragment.isHidden){
            supportFragmentManager.beginTransaction().hide(currentFragment).show(homeFragment).commit()
            currentFragment = homeFragment
        }else if(!productReviewFragment.isHidden){
            supportFragmentManager.beginTransaction().hide(currentFragment).show(homeFragment).commit()
            currentFragment = homeFragment
        }else{
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
    }

}