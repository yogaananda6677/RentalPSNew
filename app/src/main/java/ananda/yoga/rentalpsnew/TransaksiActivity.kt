package ananda.yoga.rentalpsnew

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityTransaksiBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    private var selectedProdukId: Int = 0
    private var selectedProdukHarga: Double = 0.0
    private var selectedProdukStock: Int = 0

    private var subtotalSewa = 0.0
    private var subtotalProduk = 0.0
    private var totalSemua = 0.0

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

        ambilIntent()
        setupAwal()
        setupSpinnerProduk()

        b.ivBack.setOnClickListener(this)
        b.edtJamMulai.setOnClickListener(this)
        b.btnHitungSewa.setOnClickListener(this)
        b.btnTambahProduk.setOnClickListener(this)
        b.btnSimpanTransaksi.setOnClickListener(this)
    }

    private fun ambilIntent() {
        idPs = intent.getIntExtra("id_ps", 0)
        nomorPs = intent.getStringExtra("nomor_ps").toString()
        namaTipe = intent.getStringExtra("nama_tipe").toString()
        hargaPerJam = db.getHargaSewaByNamaTipe(namaTipe)
    }

    private fun setupAwal() {
        b.tvNomorPs.text = nomorPs
        b.tvTipePs.text = namaTipe
        b.tvHargaPerJam.text = "Rp ${hargaPerJam.toInt()}/jam"

        b.tvJamSelesai.text = "-"
        b.tvSubtotalSewa.text = "Rp 0"
        b.tvSubtotalProduk.text = "Rp 0"
        b.tvTotalSemua.text = "Rp 0"
        b.tvDetailProduk.text = "Belum ada produk ditambahkan"
    }

    private fun setupSpinnerProduk() {
        listProduk = db.getAllProdukSpinner()

        val namaProduk = ArrayList<String>()
        for (item in listProduk) {
            namaProduk.add("${item["nama"]} - stok ${item["stock"]}")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaProduk)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spProduk.adapter = adapter

        b.spProduk.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val item = listProduk[position]
                selectedProdukId = item["id_produk"]!!.toInt()
                selectedProdukHarga = item["harga"]!!.toDouble()
                selectedProdukStock = item["stock"]!!.toInt()

                b.tvHargaProduk.text = "Rp ${selectedProdukHarga.toInt()}"
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> finish()
            R.id.edtJamMulai -> showTimePicker()
            R.id.btnHitungSewa -> hitungSewa()
            R.id.btnTambahProduk -> tambahProdukKeList()
            R.id.btnSimpanTransaksi -> simpanTransaksi()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val jam = calendar.get(Calendar.HOUR_OF_DAY)
        val menit = calendar.get(Calendar.MINUTE)

        val dialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            jamMulai = String.format("%02d:%02d", hourOfDay, minute)
            b.edtJamMulai.setText(jamMulai)
        }, jam, menit, true)

        dialog.show()
    }

    private fun hitungSewa() {
        val durasiText = b.edtDurasi.text.toString().trim()

        if (jamMulai.isEmpty()) {
            b.edtJamMulai.error = "Pilih jam mulai"
            b.edtJamMulai.requestFocus()
            return
        }

        if (durasiText.isEmpty()) {
            b.edtDurasi.error = "Durasi wajib diisi"
            b.edtDurasi.requestFocus()
            return
        }

        val durasi = durasiText.toInt()

        if (durasi <= 0) {
            b.edtDurasi.error = "Durasi harus lebih dari 0"
            b.edtDurasi.requestFocus()
            return
        }

        subtotalSewa = durasi * hargaPerJam
        jamSelesai = hitungJamSelesai(jamMulai, durasi)

        b.tvJamSelesai.text = jamSelesai
        b.tvSubtotalSewa.text = "Rp ${subtotalSewa.toInt()}"

        hitungTotalAkhir()
    }

    private fun tambahProdukKeList() {
        if (listProduk.isEmpty()) {
            Toast.makeText(this, "Produk tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val qtyText = b.edtQtyProduk.text.toString().trim()

        if (qtyText.isEmpty()) {
            b.edtQtyProduk.error = "Qty wajib diisi"
            b.edtQtyProduk.requestFocus()
            return
        }

        val qty = qtyText.toInt()

        if (qty <= 0) {
            b.edtQtyProduk.error = "Qty harus lebih dari 0"
            b.edtQtyProduk.requestFocus()
            return
        }

        val indexProdukSama = listProdukDipilih.indexOfFirst { it.idProduk == selectedProdukId }
        val qtySudahAda = if (indexProdukSama >= 0) listProdukDipilih[indexProdukSama].qty else 0
        val totalQtyBaru = qtySudahAda + qty

        if (totalQtyBaru > selectedProdukStock) {
            Toast.makeText(this, "Qty melebihi stok yang tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val namaProduk = listProduk[b.spProduk.selectedItemPosition]["nama"].toString()
        val subtotal = qty * selectedProdukHarga

        if (indexProdukSama >= 0) {
            val produkLama = listProdukDipilih[indexProdukSama]
            val qtyBaru = produkLama.qty + qty
            val subtotalBaru = qtyBaru * produkLama.harga

            listProdukDipilih[indexProdukSama] = ProdukDipilih(
                idProduk = produkLama.idProduk,
                namaProduk = produkLama.namaProduk,
                harga = produkLama.harga,
                qty = qtyBaru,
                subtotal = subtotalBaru
            )
        } else {
            listProdukDipilih.add(
                ProdukDipilih(
                    idProduk = selectedProdukId,
                    namaProduk = namaProduk,
                    harga = selectedProdukHarga,
                    qty = qty,
                    subtotal = subtotal
                )
            )
        }

        b.edtQtyProduk.setText("")
        hitungSubtotalProduk()
        tampilProdukDipilih()

        Toast.makeText(this, "Produk ditambahkan", Toast.LENGTH_SHORT).show()
    }

    private fun tampilProdukDipilih() {
        if (listProdukDipilih.isEmpty()) {
            b.tvDetailProduk.text = "Belum ada produk ditambahkan"
            return
        }

        val sb = StringBuilder()
        for ((index, item) in listProdukDipilih.withIndex()) {
            sb.append(index + 1)
                .append(". ")
                .append(item.namaProduk)
                .append(" x")
                .append(item.qty)
                .append(" = Rp ")
                .append(item.subtotal.toInt())
                .append("\n")
        }

        b.tvDetailProduk.text = sb.toString()
    }

    private fun hitungSubtotalProduk() {
        subtotalProduk = listProdukDipilih.sumOf { it.subtotal }
        b.tvSubtotalProduk.text = "Rp ${subtotalProduk.toInt()}"
        hitungTotalAkhir()
    }

    private fun hitungTotalAkhir() {
        totalSemua = subtotalSewa + subtotalProduk
        b.tvTotalSemua.text = "Rp ${totalSemua.toInt()}"
    }

    private fun hitungJamSelesai(jamMulai: String, durasi: Int): String {
        val parts = jamMulai.split(":")
        var jam = parts[0].toInt()
        val menit = parts[1].toInt()

        jam += durasi
        if (jam >= 24) jam -= 24

        return String.format("%02d:%02d", jam, menit)
    }

    private fun simpanTransaksi() {
        val durasiText = b.edtDurasi.text.toString().trim()

        if (jamMulai.isEmpty() || durasiText.isEmpty()) {
            Toast.makeText(this, "Hitung sewa PS dulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (subtotalSewa <= 0) {
            Toast.makeText(this, "Subtotal sewa belum dihitung", Toast.LENGTH_SHORT).show()
            return
        }

        val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // sementara admin id = 1
        val idUserDummyAdmin = 1

        val idTransaksi = db.insertTransaksi(
            idUserDummyAdmin,
            tanggal,
            totalSemua,
            "aktif"
        )

        if (idTransaksi == -1L) {
            Toast.makeText(this, "Gagal simpan transaksi", Toast.LENGTH_SHORT).show()
            return
        }

        val detailSewaBerhasil = db.insertDetailSewaPs(
            idTransaksi,
            idPs,
            durasiText.toInt(),
            jamMulai,
            jamSelesai,
            namaTipe,
            hargaPerJam,
            subtotalSewa
        )

        if (!detailSewaBerhasil) {
            Toast.makeText(this, "Gagal simpan detail sewa", Toast.LENGTH_SHORT).show()
            return
        }

        for (item in listProdukDipilih) {
            db.insertDetailProduk(
                idTransaksi,
                item.idProduk,
                item.qty,
                item.subtotal
            )
        }

        db.updateStatusPs(idPs, "dipakai")

        Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
        finish()
    }
}