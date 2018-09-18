package condom.best.condom.BottomNavPage.Product

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.storage.FirebaseStorage
import condom.best.condom.R
import condom.best.condom.View.MainActivity.Companion.currentUser
import kotlinx.android.synthetic.main.activity_photo_view.*
import condom.best.condom.View.Data.GlideApp


class PhotoViewActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = FirebaseStorage.getInstance("gs://condom-55a91").reference
        val pref  = getSharedPreferences("localDataGet", MODE_PRIVATE)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_photo_view)

        GlideApp.with(this)
                .load(storage.child(currentUser!!.photoUrl.toString()))
                .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}${pref.getInt(getString(R.string.userProfileChange),0)}")))
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .into(userProfileView)
    }
}
