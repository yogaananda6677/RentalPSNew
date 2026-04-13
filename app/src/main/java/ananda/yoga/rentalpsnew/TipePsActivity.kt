package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityTipePsBinding
import java.text.NumberFormat
import java.util.Locale

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

        // Penyesuaian Padding System Bar agar tidak tertutup header
        ViewCompat.setOnApplyWindowInsetsListener(b.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Listener Tombol
        b.ivBack.setOnClickListener(this)
        b.btnTambah.setOnClickListener(this)

        tampilData()

        // Klik untuk Edit
        b.listView.setOnItemClickListener { _, _, position, _ ->
            val item = listData[position]
            showDialogEdit(
                item["id_tipe"].toString(),
                item["nama_tipe"].toString(),
                item["harga_sewa"].toString()
            )
        }

        // Long Klik untuk Hapus
        b.listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val item = listData[position]
                val id = item["id_tipe"].toString()

                AlertDialog.Builder(this)
                    .setTitle("Hapus Data")
                    .setMessage("Yakin ingin menghapus tipe ${item["nama_tipe"]}?")
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
            R.layout.item_tipe_ps, // <--- UBAH BARIS INI (Wajib!)
            arrayOf("nama_tipe"),
            intArrayOf(R.id.text1)
        )
        b.listView.adapter = adapter

        b.listView.setOnItemClickListener { _, _, position, _ ->
            val item = listData[position]
            showDialogEdit(
                item["id_tipe"].toString(),
                item["nama_tipe"].toString(),
                item["harga_sewa"].toString()
            )
        }

        // Logic Klik Lama untuk Hapus (Tetap ada)
        b.listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val item = listData[position]
                val id = item["id_tipe"].toString()

                AlertDialog.Builder(this)
                    .setTitle("Hapus Data")
                    .setMessage("Yakin ingin menghapus tipe ${item["nama_tipe"]}?")
                    .setPositiveButton("Ya") { _, _ ->
                        val hasil = db.deleteTipePs(id)
                        if (hasil) {
                            Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                            tampilData()
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
                true
            }
    }
    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_tipe_ps, null)

        // Inisialisasi AutoCompleteTextView
        val edtNama = view.findViewById<android.widget.AutoCompleteTextView>(R.id.etNamaTipe)
        val edtHarga = view.findViewById<android.widget.EditText>(R.id.etHargaSewa)

        // Daftar saran yang akan muncul
        val saranTipe = arrayOf("PS 2", "PS 3", "PS 4", "PS 4 Pro", "PS 5")

        // Buat Adapter untuk AutoComplete
        val adapterSaran = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            saranTipe
        )

        // Pasang adapter ke view
        edtNama.setAdapter(adapterSaran)

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
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    private fun showDialogEdit(id: String, namaLama: String, hargaLama: String) {
        val view = layoutInflater.inflate(R.layout.dialog_tipe_ps, null)
        val edtNama = view.findViewById<android.widget.EditText>(R.id.etNamaTipe)
        val edtHarga = view.findViewById<android.widget.EditText>(R.id.etHargaSewa)

        edtNama.setText(namaLama)
        edtHarga.setText(hargaLama)

        AlertDialog.Builder(this)
            .setTitle("Edit Tipe PS")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val nama = edtNama.text.toString().trim()
                val hargaStr = edtHarga.text.toString().trim()

                if (nama.isNotEmpty() && hargaStr.isNotEmpty()) {
                    val hasil = db.updateTipePs(id, nama, hargaStr.toDouble())
                    if (hasil) {
                        Toast.makeText(this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                        tampilData()
                    } else {
                        Toast.makeText(this, "Gagal update data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Input tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}