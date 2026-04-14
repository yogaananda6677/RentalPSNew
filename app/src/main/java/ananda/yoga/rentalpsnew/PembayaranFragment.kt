package ananda.yoga.rentalpsnew

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentPembayaranBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBOpenHelper(requireContext())
        tampilData()
    }

    private fun tampilData() {
        val semuaData = db.getAllTransaksiAktif()
        val listData = ArrayList<HashMap<String, String>>()

        for (item in semuaData) {
            val statusBayar = item["status_bayar"] ?: "belum_lunas"
            if (statusBayar.equals("belum_lunas", ignoreCase = true)) {
                listData.add(item)
            }
        }

        b.tvTotalOmzet.text = rupiahFmt(db.getTotalPendapatanHariIni())

        if (listData.isEmpty()) {
            b.tvKosong.visibility = View.VISIBLE
            b.lvPembayaran.visibility = View.GONE
        } else {
            b.tvKosong.visibility = View.GONE
            b.lvPembayaran.visibility = View.VISIBLE

            val adapter = object : SimpleAdapter(
                requireContext(),
                listData,
                R.layout.item_riwayat,
                arrayOf("nomor_ps", "total"),
                intArrayOf(R.id.tvNomorPs, R.id.tvTotal)
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val v = super.getView(position, convertView, parent)
                    val item = listData[position]

                    val mulai = item["jam_mulai"] ?: "-"
                    val selesai = item["jam_selesai"] ?: "-"
                    val statusBayar = item["status_bayar"] ?: "belum_lunas"

                    v.findViewById<TextView>(R.id.tvStatus)?.apply {
                        if (statusBayar.equals("lunas", ignoreCase = true)) {
                            text = "$mulai – $selesai  •  SUDAH BAYAR"
                            setTextColor(Color.parseColor("#16A34A"))
                        } else {
                            text = "$mulai – $selesai  •  BELUM BAYAR"
                            setTextColor(Color.parseColor("#DC2626"))
                        }
                    }
                    return v
                }
            }

            b.lvPembayaran.adapter = adapter
            b.lvPembayaran.setOnItemClickListener { _, _, pos, _ ->
                showDialogBayar(listData[pos])
            }
        }
    }

    private fun showDialogBayar(data: HashMap<String, String>) {
        val idTrans = data["id_transaksi"]?.toIntOrNull() ?: 0
        val tagihan = data["total"]?.toDoubleOrNull() ?: 0.0

        if (db.getStatusBayar(idTrans).equals("lunas", ignoreCase = true)) {
            Toast.makeText(requireContext(), "Transaksi ini sudah lunas!", Toast.LENGTH_SHORT).show()
            tampilData()
            return
        }

        val view = layoutInflater.inflate(R.layout.dialog_pembayaran, null)
        val edtBayar = view.findViewById<EditText>(R.id.edtUangBayar)
        val tvTagihan = view.findViewById<TextView>(R.id.tvTotalDialog)
        val tvKembali = view.findViewById<TextView>(R.id.tvKembalianDialog)
        val rgMetode = view.findViewById<RadioGroup>(R.id.rgMetode)
        val btnBayar = view.findViewById<Button>(R.id.btnHitungKembali)

        tvTagihan.text = "Tagihan: ${rupiahFmt(tagihan)}"
        btnBayar.text = "BAYAR SEKARANG"

        fun updateKembalian() {
            val bayar = edtBayar.text.toString().toDoubleOrNull() ?: 0.0
            val kembalian = bayar - tagihan
            when {
                bayar == 0.0 -> {
                    tvKembali.text = ""
                    tvKembali.setTextColor(Color.parseColor("#64748B"))
                }
                kembalian < 0 -> {
                    tvKembali.text = "Kurang: ${rupiahFmt(-kembalian)}"
                    tvKembali.setTextColor(Color.parseColor("#DC2626"))
                }
                kembalian == 0.0 -> {
                    tvKembali.text = "Pas, tidak ada kembalian"
                    tvKembali.setTextColor(Color.parseColor("#16A34A"))
                }
                else -> {
                    tvKembali.text = "Kembalian: ${rupiahFmt(kembalian)}"
                    tvKembali.setTextColor(Color.parseColor("#16A34A"))
                }
            }
        }

        edtBayar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateKembalian()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        rgMetode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == -1) return@setOnCheckedChangeListener

            val radio = view.findViewById<RadioButton>(checkedId)
            val metode = radio.text.toString().trim().lowercase(Locale.getDefault())

            val isEwallet = metode.contains("e-wallet") ||
                    metode.contains("ewallet") ||
                    metode.contains("dana") ||
                    metode.contains("ovo") ||
                    metode.contains("gopay") ||
                    metode.contains("shopeepay") ||
                    metode.contains("qris")

            if (isEwallet) {
                edtBayar.setText(tagihan.toInt().toString())
                edtBayar.isEnabled = false
                edtBayar.isFocusable = false
                edtBayar.isClickable = false
                edtBayar.inputType = InputType.TYPE_NULL
                tvKembali.text = "Pembayaran e-wallet: otomatis pas"
                tvKembali.setTextColor(Color.parseColor("#16A34A"))
            } else {
                edtBayar.setText("")
                edtBayar.isEnabled = true
                edtBayar.isFocusable = true
                edtBayar.isFocusableInTouchMode = true
                edtBayar.isClickable = true
                edtBayar.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                tvKembali.text = ""
            }
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

        btnBayar.setOnClickListener {
            if (db.getStatusBayar(idTrans).equals("lunas", ignoreCase = true)) {
                Toast.makeText(requireContext(), "Transaksi sudah lunas, tidak bisa dibayar lagi!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                tampilData()
                return@setOnClickListener
            }

            val selectedId = rgMetode.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "Pilih metode pembayaran!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val metode = view.findViewById<RadioButton>(selectedId).text.toString()
            val bayar = edtBayar.text.toString().toDoubleOrNull() ?: 0.0

            if (bayar < tagihan) {
                Toast.makeText(
                    requireContext(),
                    "Uang kurang! Kurang ${rupiahFmt(tagihan - bayar)}",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (db.updateStatusBayar(idTrans, "lunas", metode)) {
                val kembalian = bayar - tagihan
                val pesan = if (kembalian > 0) {
                    "Pembayaran berhasil!\nKembalian: ${rupiahFmt(kembalian)}"
                } else {
                    "Pembayaran berhasil!"
                }

                Toast.makeText(requireContext(), pesan, Toast.LENGTH_LONG).show()
                dialog.dismiss()
                tampilData()
            } else {
                Toast.makeText(requireContext(), "Gagal memproses pembayaran!", Toast.LENGTH_SHORT).show()
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