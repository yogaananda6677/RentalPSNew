package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityUserBinding

class UserActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var b: ActivityUserBinding
    private lateinit var db: DBOpenHelper
    private var listData = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityUserBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        b.ivBack.setOnClickListener(this)
        b.btnTambah.setOnClickListener(this)

        tampilData()

        b.listView.setOnItemClickListener { _, _, position, _ ->
            val item = listData[position]
            showDialogEdit(
                item["id_user"]!!.toInt(),
                item["nama"].toString(),
                item["email"].toString(),
                item["no_hp"].toString(),
                item["password"].toString(),
                item["role"].toString()
            )
        }

        b.listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val item = listData[position]
                val idUser = item["id_user"]!!.toInt()

                AlertDialog.Builder(this)
                    .setTitle("Hapus Data")
                    .setMessage("Yakin ingin menghapus pengguna ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        val hasil = db.deletePengguna(idUser)
                        if (hasil) {
                            Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                            tampilData()
                        } else {
                            Toast.makeText(this, "Data gagal dihapus", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()

                true
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> finish()
            R.id.btnTambah -> showDialogTambah()
        }
    }

    private fun tampilData() {
        listData = db.getAllUser()

        val adapter = SimpleAdapter(
            this,
            listData,
            android.R.layout.simple_list_item_2,
            arrayOf("nama", "email"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )

        b.listView.adapter = adapter
    }

    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_user, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaUser)
        val edtEmail = view.findViewById<EditText>(R.id.edtEmailUser)
        val edtNoHp = view.findViewById<EditText>(R.id.edtNoHpUser)
        val edtPassword = view.findViewById<EditText>(R.id.edtPasswordUser)
        val spRole = view.findViewById<Spinner>(R.id.spRoleUser)

        val roleList = arrayOf("admin", "customer")
        val adapterRole = ArrayAdapter(this, android.R.layout.simple_spinner_item, roleList)
        adapterRole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRole.adapter = adapterRole

        AlertDialog.Builder(this)
            .setTitle("Tambah Pengguna")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = edtNama.text.toString().trim()
                val email = edtEmail.text.toString().trim()
                val noHp = edtNoHp.text.toString().trim()
                val password = edtPassword.text.toString().trim()
                val role = spRole.selectedItem.toString()

                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (email.isEmpty()) {
                    Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (noHp.isEmpty()) {
                    Toast.makeText(this, "No HP tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (password.isEmpty()) {
                    Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (db.checkEmail(email)) {
                    Toast.makeText(this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                } else {
                    val hasil = db.insertPengguna(nama, email, noHp, password, role)
                    if (hasil) {
                        Toast.makeText(this, "Data berhasil ditambah", Toast.LENGTH_SHORT).show()
                        tampilData()
                    } else {
                        Toast.makeText(this, "Data gagal ditambah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDialogEdit(
        idUser: Int,
        namaLama: String,
        emailLama: String,
        noHpLama: String,
        passwordLama: String,
        roleLama: String
    ) {
        val view = layoutInflater.inflate(R.layout.dialog_user, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaUser)
        val edtEmail = view.findViewById<EditText>(R.id.edtEmailUser)
        val edtNoHp = view.findViewById<EditText>(R.id.edtNoHpUser)
        val edtPassword = view.findViewById<EditText>(R.id.edtPasswordUser)
        val spRole = view.findViewById<Spinner>(R.id.spRoleUser)

        edtNama.setText(namaLama)
        edtEmail.setText(emailLama)
        edtNoHp.setText(noHpLama)
        edtPassword.setText(passwordLama)

        val roleList = arrayOf("admin", "customer")
        val adapterRole = ArrayAdapter(this, android.R.layout.simple_spinner_item, roleList)
        adapterRole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spRole.adapter = adapterRole

        val posisiRole = roleList.indexOf(roleLama)
        if (posisiRole >= 0) {
            spRole.setSelection(posisiRole)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Pengguna")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val nama = edtNama.text.toString().trim()
                val email = edtEmail.text.toString().trim()
                val noHp = edtNoHp.text.toString().trim()
                val password = edtPassword.text.toString().trim()
                val role = spRole.selectedItem.toString()

                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (email.isEmpty()) {
                    Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (noHp.isEmpty()) {
                    Toast.makeText(this, "No HP tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (password.isEmpty()) {
                    Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (db.checkEmailUserLain(email, idUser)) {
                    Toast.makeText(this, "Email sudah dipakai user lain", Toast.LENGTH_SHORT).show()
                } else {
                    val hasil = db.updatePengguna(idUser, nama, email, noHp, password, role)
                    if (hasil) {
                        Toast.makeText(this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                        tampilData()
                    } else {
                        Toast.makeText(this, "Data gagal diupdate", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}