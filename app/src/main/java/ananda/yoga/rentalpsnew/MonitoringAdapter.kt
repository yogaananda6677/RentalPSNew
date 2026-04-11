package ananda.yoga.rentalpsnew

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ananda.yoga.rentalpsnew.databinding.ItemMonitoringPsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonitoringAdapter(
    private val context: Context,
    private val listData: ArrayList<HashMap<String, String>>
) : RecyclerView.Adapter<MonitoringAdapter.ViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val notifiedPs = HashSet<Int>()
    private val db = DBOpenHelper(context)

    inner class ViewHolder(val b: ItemMonitoringPsBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMonitoringPsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listData[position]

        val idPs = item["id_ps"]?.toIntOrNull() ?: 0
        val statusPs = item["status_ps"].toString()
        val jamSelesai = item["jam_selesai"].toString()

        holder.b.tvStatus.text = statusPs.uppercase()
        holder.b.tvNomorPs.text = item["nomor_ps"]
        holder.b.tvTipePs.text = item["nama_tipe"]

        holder.b.tvNomorPs.setTextColor(Color.parseColor("#0F172A"))
        holder.b.tvTipePs.setTextColor(Color.parseColor("#475569"))
        holder.b.tvJamSelesai.setTextColor(Color.parseColor("#334155"))
        holder.b.tvCountdown.setTextColor(Color.parseColor("#64748B"))

        // Cari bagian when (statusPs) dan ganti kodenya jadi ini:
        when (statusPs.lowercase()) {
            "tersedia" -> {
                holder.b.cardPs.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                holder.b.tvStatus.text = "Tersedia"
                holder.b.tvStatus.setTextColor(Color.parseColor("#16A34A")) // Hijau
                holder.b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                holder.b.tvCountdown.text = "--:--:--"
                holder.b.tvCountdown.setTextColor(Color.parseColor("#94A3B8"))
            }
            "dipakai" -> {
                holder.b.cardPs.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                holder.b.tvStatus.text = "Dipakai"
                holder.b.tvStatus.setTextColor(Color.parseColor("#2563EB")) // Biru
                holder.b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
                holder.b.tvCountdown.setTextColor(Color.parseColor("#EF4444")) // Merah biar admin waspada
                updateCountdown(holder, idPs, jamSelesai)
            }
            "maintenance" -> {
                holder.b.cardPs.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                holder.b.tvStatus.text = "Maintenance"
                holder.b.tvStatus.setTextColor(Color.parseColor("#EA580C")) // Orange
                holder.b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF7ED"))
                holder.b.tvCountdown.text = "OFFLINE"
            }
        }
        holder.b.cardPs.setOnClickListener {
            if (item["status_ps"] == "tersedia") {
                val intent = Intent(context, TransaksiActivity::class.java)
                intent.putExtra("id_ps", item["id_ps"]!!.toInt())
                intent.putExtra("nomor_ps", item["nomor_ps"])
                intent.putExtra("nama_tipe", item["nama_tipe"])
                context.startActivity(intent)
            }
        }
    }

    private fun updateCountdown(holder: ViewHolder, idPs: Int, jamSelesai: String) {
        if (jamSelesai.isEmpty()) {
            holder.b.tvCountdown.text = "Waktu tidak tersedia"
            return
        }

        val runnable = object : Runnable {
            override fun run() {
                val diff = getDiffMillis(jamSelesai)

                if (diff <= 0) {
                    holder.b.tvCountdown.text = "Waktu habis"
                    holder.b.tvJamSelesai.text = "Selesai: -"
                    holder.b.tvStatus.text = "TERSEDIA"
                    holder.b.tvStatus.setTextColor(Color.parseColor("#16A34A"))
                    holder.b.cardPs.setCardBackgroundColor(Color.parseColor("#F0FDF4"))

                    if (!notifiedPs.contains(idPs)) {
                        notifiedPs.add(idPs)

                        val berhasil = db.selesaiTransaksiByPs(idPs)
                        if (berhasil) {
                            Toast.makeText(
                                context,
                                "Waktu PS ${holder.b.tvNomorPs.text} sudah habis",
                                Toast.LENGTH_SHORT
                            ).show()

                            val position = holder.bindingAdapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                listData[position]["status_ps"] = "tersedia"
                                listData[position]["jam_selesai"] = ""
                                notifyItemChanged(position)
                            }
                        }
                    }

                    return
                }

                val jam = diff / (1000 * 60 * 60)
                val menit = (diff / (1000 * 60)) % 60
                val detik = (diff / 1000) % 60

                holder.b.tvCountdown.text =
                    String.format("Sisa: %02d:%02d:%02d", jam, menit, detik)

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }

    private fun getDiffMillis(jamSelesai: String): Long {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())

            val sekarang = Calendar.getInstance()
            val selesai = Calendar.getInstance()

            val parsed = format.parse(jamSelesai) ?: return -1
            val temp = Calendar.getInstance()
            temp.time = parsed

            selesai.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
            selesai.set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
            selesai.set(Calendar.SECOND, 0)

            if (selesai.before(sekarang)) {
                selesai.add(Calendar.DAY_OF_MONTH, 1)
            }

            selesai.timeInMillis - sekarang.timeInMillis
        } catch (e: Exception) {
            -1
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        handler.removeCallbacksAndMessages(null)
    }
}