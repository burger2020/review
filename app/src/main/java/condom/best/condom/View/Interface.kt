package condom.best.condom.View

import com.google.firebase.auth.FirebaseUser
import condom.best.condom.View.Data.UserInfo

interface LogInConst {
    fun startSignIn()
    fun startSignUp()
    fun finishLogIn(user: FirebaseUser)
}

interface FirstSettingInterface {
    fun successSetting(userInfo: UserInfo)
    fun profileImageSetting(sect : Int)
}