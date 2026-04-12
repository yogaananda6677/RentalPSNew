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

            val adapter = SimpleAdapter(
                this, listData, R.layout.item_produk,
                // Kita petakan harga ke text2 saja, stok nanti kita ambil manual di ViewBinder
                arrayOf("nama", "jenis", "harga", "id_produk"),
                intArrayOf(R.id.text1, R.id.tvJenisBadge, R.id.text2, R.id.btnMenuProduk)
            )

            adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
                when (view.id) {
                    R.id.text2 -> {
                        val tv = view as TextView
                        val pos = b.listView.getPositionForView(view)

                        // CEK POSISI: Jika tidak valid (-1), jangan diproses agar tidak FC
                        if (pos != AdapterView.INVALID_POSITION && pos < listData.size) {
                            val item = listData[pos]
                            val hargaRaw = item["harga"]?.toDoubleOrNull() ?: 0.0
                            val stok = item["stock"] ?: "0"

                            val localeID = Locale("in", "ID")
                            val formatter = NumberFormat.getCurrencyInstance(localeID)
                            formatter.maximumFractionDigits = 0

                            tv.text = "${formatter.format(hargaRaw)} | Stok: $stok"
                        }
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
                                val item = listData.find { it["id_produk"] == idProduk }
                                if (item != null) {
                                    if (menu.title == "Edit") showDialogEdit(item)
                                    else confirmHapusProduk(item)
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
            // Jika ada error, munculkan pesan agar kita tahu masalahnya di mana
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_produk, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaProduk)
        val spJenis = view.findViewById<Spinner>(R.id.spJenisProduk)
        val edtHarga = view.findViewById<EditText>(R.id.edtHargaProduk)
        val edtStock = view.findViewById<EditText>(R.id.edtStockProduk)

        val jenisList = arrayOf("Makanan", "Minuman", "Snack", "Lainnya")
        spJenis.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenisList)

        AlertDialog.Builder(this)
            .setTitle("Tambah Produk")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = edtNama.text.toString()
                val jenis = spJenis.selectedItem.toString()
                val harga = edtHarga.text.toString()
                val stock = edtStock.text.toString()

                if (nama.isNotEmpty() && harga.isNotEmpty()) {
                    if (db.insertProduk(nama, jenis, harga.toDouble(), stock.toInt())) {
                        Toast.makeText(this, "Produk ditambahkan", Toast.LENGTH_SHORT).show()
                        tampilData()
                    }
                }
            }
            .setNegativeButton("Batal", null).show()
    }

    private fun showDialogEdit(item: HashMap<String, String>) {
        val view = layoutInflater.inflate(R.layout.dialog_produk, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaProduk)
        val spJenis = view.findViewById<Spinner>(R.id.spJenisProduk)
        val edtHarga = view.findViewById<EditText>(R.id.edtHargaProduk)
        val edtStock = view.findViewById<EditText>(R.id.edtStockProduk)

        // Set data lama (hilangkan .0 di inputan agar user tidak bingung)
        edtNama.setText(item["nama"])
        val hargaBulat = item["harga"]?.toDoubleOrNull()?.toLong()?.toString() ?: "0"
        edtHarga.setText(hargaBulat)
        edtStock.setText(item["stock"])

        val jenisList = arrayOf("Makanan", "Minuman", "Snack", "Lainnya")
        spJenis.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenisList)
        val pos = jenisList.indexOf(item["jenis"])
        if (pos >= 0) spJenis.setSelection(pos)

        AlertDialog.Builder(this)
            .setTitle("Edit Produk")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val id = item["id_produk"]!!.toInt()
                val hrg = edtHarga.text.toString().toDoubleOrNull() ?: 0.0
                val stk = edtStock.text.toString().toIntOrNull() ?: 0

                if (db.updateProduk(id, edtNama.text.toString(), spJenis.selectedItem.toString(), hrg, stk)) {
                    Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show()
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
                if (db.deleteProduk(item["id_produk"]!!.toInt())) {
                    Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                    tampilData()
                }
            }.setNegativeButton("Batal", null).show()
    }
}