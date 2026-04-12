package ananda.yoga.rentalpsnew

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
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

    private var listPsTersedia = ArrayList<HashMap<String, String>>()
    private var listProduk = ArrayList<HashMap<String, String>>()
    private val listProdukDipilih = ArrayList<ProdukDipilih>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        b = ActivityTransaksiBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupSpinnerPs()
        setupSpinnerProduk()

        b.ivBack.setOnClickListener(this)
        b.edtJamMulai.setOnClickListener(this)
        b.btnHitungSewa.setOnClickListener(this)
        b.btnTambahProduk.setOnClickListener(this)
        b.btnSimpanTransaksi.setOnClickListener(this)
    }

    private fun setupSpinnerPs() {
        // Ambil data PS yang statusnya 'tersedia'
        val rawData = db.getAllPlaystation()
        listPsTersedia = rawData.filter { it["status_ps"] == "tersedia" } as ArrayList<HashMap<String, String>>

        val namaPsList = listPsTersedia.map { "${it["nomor_ps"]} (${it["nama_tipe"]})" }

        // Gunakan layout custom agar Spinner rapi (tidak mepet)
        val adapter = ArrayAdapter(this, R.layout.item_spinner_custom, namaPsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spPilihPs.adapter = adapter

        b.spPilihPs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (listPsTersedia.isNotEmpty()) {
                    val item = listPsTersedia[pos]
                    idPs = item["id_ps"]!!.toInt()
                    nomorPs = item["nomor_ps"]!!
                    namaTipe = item["nama_tipe"]!!
                    hargaPerJam = db.getHargaSewaByNamaTipe(namaTipe)

                    b.tvNomorPs.text = nomorPs
                    b.tvTipePs.text = "Tipe: $namaTipe"
                    b.tvHargaPerJam.text = "Harga: Rp ${hargaPerJam.toInt()}/jam"
                }
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun setupSpinnerProduk() {
        listProduk = db.getAllProdukSpinner()
        val namaProd = listProduk.map { "${it["nama"]} (Rp ${it["harga"]?.toDouble()?.toInt()})" }

        // Gunakan layout custom di sini juga
        val adapterProd = ArrayAdapter(this, R.layout.item_spinner_custom, namaProd)
        adapterProd.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spProduk.adapter = adapterProd
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> finish()
            R.id.edtJamMulai -> {
                val c = Calendar.getInstance()
                TimePickerDialog(this, { _, h, m ->
                    jamMulai = String.format("%02d:%02d", h, m)
                    b.edtJamMulai.setText(jamMulai)
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
            }
            R.id.btnHitungSewa -> {
                val durasiStr = b.edtDurasi.text.toString()
                val durasi = durasiStr.toIntOrNull() ?: 0

                if (jamMulai.isEmpty() || durasi <= 0) {
                    Toast.makeText(this, "Isi Jam Mulai & Durasi dengan benar!", Toast.LENGTH_SHORT).show()
                    return
                }

                subtotalSewa = durasi * hargaPerJam
                val parts = jamMulai.split(":")
                val jam = (parts[0].toInt() + durasi) % 24
                jamSelesai = String.format("%02d:%02d", jam, parts[1].toInt())

                b.tvJamSelesai.text = "Selesai: $jamSelesai"
                b.tvSubtotalSewa.text = "Subtotal PS: Rp ${subtotalSewa.toInt()}"
                updateTotal()
            }
            R.id.btnTambahProduk -> {
                val qty = b.edtQtyProduk.text.toString().toIntOrNull() ?: 0
                val pos = b.spProduk.selectedItemPosition

                if (pos == -1 || listProduk.isEmpty() || qty <= 0) {
                    Toast.makeText(this, "Pilih produk dan isi jumlah!", Toast.LENGTH_SHORT).show()
                    return
                }

                val item = listProduk[pos]
                val harga = item["harga"]?.toDouble() ?: 0.0
                val sub = qty * harga

                listProdukDipilih.add(ProdukDipilih(
                    item["id_produk"]!!.toInt(),
                    item["nama"]!!,
                    harga,
                    qty,
                    sub
                ))

                subtotalProduk = listProdukDipilih.sumOf { it.subtotal }
                b.tvSubtotalProduk.text = "Subtotal Produk: Rp ${subtotalProduk.toInt()}"
                b.tvDetailProduk.text = listProdukDipilih.joinToString("\n") { "${it.namaProduk} x${it.qty}" }
                updateTotal()
                b.edtQtyProduk.setText("")
            }
            R.id.btnSimpanTransaksi -> simpan()
        }
    }

    private fun updateTotal() {
        totalSemua = subtotalSewa + subtotalProduk
        b.tvTotalSemua.text = "TOTAL: Rp ${totalSemua.toInt()}"
    }

    private fun simpan() {
        if (subtotalSewa <= 0) {
            Toast.makeText(this, "Silakan hitung sewa terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        val tgl = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Asumsi ID User 1 (Admin) sudah ada di database
        val idTrans = db.insertTransaksi(1, tgl, totalSemua, "aktif")

        if (idTrans != -1L) {
            // Simpan detail sewa
            db.insertDetailSewaPs(
                idTrans, idPs, b.edtDurasi.text.toString().toInt(),
                jamMulai, jamSelesai, namaTipe, hargaPerJam, subtotalSewa
            )

            // Simpan detail jajan jika ada
            if (listProdukDipilih.isNotEmpty()) {
                for (p in listProdukDipilih) {
                    db.insertDetailProduk(idTrans, p.idProduk, p.qty, p.subtotal)
                }
            }

            // Update status PS menjadi 'dipakai' agar tidak muncul di transaksi baru
            db.updateStatusPs(idPs, "dipakai")

            Toast.makeText(this, "Transaksi Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Gagal menyimpan transaksi!", Toast.LENGTH_SHORT).show()
        }
    }
}