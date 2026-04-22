package ananda.yoga.rentalpsnew

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MonitoringAdapter(
    private val context: Context,
    private val listData: ArrayList<HashMap<String, String>>
) : RecyclerView.Adapter<MonitoringAdapter.ViewHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val db      = DBOpenHelper(context)

    var onPsSelesai: ((idPs: Int) -> Unit)? = null

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvNomorPs   = v.findViewById<TextView>(R.id.tvNomorPs)
        val tvTipePs    = v.findViewById<TextView>(R.id.tvTipePs)
        val tvStatus    = v.findViewById<TextView>(R.id.tvStatus)
        val tvJamSelesai= v.findViewById<TextView>(R.id.tvJamSelesai)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_monitoring_ps, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item       = listData[position]
        val status     = item["status_ps"]?.lowercase(Locale.getDefault()) ?: ""
        val jamSelesai = item["jam_selesai"] ?: "-"
        val idPs       = item["id_ps"]?.toIntOrNull() ?: 0

        holder.tvNomorPs.text = item["nomor_ps"]
        holder.tvTipePs.text  = item["nama_tipe"]
        holder.tvStatus.text  = status.uppercase(Locale.getDefault())

        when (status) {
            "tersedia" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#16A34A"))
                holder.tvStatus.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F0FDF4"))
            }
            "dipakai" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#2563EB"))
                holder.tvStatus.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
            }
            "maintenance" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#EA580C"))
                holder.tvStatus.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF7ED"))
            }
            else -> {
                holder.tvStatus.setTextColor(Color.parseColor("#64748B"))
                holder.tvStatus.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F1F5F9"))
            }
        }

        val idTransaksi = item["id_transaksi"]?.toIntOrNull() ?: 0

        if (status == "dipakai" && jamSelesai != "-" && jamSelesai.isNotEmpty()) {
            holder.tvJamSelesai.visibility = View.VISIBLE
            startCountdown(holder.tvJamSelesai, jamSelesai, idPs, idTransaksi)
        } else {
            holder.tvJamSelesai.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (status == "tersedia") {
                val intent = Intent(context, TransaksiActivity::class.java).apply {
                    putExtra("ID_PS_TARGET",    item["id_ps"])
                    putExtra("NOMOR_PS_TARGET", item["nomor_ps"])
                    putExtra("TIPE_PS_TARGET",  item["nama_tipe"])
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Unit PS sedang $status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCountdown(
        tv: TextView,
        jamSelesai: String,
        idPs: Int,
        idTransaksi: Int
    ) {
        tv.textSize = 20f
        tv.setTypeface(null, android.graphics.Typeface.BOLD)

        val runnable = object : Runnable {
            override fun run() {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val sekarang = Calendar.getInstance()

                try {
                    val dateSelesai = sdf.parse(jamSelesai)
                    if (dateSelesai != null) {
                        val calSelesai = Calendar.getInstance().apply {
                            time = dateSelesai
                            set(Calendar.YEAR, sekarang.get(Calendar.YEAR))
                            set(Calendar.MONTH, sekarang.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, sekarang.get(Calendar.DAY_OF_MONTH))
                        }

                        val selisih = calSelesai.timeInMillis - sekarang.timeInMillis

                        if (selisih > 0) {
                            val h = (selisih / 3_600_000) % 24
                            val m = (selisih / 60_000) % 60
                            val s = (selisih / 1_000) % 60
                            tv.text = String.format("%02d:%02d:%02d", h, m, s)
                            handler.postDelayed(this, 1000)
                        } else {
                            val lunas = db.isTransaksiLunas(idTransaksi)

                            if (lunas) {
                                tv.text = "SELESAI"
                                if (idPs > 0) db.updateStatusPs(idPs, "tersedia")
                                if (idTransaksi > 0) db.selesaikanTransaksi(idTransaksi)
                                handler.post { onPsSelesai?.invoke(idPs) }
                            } else {
                                tv.text = "WAKTU HABIS - BELUM LUNAS"
                                tv.setTextColor(Color.parseColor("#DC2626"))
                                handler.postDelayed(this, 1000)
                            }
                        }
                    }
                } catch (e: Exception) {
                    tv.text = "00:00:00"
                }
            }
        }

        handler.post(runnable)
    }

    override fun getItemCount() = listData.size
}