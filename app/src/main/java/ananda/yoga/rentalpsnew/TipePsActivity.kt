package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityTipePsBinding

class TipePsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var b: ActivityTipePsBinding
    private lateinit var db: DBOpenHelper
    private var listData = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityTipePsBinding.inflate(layoutInflater)
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
                item["id_tipe"]!!.toInt(),
                item["nama_tipe"].toString(),
                item["harga_sewa"].toString()
            )
        }

        b.listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val item = listData[position]
                val id = item["id_tipe"]!!.toInt()

                AlertDialog.Builder(this)
                    .setTitle("Hapus Data")
                    .setMessage("Yakin ingin menghapus tipe PS ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        val hasil = db.deleteTipePs(id)
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
        listData = db.getAllTipePs()

        val adapter = SimpleAdapter(
            this,
            listData,
            android.R.layout.simple_list_item_2,
            arrayOf("nama_tipe", "harga_sewa"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )

        b.listView.adapter = adapter
    }

    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_tipe_ps, null)
        val edtNama = view.findViewById<android.widget.EditText>(R.id.edtNamaTipe)
        val edtHarga = view.findViewById<android.widget.EditText>(R.id.edtHargaSewa)

        AlertDialog.Builder(this)
            .setTitle("Tambah Tipe PS")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = edtNama.text.toString().trim()
                val harga = edtHarga.text.toString().trim()

                if (nama.isNotEmpty() && harga.isNotEmpty()) {
                    val hasil = db.insertTipePs(nama, harga.toDouble())
                    if (hasil) {
                        Toast.makeText(this, "Data berhasil ditambah", Toast.LENGTH_SHORT).show()
                        tampilData()
                    } else {
                        Toast.makeText(this, "Data gagal ditambah", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Input tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDialogEdit(id: Int, namaLama: String, hargaLama: String) {
        val view = layoutInflater.inflate(R.layout.dialog_tipe_ps, null)
        val edtNama = view.findViewById<android.widget.EditText>(R.id.edtNamaTipe)
        val edtHarga = view.findViewById<android.widget.EditText>(R.id.edtHargaSewa)

        edtNama.setText(namaLama)
        edtHarga.setText(hargaLama)

        AlertDialog.Builder(this)
            .setTitle("Edit Tipe PS")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val nama = edtNama.text.toString().trim()
                val harga = edtHarga.text.toString().trim()

                if (nama.isNotEmpty() && harga.isNotEmpty()) {
                    val hasil = db.updateTipePs(id, nama, harga.toDouble())
                    if (hasil) {
                        Toast.makeText(this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                        tampilData()
                    } else {
                        Toast.makeText(this, "Data gagal diupdate", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Input tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}