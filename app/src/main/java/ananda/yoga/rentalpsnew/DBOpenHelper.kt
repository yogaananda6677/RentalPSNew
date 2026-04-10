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

    companion object {
        private const val DB_NAME = "rental_ps.db"
        private const val DB_VERSION = 1
    }
}
