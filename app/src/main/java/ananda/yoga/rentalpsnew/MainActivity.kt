package ananda.yoga.rentalpsnew

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ananda.yoga.rentalpsnew.databinding.ActivityMainBinding
import android.content.Intent
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, DashboardFragment()).commit()
        }

        b.bottomNavigationView.setOnItemSelectedListener { item ->
            val frag = when (item.itemId) {
                R.id.itemHome -> DashboardFragment()
                R.id.monitoring -> MonitoringFragment()
                R.id.riwayat -> RiwayatFragment()
                R.id.menuLain -> MenuLainFragment()
                else -> null
            }
            frag?.let { supportFragmentManager.beginTransaction().replace(R.id.frame_layout, it).commit() }
            true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }



    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Keluar Aplikasi")
        builder.setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
        builder.setPositiveButton("Ya") { _, _ ->
            finishAffinity()
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}