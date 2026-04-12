package ananda.yoga.rentalpsnew

import ananda.yoga.rentalpsnew.databinding.ActivityRegisterBinding
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var b: ActivityRegisterBinding
    private lateinit var db: DBOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom) // <--- Cek padding bottom ini
            insets
        }

        b.ivBack.setOnClickListener(this)
        b.btnRegister.setOnClickListener(this)
        b.tvLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }

            R.id.tvLogin -> {
                finish()
            }

            R.id.btnRegister -> {
                val nama = b.edtNama.text.toString().trim()
                val email = b.edtEmail.text.toString().trim()
                val noHp = b.edtNoHp.text.toString().trim()
                val password = b.edtPassword.text.toString().trim()
                val konfirmasiPassword = b.edtKonfirmasiPassword.text.toString().trim()

                if (nama.isEmpty()) {
                    b.edtNama.error = "Nama tidak boleh kosong"
                    b.edtNama.requestFocus()
                } else if (email.isEmpty()) {
                    b.edtEmail.error = "Email tidak boleh kosong"
                    b.edtEmail.requestFocus()
                } else if (noHp.isEmpty()) {
                    b.edtNoHp.error = "No HP tidak boleh kosong"
                    b.edtNoHp.requestFocus()
                } else if (password.isEmpty()) {
                    b.edtPassword.error = "Password tidak boleh kosong"
                    b.edtPassword.requestFocus()
                } else if (konfirmasiPassword.isEmpty()) {
                    b.edtKonfirmasiPassword.error = "Konfirmasi password tidak boleh kosong"
                    b.edtKonfirmasiPassword.requestFocus()
                } else if (password != konfirmasiPassword) {
                    b.edtKonfirmasiPassword.error = "Konfirmasi password tidak sama"
                    b.edtKonfirmasiPassword.requestFocus()
                } else {
                    if (db.checkEmail(email)) {
                        b.edtEmail.error = "Email sudah terdaftar"
                        b.edtEmail.requestFocus()
                    } else {
                        val hasil = db.insertUser(nama, email, noHp, password)

                        if (hasil) {
                            Toast.makeText(this, "Register berhasil", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Register gagal", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}