package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityRegisterBinding

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        b.ivBack.setOnClickListener(this)
        b.btnRegister.setOnClickListener(this)
        b.tvLogin.setOnClickListener(this)

        if (!b.cbSetuju.isChecked) {
            Toast.makeText(this, "Anda harus menyetujui syarat dan ketentuan!", Toast.LENGTH_SHORT).show()
            return
        }
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
                prosesRegister()
            }
        }
    }

    private fun prosesRegister() {
        val nama = b.edtNama.text.toString().trim()
        val email = b.edtEmail.text.toString().trim()
        val noHp = b.edtNoHp.text.toString().trim()
        val password = b.edtPassword.text.toString().trim()
        val konfirmasiPassword = b.edtKonfirmasiPassword.text.toString().trim()

        when {
            nama.isEmpty() -> {
                b.edtNama.error = "Nama tidak boleh kosong"
                b.edtNama.requestFocus()
            }

            email.isEmpty() -> {
                b.edtEmail.error = "Email tidak boleh kosong"
                b.edtEmail.requestFocus()
            }

            noHp.isEmpty() -> {
                b.edtNoHp.error = "No HP tidak boleh kosong"
                b.edtNoHp.requestFocus()
            }

            password.isEmpty() -> {
                b.edtPassword.error = "Password tidak boleh kosong"
                b.edtPassword.requestFocus()
            }

            konfirmasiPassword.isEmpty() -> {
                b.edtKonfirmasiPassword.error = "Konfirmasi password tidak boleh kosong"
                b.edtKonfirmasiPassword.requestFocus()
            }

            password != konfirmasiPassword -> {
                b.edtKonfirmasiPassword.error = "Konfirmasi password tidak sama"
                b.edtKonfirmasiPassword.requestFocus()
            }

            !b.cbSetuju.isChecked -> {
                Toast.makeText(
                    this,
                    "Anda harus menyetujui syarat dan ketentuan!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            db.checkEmail(email) -> {
                b.edtEmail.error = "Email sudah terdaftar"
                b.edtEmail.requestFocus()
            }

            else -> {
                val hasil = db.insertUser(nama, email, noHp, password, "customer")
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