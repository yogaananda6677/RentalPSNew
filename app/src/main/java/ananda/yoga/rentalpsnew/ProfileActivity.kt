package ananda.yoga.rentalpsnew

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ananda.yoga.rentalpsnew.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var b: ActivityProfileBinding

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(it, takeFlags)
            } catch (_: Exception) { }
            b.imgProfile.setImageURI(it)
            saveImageUri(it)
            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        val sp    = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        val email = sp.getString("userEmail", "admin@gmail.com") ?: "admin@gmail.com"

        // Ambil data user terbaru dari DB sesuai email yang login
        val db   = DBOpenHelper(this)
        val user = db.getUserData(email)

        val nama  = user?.get("nama")  ?: sp.getString("userName", "Administrator") ?: "-"
        val noHp  = user?.get("no_hp") ?: "-"
        val role  = user?.get("role")  ?: "customer"

        // Tampilkan di header
        b.tvNamaProfile.text = nama
        b.tvRoleProfile.text  = role.replaceFirstChar { it.uppercase() }

        // Tampilkan di card info
        b.tvNamaInfo.text    = nama
        b.tvEmailProfile.text = email
        b.tvNoHpProfile.text  = noHp

        // Foto profil
        val savedUri = sp.getString("userImageUri", null)
        if (savedUri != null) {
            try { b.imgProfile.setImageURI(Uri.parse(savedUri)) }
            catch (_: Exception) { b.imgProfile.setImageResource(R.drawable.ic_person) }
        } else {
            b.imgProfile.setImageResource(R.drawable.ic_person)
        }

        b.ivBack.setOnClickListener { finish() }

        b.btnEditFoto.setOnClickListener { getImage.launch("image/*") }

        b.btnLogout.setOnClickListener {
            sp.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun saveImageUri(uri: Uri) {
        getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .edit().putString("userImageUri", uri.toString()).apply()
    }
}