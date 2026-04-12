package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import ananda.yoga.rentalpsnew.databinding.FragmentMonitoringBinding

class MonitoringFragment : Fragment() {
    private var _b: FragmentMonitoringBinding? = null
    private val b get() = _b!!
    private lateinit var db: DBOpenHelper
    private var listData = ArrayList<HashMap<String, String>>()

    // Variabel global agar bisa diupdate datanya tanpa bikin adapter baru terus
    private var monitoringAdapter: MonitoringAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentMonitoringBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DBOpenHelper(requireContext())

        b.rvMonitoring.layoutManager = GridLayoutManager(requireContext(), 2)
        b.rvMonitoring.setHasFixedSize(true)

        tampilData()
    }

    override fun onResume() {
        super.onResume()
        tampilData()
    }

    // Fungsi dibuat PUBLIC agar bisa dipanggil dari Adapter saat countdown habis
    fun tampilData() {
        if (!isAdded) return

        listData = db.getMonitoringPs()

        // Update info di header
        val unitDipakai = listData.count { it["status_ps"]?.lowercase() == "dipakai" }
        b.tvTotalAktif.text = "$unitDipakai Unit Sedang Digunakan"

        // Logika Re-use Adapter
        if (monitoringAdapter == null) {
            monitoringAdapter = MonitoringAdapter(requireContext(), listData)
            b.rvMonitoring.adapter = monitoringAdapter
        } else {
            // Update isi list tanpa menghancurkan adapter (mencegah layar kosong/FC)
            monitoringAdapter?.updateList(listData)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}