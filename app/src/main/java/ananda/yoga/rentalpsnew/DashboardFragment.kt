package ananda.yoga.rentalpsnew

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import ananda.yoga.rentalpsnew.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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

        b.btnMenu.setOnClickListener { showPopupMenu(it) }

        b.cardPembayaran.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, PembayaranFragment())
                .addToBackStack(null)
                .commit()
        }

        b.cardLaporan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, LaporanFragment())
                .addToBackStack(null)
                .commit()
        }

        refreshDashboard()
    }

    private fun refreshDashboard() {
        val pendapatanHariIni = db.getTotalPendapatanHariIni()
        b.tvPendapatanHariIni.text = rupiahFmt(pendapatanHariIni)
        setupLineChart()
    }

    private fun setupLineChart() {
        val data7Hari = db.getPendapatan7Hari()

        val labels = if (data7Hari.isNotEmpty()) {
            data7Hari.map { it.first }
        } else {
            listOf("H-6", "H-5", "H-4", "H-3", "H-2", "H-1", "Hari Ini")
        }

        val entries = if (data7Hari.isNotEmpty()) {
            data7Hari.mapIndexed { i, pair ->
                Entry(i.toFloat(), pair.second.toFloat())
            }
        } else {
            listOf(
                Entry(0f, 0f), Entry(1f, 0f), Entry(2f, 0f), Entry(3f, 0f),
                Entry(4f, 0f), Entry(5f, 0f), Entry(6f, 0f)
            )
        }

        val dataSet = LineDataSet(entries, "Pendapatan (Rp)").apply {
            color = Color.parseColor("#2563EB")
            setCircleColor(Color.parseColor("#2563EB"))
            circleRadius = 5f
            circleHoleRadius = 3f
            circleHoleColor = Color.WHITE
            lineWidth = 2.5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#2563EB")
            fillAlpha = 25
            highLightColor = Color.parseColor("#93C5FD")
        }

        b.lineChart.clear()
        b.lineChart.data = LineData(dataSet)

        b.lineChart.description.isEnabled = false
        b.lineChart.legend.isEnabled = false
        b.lineChart.setTouchEnabled(true)
        b.lineChart.setPinchZoom(false)
        b.lineChart.setScaleEnabled(false)
        b.lineChart.setDrawGridBackground(false)
        b.lineChart.setBackgroundColor(Color.TRANSPARENT)

        b.lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelCount = labels.size
            setDrawGridLines(false)
            textColor = Color.parseColor("#94A3B8")
            textSize = 11f
        }

        b.lineChart.axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.parseColor("#F1F5F9")
            textColor = Color.parseColor("#94A3B8")
            textSize = 10f
            axisMinimum = 0f
        }

        b.lineChart.axisRight.isEnabled = false
        b.lineChart.animateX(800, Easing.EaseInOutCubic)
        b.lineChart.notifyDataSetChanged()
        b.lineChart.invalidate()
    }

    private fun showPopupMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, "Profil Saya")
        popup.menu.add(0, 2, 1, "Keluar")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> {
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                    true
                }
                2 -> {
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

    override fun onResume() {
        super.onResume()
        if (_b != null) {
            refreshDashboard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}