package ananda.yoga.rentalpsnew

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityUserBinding

class UserActivity : AppCompatActivity() {

    private lateinit var b: ActivityUserBinding
    private lateinit var db: DBOpenHelper
    private var listData = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityUserBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        b.ivBack.setOnClickListener { finish() }
        b.btnTambah.setOnClickListener { showDialogTambah() }

        tampilData()
    }

    private fun tampilData() {
        try {
            listData = db.getAllUser()

            val adapter = SimpleAdapter(
                this, listData, R.layout.item_user,
                arrayOf("nama", "email", "role", "id_user"),
                intArrayOf(R.id.text1, R.id.text2, R.id.tvLevelBadge, R.id.btnMenuUser)
            )

            adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
                when (view.id) {
                    R.id.tvLevelBadge -> {
                        val tv = view as TextView
                        val roleVal = data.toString().lowercase()
                        tv.text = roleVal.uppercase()

                        if (roleVal == "admin") {
                            tv.setTextColor(Color.parseColor("#2563EB"))
                            tv.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
                        } else {
                            tv.setTextColor(Color.parseColor("#16A34A"))
                            tv.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                        }
                        true
                    }
                    R.id.btnMenuUser -> {
                        val btn = view as ImageButton
                        val idTerpilih = data.toString()

                        btn.setOnClickListener { v ->
                            val popup = PopupMenu(this, v)
                            popup.menu.add("Edit")
                            popup.menu.add("Hapus")

                            popup.setOnMenuItemClickListener { menuItem ->
                                val userData = listData.find { it["id_user"] == idTerpilih }
                                when (menuItem.title) {
                                    "Edit" -> if (userData != null) showDialogEdit(userData)
                                    "Hapus" -> if (userData != null) confirmHapus(userData)
                                }
                                true
                            }
                            popup.show() // Penting: Agar menu muncul
                        }
                        true
                    }
                    else -> false
                }
            }
            b.listView.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_user, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaUser)
        val edtEmail = view.findViewById<EditText>(R.id.edtEmailUser)
        val edtHp = view.findViewById<EditText>(R.id.edtNoHpUser)
        val edtPass = view.findViewById<EditText>(R.id.edtPasswordUser)
        val spRole = view.findViewById<Spinner>(R.id.spRoleUser)

        val roles = arrayOf("admin", "customer")
        spRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        AlertDialog.Builder(this)
            .setTitle("Tambah User Baru")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = edtNama.text.toString()
                val email = edtEmail.text.toString()
                val hp = edtHp.text.toString()
                val pass = edtPass.text.toString()
                val role = spRole.selectedItem.toString()

                if (nama.isNotEmpty() && email.isNotEmpty()) {
                    if (db.insertUser(nama, email, hp, pass, role)) {
                        Toast.makeText(this, "User berhasil ditambah", Toast.LENGTH_SHORT).show()
                        tampilData()
                    }
                } else {
                    Toast.makeText(this, "Nama & Email wajib diisi!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null).show()
    }

    private fun showDialogEdit(item: HashMap<String, String>) {
        val view = layoutInflater.inflate(R.layout.dialog_user, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaUser)
        val edtEmail = view.findViewById<EditText>(R.id.edtEmailUser)
        val edtHp = view.findViewById<EditText>(R.id.edtNoHpUser)
        val edtPass = view.findViewById<EditText>(R.id.edtPasswordUser)
        val spRole = view.findViewById<Spinner>(R.id.spRoleUser)

        edtNama.setText(item["nama"])
        edtEmail.setText(item["email"])
        edtHp.setText(item["no_hp"])
        edtPass.setText(item["password"])

        val roles = arrayOf("admin", "customer")
        spRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        if (item["role"]?.lowercase() == "admin") spRole.setSelection(0) else spRole.setSelection(1)

        AlertDialog.Builder(this)
            .setTitle("Edit Data User")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val idUser = item["id_user"]!!.toInt()
                val berhasil = db.updatePengguna(
                    idUser,
                    edtNama.text.toString(),
                    edtEmail.text.toString(),
                    edtHp.text.toString(),
                    edtPass.text.toString(),
                    spRole.selectedItem.toString()
                )
                if (berhasil) {
                    Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    tampilData()
                }
            }
            .setNegativeButton("Batal", null).show()
    }

    private fun confirmHapus(userData: HashMap<String, String>) {
        val idUser = userData["id_user"]?.toInt() ?: return
        AlertDialog.Builder(this)
            .setTitle("Hapus User")
            .setMessage("Yakin ingin menghapus ${userData["nama"]}?")
            .setPositiveButton("Hapus") { _, _ ->
                if (db.deleteUser(idUser)) {
                    Toast.makeText(this, "User dihapus", Toast.LENGTH_SHORT).show()
                    tampilData()
                }
            }
            .setNegativeButton("Batal", null).show()
    }
}