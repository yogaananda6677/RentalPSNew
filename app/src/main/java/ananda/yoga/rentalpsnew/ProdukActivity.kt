package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityProdukBinding
import androidx.activity.enableEdgeToEdge
import java.text.NumberFormat
import java.util.Locale

class ProdukActivity : AppCompatActivity() {

    private lateinit var b: ActivityProdukBinding
    private lateinit var db: DBOpenHelper
    private var listData = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        b = ActivityProdukBinding.inflate(layoutInflater)
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
            listData = db.getAllProduk()

            // Tambahkan "stock" ke dalam arrayOf
            val adapter = SimpleAdapter(
                this, listData, R.layout.item_produk,
                arrayOf("nama", "jenis", "harga", "stock", "id_produk"),
                intArrayOf(R.id.text1, R.id.tvJenisBadge, R.id.text2, R.id.text2, R.id.btnMenuProduk)
            )

            adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
                when (view.id) {
                    R.id.text2 -> {
                        val tv = view as TextView
                        // Kita cari item ini di listData biar stoknya akurat
                        val parent = view.parent as View
                        val namaDiLayar = parent.findViewById<TextView>(R.id.text1).text.toString()
                        val item = listData.find { it["nama"] == namaDiLayar }

                        val harga = item?.get("harga") ?: "0"
                        val stok = item?.get("stock") ?: "0"

                        tv.text = "Rp $harga | Stok: $stok"
                        true
                    }
                    R.id.btnMenuProduk -> {
                        val btn = view as ImageButton
                        val idProduk = data.toString()
                        btn.setOnClickListener { v ->
                            val popup = PopupMenu(this, v)
                            popup.menu.add("Edit")
                            popup.menu.add("Hapus")
                            popup.setOnMenuItemClickListener { menu ->
                                val itemMap = listData.find { it["id_produk"] == idProduk }
                                if (itemMap != null) {
                                    if (menu.title == "Edit") showDialogEdit(itemMap)
                                    else confirmHapusProduk(itemMap)
                                }
                                true
                            }
                            popup.show()
                        }
                        true
                    }
                    else -> false
                }
            }
            b.listView.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat data produk", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_produk, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaProduk)
        val edtHarga = view.findViewById<EditText>(R.id.edtHargaProduk)
        val edtStok = view.findViewById<EditText>(R.id.edtStokProduk)
        val rgKategori = view.findViewById<RadioGroup>(R.id.rgKategoriProduk)

        view.findViewById<RadioButton>(R.id.rbMakanan).isChecked = true

        AlertDialog.Builder(this)
            .setTitle("Tambah Produk")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val rbTerpilih = view.findViewById<RadioButton>(rgKategori.checkedRadioButtonId)
                val kategori = rbTerpilih.text.toString()
                val nama = edtNama.text.toString()
                val harga = edtHarga.text.toString().toDoubleOrNull() ?: 0.0
                val stok = edtStok.text.toString().toIntOrNull() ?: 0

                if (db.insertProduk(nama, kategori, harga, stok)) {
                    Toast.makeText(this, "Berhasil simpan", Toast.LENGTH_SHORT).show()
                    tampilData()
                }
            }
            .setNegativeButton("Batal", null).show()
    }

    private fun showDialogEdit(item: HashMap<String, String>) {
        val view = layoutInflater.inflate(R.layout.dialog_produk, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaProduk)
        val edtHarga = view.findViewById<EditText>(R.id.edtHargaProduk)
        val edtStok = view.findViewById<EditText>(R.id.edtStokProduk)
        val rgKategori = view.findViewById<RadioGroup>(R.id.rgKategoriProduk)

        // Isi data lama ke form
        edtNama.setText(item["nama"])
        edtHarga.setText(item["harga"])
        edtStok.setText(item["stock"])

        val kategoriLama = item["jenis"]?.lowercase() ?: ""
        when {
            kategoriLama.contains("makanan") -> view.findViewById<RadioButton>(R.id.rbMakanan).isChecked = true
            kategoriLama.contains("minuman") -> view.findViewById<RadioButton>(R.id.rbMinuman).isChecked = true
            else -> view.findViewById<RadioButton>(R.id.rbSnack).isChecked = true
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Produk")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val rbTerpilih = view.findViewById<RadioButton>(rgKategori.checkedRadioButtonId)
                val kategoriNew = rbTerpilih.text.toString()
                val namaNew = edtNama.text.toString()
                val hargaNew = edtHarga.text.toString().toDoubleOrNull() ?: 0.0
                val stokNew = edtStok.text.toString().toIntOrNull() ?: 0
                val idProduk = item["id_produk"]!!.toInt()

                if (db.updateProduk(idProduk, namaNew, kategoriNew, hargaNew, stokNew)) {
                    Toast.makeText(this, "Berhasil update", Toast.LENGTH_SHORT).show()
                    tampilData()
                }
            }
            .setNegativeButton("Batal", null).show()
    }

    private fun confirmHapusProduk(item: HashMap<String, String>) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus ${item["nama"]}?")
            .setPositiveButton("Hapus") { _, _ ->
                val idProduk = item["id_produk"]!!.toInt()
                if (db.deleteProduk(idProduk)) {
                    Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                    tampilData()
                }
            }.setNegativeButton("Batal", null).show()
    }
}