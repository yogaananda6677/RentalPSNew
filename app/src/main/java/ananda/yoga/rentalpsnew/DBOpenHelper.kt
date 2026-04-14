package ananda.yoga.rentalpsnew

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DBOpenHelper(context: Context) : SQLiteOpenHelper(context, "rental_ps.db", null, 5) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE user (" +
                    "id_user INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nama TEXT, email TEXT UNIQUE, no_hp TEXT, password TEXT, role TEXT DEFAULT 'customer')"
        )

        db?.execSQL(
            "CREATE TABLE tipe_ps (" +
                    "id_tipe INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nama_tipe TEXT, harga_sewa REAL)"
        )

        db?.execSQL(
            "CREATE TABLE playstation (" +
                    "id_ps INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nomor_ps TEXT, id_tipe INTEGER, status_ps TEXT)"
        )

        db?.execSQL(
            "CREATE TABLE produk (" +
                    "id_produk INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nama TEXT, jenis TEXT, harga REAL, stock INTEGER)"
        )

        db?.execSQL(
            "CREATE TABLE transaksi (" +
                    "id_transaksi INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_user INTEGER, tanggal TEXT, total_harga REAL, " +
                    "status_transaksi TEXT, metode_pembayaran TEXT, status_bayar TEXT)"
        )

        db?.execSQL(
            "CREATE TABLE detail_sewa_ps (" +
                    "id_dt_booking INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_transaksi INTEGER, id_ps INTEGER, durasi INTEGER, " +
                    "jam_mulai TEXT, jam_selesai TEXT, subtotal REAL)"
        )

        db?.execSQL(
            "CREATE TABLE detail_produk (" +
                    "id_dt_produk INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_transaksi INTEGER, id_produk INTEGER, qty INTEGER, subtotal REAL)"
        )

        db?.execSQL(
            "INSERT INTO user(nama, email, no_hp, password, role) VALUES " +
                    "('Admin', 'admin@gmail.com', '081234567890', 'admin123', 'admin')"
        )

        db?.execSQL(
            "INSERT INTO tipe_ps(nama_tipe, harga_sewa) VALUES " +
                    "('PS3', 5000), ('PS4', 7000), ('PS5', 10000)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        listOf("user", "transaksi", "tipe_ps", "playstation", "detail_sewa_ps", "produk", "detail_produk")
            .forEach { db?.execSQL("DROP TABLE IF EXISTS $it") }
        onCreate(db)
    }

    // =========================================================================
    // 1. AUTH & USER
    // =========================================================================
    fun checkLogin(e: String, p: String): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT * FROM user WHERE email=? AND password=?",
            arrayOf(e, p)
        )
        val exist = c.count > 0
        c.close()
        return exist
    }

    fun checkEmail(e: String): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT * FROM user WHERE email=?",
            arrayOf(e)
        )
        val exist = c.count > 0
        c.close()
        return exist
    }

    fun insertUser(n: String, e: String, h: String, p: String, r: String): Boolean {
        val v = ContentValues().apply {
            put("nama", n)
            put("email", e)
            put("no_hp", h)
            put("password", p)
            put("role", r)
        }
        return writableDatabase.insert("user", null, v) > 0
    }

    fun getAllUser(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val c = readableDatabase.rawQuery("SELECT * FROM user", null)

        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_user"] = c.getString(0)
                m["nama"] = c.getString(1)
                m["email"] = c.getString(2)
                m["no_hp"] = c.getString(3)
                m["password"] = c.getString(4)
                m["role"] = c.getString(5)
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    fun updatePengguna(id: Int, n: String, e: String, h: String, p: String, r: String): Boolean {
        val v = ContentValues().apply {
            put("nama", n)
            put("email", e)
            put("no_hp", h)
            put("password", p)
            put("role", r)
        }
        return writableDatabase.update("user", v, "id_user=?", arrayOf(id.toString())) > 0
    }

    fun getUserData(email: String): HashMap<String, String>? {
        val c = readableDatabase.rawQuery(
            "SELECT nama, email, no_hp, role FROM user WHERE email=?",
            arrayOf(email)
        )

        var user: HashMap<String, String>? = null
        if (c.moveToFirst()) {
            user = HashMap()
            user["nama"] = c.getString(0)
            user["email"] = c.getString(1)
            user["no_hp"] = c.getString(2)
            user["role"] = c.getString(3)
        }

        c.close()
        return user
    }

    fun deleteUser(id: Int): Boolean {
        return writableDatabase.delete("user", "id_user=?", arrayOf(id.toString())) > 0
    }

    // =========================================================================
    // 2. PLAYSTATION & MONITORING
    // =========================================================================
    fun getMonitoringPs(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val sql = """
            SELECT p.id_ps, p.nomor_ps, t.nama_tipe, p.status_ps, p.id_tipe,
                   tr.id_transaksi, tr.status_bayar, d.jam_mulai, d.jam_selesai
            FROM playstation p
            JOIN tipe_ps t ON p.id_tipe = t.id_tipe
            LEFT JOIN detail_sewa_ps d ON d.id_ps = p.id_ps
            LEFT JOIN transaksi tr ON d.id_transaksi = tr.id_transaksi
            WHERE tr.status_transaksi = 'aktif'
               OR p.status_ps = 'tersedia'
               OR p.status_ps = 'maintenance'
            ORDER BY p.id_ps ASC
        """.trimIndent()

        val c = readableDatabase.rawQuery(sql, null)

        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_ps"] = c.getString(0)
                m["nomor_ps"] = c.getString(1)
                m["nama_tipe"] = c.getString(2)
                m["status_ps"] = c.getString(3)
                m["id_tipe"] = c.getString(4)
                m["id_transaksi"] = c.getString(5) ?: "0"
                m["status_bayar"] = c.getString(6) ?: "belum_lunas"
                m["jam_mulai"] = c.getString(7) ?: "-"
                m["jam_selesai"] = c.getString(8) ?: "-"
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    fun updateStatusPs(id: Int, status: String) {
        val v = ContentValues().apply { put("status_ps", status) }
        writableDatabase.update("playstation", v, "id_ps=?", arrayOf(id.toString()))
    }

    fun isTransaksiLunas(idTransaksi: Int): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT status_bayar FROM transaksi WHERE id_transaksi=?",
            arrayOf(idTransaksi.toString())
        )

        var lunas = false
        if (c.moveToFirst()) {
            lunas = (c.getString(0) ?: "").equals("lunas", ignoreCase = true)
        }

        c.close()
        return lunas
    }

    fun selesaikanTransaksi(idTransaksi: Int): Boolean {
        val v = ContentValues().apply {
            put("status_transaksi", "selesai")
        }
        return writableDatabase.update(
            "transaksi",
            v,
            "id_transaksi=?",
            arrayOf(idTransaksi.toString())
        ) > 0
    }

    fun insertPlaystation(nomor: String, idTipe: Int, status: String): Boolean {
        val v = ContentValues().apply {
            put("nomor_ps", nomor)
            put("id_tipe", idTipe)
            put("status_ps", status)
        }
        return writableDatabase.insert("playstation", null, v) > 0
    }

    fun updatePlaystation(id: Int, nomor: String, idTipe: Int, status: String): Boolean {
        val v = ContentValues().apply {
            put("nomor_ps", nomor)
            put("id_tipe", idTipe)
            put("status_ps", status)
        }
        return writableDatabase.update("playstation", v, "id_ps=?", arrayOf(id.toString())) > 0
    }

    fun deletePlaystation(id: Int): Boolean {
        return writableDatabase.delete("playstation", "id_ps=?", arrayOf(id.toString())) > 0
    }

    // =========================================================================
    // 3. TIPE PS & PRODUK
    // =========================================================================
    fun getAllTipePs(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val c = readableDatabase.rawQuery("SELECT * FROM tipe_ps", null)

        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_tipe"] = c.getString(0)
                m["nama_tipe"] = c.getString(1)
                m["harga_sewa"] = c.getString(2)
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    fun insertTipePs(n: String, h: Double): Boolean {
        val v = ContentValues().apply {
            put("nama_tipe", n)
            put("harga_sewa", h)
        }
        return writableDatabase.insert("tipe_ps", null, v) > 0
    }

    fun updateTipePs(id: String, n: String, h: Double): Boolean {
        val v = ContentValues().apply {
            put("nama_tipe", n)
            put("harga_sewa", h)
        }
        return writableDatabase.update("tipe_ps", v, "id_tipe=?", arrayOf(id)) > 0
    }

    fun deleteTipePs(id: String): Boolean {
        return writableDatabase.delete("tipe_ps", "id_tipe=?", arrayOf(id)) > 0
    }

    fun getAllProduk(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val c = readableDatabase.rawQuery("SELECT * FROM produk", null)

        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_produk"] = c.getString(0)
                m["nama"] = c.getString(1)
                m["jenis"] = c.getString(2)
                m["harga"] = c.getString(3)
                m["stock"] = c.getString(4)
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    fun getAllProdukSpinner(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val c = readableDatabase.rawQuery(
            "SELECT id_produk, nama, harga FROM produk WHERE stock > 0",
            null
        )

        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_produk"] = c.getString(0)
                m["nama"] = c.getString(1)
                m["harga"] = c.getString(2)
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    fun insertProduk(n: String, j: String, h: Double, s: Int): Boolean {
        val v = ContentValues().apply {
            put("nama", n)
            put("jenis", j)
            put("harga", h)
            put("stock", s)
        }
        return writableDatabase.insert("produk", null, v) > 0
    }

    fun updateProduk(id: Int, n: String, j: String, h: Double, s: Int): Boolean {
        val v = ContentValues().apply {
            put("nama", n)
            put("jenis", j)
            put("harga", h)
            put("stock", s)
        }
        return writableDatabase.update("produk", v, "id_produk=?", arrayOf(id.toString())) > 0
    }

    fun deleteProduk(id: Int): Boolean {
        return writableDatabase.delete("produk", "id_produk=?", arrayOf(id.toString())) > 0
    }

    // =========================================================================
    // 4. TRANSAKSI
    // =========================================================================
    fun insertTransaksi(idU: Int, tgl: String, total: Double, status: String): Long {
        if (total <= 0) return -1L

        val v = ContentValues().apply {
            put("id_user", idU)
            put("tanggal", tgl)
            put("total_harga", total)
            put("status_transaksi", status)
            put("metode_pembayaran", "-")
            put("status_bayar", "belum_lunas")
        }
        return writableDatabase.insert("transaksi", null, v)
    }

    fun updateStatusBayar(idTransaksi: Int, statusBayar: String, metode: String): Boolean {
        val v = ContentValues().apply {
            put("status_bayar", statusBayar)
            put("metode_pembayaran", metode)
        }
        return writableDatabase.update(
            "transaksi",
            v,
            "id_transaksi=?",
            arrayOf(idTransaksi.toString())
        ) > 0
    }

    fun getStatusBayar(idTransaksi: Int): String {
        val c = readableDatabase.rawQuery(
            "SELECT status_bayar FROM transaksi WHERE id_transaksi=?",
            arrayOf(idTransaksi.toString())
        )

        var status = "belum_lunas"
        if (c.moveToFirst()) {
            status = c.getString(0) ?: "belum_lunas"
        }

        c.close()
        return status
    }

    fun updateStatusTransaksi(id: Int, status: String, metode: String): Boolean {
        val v = ContentValues().apply {
            put("status_transaksi", status)
            put("metode_pembayaran", metode)
        }
        return writableDatabase.update("transaksi", v, "id_transaksi=?", arrayOf(id.toString())) > 0
    }

    fun insertDetailSewaPs(idT: Long, idPs: Int, dur: Int, mulai: String, selesai: String, sub: Double) {
        if (idPs <= 0) return

        val v = ContentValues().apply {
            put("id_transaksi", idT)
            put("id_ps", idPs)
            put("durasi", dur)
            put("jam_mulai", mulai)
            put("jam_selesai", selesai)
            put("subtotal", sub)
        }
        writableDatabase.insert("detail_sewa_ps", null, v)
    }

    fun insertDetailProduk(idT: Long, idP: Int, q: Int, sub: Double) {
        val v = ContentValues().apply {
            put("id_transaksi", idT)
            put("id_produk", idP)
            put("qty", q)
            put("subtotal", sub)
        }
        writableDatabase.insert("detail_produk", null, v)
    }

    fun getDetailProdukByTrans(id: Int): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val sql = """
            SELECT p.nama, dp.qty, dp.subtotal
            FROM detail_produk dp
            JOIN produk p ON dp.id_produk = p.id_produk
            WHERE dp.id_transaksi=?
        """.trimIndent()

        val c = readableDatabase.rawQuery(sql, arrayOf(id.toString()))
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["nama"] = c.getString(0)
                m["qty"] = c.getString(1)
                m["subtotal"] = c.getString(2)
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    fun getDetailSewaByTrans(id: Int): HashMap<String, String>? {
        val sql = """
            SELECT d.jam_mulai, d.jam_selesai, d.durasi, d.subtotal, p.nomor_ps, t.nama_tipe
            FROM detail_sewa_ps d
            JOIN playstation p ON d.id_ps = p.id_ps
            JOIN tipe_ps t ON p.id_tipe = t.id_tipe
            WHERE d.id_transaksi = ? LIMIT 1
        """.trimIndent()

        val c = readableDatabase.rawQuery(sql, arrayOf(id.toString()))
        var r: HashMap<String, String>? = null

        if (c.moveToFirst()) {
            r = HashMap()
            r["jam_mulai"] = c.getString(0) ?: "-"
            r["jam_selesai"] = c.getString(1) ?: "-"
            r["durasi"] = c.getString(2) ?: "0"
            r["subtotal"] = c.getString(3) ?: "0"
            r["nomor_ps"] = c.getString(4) ?: "-"
            r["nama_tipe"] = c.getString(5) ?: "-"
        }

        c.close()
        return r
    }

    fun getAllTransaksiAktif(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val sql = """
            SELECT t.id_transaksi, t.total_harga, p.nomor_ps, p.id_ps,
                   d.jam_mulai, d.jam_selesai, t.status_bayar
            FROM transaksi t
            JOIN detail_sewa_ps d ON t.id_transaksi = d.id_transaksi
            JOIN playstation p ON d.id_ps = p.id_ps
            WHERE t.status_transaksi = 'aktif'
            ORDER BY t.id_transaksi DESC
        """.trimIndent()

        val c = readableDatabase.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_transaksi"] = c.getString(0)
                m["total"] = c.getString(1)
                m["nomor_ps"] = c.getString(2)
                m["id_ps"] = c.getString(3)
                m["jam_mulai"] = c.getString(4) ?: "-"
                m["jam_selesai"] = c.getString(5) ?: "-"
                m["status_bayar"] = c.getString(6) ?: "belum_lunas"
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    // =========================================================================
    // 5. RIWAYAT, LAPORAN, DASHBOARD
    // =========================================================================
    fun getAllRiwayatTransaksi(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val sql = """
            SELECT t.id_transaksi, t.tanggal, t.total_harga, p.nomor_ps,
                   t.metode_pembayaran, d.jam_mulai, d.jam_selesai, d.durasi, tp.nama_tipe
            FROM transaksi t
            JOIN detail_sewa_ps d ON t.id_transaksi = d.id_transaksi
            JOIN playstation p ON d.id_ps = p.id_ps
            JOIN tipe_ps tp ON p.id_tipe = tp.id_tipe
            WHERE t.status_bayar = 'lunas'
            ORDER BY t.id_transaksi DESC
        """.trimIndent()

        val c = readableDatabase.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_transaksi"] = c.getString(0)
                m["tanggal"] = c.getString(1)
                m["total"] = c.getString(2)
                m["nomor_ps"] = c.getString(3)
                m["metode_pembayaran"] = c.getString(4) ?: "-"
                m["jam_mulai"] = c.getString(5) ?: "-"
                m["jam_selesai"] = c.getString(6) ?: "-"
                m["durasi"] = c.getString(7) ?: "0"
                m["nama_tipe"] = c.getString(8) ?: "-"
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    fun getLaporanPerTanggal(tgl: String): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val sql = """
            SELECT t.id_transaksi, t.tanggal, t.total_harga, p.nomor_ps,
                   t.metode_pembayaran, d.jam_mulai, d.jam_selesai, d.durasi, tp.nama_tipe
            FROM transaksi t
            JOIN detail_sewa_ps d ON t.id_transaksi = d.id_transaksi
            JOIN playstation p ON d.id_ps = p.id_ps
            JOIN tipe_ps tp ON p.id_tipe = tp.id_tipe
            WHERE t.tanggal = ? AND t.status_bayar = 'lunas'
            ORDER BY t.id_transaksi DESC
        """.trimIndent()

        val c = readableDatabase.rawQuery(sql, arrayOf(tgl))
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_transaksi"] = c.getString(0)
                m["tanggal"] = c.getString(1)
                m["total"] = c.getString(2)
                m["nomor_ps"] = c.getString(3)
                m["metode_pembayaran"] = c.getString(4) ?: "-"
                m["jam_mulai"] = c.getString(5) ?: "-"
                m["jam_selesai"] = c.getString(6) ?: "-"
                m["durasi"] = c.getString(7) ?: "0"
                m["nama_tipe"] = c.getString(8) ?: "-"
                list.add(m)
            } while (c.moveToNext())
        }

        c.close()
        return list
    }

    fun getTotalPendapatanHariIni(): Double {
        var total = 0.0
        val c = readableDatabase.rawQuery(
            "SELECT SUM(total_harga) FROM transaksi WHERE date(tanggal)=date('now') AND status_bayar='lunas'",
            null
        )

        if (c.moveToFirst()) total = c.getDouble(0)
        c.close()
        return total
    }

    fun getTotalOmzetHariIni(): Double = getTotalPendapatanHariIni()

    fun getPendapatan7Hari(): List<Pair<String, Double>> {
        val result = mutableListOf<Pair<String, Double>>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfLabel = SimpleDateFormat("dd/MM", Locale.getDefault())
        val cal = Calendar.getInstance()

        for (i in 6 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)

            val tgl = sdf.format(cal.time)
            val label = sdfLabel.format(cal.time)

            var total = 0.0
            val c = readableDatabase.rawQuery(
                "SELECT SUM(total_harga) FROM transaksi WHERE tanggal=? AND status_bayar='lunas'",
                arrayOf(tgl)
            )
            if (c.moveToFirst()) total = c.getDouble(0)
            c.close()

            result.add(Pair(label, total))
        }

        return result
    }

    fun getHargaSewaByNamaTipe(nama: String): Double {
        var harga = 0.0
        val c = readableDatabase.rawQuery(
            "SELECT harga_sewa FROM tipe_ps WHERE nama_tipe=?",
            arrayOf(nama)
        )

        if (c.moveToFirst()) harga = c.getDouble(0)
        c.close()
        return harga
    }
}