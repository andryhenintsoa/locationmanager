package mg.sparks.eqworks_kotlin


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import mg.sparks.eqworks_kotlin.activity.SampleActivity
import mg.sparks.eqworks_kotlin.fragment.SampleFragmentActivity
import mg.sparks.eqworks_kotlin.service.SampleServiceActivity


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun inActivityClick(view: View?) {
        startActivity(Intent(this, SampleActivity::class.java))
    }

    fun inFragmentClick(view: View?) {
        startActivity(Intent(this, SampleFragmentActivity::class.java))
    }

    fun inServiceClick(view: View?) {
        startActivity(Intent(this, SampleServiceActivity::class.java))
    }
}