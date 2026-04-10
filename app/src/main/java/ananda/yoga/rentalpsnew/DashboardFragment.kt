package ananda.yoga.rentalpsnew

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import ananda.yoga.rentalpsnew.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(), View.OnClickListener {

    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.ivMenu.setOnClickListener(this)
        b.cardUser.setOnClickListener(this)
        b.cardPlaystation.setOnClickListener(this)
        b.cardProduk.setOnClickListener(this)
        b.cardTransaksi.setOnClickListener(this)
        b.cardPembayaran.setOnClickListener(this)
        b.cardLaporan.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivMenu -> {
                Toast.makeText(requireContext(), "MEnu", Toast.LENGTH_SHORT).show()
            }
//                val popupMenu = PopupMenu(requireContext(), b.ivMenu)
//                popupMenu.menuInflater.inflate(R.menu.menu_dashboard_admin, popupMenu.menu)

//                popupMenu.setOnMenuItemClickListener { item ->
//                    when (item.itemId) {
//                        R.id.menu_profile -> {
//                            Toast.makeText(requireContext(), "Profile Admin", Toast.LENGTH_SHORT).show()
//                            true
//                        }
//
//                        R.id.menu_tentang -> {
//                            Toast.makeText(requireContext(), "Aplikasi Rental PS Admin", Toast.LENGTH_SHORT).show()
//                            true
//                        }
//
//                        R.id.menu_logout -> {
//                            startActivity(Intent(requireContext(), LoginActivity::class.java))
//                            requireActivity().finish()
//                            true
//                        }
//
//                        else -> false
//                    }
//                }
//                popupMenu.show()
//            }

            R.id.cardUser -> {
                Toast.makeText(requireContext(), "Data User", Toast.LENGTH_SHORT).show()
            }

            R.id.cardPlaystation -> {
                Toast.makeText(requireContext(), "Data Playstation", Toast.LENGTH_SHORT).show()
            }

            R.id.cardProduk -> {
                Toast.makeText(requireContext(), "Data Produk", Toast.LENGTH_SHORT).show()
            }

            R.id.cardTransaksi -> {
                Toast.makeText(requireContext(), "Data Transaksi", Toast.LENGTH_SHORT).show()
            }

            R.id.cardPembayaran -> {
                Toast.makeText(requireContext(), "Data Pembayaran", Toast.LENGTH_SHORT).show()
            }

            R.id.cardLaporan -> {
                Toast.makeText(requireContext(), "Data Laporan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}