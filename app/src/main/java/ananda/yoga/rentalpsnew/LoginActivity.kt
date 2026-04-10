package ananda.yoga.rentalpsnew

import ananda.yoga.rentalpsnew.databinding.ActivityLoginBinding
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityLoginBinding
    private lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        b.btnLogin.setOnClickListener(this)
        b.tvRegister.setOnClickListener(this)
        b.ivBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnLogin -> {
                val email = b.edtEmail.text.toString().trim()
                val password = b.edtPassword.text.toString().trim()

                if (email.isEmpty()) {
                    b.edtEmail.error = "Email tidak boleh kosong"
                    b.edtEmail.requestFocus()
                } else if (password.isEmpty()) {
                    b.edtPassword.error = "Password tidak boleh kosong"
                    b.edtPassword.requestFocus()
                } else {
                    val loginBerhasil = db.checkLogin(email, password)

                    if (loginBerhasil) {
                        Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()

                        // pindah ke dashboard
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            R.id.tvRegister -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }

            R.id.ivBack -> {
                finish()
            }
        }
    }
}