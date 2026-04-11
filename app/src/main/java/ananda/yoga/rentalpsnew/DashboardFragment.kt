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
                val popupMenu = PopupMenu(requireContext(), b.ivMenu)
                popupMenu.menuInflater.inflate(R.menu.menu_dashboard_admin, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_profile -> {
                            Toast.makeText(requireContext(), "Profile Admin", Toast.LENGTH_SHORT).show()
                            true
                        }

                        R.id.menu_tentang -> {
                            Toast.makeText(
                                requireContext(),
                                "Aplikasi Rental PS Admin",
                                Toast.LENGTH_SHORT
                            ).show()
                            true
                        }

                        R.id.menu_logout -> {
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            requireActivity().finish()
                            true
                        }

                        else -> false
                    }
                }

                popupMenu.show()
            }

            R.id.cardUser -> {
                startActivity(Intent(requireContext(), UserActivity::class.java))
            }

            R.id.cardPlaystation -> {
                startActivity(Intent(requireContext(), PlaystationActivity::class.java))
            }

            R.id.cardProduk -> {
                startActivity(Intent(requireContext(), ProdukActivity::class.java))
            }

            R.id.cardTransaksi -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, MonitoringFragment())
                    .commit()
            }

            R.id.cardPembayaran -> {
                Toast.makeText(requireContext(), "Menu Pembayaran belum dibuat", Toast.LENGTH_SHORT)
                    .show()
            }

            R.id.cardLaporan -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, RiwayatFragment())
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}