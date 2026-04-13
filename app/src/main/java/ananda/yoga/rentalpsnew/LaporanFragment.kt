package ananda.yoga.rentalpsnew

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ananda.yoga.rentalpsnew.databinding.FragmentLaporanBinding
import java.text.NumberFormat
import java.util.*

class LaporanFragment : Fragment(R.layout.fragment_laporan) {

    private var _binding: FragmentLaporanBinding? = null
    private val b get() = _binding!!
    private lateinit var db: DBOpenHelper
    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private fun rupiahFmt(v: Double) = fmt.format(v).replace(",00", "")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLaporanBinding.bind(view)
        db = DBOpenHelper(requireContext())

        b.rvLaporan.layoutManager = LinearLayoutManager(requireContext())

        // Tampilkan data hari ini secara default
        val hariIni = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        b.edtPilihTanggal.setText(hariIni)
        loadDataHarian(hariIni)

        b.edtPilihTanggal.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val tglTerpilih = String.format("%04d-%02d-%02d", year, month + 1, day)
                b.edtPilihTanggal.setText(tglTerpilih)
                loadDataHarian(tglTerpilih)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun loadDataHarian(tgl: String) {
        val data  = db.getLaporanPerTanggal(tgl)
        val total = data.sumOf { it["total"]?.toDoubleOrNull() ?: 0.0 }
        b.tvTotalOmzet.text = "Total: ${rupiahFmt(total)}"

        if (data.isEmpty()) {
            b.tvKosong.visibility   = View.VISIBLE
            b.rvLaporan.visibility  = View.GONE
        } else {
            b.tvKosong.visibility   = View.GONE
            b.rvLaporan.visibility  = View.VISIBLE
            b.rvLaporan.adapter = RiwayatAdapter(requireContext(), data) { item ->
                tampilkanDetail(item)
            }
        }
    }

    private fun tampilkanDetail(item: HashMap<String, String>) {
        val idTrans = item["id_transaksi"]?.toIntOrNull() ?: 0

        val view    = layoutInflater.inflate(R.layout.dialog_detail_laporan, null)
        view.findViewById<TextView>(R.id.tvDetailPs).text    = "Unit   : PS ${item["nomor_ps"]}"
        view.findViewById<TextView>(R.id.tvDetailWaktu).text = "Tanggal: ${item["tanggal"]}"
        view.findViewById<TextView>(R.id.tvTotalDetail).text = "Total  : ${rupiahFmt(item["total"]?.toDoubleOrNull() ?: 0.0)}"

        // Tampilkan jam sewa
        val sewa = db.getDetailSewaByTrans(idTrans)
        val tvSewa = view.findViewById<TextView>(R.id.tvDetailPs)
        if (sewa != null) {
            tvSewa.text = "Unit: PS ${item["nomor_ps"]} (${sewa["nama_tipe"]})\n" +
                    "Jam : ${sewa["jam_mulai"]} – ${sewa["jam_selesai"]}  (${sewa["durasi"]} jam)\n" +
                    "Sewa: ${rupiahFmt(sewa["subtotal"]?.toDoubleOrNull() ?: 0.0)}"
        }

        // Detail produk
        val listP   = db.getDetailProdukByTrans(idTrans)
        val tvProduk = view.findViewById<TextView>(R.id.tvDetailProdukList)
        tvProduk.text = if (listP.isEmpty()) "Tidak ada tambahan produk"
        else listP.joinToString("\n") { "${it["nama"]} x${it["qty"]}  →  ${rupiahFmt(it["subtotal"]?.toDoubleOrNull() ?: 0.0)}" }

        // Metode bayar
        view.findViewById<TextView>(R.id.tvDetailWaktu).text =
            "Tanggal: ${item["tanggal"]}\nMetode : ${item["metode_pembayaran"] ?: "-"}"

        AlertDialog.Builder(requireContext()).setView(view).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}