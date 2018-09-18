package condom.best.condom.View.BindingAdapter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import android.support.v4.app.Fragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.nguyenhoanglam.imagepicker.model.Image
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker
import condom.best.condom.R
import condom.best.condom.View.Data.StringData.Companion.COLUMN
import condom.best.condom.View.Data.StringData.Companion.HOME
import condom.best.condom.View.Data.StringData.Companion.MY
import condom.best.condom.View.Data.StringData.Companion.TAG
import condom.best.condom.View.MainActivity
import java.util.*

object ProfileOption{//프로필 변경 옵션
    @JvmStatic//액티비티 사진찍기
    fun imageCapture(activity: Activity,requestCode : Int){
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivityForResult(takePictureIntent, requestCode)
                }                      //  Start ImagePicker
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {}
        }
        TedPermission.with(activity)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진을 찍기위해 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.CAMERA)
                .check()
    }
    @JvmStatic//프래그먼트 사진찍기
    fun imageCapture(fragment: Fragment, requestCode : Int){
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(fragment.context!!.packageManager) != null) {
                    fragment.startActivityForResult(takePictureIntent, requestCode)
                }                      //  Start ImagePicker
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {}
        }
        TedPermission.with(fragment.context)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("사진을 찍기위해 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.CAMERA)
                .check()
    }
    @JvmStatic//액티비티 사진찍기
    fun gallerySelect(activity: Activity){
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val images = arrayListOf<Image>()
                ImagePicker.with(activity)                         //  Initialize ImagePicker with activity or fragment context
                        .setToolbarColor("#78aaff")         //  Toolbar color
                        .setStatusBarColor("#4188fe")       //  StatusBar color (works with SDK >= 21  )
                        .setToolbarTextColor("#FFFFFF")     //  Toolbar text color (Title and Done button)
                        .setToolbarIconColor("#FFFFFF")     //  Toolbar icon color (Back and Camera button)
                        .setProgressBarColor("#4CAF50")     //  ProgressBar color
                        .setBackgroundColor("#ffffff")      //  Background color
                        .setCameraOnly(false)               //  Camera mode
                        .setMultipleMode(false)              //  Select multiple images or single image
                        .setFolderMode(false)                //  Folder mode
                        .setShowCamera(true)                //  Show camera button
                        .setFolderTitle(activity.getString(R.string.album))           //  Folder title (works with FolderMode = true)
                        .setImageTitle(activity.getString(R.string.galleries))         //  Image title (works with FolderMode = false)
                        .setDoneTitle(activity.getString(R.string.selectDone))               //  Done button title
                        .setLimitMessage(activity.getString(R.string.limitSelectMessage))    // Selection limit message
                        .setMaxSize(1)                     //  Max images can be selected
                        .setSavePath(activity.getString(R.string.app_name))         //  Image capture folder name
                        .setSelectedImages(images)          //  Selected images
                        .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                        .setKeepScreenOn(true)              //  Keep screen on when selecting images
                        .start()                            //  Start ImagePicker
            }
            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {}
        }
        TedPermission.with(activity)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("앨범에서 사진을 가져오기위해 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
    }
    @JvmStatic//프래그먼트 갤러리 선택
    fun gallerySelect(fragment: Fragment){
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val images = arrayListOf<Image>()
                ImagePicker.with(fragment)                         //  Initialize ImagePicker with activity or fragment context
                        .setToolbarColor("#78aaff")         //  Toolbar color
                        .setStatusBarColor("#4188fe")       //  StatusBar color (works with SDK >= 21  )
                        .setToolbarTextColor("#FFFFFF")     //  Toolbar text color (Title and Done button)
                        .setToolbarIconColor("#FFFFFF")     //  Toolbar icon color (Back and Camera button)
                        .setProgressBarColor("#4CAF50")     //  ProgressBar color
                        .setBackgroundColor("#ffffff")      //  Background color
                        .setCameraOnly(false)               //  Camera mode
                        .setMultipleMode(false)              //  Select multiple images or single image
                        .setFolderMode(false)                //  Folder mode
                        .setShowCamera(true)                //  Show camera button
                        .setFolderTitle(fragment.getString(R.string.album))           //  Folder title (works with FolderMode = true)
                        .setImageTitle(fragment.getString(R.string.galleries))         //  Image title (works with FolderMode = false)
                        .setDoneTitle(fragment.getString(R.string.selectDone))               //  Done button title
                        .setLimitMessage(fragment.getString(R.string.limitSelectMessage))    // Selection limit message
                        .setMaxSize(1)                     //  Max images can be selected
                        .setSavePath(fragment.getString(R.string.app_name))         //  Image capture folder name
                        .setSelectedImages(images)          //  Selected images
                        .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                        .setKeepScreenOn(true)              //  Keep screen on when selecting images
                        .start()                            //  Start ImagePicker
            }
            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {}
        }
        TedPermission.with(fragment.context)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("앨범에서 사진을 가져오기위해 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check()
    }
}
object FragmentUtil{
    @JvmStatic
    fun fragmentChanger(activity:Activity,fragment : android.support.v4.app.Fragment){
        if(fragment.isAdded){
            (activity as MainActivity).supportFragmentManager.beginTransaction().hide(activity.currentHomeFragment).show(fragment).commit()
            activity.currentHomeFragment = fragment
        }
    }
    @JvmStatic
    fun fragmentAddChanger(activity:Activity, fragment : android.support.v4.app.Fragment, container : String){
        when(container){
            HOME->{
                (activity as MainActivity).supportFragmentManager.beginTransaction()
                        .hide(activity.currentHomeFragment).add(R.id.bottomNavPageContainerHome,fragment).commit()
                activity.currentHomeFragment = fragment
            }
            TAG->{
                (activity as MainActivity).supportFragmentManager.beginTransaction()
                        .hide(activity.currentTagFragment).add(R.id.bottomNavPageContainerTag,fragment).commit()
                activity.currentTagFragment = fragment
            }
            COLUMN->{
                (activity as MainActivity).supportFragmentManager.beginTransaction()
                        .hide(activity.currentColumnFragment).add(R.id.bottomNavPageContainerColumn,fragment).commit()
                activity.currentColumnFragment = fragment
            }
            MY->{
                (activity as MainActivity).supportFragmentManager.beginTransaction()
                        .hide(activity.currentMyFragment).add(R.id.bottomNavPageContainerMy,fragment).commit()
                activity.currentMyFragment = fragment
            }
        }
    }
}