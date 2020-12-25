package brotifypacha.scheduler.main_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import brotifypacha.scheduler.R

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)
    }

}
