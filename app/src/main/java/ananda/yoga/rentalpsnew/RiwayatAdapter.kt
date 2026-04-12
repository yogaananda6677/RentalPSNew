package ananda.yoga.rentalpsnew

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ananda.yoga.rentalpsnew.databinding.ItemRiwayatBinding

class RiwayatAdapter(private val list: ArrayList<HashMap<String, String>>) :
    RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    class ViewHolder(val b: ItemRiwayatBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val b = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(b)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val context = holder.itemView.context

        holder.b.apply {
            tvIdTransaksi.text = "TRX-${item["id_transaksi"]}"
            tvTanggal.text = item["tanggal"]
            tvNomorPs.text = item["nomor_ps"]
            tvTipePs.text = item["type_ps"]
            tvJam.text = item["jam"]
            tvDurasi.text = "Durasi: ${item["durasi"]} jam"
            tvTotal.text = "Rp ${item["total_harga"]}"

            val status = item["status_transaksi"] ?: "aktif"
            tvStatus.text = status.uppercase()

            // Warna status: Biru untuk Aktif (Belum Bayar), Hijau untuk Selesai (Lunas)
            if (status == "aktif") {
                tvStatus.setTextColor(Color.parseColor("#2563EB"))
            } else {
                tvStatus.setTextColor(Color.parseColor("#16A34A"))
            }
        }

        // Klik item untuk memproses pembayaran jika status masih aktif
        holder.itemView.setOnClickListener {
            if (item["status_transaksi"] == "aktif") {
                showDialogBayar(context, item)
            } else {
                Toast.makeText(context, "Transaksi ini sudah lunas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDialogBayar(context: android.content.Context, item: HashMap<String, String>) {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_pembayaran, null)

        val tvTotal = view.findViewById<TextView>(R.id.tvTotalDialog)
        val edtBayar = view.findViewById<EditText>(R.id.edtUangBayar)
        val tvKembali = view.findViewById<TextView>(R.id.tvKembalianDialog)
        val btnCek = view.findViewById<Button>(R.id.btnHitungKembali)

        val totalHarga = item["total_harga"]?.toDouble() ?: 0.0
        tvTotal.text = "Total Tagihan: Rp ${totalHarga.toInt()}"

        btnCek.setOnClickListener {
            val bayarText = edtBayar.text.toString()
            if (bayarText.isEmpty()) {
                edtBayar.error = "Masukkan uang bayar"
                return@setOnClickListener
            }

            val bayar = bayarText.toDouble()
            if (bayar < totalHarga) {
                edtBayar.error = "Uang kurang!"
                tvKembali.text = "Kembalian: Rp 0"
            } else {
                val kembalian = bayar - totalHarga
                tvKembali.text = "Kembalian: Rp ${kembalian.toInt()}"
            }
        }

        builder.setView(view)
        builder.setTitle("Proses Pembayaran")
        builder.setPositiveButton("LUNASKAN") { _, _ ->
            val db = DBOpenHelper(context)
            val idTrans = item["id_transaksi"]!!.toInt()

            // 1. Ubah status transaksi jadi 'selesai' agar hilang dari daftar PEMBAYARAN
            // PS tetap 'tidak tersedia' sampai jam sewa habis (fleksibel)
            if (db.updateStatusTransaksi(idTrans, "selesai")) {
                Toast.makeText(context, "Pembayaran Berhasil! Data masuk ke Laporan.", Toast.LENGTH_SHORT).show()

                // 2. Refresh Halaman (Opsional: tergantung struktur navigasi kamu)
                if (context is MainActivity) {
                    (context as MainActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, PembayaranFragment())
                        .commit()
                }
            }
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    override fun getItemCount(): Int = list.size
}