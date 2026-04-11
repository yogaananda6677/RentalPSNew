package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityProdukBinding
import android.widget.TextView

class ProdukActivity : AppCompatActivity(), View.OnClickListener {

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

        b.ivBack.setOnClickListener(this)
        b.btnTambah.setOnClickListener(this)

        tampilData()

        b.listView.setOnItemClickListener { _, _, position, _ ->
            val item = listData[position]
            showDialogEdit(
                item["id_produk"]!!.toInt(),
                item["nama"].toString(),
                item["jenis"].toString(),
                item["harga"].toString(),
                item["stock"].toString()
            )
        }

        b.listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val item = listData[position]
                val id = item["id_produk"]!!.toInt()

                AlertDialog.Builder(this)
                    .setTitle("Hapus Data")
                    .setMessage("Yakin ingin menghapus produk ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        val hasil = db.deleteProduk(id)
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
        listData = db.getAllProduk()

        val adapter = SimpleAdapter(
            this,
            listData,
            R.layout.item_produk,
            arrayOf("nama", "harga", "jenis"),
            intArrayOf(R.id.text1, R.id.text2, R.id.tvJenisBadge)
        )

        adapter.setViewBinder { view, data, _ ->
            if (view.id == R.id.text2) {
                // Gabungkan Harga dan Stok untuk sub-text
                val index = listData.indexOfFirst { it["harga"] == data.toString() }
                val stok = listData[index]["stock"]
                val tv = view as TextView
                tv.text = "Rp ${data} | Stok: $stok"
                return@setViewBinder true
            }

            if (view.id == R.id.tvJenisBadge) {
                val jenis = data.toString().lowercase()
                val tv = view as TextView
                tv.text = jenis.uppercase()
                if (jenis == "makanan") {
                    tv.setTextColor(android.graphics.Color.parseColor("#2563EB"))
                    tv.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EFF6FF"))
                } else {
                    tv.setTextColor(android.graphics.Color.parseColor("#EA580C"))
                    tv.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF7ED"))
                }
                return@setViewBinder true
            }
            false
        }
        b.listView.adapter = adapter
    }
    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_produk, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaProduk)
        val spJenis = view.findViewById<Spinner>(R.id.spJenisProduk)
        val edtHarga = view.findViewById<EditText>(R.id.edtHargaProduk)
        val edtStock = view.findViewById<EditText>(R.id.edtStockProduk)

        val jenisList = arrayOf("Makanan", "Minuman", "Snack", "Lainnya")
        val adapterJenis = ArrayAdapter(this, android.R.layout.simple_spinner_item, jenisList)
        adapterJenis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spJenis.adapter = adapterJenis

        AlertDialog.Builder(this)
            .setTitle("Tambah Produk")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = edtNama.text.toString().trim()
                val jenis = spJenis.selectedItem.toString()
                val harga = edtHarga.text.toString().trim()
                val stock = edtStock.text.toString().trim()

                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama produk tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (harga.isEmpty()) {
                    Toast.makeText(this, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (stock.isEmpty()) {
                    Toast.makeText(this, "Stock tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else {
                    val hasil = db.insertProduk(nama, jenis, harga.toDouble(), stock.toInt())
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
        idProduk: Int,
        namaLama: String,
        jenisLama: String,
        hargaLama: String,
        stockLama: String
    ) {
        val view = layoutInflater.inflate(R.layout.dialog_produk, null)
        val edtNama = view.findViewById<EditText>(R.id.edtNamaProduk)
        val spJenis = view.findViewById<Spinner>(R.id.spJenisProduk)
        val edtHarga = view.findViewById<EditText>(R.id.edtHargaProduk)
        val edtStock = view.findViewById<EditText>(R.id.edtStockProduk)

        edtNama.setText(namaLama)
        edtHarga.setText(hargaLama)
        edtStock.setText(stockLama)

        val jenisList = arrayOf("Makanan", "Minuman", "Snack", "Lainnya")
        val adapterJenis = ArrayAdapter(this, android.R.layout.simple_spinner_item, jenisList)
        adapterJenis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spJenis.adapter = adapterJenis

        val posisiJenis = jenisList.indexOf(jenisLama)
        if (posisiJenis >= 0) {
            spJenis.setSelection(posisiJenis)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Produk")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val nama = edtNama.text.toString().trim()
                val jenis = spJenis.selectedItem.toString()
                val harga = edtHarga.text.toString().trim()
                val stock = edtStock.text.toString().trim()

                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama produk tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (harga.isEmpty()) {
                    Toast.makeText(this, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else if (stock.isEmpty()) {
                    Toast.makeText(this, "Stock tidak boleh kosong", Toast.LENGTH_SHORT).show()
                } else {
                    val hasil = db.updateProduk(idProduk, nama, jenis, harga.toDouble(), stock.toInt())
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