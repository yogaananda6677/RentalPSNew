package ananda.yoga.rentalpsnew

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ananda.yoga.rentalpsnew.databinding.FragmentRiwayatBinding
import java.text.NumberFormat
import java.util.*

class RiwayatFragment : Fragment() {

    private var _b: FragmentRiwayatBinding? = null
    private val b get() = _b!!
    private lateinit var db: DBOpenHelper
    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private fun rupiahFmt(v: Double) = fmt.format(v).replace(",00", "")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentRiwayatBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBOpenHelper(requireContext())
        b.rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        tampilData()
    }

    private fun tampilData() {
        val listData = db.getAllRiwayatTransaksi()

        if (listData.isEmpty()) {
            b.tvKosong.visibility  = View.VISIBLE
            b.rvRiwayat.visibility = View.GONE
        } else {
            b.tvKosong.visibility  = View.GONE
            b.rvRiwayat.visibility = View.VISIBLE
            b.rvRiwayat.adapter    = RiwayatAdapter(requireContext(), listData) { item ->
                tampilkanDetail(item)
            }
        }
    }

    private fun tampilkanDetail(item: HashMap<String, String>) {
        val idTrans = item["id_transaksi"]?.toIntOrNull() ?: 0
        val total   = item["total"]?.toDoubleOrNull() ?: 0.0

        val view = layoutInflater.inflate(R.layout.dialog_detail_laporan, null)

        // Info sewa PS
        view.findViewById<TextView>(R.id.tvDetailPs).text =
            "Unit   : ${item["nomor_ps"] ?: "-"} (${item["nama_tipe"] ?: "-"})"

        view.findViewById<TextView>(R.id.tvDetailWaktu).text =
            "Tanggal: ${item["tanggal"] ?: "-"}\n" +
                    "Jam    : ${item["jam_mulai"] ?: "-"} – ${item["jam_selesai"] ?: "-"}  (${item["durasi"] ?: "-"} jam)\n" +
                    "Metode : ${item["metode_pembayaran"] ?: "-"}"

        view.findViewById<TextView>(R.id.tvTotalDetail).text =
            "Total  : ${rupiahFmt(total)}"

        // Detail produk tambahan
        val listP   = db.getDetailProdukByTrans(idTrans)
        val tvProduk = view.findViewById<TextView>(R.id.tvDetailProdukList)
        tvProduk.text = if (listP.isEmpty()) "Tidak ada tambahan produk"
        else listP.joinToString("\n") {
            "• ${it["nama"]} x${it["qty"]}  →  ${rupiahFmt(it["subtotal"]?.toDoubleOrNull() ?: 0.0)}"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Detail Transaksi #$idTrans")
            .setView(view)
            .setPositiveButton("Tutup", null)
            .show()
    }

    override fun onResume() { super.onResume(); tampilData() }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}