package ananda.yoga.rentalpsnew

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
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

        val statusPs = item["status_ps"].toString()
        val jamSelesai = item["jam_selesai"].toString()

        holder.b.tvStatus.text = statusPs.uppercase()
        holder.b.tvNomorPs.text = item["nomor_ps"]
        holder.b.tvTipePs.text = item["nama_tipe"]

        // warna default text
        holder.b.tvNomorPs.setTextColor(Color.parseColor("#0F172A"))
        holder.b.tvTipePs.setTextColor(Color.parseColor("#475569"))
        holder.b.tvJamSelesai.setTextColor(Color.parseColor("#334155"))
        holder.b.tvCountdown.setTextColor(Color.parseColor("#64748B"))

        when (statusPs) {
            "tersedia" -> {
                holder.b.cardPs.setCardBackgroundColor(Color.parseColor("#F0FDF4"))   // hijau soft
                holder.b.tvStatus.setTextColor(Color.parseColor("#16A34A"))
                holder.b.tvJamSelesai.text = "Selesai: -"
                holder.b.tvCountdown.text = "Belum ada transaksi aktif"
            }

            "dipakai" -> {
                holder.b.cardPs.setCardBackgroundColor(Color.parseColor("#EFF6FF"))   // biru soft
                holder.b.tvStatus.setTextColor(Color.parseColor("#2563EB"))
                holder.b.tvJamSelesai.text = "Selesai: $jamSelesai"
                holder.b.tvCountdown.setTextColor(Color.parseColor("#1D4ED8"))
                updateCountdown(holder, jamSelesai)
            }

            "maintenance" -> {
                holder.b.cardPs.setCardBackgroundColor(Color.parseColor("#FFF7ED"))   // orange soft
                holder.b.tvStatus.setTextColor(Color.parseColor("#EA580C"))
                holder.b.tvJamSelesai.text = "Selesai: -"
                holder.b.tvCountdown.text = "Dalam perawatan"
                holder.b.tvCountdown.setTextColor(Color.parseColor("#C2410C"))
            }

            else -> {
                holder.b.cardPs.setCardBackgroundColor(Color.parseColor("#F8FAFC"))   // netral
                holder.b.tvStatus.setTextColor(Color.parseColor("#64748B"))
                holder.b.tvJamSelesai.text = "Selesai: -"
                holder.b.tvCountdown.text = "-"
            }
        }

        holder.b.cardPs.setOnClickListener {
            if (statusPs == "tersedia") {
                val intent = Intent(context, TransaksiActivity::class.java)
                intent.putExtra("id_ps", item["id_ps"]!!.toInt())
                intent.putExtra("nomor_ps", item["nomor_ps"])
                intent.putExtra("nama_tipe", item["nama_tipe"])
                context.startActivity(intent)
            }
        }
    }

    private fun updateCountdown(holder: ViewHolder, jamSelesai: String) {
        if (jamSelesai.isEmpty()) {
            holder.b.tvCountdown.text = "Waktu tidak tersedia"
            return
        }

        val runnable = object : Runnable {
            override fun run() {
                val sisa = hitungSisaWaktu(jamSelesai)
                holder.b.tvCountdown.text = sisa
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun hitungSisaWaktu(jamSelesai: String): String {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())

            val sekarang = Calendar.getInstance()
            val selesai = Calendar.getInstance()

            val parsed = format.parse(jamSelesai)
            val temp = Calendar.getInstance()
            temp.time = parsed!!

            selesai.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
            selesai.set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
            selesai.set(Calendar.SECOND, 0)

            if (selesai.before(sekarang)) {
                selesai.add(Calendar.DAY_OF_MONTH, 1)
            }

            val diff = selesai.timeInMillis - sekarang.timeInMillis

            if (diff <= 0) {
                "Waktu habis"
            } else {
                val jam = diff / (1000 * 60 * 60)
                val menit = (diff / (1000 * 60)) % 60
                val detik = (diff / 1000) % 60
                String.format("Sisa: %02d:%02d:%02d", jam, menit, detik)
            }
        } catch (e: Exception) {
            "Format waktu salah"
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        handler.removeCallbacksAndMessages(null)
    }
}