package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ananda.yoga.rentalpsnew.databinding.FragmentRiwayatBinding

class RiwayatFragment : Fragment() {

    private var _b: FragmentRiwayatBinding? = null
    private val b get() = _b!!
    private lateinit var db: DBOpenHelper
    private var listData = ArrayList<HashMap<String, String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentRiwayatBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DBOpenHelper(requireContext())

        b.rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        tampilData()
    }

    override fun onResume() {
        super.onResume()
        tampilData()
    }

    private fun tampilData() {
        listData = db.getAllRiwayatTransaksi()
        b.rvRiwayat.adapter = RiwayatAdapter(listData)
        b.tvKosong.visibility = if (listData.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}