package ananda.yoga.rentalpsnew

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import ananda.yoga.rentalpsnew.databinding.ItemMonitoringPsBinding
import java.text.SimpleDateFormat
import java.util.*

class MonitoringAdapter(
    private val context: Context,
    private var listData: ArrayList<HashMap<String, String>>
) : RecyclerView.Adapter<MonitoringAdapter.ViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val notifiedPs = HashSet<Int>()
    private val db = DBOpenHelper(context)

    inner class ViewHolder(val b: ItemMonitoringPsBinding) :
        RecyclerView.ViewHolder(b.root) {
        var countdownRunnable: Runnable? = null
    }

    // Fungsi untuk memperbarui data dari Fragment
    fun updateList(newList: ArrayList<HashMap<String, String>>) {
        this.listData = ArrayList(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMonitoringPsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listData[position]
        val idPs = item["id_ps"]?.toIntOrNull() ?: 0
        val statusPs = item["status_ps"].toString()
        val jamSelesai = item["jam_selesai"].toString()

        holder.b.tvNomorPs.text = item["nomor_ps"]
        holder.b.tvTipePs.text = item["nama_tipe"]
        holder.b.tvJamSelesai.text = if (jamSelesai.isNotEmpty()) "Selesai: $jamSelesai" else "Selesai: -"

        // Stop countdown lama
        holder.countdownRunnable?.let { handler.removeCallbacks(it) }

        when (statusPs.lowercase()) {
            "tersedia" -> {
                holder.b.tvStatus.text = "Tersedia"
                holder.b.tvStatus.setTextColor(Color.parseColor("#16A34A"))
                holder.b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                holder.b.tvCountdown.text = "READY"
                holder.b.tvCountdown.setTextColor(Color.parseColor("#94A3B8"))
            }
            "dipakai" -> {
                holder.b.tvStatus.text = "Dipakai"
                holder.b.tvStatus.setTextColor(Color.parseColor("#2563EB"))
                holder.b.tvStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
                holder.b.tvCountdown.setTextColor(Color.parseColor("#EF4444"))
                startCountdown(holder, idPs, jamSelesai)
            }
            else -> {
                holder.b.tvStatus.text = "Offline"
                holder.b.tvStatus.setTextColor(Color.parseColor("#EA580C"))
                holder.b.tvCountdown.text = "--:--:--"
            }
        }

        holder.b.cardPs.setOnClickListener {
            if (statusPs.lowercase() == "tersedia") {
                val intent = Intent(context, TransaksiActivity::class.java)
                intent.putExtra("id_ps", idPs)
                intent.putExtra("nomor_ps", item["nomor_ps"])
                intent.putExtra("nama_tipe", item["nama_tipe"])
                context.startActivity(intent)
            }
        }
    }

    private fun startCountdown(holder: ViewHolder, idPs: Int, jamSelesai: String) {
        if (jamSelesai.isEmpty()) return

        val runnable = object : Runnable {
            override fun run() {
                val diff = getDiffMillis(jamSelesai)

                if (diff <= 0) {
                    if (!notifiedPs.contains(idPs)) {
                        notifiedPs.add(idPs)
                        db.updateStatusPs(idPs, "tersedia")

                        // Panggil refresh ke Fragment
                        val activity = context as? AppCompatActivity
                        val frag = activity?.supportFragmentManager?.fragments?.find { it is MonitoringFragment } as? MonitoringFragment
                        frag?.tampilData()

                        Toast.makeText(context, "PS ${holder.b.tvNomorPs.text} Selesai!", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val jam = diff / (1000 * 60 * 60)
                val menit = (diff / (1000 * 60)) % 60
                val detik = (diff / 1000) % 60
                holder.b.tvCountdown.text = String.format("%02d:%02d:%02d", jam, menit, detik)

                holder.countdownRunnable = this
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun getDiffMillis(jamSelesai: String): Long {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = Calendar.getInstance()
            val end = Calendar.getInstance()
            val dateSelesai = sdf.parse(jamSelesai) ?: return -1

            val temp = Calendar.getInstance()
            temp.time = dateSelesai

            end.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
            end.set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
            end.set(Calendar.SECOND, 0)

            if (end.before(now)) return -1
            end.timeInMillis - now.timeInMillis
        } catch (e: Exception) { -1 }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.countdownRunnable?.let { handler.removeCallbacks(it) }
    }
}