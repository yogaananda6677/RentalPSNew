package ananda.yoga.rentalpsnew

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var b: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        b.ivMenu.setOnClickListener(this)
        b.cardBooking.setOnClickListener(this)
        b.cardPlaystation.setOnClickListener(this)
        b.cardProduk.setOnClickListener(this)
        b.cardRiwayat.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivMenu -> {
                val popupMenu = PopupMenu(this, b.ivMenu)
                popupMenu.menu.add("Profile")
                popupMenu.menu.add("Tentang Aplikasi")
                popupMenu.menu.add("Logout")

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "Profile" -> {
                            Toast.makeText(this, "Menu Profile", Toast.LENGTH_SHORT).show()
                            true
                        }
                        "Tentang Aplikasi" -> {
                            Toast.makeText(this, "Aplikasi Rental PS", Toast.LENGTH_SHORT).show()
                            true
                        }
                        "Logout" -> {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }

            R.id.cardBooking -> {
                Toast.makeText(this, "Menu Booking", Toast.LENGTH_SHORT).show()
            }

            R.id.cardPlaystation -> {
                Toast.makeText(this, "Menu Playstation", Toast.LENGTH_SHORT).show()
            }

            R.id.cardProduk -> {
                Toast.makeText(this, "Menu Produk", Toast.LENGTH_SHORT).show()
            }

            R.id.cardRiwayat -> {
                Toast.makeText(this, "Menu Riwayat", Toast.LENGTH_SHORT).show()
            }
        }
    }
}