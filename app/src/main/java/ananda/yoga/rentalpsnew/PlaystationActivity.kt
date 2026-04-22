package ananda.yoga.rentalpsnew

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ananda.yoga.rentalpsnew.databinding.ActivityPlaystationBinding

class PlaystationActivity : AppCompatActivity() {

    private lateinit var b: ActivityPlaystationBinding
    private lateinit var db: DBOpenHelper
    private var listData = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPlaystationBinding.inflate(layoutInflater)
        setContentView(b.root)

        db = DBOpenHelper(this)
        tampilData()

        b.ivBack.setOnClickListener { finish() }
        b.btnTambah.setOnClickListener { showDialogTambah() }
    }

    fun tampilData() {
        // Menggunakan fungsi yang sudah kita buat di DBOpenHelper
        listData = db.getMonitoringPs()

        val adapter = SimpleAdapter(
            this, listData, R.layout.item_playstation,
            arrayOf("nomor_ps", "nama_tipe", "status_ps", "id_ps"),
            intArrayOf(R.id.text1, R.id.text2, R.id.tvStatusBadge, R.id.btnMenuPs)
        )

        adapter.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
            when (view.id) {
                R.id.tvStatusBadge -> {
                    val status = data.toString()
                    val tv = view as TextView
                    tv.text = status.uppercase()

                    when (status.lowercase()) {
                        "maintenance" -> {
                            tv.setTextColor(Color.parseColor("#DC2626"))
                            tv.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                        }
                        "dipakai" -> {
                            tv.setTextColor(Color.parseColor("#2563EB"))
                            tv.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EFF6FF"))
                        }
                        "tersedia" -> {
                            tv.setTextColor(Color.parseColor("#16A34A"))
                            tv.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                        }
                    }
                    true
                }
                R.id.btnMenuPs -> {
                    val btn = view as ImageButton
                    val idPs = data.toString()

                    btn.setOnClickListener { v ->
                        val popup = PopupMenu(this, v)
                        popup.menu.add("Edit")
                        popup.menu.add("Hapus")

                        popup.setOnMenuItemClickListener { menuItem ->
                            val item = listData.find { it["id_ps"] == idPs }
                            when (menuItem.title) {
                                "Edit" -> if (item != null) showDialogEdit(item)
                                "Hapus" -> if (item != null) confirmHapusPs(item)
                            }
                            true
                        }
                        popup.show()
                    }
                    true
                }
                else -> false
            }
        }
        b.listView.adapter = adapter
    }

    private fun showDialogTambah() {
        val view = layoutInflater.inflate(R.layout.dialog_playstation, null)
        val edtNomor = view.findViewById<EditText>(R.id.edtNomorPs)
        val spTipe = view.findViewById<Spinner>(R.id.spTipePs)
        val spStatus = view.findViewById<Spinner>(R.id.spStatusPs)

        // Mengambil data tipe untuk Spinner
        val listTipe = db.getAllTipePs()
        val namaTipe = listTipe.map { it["nama_tipe"].toString() }

        val adapterTipe = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaTipe)
        adapterTipe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipe.adapter = adapterTipe

        spStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arrayOf("tersedia", "dipakai", "maintenance"))

        AlertDialog.Builder(this).setTitle("Tambah Unit PS").setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val nomor = edtNomor.text.toString()
                if (nomor.isNotEmpty() && listTipe.isNotEmpty()) {
                    val idTipe = listTipe[spTipe.selectedItemPosition]["id_tipe"]!!.toInt()
                    val status = spStatus.selectedItem.toString()
                    if (db.insertPlaystation(nomor, idTipe, status)) {
                        tampilData()
                        Toast.makeText(this, "Berhasil simpan", Toast.LENGTH_SHORT).show()
                    }
                }
            }.setNegativeButton("Batal", null).show()
    }

    private fun showDialogEdit(item: HashMap<String, String>) {
        val view = layoutInflater.inflate(R.layout.dialog_playstation, null)
        val edtNomor = view.findViewById<EditText>(R.id.edtNomorPs)
        val spTipe = view.findViewById<Spinner>(R.id.spTipePs)
        val spStatus = view.findViewById<Spinner>(R.id.spStatusPs)

        val listTipe = db.getAllTipePs()
        val namaTipe = listTipe.map { it["nama_tipe"].toString() }

        val adapterTipe = ArrayAdapter(this, android.R.layout.simple_spinner_item, namaTipe)
        adapterTipe.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipe.adapter = adapterTipe

        edtNomor.setText(item["nomor_ps"])

        val posTipe = listTipe.indexOfFirst { it["id_tipe"] == item["id_tipe"] }
        if (posTipe >= 0) spTipe.setSelection(posTipe)

        val statusArray = arrayOf("tersedia", "dipakai", "maintenance")
        spStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusArray)
        spStatus.setSelection(statusArray.indexOf(item["status_ps"]))

        AlertDialog.Builder(this)
            .setTitle("Edit Unit PS")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val idPs = item["id_ps"]!!.toInt()
                val idTipe = listTipe[spTipe.selectedItemPosition]["id_tipe"]!!.toInt()
                val status = spStatus.selectedItem.toString()

                if (db.updatePlaystation(idPs, edtNomor.text.toString(), idTipe, status)) {
                    tampilData()
                    Toast.makeText(this, "Berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null).show()
    }

    private fun confirmHapusPs(item: HashMap<String, String>) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Unit")
            .setMessage("Yakin ingin menghapus ${item["nomor_ps"]}?")
            .setPositiveButton("Ya, Hapus") { _, _ ->
                val idPs = item["id_ps"]!!.toInt()
                if (db.deletePlaystation(idPs)) {
                    Toast.makeText(this, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    tampilData()
                }
            }
            .setNegativeButton("Batal", null).show()
    }
}