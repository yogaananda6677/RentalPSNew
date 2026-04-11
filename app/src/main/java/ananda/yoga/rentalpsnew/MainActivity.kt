package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Padding bawah dibuat 0 agar BottomNav mepet ke bawah (jika desainmu begitu)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Set Fragment Awal
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, DashboardFragment())
                .commit()
        }

        // Navigasi Bottom Navigation
        b.bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.itemHome -> DashboardFragment()
                R.id.monitoring -> MonitoringFragment()
                R.id.riwayat -> RiwayatFragment()
                R.id.menuLain -> MenuLainFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, it)
                    .commit()
                return@setOnItemSelectedListener true
            }
            false
        }

        // Ganti kode onBackPressedDispatcher di MainActivity.kt dengan ini:
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 1. Cek Fragment apa yang sekarang lagi tampil
                val currentFragment = supportFragmentManager.findFragmentById(R.id.frame_layout)

                if (currentFragment !is DashboardFragment) {
                    // 2. Kalau lagi BUKAN di Dashboard (misal di Monitoring), balikkan ke Dashboard
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, DashboardFragment())
                        .commit()

                    // 3. Update icon BottomNav agar balik ke Home
                    b.bottomNavigationView.selectedItemId = R.id.itemHome
                } else {
                    // 4. Kalau sudah di Dashboard dan ditekan Back, baru tawarkan keluar
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Keluar")
                        .setMessage("Yakin ingin menutup aplikasi?")
                        .setPositiveButton("Ya") { _, _ -> finishAffinity() }
                        .setNegativeButton("Tidak", null)
                        .show()
                }
            }
        })
    }
}