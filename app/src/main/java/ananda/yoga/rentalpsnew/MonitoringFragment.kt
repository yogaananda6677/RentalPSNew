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

        tampilData()
    }

    override fun onResume() {
        super.onResume()
        tampilData()
    }

    private fun tampilData() {
        listData = db.getMonitoringPs()
        b.rvMonitoring.adapter = MonitoringAdapter(requireContext(), listData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}