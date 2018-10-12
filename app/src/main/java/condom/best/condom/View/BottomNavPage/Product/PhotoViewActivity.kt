package condom.best.condom.BottomNavPage.Product

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import condom.best.condom.R
import condom.best.condom.View.Data.GlideApp
import condom.best.condom.View.MainActivity.Companion.currentUser
import condom.best.condom.View.MainActivity.Companion.localDataGet
import condom.best.condom.View.MainActivity.Companion.storage
import kotlinx.android.synthetic.main.activity_photo_view.*


class PhotoViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_photo_view)

        GlideApp.with(this)
                .load(storage.child(currentUser!!.photoUrl.toString()))
                .apply(RequestOptions.signatureOf(ObjectKey("${getString(R.string.userProfileChange)}${localDataGet.getInt(getString(R.string.userProfileChange),0)}")))
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .into(userProfileView)
    }
}
