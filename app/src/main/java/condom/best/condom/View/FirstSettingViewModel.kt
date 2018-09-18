package condom.best.condom.View

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.view.View
import com.orhanobut.dialogplus.DialogPlus
import condom.best.condom.View.Data.UserInfo
import condom.best.condom.R
import condom.best.condom.View.Dialog.ProfileDialog
import java.util.*

class FirstSettingViewModel(@SuppressLint("StaticFieldLeak") val context : Activity,private val firstSettingInterface : FirstSettingInterface) : ViewModel(){

    val userName = ObservableField<String>()
    val nameTxtCheckImg = ObservableInt()
    val successEnable = ObservableBoolean()
    val successBtnImg = ObservableField<Drawable>()
    val userBirthTxt = ObservableField<String>()
    val birthTxtCheckImg = ObservableInt()
    val maleCheck = ObservableBoolean()
    val femaleCheck = ObservableBoolean()

    var name = false
    var birth = false
    var gender = 0
    var profileUri = "none"

    private var datePickerBool = true
    private val c = Calendar.getInstance()
    private var userBirthYear = c.get(Calendar.YEAR)-19
    private var userBirthMonth = c.get(Calendar.MONTH)
    private var userBirthDay = c.get(Calendar.DAY_OF_MONTH)
    val listener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth -> //데이트피커 셋 이벤트
        userBirthYear = year
        userBirthMonth = monthOfYear
        userBirthDay = dayOfMonth
        userBirthTxt.set("$userBirthYear. ${userBirthMonth + 1}. $userBirthDay")
        birthTxtCheckImg.set(View.VISIBLE)
        birth = true
        checkNext(gender, name, birth)
    }

    init {
        nameTxtCheckImg.set(View.INVISIBLE)
        successEnable.set(false)
        birthTxtCheckImg.set(View.INVISIBLE)

        userName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback(){ //유저이름설정 길이 체크
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if(userName.get().toString().length in 2..8){
                    nameTxtCheckImg.set(View.VISIBLE)
                    name = true
                    checkNext(gender,name,birth)
                }else {
                    nameTxtCheckImg.set(View.INVISIBLE)
                    name = false
                    checkNext(gender,name,birth)
                }
            }
        })
    }
    fun setBirth(){ //생일 선택
        if(datePickerBool) {
            datePickerBool = false
            val dialog = DatePickerDialog(context, android.R.style.Theme_Holo_Light_Dialog_MinWidth, listener, userBirthYear, userBirthMonth, userBirthDay)
            dialog.datePicker.calendarViewShown = false
            dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.datePicker.maxDate = c.timeInMillis
            dialog.show()
            val mHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    datePickerBool = true
                }
            }
            mHandler.sendEmptyMessageDelayed(0, 500)
        }
    }

    fun maleSet(){//남자 선택
        femaleCheck.set(false)
        gender = 1
        checkNext(gender,name,birth)
    }
    fun femaleSet(){//여자 선택
        maleCheck.set(false)
        gender = 2
        checkNext(gender,name,birth)
    }
    //다음 버튼
    fun successBtnClick(){
        val cal = Calendar.getInstance()
        cal.set(userBirthYear,userBirthMonth,userBirthDay)
        firstSettingInterface.successSetting(UserInfo(profileUri,gender,userName.get().toString(),cal.timeInMillis))
        //            val state = pref.getString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), StringData.NON_SETTING)
        // 취향, 정보 입력 현황 데이터 저장
        //            if(state == SECOND_SETTING){
        //                editor.putString(MainActivity.currentUser!!.uid+getString(R.string.firstUser), ALL_SETTING)
        //            }else
    }
    private fun checkNext(gender : Int, name:Boolean, birth:Boolean){//입력 다 됐는지 확인
        if(birth && name && gender>0) {
            successEnable.set(true)
            successBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_button))
        }else{
            successEnable.set(false)
            successBtnImg.set(context.resources.getDrawable(R.drawable.sign_up_success_non_button))
        }
    }

    fun profileChange() { //프사 선택
        val adapter = ProfileDialog(context, 4)
        var overlapClick = true
        val dialog = DialogPlus.newDialog(context)
                .setAdapter(adapter)
                .setExpanded(false, 600)
                .setOnItemClickListener { dialog, _, _, position ->
                    if(overlapClick) {
                        overlapClick = false
                        firstSettingInterface.profileImageSetting(position)
                        dialog.dismiss()
                        overlapClick = true
                    }
                }
                .create()
        dialog.show()
    }
}