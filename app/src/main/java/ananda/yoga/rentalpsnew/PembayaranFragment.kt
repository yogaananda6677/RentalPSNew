package ananda.yoga.rentalpsnew

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import ananda.yoga.rentalpsnew.databinding.FragmentPembayaranBinding
import java.text.NumberFormat
import java.util.*

class PembayaranFragment : Fragment() {
    private var _b: FragmentPembayaranBinding? = null
    private val b get() = _b!!
    private lateinit var db: DBOpenHelper
    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    private fun rupiahFmt(v: Double) = fmt.format(v).replace(",00", "")

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
        b.tvTotalOmzet.text = rupiahFmt(db.getTotalPendapatanHariIni())

        if (listData.isEmpty()) {
            b.tvKosong.visibility     = View.VISIBLE
            b.lvPembayaran.visibility = View.GONE
        } else {
            b.tvKosong.visibility     = View.GONE
            b.lvPembayaran.visibility = View.VISIBLE

            val adapter = object : SimpleAdapter(
                requireContext(), listData, R.layout.item_riwayat,
                arrayOf("nomor_ps", "total"),
                intArrayOf(R.id.tvNomorPs, R.id.tvTotal)
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val v    = super.getView(position, convertView, parent)
                    val item = listData[position]
                    v.findViewById<TextView>(R.id.tvStatus)?.apply {
                        val mulai   = item["jam_mulai"]   ?: "-"
                        val selesai = item["jam_selesai"] ?: "-"
                        text        = "$mulai – $selesai  •  BELUM BAYAR"
                        setTextColor(android.graphics.Color.parseColor("#DC2626"))
                    }
                    return v
                }
            }
            b.lvPembayaran.adapter = adapter
            b.lvPembayaran.setOnItemClickListener { _, _, pos, _ -> showDialogBayar(listData[pos]) }
        }
    }

    private fun showDialogBayar(data: HashMap<String, String>) {
        val idTrans = data["id_transaksi"]?.toIntOrNull() ?: 0
        val tagihan = data["total"]?.toDoubleOrNull() ?: 0.0

        val view      = layoutInflater.inflate(R.layout.dialog_pembayaran, null)
        val edtBayar  = view.findViewById<EditText>(R.id.edtUangBayar)
        val tvTagihan = view.findViewById<TextView>(R.id.tvTotalDialog)
        val tvKembali = view.findViewById<TextView>(R.id.tvKembalianDialog)
        val rgMetode  = view.findViewById<RadioGroup>(R.id.rgMetode)
        val btnBayar  = view.findViewById<Button>(R.id.btnHitungKembali)

        tvTagihan.text = "Tagihan: ${rupiahFmt(tagihan)}"

        // Kembalian real-time saat user mengetik nominal
        edtBayar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val bayar     = s.toString().toDoubleOrNull() ?: 0.0
                val kembalian = bayar - tagihan
                when {
                    bayar == 0.0     -> { tvKembali.text = ""; tvKembali.setTextColor(0xFF64748B.toInt()) }
                    kembalian < 0    -> { tvKembali.text = "Kurang: ${rupiahFmt(-kembalian)}"; tvKembali.setTextColor(0xFFDC2626.toInt()) }
                    kembalian == 0.0 -> { tvKembali.text = "Pas, tidak ada kembalian"; tvKembali.setTextColor(0xFF16A34A.toInt()) }
                    else             -> { tvKembali.text = "Kembalian: ${rupiahFmt(kembalian)}"; tvKembali.setTextColor(0xFF16A34A.toInt()) }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, bef: Int, a: Int) {}
        })

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        btnBayar.text = "BAYAR SEKARANG"

        btnBayar.setOnClickListener {
            val bayar      = edtBayar.text.toString().toDoubleOrNull() ?: 0.0
            val selectedId = rgMetode.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Pilih metode pembayaran!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (bayar < tagihan) {
                Toast.makeText(requireContext(), "Uang kurang! Kurang ${rupiahFmt(tagihan - bayar)}", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val metode = view.findViewById<RadioButton>(selectedId).text.toString()

            // Update status transaksi → "selesai" dan simpan metode pembayaran
            // PS TIDAK diubah ke "tersedia" di sini.
            // PS akan otomatis jadi "tersedia" ketika countdown di MonitoringAdapter habis.
            if (db.updateStatusTransaksi(idTrans, "selesai", metode)) {
                val kembalian = bayar - tagihan
                val pesan = if (kembalian > 0)
                    "Pembayaran berhasil!\nKembalian: ${rupiahFmt(kembalian)}"
                else
                    "Pembayaran berhasil!"
                Toast.makeText(requireContext(), pesan, Toast.LENGTH_LONG).show()
                dialog.dismiss()
                tampilData()
            } else {
                Toast.makeText(requireContext(), "Gagal memproses pembayaran!", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    override fun onResume() { super.onResume(); tampilData() }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}