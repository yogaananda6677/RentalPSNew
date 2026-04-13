package ananda.yoga.rentalpsnew

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ananda.yoga.rentalpsnew.databinding.FragmentMenuLainBinding

class MenuLainFragment : Fragment(), View.OnClickListener {

    private var _b: FragmentMenuLainBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = FragmentMenuLainBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.cardTipePs.setOnClickListener(this)
        b.cardPlaystation.setOnClickListener(this)
        b.cardProduk.setOnClickListener(this)
        b.cardUser.setOnClickListener(this)
        b.cardProfile.setOnClickListener(this) // TAMBAHKAN INI
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cardTipePs -> {
                startActivity(Intent(requireContext(), TipePsActivity::class.java))
            }

            R.id.cardPlaystation -> {
                startActivity(Intent(requireContext(), PlaystationActivity::class.java))
            }

            R.id.cardProduk -> {
                startActivity(Intent(requireContext(), ProdukActivity::class.java))
            }

            R.id.cardUser -> {
                startActivity(Intent(requireContext(), UserActivity::class.java))
            }
            R.id.cardProfile -> { // TAMBAHKAN INI
                startActivity(Intent(requireContext(), ProfileActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}