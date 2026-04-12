package ananda.yoga.rentalpsnew

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE user (id_user INTEGER PRIMARY KEY AUTOINCREMENT, nama TEXT NOT NULL, email TEXT NOT NULL UNIQUE, no_hp TEXT, password TEXT NOT NULL, role TEXT NOT NULL DEFAULT 'customer')")
        db?.execSQL("CREATE TABLE transaksi (id_transaksi INTEGER PRIMARY KEY AUTOINCREMENT, id_user INTEGER NOT NULL, tanggal TEXT NOT NULL, total_harga REAL NOT NULL, status_transaksi TEXT NOT NULL, FOREIGN KEY(id_user) REFERENCES user(id_user))")
        db?.execSQL("CREATE TABLE tipe_ps (id_tipe INTEGER PRIMARY KEY AUTOINCREMENT, nama_tipe TEXT NOT NULL, harga_sewa REAL NOT NULL)")
        db?.execSQL("CREATE TABLE playstation (id_ps INTEGER PRIMARY KEY AUTOINCREMENT, nomor_ps TEXT NOT NULL, id_tipe INTEGER NOT NULL, status_ps TEXT NOT NULL, FOREIGN KEY(id_tipe) REFERENCES tipe_ps(id_tipe))")
        db?.execSQL("CREATE TABLE produk (id_produk INTEGER PRIMARY KEY AUTOINCREMENT, nama TEXT NOT NULL, jenis TEXT NOT NULL, harga REAL NOT NULL, stock INTEGER NOT NULL)")
        db?.execSQL("CREATE TABLE detail_sewa_ps (id_dt_booking INTEGER PRIMARY KEY AUTOINCREMENT, id_transaksi INTEGER NOT NULL, id_ps INTEGER NOT NULL, durasi INTEGER NOT NULL, jam_mulai TEXT NOT NULL, jam_selesai TEXT NOT NULL, type_ps TEXT NOT NULL, harga_perjam REAL NOT NULL, subtotal REAL NOT NULL, FOREIGN KEY(id_transaksi) REFERENCES transaksi(id_transaksi), FOREIGN KEY(id_ps) REFERENCES playstation(id_ps))")
        db?.execSQL("CREATE TABLE detail_produk (id_dt_produk INTEGER PRIMARY KEY AUTOINCREMENT, id_transaksi INTEGER NOT NULL, id_produk INTEGER NOT NULL, qty INTEGER NOT NULL, subtotal REAL NOT NULL, FOREIGN KEY(id_transaksi) REFERENCES transaksi(id_transaksi), FOREIGN KEY(id_produk) REFERENCES produk(id_produk))")

        // Data Default (Seeder)
        db?.execSQL("INSERT INTO tipe_ps(nama_tipe, harga_sewa) VALUES ('PS3', 5000), ('PS4', 7000), ('PS5', 10000)")
        db?.execSQL("INSERT INTO playstation(nomor_ps, id_tipe, status_ps) VALUES ('PS-01', 1, 'tersedia'), ('PS-02', 2, 'tersedia'), ('PS-03', 3, 'tersedia')")
        db?.execSQL("INSERT INTO produk(nama, jenis, harga, stock) VALUES ('Pop Mie', 'Makanan', 7000, 20), ('Es Teh', 'Minuman', 5000, 25)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS detail_produk")
        db?.execSQL("DROP TABLE IF EXISTS detail_sewa_ps")
        db?.execSQL("DROP TABLE IF EXISTS produk")
        db?.execSQL("DROP TABLE IF EXISTS playstation")
        db?.execSQL("DROP TABLE IF EXISTS tipe_ps")
        db?.execSQL("DROP TABLE IF EXISTS transaksi")
        db?.execSQL("DROP TABLE IF EXISTS user")
        onCreate(db)
    }

    // --- 1. USER & AUTHENTICATION ---
    fun insertUser(nama: String, email: String, noHp: String, password: String, role: String = "customer"): Boolean {
        val v = ContentValues().apply { put("nama", nama); put("email", email); put("no_hp", noHp); put("password", password); put("role", role) }
        return writableDatabase.insert("user", null, v) != -1L
    }

    fun checkLogin(email: String, password: String): Boolean {
        val c = readableDatabase.rawQuery("SELECT * FROM user WHERE email = ? AND password = ?", arrayOf(email, password))
        val res = c.count > 0; c.close(); return res
    }

    fun checkEmail(email: String): Boolean {
        val c = readableDatabase.rawQuery("SELECT * FROM user WHERE email = ?", arrayOf(email))
        val res = c.count > 0; c.close(); return res
    }

    fun checkEmailUserLain(email: String, id: Int): Boolean {
        val c = readableDatabase.rawQuery("SELECT * FROM user WHERE email = ? AND id_user != ?", arrayOf(email, id.toString()))
        val res = c.count > 0; c.close(); return res
    }

    fun getAllUser(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val c = readableDatabase.rawQuery("SELECT * FROM user", null)
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_user"] = c.getInt(0).toString()
                m["nama"] = c.getString(1)
                m["email"] = c.getString(2)
                m["no_hp"] = c.getString(3)
                m["password"] = c.getString(4)
                m["role"] = c.getString(5)
                list.add(m)
            } while (c.moveToNext())
        }
        c.close(); return list
    }

    fun insertPengguna(n: String, e: String, hp: String, p: String, r: String): Boolean = insertUser(n, e, hp, p, r)

    fun updatePengguna(id: Int, n: String, e: String, hp: String, p: String, r: String): Boolean {
        val v = ContentValues().apply { put("nama", n); put("email", e); put("no_hp", hp); put("password", p); put("role", r) }
        return writableDatabase.update("user", v, "id_user = ?", arrayOf(id.toString())) > 0
    }

    fun deleteUser(id: Int): Boolean {
        val db = this.writableDatabase
        // Ganti 'user' dengan nama tabel usermu, dan 'id_user' dengan nama kolom ID-nya
        val hasil = db.delete("user", "id_user = ?", arrayOf(id.toString()))
        return hasil > 0
    }

    // --- 2. TIPE PS ---
    fun getAllTipePs(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val c = readableDatabase.rawQuery("SELECT * FROM tipe_ps", null)
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_tipe"] = c.getInt(0).toString()
                m["nama_tipe"] = c.getString(1)
                m["harga_sewa"] = c.getDouble(2).toString()
                list.add(m)
            } while (c.moveToNext())
        }
        c.close(); return list
    }

    fun getAllTipePsForSpinner(): ArrayList<HashMap<String, String>> = getAllTipePs()

    fun getHargaSewaByNamaTipe(n: String): Double {
        val c = readableDatabase.rawQuery("SELECT harga_sewa FROM tipe_ps WHERE nama_tipe = ?", arrayOf(n))
        var h = 0.0; if (c.moveToFirst()) h = c.getDouble(0); c.close(); return h
    }

    fun insertTipePs(n: String, h: Double): Boolean {
        val v = ContentValues().apply { put("nama_tipe", n); put("harga_sewa", h) }
        return writableDatabase.insert("tipe_ps", null, v) != -1L
    }

    fun updateTipePs(id: Int, n: String, h: Double): Boolean {
        val v = ContentValues().apply { put("nama_tipe", n); put("harga_sewa", h) }
        return writableDatabase.update("tipe_ps", v, "id_tipe = ?", arrayOf(id.toString())) > 0
    }

    fun deleteTipePs(id: Int): Boolean = writableDatabase.delete("tipe_ps", "id_tipe = ?", arrayOf(id.toString())) > 0

    // --- 3. PLAYSTATION ---
    fun getAllPlaystation(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val sql = "SELECT p.id_ps, p.nomor_ps, p.id_tipe, p.status_ps, t.nama_tipe FROM playstation p INNER JOIN tipe_ps t ON p.id_tipe = t.id_tipe"
        val c = readableDatabase.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_ps"] = c.getInt(0).toString()
                m["nomor_ps"] = c.getString(1)
                m["id_tipe"] = c.getInt(2).toString()
                m["status_ps"] = c.getString(3)
                m["nama_tipe"] = c.getString(4)
                list.add(m)
            } while (c.moveToNext())
        }
        c.close(); return list
    }

    fun insertPlaystation(no: String, idT: Int, stat: String): Boolean {
        val v = ContentValues().apply { put("nomor_ps", no); put("id_tipe", idT); put("status_ps", stat) }
        return writableDatabase.insert("playstation", null, v) != -1L
    }

    fun updatePlaystation(id: Int, no: String, idT: Int, stat: String): Boolean {
        val v = ContentValues().apply { put("nomor_ps", no); put("id_tipe", idT); put("status_ps", stat) }
        return writableDatabase.update("playstation", v, "id_ps = ?", arrayOf(id.toString())) > 0
    }

    fun deletePlaystation(id: Int): Boolean {
        val db = writableDatabase
        return db.delete("playstation", "id_ps = ?", arrayOf(id.toString())) > 0
    }
    fun updateStatusPs(id: Int, stat: String): Boolean {
        val v = ContentValues().apply { put("status_ps", stat) }
        return writableDatabase.update("playstation", v, "id_ps = ?", arrayOf(id.toString())) > 0
    }

    // --- 4. PRODUK ---
    fun getAllProduk(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val c = readableDatabase.rawQuery("SELECT * FROM produk", null)
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_produk"] = c.getInt(0).toString()
                m["nama"] = c.getString(1)
                m["jenis"] = c.getString(2)
                m["harga"] = c.getDouble(3).toString()
                m["stock"] = c.getInt(4).toString()
                list.add(m)
            } while (c.moveToNext())
        }
        c.close(); return list
    }

    fun getAllProdukSpinner(): ArrayList<HashMap<String, String>> = getAllProduk()

    fun insertProduk(n: String, j: String, h: Double, s: Int): Boolean {
        val v = ContentValues().apply { put("nama", n); put("jenis", j); put("harga", h); put("stock", s) }
        return writableDatabase.insert("produk", null, v) != -1L
    }

    fun updateProduk(id: Int, n: String, j: String, h: Double, s: Int): Boolean {
        val v = ContentValues().apply { put("nama", n); put("jenis", j); put("harga", h); put("stock", s) }
        return writableDatabase.update("produk", v, "id_produk = ?", arrayOf(id.toString())) > 0
    }

    fun deleteProduk(id: Int): Boolean {
        val db = writableDatabase
        return db.delete("produk", "id_produk = ?", arrayOf(id.toString())) > 0
    }
    // --- 5. MONITORING & TRANSAKSI ---
    fun getMonitoringPs(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val sql = """
        SELECT p.id_ps, p.nomor_ps, t.nama_tipe, p.status_ps, dsp.jam_mulai, dsp.jam_selesai 
        FROM playstation p 
        INNER JOIN tipe_ps t ON p.id_tipe = t.id_tipe 
        LEFT JOIN (
            SELECT id_ps, jam_mulai, jam_selesai 
            FROM detail_sewa_ps 
            WHERE id_dt_booking IN (SELECT MAX(id_dt_booking) FROM detail_sewa_ps GROUP BY id_ps)
        ) dsp ON p.id_ps = dsp.id_ps
    """.trimIndent()

        val c = readableDatabase.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_ps"] = c.getString(0)      // Benar
                m["nomor_ps"] = c.getString(1)   // Benar
                m["nama_tipe"] = c.getString(2)  // SEBELUMNYA index 4 (Salah)
                m["status_ps"] = c.getString(3)  // SEBELUMNYA index 3 (Benar)
                m["jam_mulai"] = c.getString(4) ?: "" // SEBELUMNYA index 5 (Salah)
                m["jam_selesai"] = c.getString(5) ?: "" // SEBELUMNYA index 6 (Salah - Index 6 ga ada!)
                list.add(m)
            } while (c.moveToNext())
        }
        c.close()
        return list
    }

    fun selesaiTransaksiByPs(idPs: Int): Boolean {
        val db = writableDatabase
        val v = ContentValues()
        v.put("status_ps", "tersedia")
        // Mengubah status PS berdasarkan ID-nya
        return db.update("playstation", v, "id_ps = ?", arrayOf(idPs.toString())) > 0
    }
    fun insertTransaksi(idU: Int, tgl: String, tot: Double, stat: String): Long {
        val v = ContentValues().apply { put("id_user", idU); put("tanggal", tgl); put("total_harga", tot); put("status_transaksi", stat) }
        return writableDatabase.insert("transaksi", null, v)
    }

    fun insertDetailSewaPs(idT: Long, idPs: Int, dur: Int, m: String, s: String, tipe: String, h: Double, sub: Double): Boolean {
        val v = ContentValues().apply { put("id_transaksi", idT); put("id_ps", idPs); put("durasi", dur); put("jam_mulai", m); put("jam_selesai", s); put("type_ps", tipe); put("harga_perjam", h); put("subtotal", sub) }
        return writableDatabase.insert("detail_sewa_ps", null, v) != -1L
    }

    fun insertDetailProduk(idT: Long, idP: Int, q: Int, sub: Double): Boolean {
        val v = ContentValues().apply { put("id_transaksi", idT); put("id_produk", idP); put("qty", q); put("subtotal", sub) }
        return writableDatabase.insert("detail_produk", null, v) != -1L
    }
    // TAMBAHKAN INI DI DBOpenHelper.kt (Wajib!)
    fun getAllRiwayatTransaksi(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()

        // Ambil tanggal hari ini dalam format yang sama saat simpan (yyyy-MM-dd)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val tglHariIni = sdf.format(java.util.Date())

        val sql = """
        SELECT tr.id_transaksi, tr.tanggal, tr.total_harga, tr.status_transaksi, 
               p.nomor_ps, dsp.type_ps, dsp.jam_mulai, dsp.jam_selesai, dsp.durasi
        FROM transaksi tr 
        LEFT JOIN detail_sewa_ps dsp ON tr.id_transaksi = dsp.id_transaksi 
        LEFT JOIN playstation p ON dsp.id_ps = p.id_ps 
        WHERE tr.tanggal = ?
        ORDER BY tr.id_transaksi DESC
    """.trimIndent()

        val c = readableDatabase.rawQuery(sql, arrayOf(tglHariIni))
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_transaksi"] = c.getString(0)
                m["tanggal"] = c.getString(1)
                m["total_harga"] = c.getString(2)
                m["status_transaksi"] = c.getString(3)
                m["nomor_ps"] = c.getString(4) ?: "-"
                m["type_ps"] = c.getString(5) ?: "-"
                m["jam"] = "${c.getString(6) ?: ""} - ${c.getString(7) ?: ""}"
                m["durasi"] = c.getString(8) ?: "0"
                list.add(m)
            } while (c.moveToNext())
        }
        c.close()
        return list
    }
    // Tambahkan ini di DBOpenHelper.kt
    fun getAllTransaksiAktif(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val sql = """
        SELECT tr.id_transaksi, tr.tanggal, tr.total_harga, tr.status_transaksi, 
               p.nomor_ps, dsp.type_ps, dsp.jam_mulai, dsp.jam_selesai, dsp.durasi, p.id_ps
        FROM transaksi tr 
        INNER JOIN detail_sewa_ps dsp ON tr.id_transaksi = dsp.id_transaksi 
        INNER JOIN playstation p ON dsp.id_ps = p.id_ps 
        WHERE tr.status_transaksi = 'aktif' 
        ORDER BY tr.id_transaksi DESC
    """.trimIndent()

        val c = readableDatabase.rawQuery(sql, null)
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_transaksi"] = c.getString(0)
                m["total_harga"] = c.getString(2)
                m["nomor_ps"] = c.getString(4)
                m["type_ps"] = c.getString(5)
                m["jam"] = "${c.getString(6)} - ${c.getString(7)}"
                m["durasi"] = "Durasi: ${c.getString(8)} jam"
                m["id_ps"] = c.getString(9) // Ambil ID PS-nya di sini
                list.add(m)
            } while (c.moveToNext())
        }
        c.close()
        return list
    }
    fun updateStatusTransaksi(id: Int, status: String): Boolean {
        val db = writableDatabase
        val v = android.content.ContentValues()
        v.put("status_transaksi", status)
        return db.update("transaksi", v, "id_transaksi = ?", arrayOf(id.toString())) > 0
    }

    fun getTotalPendapatanHariIni(): Double {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val tglHariIni = sdf.format(java.util.Date())

        var total = 0.0
        val c = readableDatabase.rawQuery(
            "SELECT SUM(total_harga) FROM transaksi WHERE tanggal = ? AND status_transaksi = 'selesai'",
            arrayOf(tglHariIni)
        )
        if (c.moveToFirst()) total = c.getDouble(0)
        c.close()
        return total
    }
    fun getLaporanHarian(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val tglHariIni = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val sql = """
        SELECT tr.id_transaksi, tr.tanggal, tr.total_harga, tr.status_transaksi, 
               p.nomor_ps, dsp.type_ps, dsp.jam_mulai, dsp.jam_selesai, dsp.durasi
        FROM transaksi tr 
        INNER JOIN detail_sewa_ps dsp ON tr.id_transaksi = dsp.id_transaksi 
        INNER JOIN playstation p ON dsp.id_ps = p.id_ps 
        WHERE tr.tanggal = ? AND tr.status_transaksi = 'selesai'
        ORDER BY tr.id_transaksi DESC
    """.trimIndent()

        val c = readableDatabase.rawQuery(sql, arrayOf(tglHariIni))
        if (c.moveToFirst()) {
            do {
                val m = HashMap<String, String>()
                m["id_transaksi"] = c.getString(0)
                m["tanggal"] = c.getString(1)
                m["total_harga"] = c.getString(2)
                m["status_transaksi"] = c.getString(3)
                m["nomor_ps"] = c.getString(4) ?: "-"
                m["type_ps"] = c.getString(5) ?: "-"
                m["jam"] = "${c.getString(6) ?: ""} - ${c.getString(7) ?: ""}"
                m["durasi"] = c.getString(8) ?: "0"
                list.add(m)
            } while (c.moveToNext())
        }
        c.close()
        return list
    }
    fun getTotalOmzetHariIni(): Double {
        val tgl = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        var total = 0.0
        val c = readableDatabase.rawQuery("SELECT SUM(total_harga) FROM transaksi WHERE tanggal = ? AND status_transaksi = 'selesai'", arrayOf(tgl))
        if (c.moveToFirst()) total = c.getDouble(0)
        c.close()
        return total
    }
    companion object {
        private const val DB_NAME = "siperpsa_clean.db"
        private const val DB_VERSION = 1
    }
}