package ananda.yoga.rentalpsnew

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityTransaksiBinding
import java.text.SimpleDateFormat
import java.util.*

class TransaksiActivity : AppCompatActivity(), View.OnClickListener {

    data class ProdukDipilih(
        val idProduk: Int,
        val namaProduk: String,
        val harga: Double,
        var qty: Int,
        var subtotal: Double
    )

    private var isProcessing = false
    private lateinit var b: ActivityTransaksiBinding
    private lateinit var db: DBOpenHelper

    private var idPs: Int = 0
    private var nomorPs: String = ""
    private var namaTipe: String = ""
    private var hargaPerJam: Double = 0.0
    private var jamMulai: String = ""
    private var jamSelesai: String = ""
    private var subtotalSewa = 0.0
    private var subtotalProduk = 0.0
    private var totalSemua = 0.0
    private var durasiTerpilih: Int = 1

    private var listProduk = ArrayList<HashMap<String, String>>()
    private val listProdukDipilih = ArrayList<ProdukDipilih>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        b = ActivityTransaksiBinding.inflate(layoutInflater)
        setContentView(b.root)
        db = DBOpenHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        setupSpinnerProduk()
        setupSpinnerDurasi()

        val idPsTarget = intent.getSerializableExtra("ID_PS_TARGET")?.toString()
        val nomorPsTarget = intent.getStringExtra("NOMOR_PS_TARGET")
        val tipePsTarget  = intent.getStringExtra("TIPE_PS_TARGET")

        if (idPsTarget != null && idPsTarget != "0") {
            idPs      = idPsTarget.toInt()
            nomorPs   = nomorPsTarget ?: ""
            namaTipe  = tipePsTarget ?: ""
            hargaPerJam = db.getHargaSewaByNamaTipe(namaTipe)

            b.tvNomorPs.text    = nomorPs
            b.tvTipePs.text     = "Tipe: $namaTipe"
            b.tvHargaPerJam.text= "Harga: Rp ${hargaPerJam.toInt()}/jam"
        } else {
            Toast.makeText(this, "Kesalahan: Data Unit PS tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
        }

        b.ivBack.setOnClickListener(this)
        b.edtJamMulai.setOnClickListener(this)
        b.btnHitungSewa.setOnClickListener(this)
        b.btnTambahProduk.setOnClickListener(this)
        b.btnSimpanTransaksi.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack            -> finish()
            R.id.edtJamMulai       -> showTimePicker()
            R.id.btnHitungSewa     -> hitungSewa()
            R.id.btnTambahProduk   -> tambahProduk()
            R.id.btnSimpanTransaksi-> simpanData()
        }
    }

    private fun showTimePicker() {
        val c = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            jamMulai = String.format("%02d:%02d", h, m)
            b.edtJamMulai.setText(jamMulai)
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }

    private fun hitungSewa() {
        if (jamMulai.isEmpty()) {
            Toast.makeText(this, "Pilih jam mulai dulu!", Toast.LENGTH_SHORT).show()
            return
        }
        subtotalSewa = durasiTerpilih * hargaPerJam

        val parts = jamMulai.split(":")
        val jamSelesaiInt = (parts[0].toInt() + durasiTerpilih) % 24
        jamSelesai = String.format("%02d:%02d", jamSelesaiInt, parts[1].toInt())

        b.tvJamSelesai.text    = "Selesai: $jamSelesai"
        b.tvSubtotalSewa.text  = "Sewa PS: Rp ${subtotalSewa.toInt()}"
        updateTotal()
    }

    private fun tambahProduk() {
        val qtyStr = b.edtQtyProduk.text.toString()
        if (qtyStr.isEmpty()) { Toast.makeText(this, "Isi jumlah produk!", Toast.LENGTH_SHORT).show(); return }
        val qty = qtyStr.toInt()
        val pos = b.spProduk.selectedItemPosition
        if (listProduk.isNotEmpty()) {
            val item  = listProduk[pos]
            val harga = item["harga"]?.toDouble() ?: 0.0
            val sub   = qty * harga
            listProdukDipilih.add(ProdukDipilih(item["id_produk"]!!.toInt(), item["nama"]!!, harga, qty, sub))
            subtotalProduk = listProdukDipilih.sumOf { it.subtotal }
            b.tvSubtotalProduk.text = "Produk: Rp ${subtotalProduk.toInt()}"
            b.tvDetailProduk.text   = listProdukDipilih.joinToString("\n") { "${it.namaProduk} x${it.qty}" }
            updateTotal()
            b.edtQtyProduk.setText("")
        }
    }

    private fun updateTotal() {
        totalSemua = subtotalSewa + subtotalProduk
        b.tvTotalSemua.text = "TOTAL: Rp ${totalSemua.toInt()}"
    }

    private fun simpanData() {
        if (isProcessing) return
        if (jamMulai.isEmpty()) { Toast.makeText(this, "Pilih jam mulai!", Toast.LENGTH_SHORT).show(); return }
        if (subtotalSewa <= 0) hitungSewa()
        if (idPs == 0 || subtotalSewa <= 0) { Toast.makeText(this, "Gagal: Data belum lengkap!", Toast.LENGTH_SHORT).show(); return }

        isProcessing = true
        b.btnSimpanTransaksi.isEnabled = false
        b.btnSimpanTransaksi.text      = "Sedang Memproses..."

        val tgl     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val idTrans = db.insertTransaksi(1, tgl, totalSemua, "aktif")

        if (idTrans != -1L) {
            db.insertDetailSewaPs(idTrans, idPs, durasiTerpilih, jamMulai, jamSelesai, subtotalSewa)
            for (p in listProdukDipilih) db.insertDetailProduk(idTrans, p.idProduk, p.qty, p.subtotal)

            // *** STATUS DISAMAKAN: "dipakai" ***
            db.updateStatusPs(idPs, "dipakai")
            Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            isProcessing = false
            b.btnSimpanTransaksi.isEnabled = true
            b.btnSimpanTransaksi.text      = "PROSES SEKARANG"
            Toast.makeText(this, "Gagal Simpan ke Database!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinnerDurasi() {
        val listJam = arrayOf("1 Jam", "2 Jam", "3 Jam", "4 Jam", "5 Jam")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listJam)
        b.spDurasi.adapter = adapter
        b.spDurasi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { durasiTerpilih = pos + 1 }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun setupSpinnerProduk() {
        listProduk = db.getAllProdukSpinner()
        val display = listProduk.map { "${it["nama"]} (Rp ${it["harga"]})" }
        b.spProduk.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, display)
    }
}