package ananda.yoga.rentalpsnew

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ananda.yoga.rentalpsnew.databinding.ActivityPlaystationBinding

class PlaystationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var b: ActivityPlaystationBinding
    private lateinit var db: DBOpenHelper
    private var listData = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        b = ActivityPlaystationBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(b.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        b.ivBack.setOnClickListener(this)
        b.btnTambah.setOnClickListener(this)

        tampilData()

        b.listView.setOnItemClickListener { _, _, position, _ ->
            val item = listData[position]
            showDialogEdit(
                item["id_ps"]!!.toInt(),
                item["nomor_ps"].toString(),
                item["id_tipe"]!!.toInt(),
                item["status_ps"].toString()
            )
        }

        b.listView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, position, _ ->
                val item = listData[position]
                val id = item["id_ps"]!!.toInt()

                AlertDialog.Builder(this)
                    .setTitle("Hapus Data")
                    .setMessage("Yakin ingin menghapus data PS ini?")
                    .setPositiveButton("Ya") { _, _ ->
                        val hasil = db.deletePlaystation(id)
                        if (hasil) {
                            Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
                            tampilData()
                        } else {
                            Toast.makeText(this, "Data gagal dihapus", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()

                true
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> finish()
            R.id.btnTambah -> showDialogTambah()
        }
    }

    private fun tampilData() {
        listData = db.getAllPlaystation()

        val adapter = SimpleAdapter(
            this,
            listData,
            android.R.layout.simple_list_item_2,
            arrayOf("nomor_ps", "nama_tipe"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )

        b.listView.adapter = adapter
    }

    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_playstation, null)
        val edtNomorPs = view.findViewById<EditText>(R.id.edtNomorPs)
        val spTipe = view.findViewById<Spinner>(R.id.spTipePs)
        val spStatus = view.findViewById<Spinner>(R.id.spStatusPs)

        val listTipe = db.getAllTipePsForSpinner()
        val namaTipeList = ArrayList<String>()

        for (item in listTipe) {
            namaTipeList.add(item["nama_tipe"].toString())
        }

        val adapterTipe = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaTipeList)
        adapterTipe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipe.adapter = adapterTipe

        val statusList = arrayOf("tersedia", "dipakai", "maintenance")
        val adapterStatus = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList)
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStatus.adapter = adapterStatus

        AlertDialog.Builder(this)
            .setTitle("Tambah Playstation")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nomorPs = edtNomorPs.text.toString().trim()
                val posisiTipe = spTipe.selectedItemPosition
                val idTipe = listTipe[posisiTipe]["id_tipe"]!!.toInt()
                val statusPs = spStatus.selectedItem.toString()

                if (nomorPs.isNotEmpty()) {
                    val hasil = db.insertPlaystation(nomorPs, idTipe, statusPs)
                    if (hasil) {
                        Toast.makeText(this, "Data berhasil ditambah", Toast.LENGTH_SHORT).show()
                        tampilData()
                    } else {
                        Toast.makeText(this, "Data gagal ditambah", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Nomor PS tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDialogEdit(idPs: Int, nomorPsLama: String, idTipeLama: Int, statusPsLama: String) {
        val view = layoutInflater.inflate(R.layout.dialog_playstation, null)
        val edtNomorPs = view.findViewById<EditText>(R.id.edtNomorPs)
        val spTipe = view.findViewById<Spinner>(R.id.spTipePs)
        val spStatus = view.findViewById<Spinner>(R.id.spStatusPs)

        edtNomorPs.setText(nomorPsLama)

        val listTipe = db.getAllTipePsForSpinner()
        val namaTipeList = ArrayList<String>()

        for (item in listTipe) {
            namaTipeList.add(item["nama_tipe"].toString())
        }

        val adapterTipe = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaTipeList)
        adapterTipe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipe.adapter = adapterTipe

        var posisiTipeTerpilih = 0
        for (i in listTipe.indices) {
            if (listTipe[i]["id_tipe"]!!.toInt() == idTipeLama) {
                posisiTipeTerpilih = i
                break
            }
        }
        spTipe.setSelection(posisiTipeTerpilih)

        val statusList = arrayOf("tersedia", "dipakai", "maintenance")
        val adapterStatus = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList)
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStatus.adapter = adapterStatus

        val posisiStatus = statusList.indexOf(statusPsLama)
        if (posisiStatus >= 0) {
            spStatus.setSelection(posisiStatus)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Playstation")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val nomorPs = edtNomorPs.text.toString().trim()
                val posisiTipe = spTipe.selectedItemPosition
                val idTipe = listTipe[posisiTipe]["id_tipe"]!!.toInt()
                val statusPs = spStatus.selectedItem.toString()

                if (nomorPs.isNotEmpty()) {
                    val hasil = db.updatePlaystation(idPs, nomorPs, idTipe, statusPs)
                    if (hasil) {
                        Toast.makeText(this, "Data berhasil diupdate", Toast.LENGTH_SHORT).show()
                        tampilData()
                    } else {
                        Toast.makeText(this, "Data gagal diupdate", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Nomor PS tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}