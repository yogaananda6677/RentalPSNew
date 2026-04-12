package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import ananda.yoga.rentalpsnew.databinding.FragmentLaporanBinding
import java.text.NumberFormat
import java.util.Locale

class LaporanFragment : Fragment() {
    private var _b: FragmentLaporanBinding? = null
    private val b get() = _b!!
    private lateinit var db: DBOpenHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentLaporanBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBOpenHelper(requireContext())

        // Kita tidak butuh LayoutManager karena sudah ganti ke ListView di XML
        tampilData()
    }

    private fun tampilData() {
        val listData = db.getLaporanHarian()
        val omzet = db.getTotalOmzetHariIni()

        // Format Rupiah agar lebih profesional (Rp 50.000)
        val localeID = Locale("in", "ID")
        val formatRupiah = NumberFormat.getCurrencyInstance(localeID)
        b.tvTotalOmzet.text = formatRupiah.format(omzet).replace(",00", "")

        if (listData.isEmpty()) {
            b.tvKosong.visibility = View.VISIBLE
            b.lvLaporan.visibility = View.GONE // ID lvLaporan sesuai XML baru
        } else {
            b.tvKosong.visibility = View.GONE
            b.lvLaporan.visibility = View.VISIBLE

            // Menggunakan SimpleAdapter agar selaras dengan modul lain
            val adapter = SimpleAdapter(
                requireContext(),
                listData,
                R.layout.item_laporan,
                arrayOf("nomor_ps", "jam", "total_harga"), // Key dari DBOpenHelper.getLaporanHarian()
                intArrayOf(R.id.text1, R.id.text2, R.id.tvHargaLaporan)
            )

            // ViewBinder untuk memberi warna hijau pada teks harga
            adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
                if (view.id == R.id.tvHargaLaporan) {
                    val tv = view as TextView
                    val harga = data.toString().toDouble()
                    tv.text = formatRupiah.format(harga).replace(",00", "")
                    tv.setTextColor(android.graphics.Color.parseColor("#16A34A")) // Warna hijau sukses
                    return@ViewBinder true
                }
                false
            }

            b.lvLaporan.adapter = adapter
        }
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