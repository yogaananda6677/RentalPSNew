package ananda.yoga.rentalpsnew

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ananda.yoga.rentalpsnew.databinding.ItemRiwayatBinding

class RiwayatAdapter(
    private val listData: ArrayList<HashMap<String, String>>
) : RecyclerView.Adapter<RiwayatAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemRiwayatBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listData[position]

        val status = item["status_transaksi"].toString()
        val totalHarga = item["total_harga"]?.toDoubleOrNull()?.toInt() ?: 0

        holder.b.tvIdTransaksi.text = "TRX-${item["id_transaksi"]}"
        holder.b.tvTanggal.text = item["tanggal"]
        holder.b.tvNomorPs.text = "PS ${item["nomor_ps"]}"
        holder.b.tvTipePs.text = item["type_ps"]
        holder.b.tvJam.text = "${item["jam_mulai"]} - ${item["jam_selesai"]}"
        holder.b.tvDurasi.text = "${item["durasi"]} jam"
        holder.b.tvTotal.text = "Rp $totalHarga"
        holder.b.tvStatus.text = status.replaceFirstChar { it.uppercase() }

        when (status.lowercase()) {
            "aktif" -> {
                holder.b.tvStatus.text = "Berjalan"
                holder.b.tvStatus.setTextColor(Color.parseColor("#2563EB")) // Blue
                holder.b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
            }
            "selesai" -> {
                holder.b.tvStatus.text = "Selesai"
                holder.b.tvStatus.setTextColor(Color.parseColor("#16A34A")) // Green
                holder.b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
            }
            "batal" -> {
                holder.b.tvStatus.text = "Dibatalkan"
                holder.b.tvStatus.setTextColor(Color.parseColor("#EF4444")) // Red
                holder.b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FEF2F2"))
            }
        }
    }
}