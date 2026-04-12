package ananda.yoga.rentalpsnew

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ananda.yoga.rentalpsnew.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val b get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.cardPlaystation.setOnClickListener {
            startActivity(Intent(requireContext(), PlaystationActivity::class.java))
        }

        b.cardProduk.setOnClickListener {
            startActivity(Intent(requireContext(), ProdukActivity::class.java))
        }

        b.cardUser.setOnClickListener {
            startActivity(Intent(requireContext(), UserActivity::class.java))
        }

        b.cardTransaksi.setOnClickListener {
            startActivity(Intent(requireContext(), TransaksiActivity::class.java))
        }
        b.cardLaporan.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, LaporanFragment()) // Ganti ke LaporanFragment
            transaction.addToBackStack(null)
            transaction.commit()
        }
        b.cardPembayaran.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, PembayaranFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}