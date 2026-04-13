package ananda.yoga.rentalpsnew

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import ananda.yoga.rentalpsnew.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.animation.Easing
import java.text.NumberFormat
import java.util.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!
    private lateinit var db: DBOpenHelper
    private val fmt = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private fun rupiahFmt(v: Double) = fmt.format(v).replace(",00", "")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentDashboardBinding.bind(view)
        db = DBOpenHelper(requireContext())

        // Tombol 3 titik (kanan atas)
        b.btnMenu.setOnClickListener { showPopupMenu(it) }

        // Navigasi card
        b.cardPembayaran.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, PembayaranFragment())
                .addToBackStack(null).commit()
        }
        b.cardLaporan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, LaporanFragment())
                .addToBackStack(null).commit()
        }

        // Tampilkan pendapatan hari ini
        val pendapatanHariIni = db.getTotalPendapatanHariIni()
        b.tvPendapatanHariIni.text = rupiahFmt(pendapatanHariIni)

        // Line chart 7 hari
        setupLineChart()
    }

    private fun setupLineChart() {
        val data7Hari = db.getPendapatan7Hari()
        val labels    = data7Hari.map { it.first }
        val entries   = data7Hari.mapIndexed { i, pair -> Entry(i.toFloat(), pair.second.toFloat()) }

        val dataSet = LineDataSet(entries, "Pendapatan (Rp)").apply {
            color                = Color.parseColor("#2563EB")
            setCircleColor(Color.parseColor("#2563EB"))
            circleRadius         = 5f
            circleHoleRadius     = 3f
            circleHoleColor      = Color.WHITE
            lineWidth            = 2.5f
            setDrawValues(false)
            mode                 = LineDataSet.Mode.CUBIC_BEZIER
            // Area di bawah garis
            setDrawFilled(true)
            fillColor            = Color.parseColor("#2563EB")
            fillAlpha            = 25
            highLightColor       = Color.parseColor("#93C5FD")
        }

        b.lineChart.apply {
            this.data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled      = false
            setTouchEnabled(true)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)

            xAxis.apply {
                valueFormatter        = IndexAxisValueFormatter(labels)
                position              = XAxis.XAxisPosition.BOTTOM
                granularity           = 1f
                setDrawGridLines(false)
                textColor             = Color.parseColor("#94A3B8")
                textSize              = 11f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor             = Color.parseColor("#F1F5F9")
                textColor             = Color.parseColor("#94A3B8")
                textSize              = 10f
                axisMinimum           = 0f
            }
            axisRight.isEnabled = false

            animateX(800, Easing.EaseInOutCubic)
            invalidate()
        }
    }

    private fun showPopupMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, "Profil Saya")
        popup.menu.add(0, 2, 1, "Keluar")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> {
                    // Buka ProfileActivity
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                    true
                }
                2 -> {
                    // Logout: hapus session dan kembali ke LoginActivity
                    val sp = requireContext().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                    sp.edit().clear().apply()
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}