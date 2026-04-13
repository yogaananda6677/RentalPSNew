package ananda.yoga.rentalpsnew

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class RiwayatAdapter(
    private val context: Context,
    private val listData: ArrayList<HashMap<String, String>>,
    private val onItemClick: (HashMap<String, String>) -> Unit
) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private fun rupiahFmt(v: Double) = fmt.format(v).replace(",00", "")

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvIdTransaksi = v.findViewById<TextView>(R.id.tvIdTransaksi)
        val tvTanggal     = v.findViewById<TextView>(R.id.tvTanggal)
        val tvNomorPs     = v.findViewById<TextView>(R.id.tvNomorPs)
        val tvTipePs      = v.findViewById<TextView>(R.id.tvTipePs)
        val tvJam         = v.findViewById<TextView>(R.id.tvJam)
        val tvDurasi      = v.findViewById<TextView>(R.id.tvDurasi)
        val tvTotal       = v.findViewById<TextView>(R.id.tvTotal)
        val tvMetode      = v.findViewById<TextView>(R.id.tvMetode)
        val tvStatus      = v.findViewById<TextView>(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item   = listData[position]
        val total  = item["total"]?.toDoubleOrNull() ?: 0.0
        val idTrans= item["id_transaksi"] ?: "-"
        val metode = item["metode_pembayaran"] ?: "-"

        holder.tvIdTransaksi.text = "TRX-$idTrans"
        holder.tvTanggal.text     = item["tanggal"] ?: "-"
        holder.tvNomorPs.text     = "Unit ${item["nomor_ps"] ?: "-"}"
        holder.tvTipePs.text      = item["nama_tipe"] ?: "-"
        holder.tvJam.text         = "${item["jam_mulai"] ?: "-"} – ${item["jam_selesai"] ?: "-"}"
        holder.tvDurasi.text      = "Durasi: ${item["durasi"] ?: "-"} jam"
        holder.tvTotal.text       = rupiahFmt(total)
        holder.tvMetode.text      = metode
        holder.tvStatus.text      = "Selesai"

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = listData.size
}