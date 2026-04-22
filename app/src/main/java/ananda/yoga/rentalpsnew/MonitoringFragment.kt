package ananda.yoga.rentalpsnew

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import ananda.yoga.rentalpsnew.databinding.FragmentMonitoringBinding

class MonitoringFragment : Fragment() {

    private var _binding: FragmentMonitoringBinding? = null
    private val b get() = _binding!!
    private lateinit var db: DBOpenHelper

    private var listFull     = ArrayList<HashMap<String, String>>()
    private var listFiltered = ArrayList<HashMap<String, String>>()
    private var statusAktif  = "semua"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBOpenHelper(requireContext())
        b.rvMonitoring.layoutManager = GridLayoutManager(requireContext(), 2)

        loadData()

        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { jalankanFilter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        b.btnFilterAll.setOnClickListener         { ubahStatusFilter("semua",       it as Button) }
        b.btnFilterTersedia.setOnClickListener    { ubahStatusFilter("tersedia",    it as Button) }
        b.btnFilterDipakai.setOnClickListener     { ubahStatusFilter("dipakai",     it as Button) }
        b.btnFilterMaintenance.setOnClickListener { ubahStatusFilter("maintenance", it as Button) }
    }

    private fun loadData() {
        listFull = db.getMonitoringPs()
        b.tvTotalAktif.text = "${listFull.size} Unit"
        jalankanFilter(b.etSearch.text.toString())
    }

    private fun ubahStatusFilter(status: String, btn: Button) {
        statusAktif = status
        val gray = Color.parseColor("#64748B")
        val blue = Color.parseColor("#2563EB")
        listOf(b.btnFilterAll, b.btnFilterTersedia, b.btnFilterDipakai, b.btnFilterMaintenance)
            .forEach { it.setTextColor(gray) }
        btn.setTextColor(blue)
        jalankanFilter(b.etSearch.text.toString())
    }

    private fun jalankanFilter(query: String) {
        listFiltered.clear()
        for (item in listFull) {
            val nomor  = item["nomor_ps"]?.lowercase() ?: ""
            val status = item["status_ps"]?.lowercase() ?: ""
            val cocokCari   = nomor.contains(query.lowercase())
            val cocokStatus = statusAktif == "semua" || status == statusAktif
            if (cocokCari && cocokStatus) listFiltered.add(item)
        }
        b.rvMonitoring.adapter = MonitoringAdapter(requireContext(), listFiltered)
    }

    override fun onResume() { super.onResume(); loadData() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}