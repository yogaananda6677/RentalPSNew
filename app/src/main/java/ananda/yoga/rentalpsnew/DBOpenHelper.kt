package ananda.yoga.rentalpsnew

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val sqlUser = """
            CREATE TABLE user (
                id_user INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                no_hp TEXT,
                password TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'customer'
            )
        """.trimIndent()

        val sqlTransaksi = """
            CREATE TABLE transaksi (
                id_transaksi INTEGER PRIMARY KEY AUTOINCREMENT,
                id_user INTEGER NOT NULL,
                tanggal TEXT NOT NULL,
                total_harga REAL NOT NULL,
                status_transaksi TEXT NOT NULL,
                FOREIGN KEY(id_user) REFERENCES user(id_user)
            )
        """.trimIndent()

        val sqlTipePs = """
            CREATE TABLE tipe_ps (
                id_tipe INTEGER PRIMARY KEY AUTOINCREMENT,
                nama_tipe TEXT NOT NULL,
                harga_sewa REAL NOT NULL
            )
        """.trimIndent()

        val sqlPlaystation = """
            CREATE TABLE playstation (
                id_ps INTEGER PRIMARY KEY AUTOINCREMENT,
                nomor_ps TEXT NOT NULL,
                id_tipe INTEGER NOT NULL,
                status_ps TEXT NOT NULL,
                FOREIGN KEY(id_tipe) REFERENCES tipe_ps(id_tipe)
            )
        """.trimIndent()

        val sqlProduk = """
            CREATE TABLE produk (
                id_produk INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                jenis TEXT NOT NULL,
                harga REAL NOT NULL,
                stock INTEGER NOT NULL
            )
        """.trimIndent()

        val sqlDetailSewaPs = """
            CREATE TABLE detail_sewa_ps (
                id_dt_booking INTEGER PRIMARY KEY AUTOINCREMENT,
                id_transaksi INTEGER NOT NULL,
                id_ps INTEGER NOT NULL,
                durasi INTEGER NOT NULL,
                jam_mulai TEXT NOT NULL,
                jam_selesai TEXT NOT NULL,
                type_ps TEXT NOT NULL,
                harga_perjam REAL NOT NULL,
                subtotal REAL NOT NULL,
                FOREIGN KEY(id_transaksi) REFERENCES transaksi(id_transaksi),
                FOREIGN KEY(id_ps) REFERENCES playstation(id_ps)
            )
        """.trimIndent()

        val sqlDetailProduk = """
            CREATE TABLE detail_produk (
                id_dt_produk INTEGER PRIMARY KEY AUTOINCREMENT,
                id_transaksi INTEGER NOT NULL,
                id_produk INTEGER NOT NULL,
                qty INTEGER NOT NULL,
                subtotal REAL NOT NULL,
                FOREIGN KEY(id_transaksi) REFERENCES transaksi(id_transaksi),
                FOREIGN KEY(id_produk) REFERENCES produk(id_produk)
            )
        """.trimIndent()

        val sqlPembayaran = """
            CREATE TABLE pembayaran (
                id_pembayaran INTEGER PRIMARY KEY AUTOINCREMENT,
                id_transaksi INTEGER NOT NULL,
                metode_pembayaran TEXT NOT NULL,
                total_bayar REAL NOT NULL,
                waktu_bayar TEXT NOT NULL,
                status_bayar TEXT NOT NULL,
                FOREIGN KEY(id_transaksi) REFERENCES transaksi(id_transaksi)
            )
        """.trimIndent()

        db?.execSQL(sqlUser)
        db?.execSQL(sqlTransaksi)
        db?.execSQL(sqlTipePs)
        db?.execSQL(sqlPlaystation)
        db?.execSQL(sqlProduk)
        db?.execSQL(sqlDetailSewaPs)
        db?.execSQL(sqlDetailProduk)
        db?.execSQL(sqlPembayaran)

        db?.execSQL("INSERT INTO tipe_ps(nama_tipe, harga_sewa) VALUES ('PS3', 5000)")
        db?.execSQL("INSERT INTO tipe_ps(nama_tipe, harga_sewa) VALUES ('PS4', 7000)")
        db?.execSQL("INSERT INTO tipe_ps(nama_tipe, harga_sewa) VALUES ('PS5', 10000)")

        db?.execSQL("INSERT INTO playstation(nomor_ps, id_tipe, status_ps) VALUES ('PS-01', 1, 'tersedia')")
        db?.execSQL("INSERT INTO playstation(nomor_ps, id_tipe, status_ps) VALUES ('PS-02', 2, 'tersedia')")
        db?.execSQL("INSERT INTO playstation(nomor_ps, id_tipe, status_ps) VALUES ('PS-03', 3, 'tersedia')")

        db?.execSQL("INSERT INTO produk(nama, jenis, harga, stock) VALUES ('Pop Mie', 'Makanan', 7000, 20)")
        db?.execSQL("INSERT INTO produk(nama, jenis, harga, stock) VALUES ('Es Teh', 'Minuman', 5000, 25)")
        db?.execSQL("INSERT INTO produk(nama, jenis, harga, stock) VALUES ('Kopi', 'Minuman', 8000, 15)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS pembayaran")
        db?.execSQL("DROP TABLE IF EXISTS detail_produk")
        db?.execSQL("DROP TABLE IF EXISTS detail_sewa_ps")
        db?.execSQL("DROP TABLE IF EXISTS produk")
        db?.execSQL("DROP TABLE IF EXISTS playstation")
        db?.execSQL("DROP TABLE IF EXISTS tipe_ps")
        db?.execSQL("DROP TABLE IF EXISTS transaksi")
        db?.execSQL("DROP TABLE IF EXISTS user")
        onCreate(db)
    }

    fun insertUser(nama: String, email: String, noHp: String, password: String, role: String = "customer"): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("nama", nama)
        values.put("email", email)
        values.put("no_hp", noHp)
        values.put("password", password)
        values.put("role", role)

        val result = db.insert("user", null, values)
        db.close()
        return result != -1L
    }

    fun checkLogin(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM user WHERE email = ? AND password = ?",
            arrayOf(email, password)
        )

        val berhasil = cursor.count > 0
        cursor.close()
        db.close()
        return berhasil
    }

    fun checkEmail(email: String): Boolean {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM user WHERE email = ?",
            arrayOf(email)
        )

        val ada = cursor.count > 0
        cursor.close()
        db.close()
        return ada
    }

//    CRUD TIPE PS
    fun insertTipePs(namaTipe: String, hargaSewa: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("nama_tipe", namaTipe)
        values.put("harga_sewa", hargaSewa)

        val result = db.insert("tipe_ps", null, values)
        db.close()
        return result != -1L
    }

    fun updateTipePs(idTipe: Int, namaTipe: String, hargaSewa: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("nama_tipe", namaTipe)
        values.put("harga_sewa", hargaSewa)

        val result = db.update(
            "tipe_ps",
            values,
            "id_tipe = ?",
            arrayOf(idTipe.toString())
        )
        db.close()
        return result > 0
    }

    fun deleteTipePs(idTipe: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(
            "tipe_ps",
            "id_tipe = ?",
            arrayOf(idTipe.toString())
        )
        db.close()
        return result > 0
    }

    fun getAllTipePs(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tipe_ps ORDER BY id_tipe DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val item = HashMap<String, String>()
                item["id_tipe"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_tipe")).toString()
                item["nama_tipe"] = cursor.getString(cursor.getColumnIndexOrThrow("nama_tipe"))
                item["harga_sewa"] = cursor.getDouble(cursor.getColumnIndexOrThrow("harga_sewa")).toString()
                list.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }

//    kelola data ps
fun insertPlaystation(nomorPs: String, idTipe: Int, statusPs: String): Boolean {
    val db = writableDatabase
    val values = ContentValues()
    values.put("nomor_ps", nomorPs)
    values.put("id_tipe", idTipe)
    values.put("status_ps", statusPs)

    val result = db.insert("playstation", null, values)
    db.close()
    return result != -1L
}

    fun updatePlaystation(idPs: Int, nomorPs: String, idTipe: Int, statusPs: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("nomor_ps", nomorPs)
        values.put("id_tipe", idTipe)
        values.put("status_ps", statusPs)

        val result = db.update(
            "playstation",
            values,
            "id_ps = ?",
            arrayOf(idPs.toString())
        )
        db.close()
        return result > 0
    }

    fun deletePlaystation(idPs: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(
            "playstation",
            "id_ps = ?",
            arrayOf(idPs.toString())
        )
        db.close()
        return result > 0
    }

    fun getAllPlaystation(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val db = readableDatabase

        val sql = """
        SELECT p.id_ps, p.nomor_ps, p.id_tipe, p.status_ps, t.nama_tipe
        FROM playstation p
        INNER JOIN tipe_ps t ON p.id_tipe = t.id_tipe
        ORDER BY p.id_ps DESC
    """.trimIndent()

        val cursor = db.rawQuery(sql, null)

        if (cursor.moveToFirst()) {
            do {
                val item = HashMap<String, String>()
                item["id_ps"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_ps")).toString()
                item["nomor_ps"] = cursor.getString(cursor.getColumnIndexOrThrow("nomor_ps"))
                item["id_tipe"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_tipe")).toString()
                item["nama_tipe"] = cursor.getString(cursor.getColumnIndexOrThrow("nama_tipe"))
                item["status_ps"] = cursor.getString(cursor.getColumnIndexOrThrow("status_ps"))
                list.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }

    fun getAllTipePsForSpinner(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tipe_ps ORDER BY nama_tipe ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val item = HashMap<String, String>()
                item["id_tipe"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_tipe")).toString()
                item["nama_tipe"] = cursor.getString(cursor.getColumnIndexOrThrow("nama_tipe"))
                list.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }


//    kelola produk
fun insertProduk(nama: String, jenis: String, harga: Double, stock: Int): Boolean {
    val db = writableDatabase
    val values = ContentValues()
    values.put("nama", nama)
    values.put("jenis", jenis)
    values.put("harga", harga)
    values.put("stock", stock)

    val result = db.insert("produk", null, values)
    db.close()
    return result != -1L
}

    fun updateProduk(idProduk: Int, nama: String, jenis: String, harga: Double, stock: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("nama", nama)
        values.put("jenis", jenis)
        values.put("harga", harga)
        values.put("stock", stock)

        val result = db.update(
            "produk",
            values,
            "id_produk = ?",
            arrayOf(idProduk.toString())
        )
        db.close()
        return result > 0
    }

    fun deleteProduk(idProduk: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(
            "produk",
            "id_produk = ?",
            arrayOf(idProduk.toString())
        )
        db.close()
        return result > 0
    }

    fun getAllProduk(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM produk ORDER BY id_produk DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val item = HashMap<String, String>()
                item["id_produk"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_produk")).toString()
                item["nama"] = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
                item["jenis"] = cursor.getString(cursor.getColumnIndexOrThrow("jenis"))
                item["harga"] = cursor.getDouble(cursor.getColumnIndexOrThrow("harga")).toString()
                item["stock"] = cursor.getInt(cursor.getColumnIndexOrThrow("stock")).toString()
                list.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }


//    kelola pengguna
fun getAllUser(): ArrayList<HashMap<String, String>> {
    val list = ArrayList<HashMap<String, String>>()
    val db = readableDatabase
    val cursor = db.rawQuery("SELECT * FROM user ORDER BY id_user DESC", null)

    if (cursor.moveToFirst()) {
        do {
            val item = HashMap<String, String>()
            item["id_user"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_user")).toString()
            item["nama"] = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
            item["email"] = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            item["no_hp"] = cursor.getString(cursor.getColumnIndexOrThrow("no_hp"))
            item["password"] = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            item["role"] = cursor.getString(cursor.getColumnIndexOrThrow("role"))
            list.add(item)
        } while (cursor.moveToNext())
    }

    cursor.close()
    db.close()
    return list
}

    fun insertPengguna(nama: String, email: String, noHp: String, password: String, role: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("nama", nama)
        values.put("email", email)
        values.put("no_hp", noHp)
        values.put("password", password)
        values.put("role", role)

        val result = db.insert("user", null, values)
        db.close()
        return result != -1L
    }

    fun updatePengguna(
        idUser: Int,
        nama: String,
        email: String,
        noHp: String,
        password: String,
        role: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("nama", nama)
        values.put("email", email)
        values.put("no_hp", noHp)
        values.put("password", password)
        values.put("role", role)

        val result = db.update(
            "user",
            values,
            "id_user = ?",
            arrayOf(idUser.toString())
        )
        db.close()
        return result > 0
    }

    fun deletePengguna(idUser: Int): Boolean {
        val db = writableDatabase
        val result = db.delete(
            "user",
            "id_user = ?",
            arrayOf(idUser.toString())
        )
        db.close()
        return result > 0
    }

    fun checkEmailUserLain(email: String, idUser: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM user WHERE email = ? AND id_user != ?",
            arrayOf(email, idUser.toString())
        )

        val ada = cursor.count > 0
        cursor.close()
        db.close()
        return ada
    }





//    monitoring
fun getMonitoringPs(): ArrayList<HashMap<String, String>> {
    val list = ArrayList<HashMap<String, String>>()
    val db = readableDatabase

    val sql = """
        SELECT 
            p.id_ps,
            p.nomor_ps,
            p.status_ps,
            t.nama_tipe,
            dsp.jam_mulai,
            dsp.jam_selesai,
            tr.id_transaksi,
            tr.status_transaksi
        FROM playstation p
        INNER JOIN tipe_ps t ON p.id_tipe = t.id_tipe
        LEFT JOIN detail_sewa_ps dsp ON p.id_ps = dsp.id_ps
        LEFT JOIN transaksi tr ON dsp.id_transaksi = tr.id_transaksi
        ORDER BY p.id_ps ASC
    """.trimIndent()

    val cursor = db.rawQuery(sql, null)

    if (cursor.moveToFirst()) {
        do {
            val item = HashMap<String, String>()
            item["id_ps"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_ps")).toString()
            item["nomor_ps"] = cursor.getString(cursor.getColumnIndexOrThrow("nomor_ps"))
            item["status_ps"] = cursor.getString(cursor.getColumnIndexOrThrow("status_ps"))
            item["nama_tipe"] = cursor.getString(cursor.getColumnIndexOrThrow("nama_tipe"))

            val jamMulaiIndex = cursor.getColumnIndex("jam_mulai")
            val jamSelesaiIndex = cursor.getColumnIndex("jam_selesai")
            val idTransaksiIndex = cursor.getColumnIndex("id_transaksi")
            val statusTransaksiIndex = cursor.getColumnIndex("status_transaksi")

            item["jam_mulai"] =
                if (jamMulaiIndex >= 0 && !cursor.isNull(jamMulaiIndex)) cursor.getString(jamMulaiIndex) else ""

            item["jam_selesai"] =
                if (jamSelesaiIndex >= 0 && !cursor.isNull(jamSelesaiIndex)) cursor.getString(jamSelesaiIndex) else ""

            item["id_transaksi"] =
                if (idTransaksiIndex >= 0 && !cursor.isNull(idTransaksiIndex)) cursor.getInt(idTransaksiIndex).toString() else ""

            item["status_transaksi"] =
                if (statusTransaksiIndex >= 0 && !cursor.isNull(statusTransaksiIndex)) cursor.getString(statusTransaksiIndex) else ""

            list.add(item)
        } while (cursor.moveToNext())
    }

    cursor.close()
    db.close()
    return list
}

// transaksi
fun insertTransaksi(
    idUser: Int,
    tanggal: String,
    totalHarga: Double,
    statusTransaksi: String
): Long {
    val db = writableDatabase
    val values = ContentValues()
    values.put("id_user", idUser)
    values.put("tanggal", tanggal)
    values.put("total_harga", totalHarga)
    values.put("status_transaksi", statusTransaksi)

    val result = db.insert("transaksi", null, values)
    db.close()
    return result
}

    fun insertDetailSewaPs(
        idTransaksi: Long,
        idPs: Int,
        durasi: Int,
        jamMulai: String,
        jamSelesai: String,
        typePs: String,
        hargaPerJam: Double,
        subtotal: Double
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("id_transaksi", idTransaksi)
        values.put("id_ps", idPs)
        values.put("durasi", durasi)
        values.put("jam_mulai", jamMulai)
        values.put("jam_selesai", jamSelesai)
        values.put("type_ps", typePs)
        values.put("harga_perjam", hargaPerJam)
        values.put("subtotal", subtotal)

        val result = db.insert("detail_sewa_ps", null, values)
        db.close()
        return result != -1L
    }

    fun insertDetailProduk(
        idTransaksi: Long,
        idProduk: Int,
        qty: Int,
        subtotal: Double
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("id_transaksi", idTransaksi)
        values.put("id_produk", idProduk)
        values.put("qty", qty)
        values.put("subtotal", subtotal)

        val result = db.insert("detail_produk", null, values)
        db.close()
        return result != -1L
    }

    fun getAllProdukSpinner(): ArrayList<HashMap<String, String>> {
        val list = ArrayList<HashMap<String, String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM produk ORDER BY nama ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val item = HashMap<String, String>()
                item["id_produk"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_produk")).toString()
                item["nama"] = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
                item["jenis"] = cursor.getString(cursor.getColumnIndexOrThrow("jenis"))
                item["harga"] = cursor.getDouble(cursor.getColumnIndexOrThrow("harga")).toString()
                item["stock"] = cursor.getInt(cursor.getColumnIndexOrThrow("stock")).toString()
                list.add(item)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }

    fun getHargaSewaByNamaTipe(namaTipe: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT harga_sewa FROM tipe_ps WHERE nama_tipe = ?",
            arrayOf(namaTipe)
        )

        var harga = 0.0
        if (cursor.moveToFirst()) {
            harga = cursor.getDouble(cursor.getColumnIndexOrThrow("harga_sewa"))
        }

        cursor.close()
        db.close()
        return harga
    }

    fun updateStatusPs(idPs: Int, statusPs: String): Boolean {
        val db = writableDatabase
        val values = ContentValues()
        values.put("status_ps", statusPs)

        val result = db.update(
            "playstation",
            values,
            "id_ps = ?",
            arrayOf(idPs.toString())
        )
        db.close()
        return result > 0
    }


//   riwayat
fun getAllRiwayatTransaksi(): ArrayList<HashMap<String, String>> {
    val list = ArrayList<HashMap<String, String>>()
    val db = readableDatabase

    val sql = """
        SELECT 
            tr.id_transaksi,
            tr.tanggal,
            tr.total_harga,
            tr.status_transaksi,
            p.nomor_ps,
            dsp.jam_mulai,
            dsp.jam_selesai,
            dsp.durasi,
            dsp.type_ps
        FROM transaksi tr
        LEFT JOIN detail_sewa_ps dsp ON tr.id_transaksi = dsp.id_transaksi
        LEFT JOIN playstation p ON dsp.id_ps = p.id_ps
        ORDER BY tr.id_transaksi DESC
    """.trimIndent()

    val cursor = db.rawQuery(sql, null)

    if (cursor.moveToFirst()) {
        do {
            val item = HashMap<String, String>()
            item["id_transaksi"] = cursor.getInt(cursor.getColumnIndexOrThrow("id_transaksi")).toString()
            item["tanggal"] = cursor.getString(cursor.getColumnIndexOrThrow("tanggal"))
            item["total_harga"] = cursor.getDouble(cursor.getColumnIndexOrThrow("total_harga")).toString()
            item["status_transaksi"] = cursor.getString(cursor.getColumnIndexOrThrow("status_transaksi"))

            val nomorPsIndex = cursor.getColumnIndex("nomor_ps")
            val jamMulaiIndex = cursor.getColumnIndex("jam_mulai")
            val jamSelesaiIndex = cursor.getColumnIndex("jam_selesai")
            val durasiIndex = cursor.getColumnIndex("durasi")
            val typePsIndex = cursor.getColumnIndex("type_ps")

            item["nomor_ps"] =
                if (nomorPsIndex >= 0 && !cursor.isNull(nomorPsIndex)) cursor.getString(nomorPsIndex) else "-"

            item["jam_mulai"] =
                if (jamMulaiIndex >= 0 && !cursor.isNull(jamMulaiIndex)) cursor.getString(jamMulaiIndex) else "-"

            item["jam_selesai"] =
                if (jamSelesaiIndex >= 0 && !cursor.isNull(jamSelesaiIndex)) cursor.getString(jamSelesaiIndex) else "-"

            item["durasi"] =
                if (durasiIndex >= 0 && !cursor.isNull(durasiIndex)) cursor.getInt(durasiIndex).toString() else "0"

            item["type_ps"] =
                if (typePsIndex >= 0 && !cursor.isNull(typePsIndex)) cursor.getString(typePsIndex) else "-"

            list.add(item)
        } while (cursor.moveToNext())
    }

    cursor.close()
    db.close()
    return list
}

    companion object {
        private const val DB_NAME = "rental_ps.db"
        private const val DB_VERSION = 1
    }
}
