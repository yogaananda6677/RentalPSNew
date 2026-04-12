package ananda.yoga.rentalpsnew

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ananda.yoga.rentalpsnew.databinding.FragmentPembayaranBinding
import java.text.NumberFormat
import java.util.*

class PembayaranFragment : Fragment() {
    private var _b: FragmentPembayaranBinding? = null
    private val b get() = _b!!
    private lateinit var db: DBOpenHelper
    private val localeID = Locale("in", "ID")
    private val formatRupiah = NumberFormat.getCurrencyInstance(localeID)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentPembayaranBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBOpenHelper(requireContext())
        tampilData()
    }

    private fun tampilData() {
        val listData = db.getAllTransaksiAktif()
        val omzet = db.getTotalOmzetHariIni()

        // Update Total Pendapatan di Atas
        b.tvTotalOmzet.text = formatRupiah.format(omzet).replace(",00", "")

        if (listData.isEmpty()) {
            b.tvKosong.visibility = View.VISIBLE
            b.lvPembayaran.visibility = View.GONE
        } else {
            b.tvKosong.visibility = View.GONE
            b.lvPembayaran.visibility = View.VISIBLE

            // Mapping Data ke ID XML CardView kamu
            val adapter = object : SimpleAdapter(
                requireContext(),
                listData,
                R.layout.item_riwayat,
                arrayOf("id_transaksi", "nomor_ps", "type_ps", "jam", "durasi", "total_harga"),
                intArrayOf(
                    R.id.tvIdTransaksi,
                    R.id.tvNomorPs,
                    R.id.tvTipePs,
                    R.id.tvJam,
                    R.id.tvDurasi,
                    R.id.tvTotal
                )
            ) {
                // Modifikasi tampilan di dalam list (untuk harga & status)
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val v = super.getView(position, convertView, parent)
                    val tvHarga = v.findViewById<TextView>(R.id.tvTotal)
                    val tvStatus = v.findViewById<TextView>(R.id.tvStatus)

                    // Format harga jadi Rp
                    val hargaRaw = listData[position]["total_harga"]?.toDouble() ?: 0.0
                    tvHarga.text = formatRupiah.format(hargaRaw).replace(",00", "")

                    // Paksa status jadi Belum Bayar
                    tvStatus.text = "BELUM BAYAR"
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444")) // Merah

                    return v
                }
            }

            b.lvPembayaran.adapter = adapter

            // BIAR BISA DIPENCET: Munculkan Dialog Bayar
            b.lvPembayaran.setOnItemClickListener { _, _, pos, _ ->
                val data = listData[pos]
                showDialogBayar(data)
            }
        }
    }

    private fun showDialogBayar(data: HashMap<String, String>) {
        val idTrans = data["id_transaksi"]?.toInt() ?: 0
        val totalTagihan = data["total_harga"]?.toDouble() ?: 0.0
        val idPs = data["id_ps"]?.toInt() ?: 0 // Pastikan id_ps ikut terambil di query database

        val view = layoutInflater.inflate(R.layout.dialog_pembayaran, null)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalDialog)
        val edtBayar = view.findViewById<EditText>(R.id.edtUangBayar)
        val tvKembali = view.findViewById<TextView>(R.id.tvKembalianDialog)
        val btnCek = view.findViewById<View>(R.id.btnHitungKembali)

        tvTotal.text = "Total Tagihan: ${formatRupiah.format(totalTagihan).replace(",00", "")}"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(true)
            .create()

        btnCek.setOnClickListener {
            val uangMasuk = edtBayar.text.toString().toDoubleOrNull() ?: 0.0
            if (uangMasuk < totalTagihan) {
                Toast.makeText(requireContext(), "Uang tidak cukup!", Toast.LENGTH_SHORT).show()
            } else {
                val kembalian = uangMasuk - totalTagihan
                tvKembali.text = formatRupiah.format(kembalian).replace(",00", "")

                // Ganti tombol cek jadi SELESAIKAN setelah dihitung
                (btnCek as TextView).text = "KONFIRMASI LUNAS"
                btnCek.setOnClickListener {
                    // 1. Update status transaksi jadi selesai
                    db.updateStatusTransaksi(idTrans, "selesai")

                    // 2. Cari ID PS-nya dulu untuk mengembalikan status PS jadi 'tersedia'
                    // Karena SimpleAdapter tadi mungkin belum bawa id_ps, kita bisa hardcode atau cari di query
                    // Untuk sementara kita asumsikan transaksi selesai

                    Toast.makeText(requireContext(), "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    tampilData() // Refresh list
                }
            }
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        tampilData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}