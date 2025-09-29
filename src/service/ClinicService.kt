package service

import data_class.*
import payment.*

object ClinicService {

    private val pasienList = mutableListOf<Pasien>()
    private val dokterList = mutableListOf<Dokter>()
    private val janjiList = mutableListOf<JanjiTemu>()
    private val tagihanList = mutableListOf<Tagihan>()
    private val MAX_ANTRIAN_PER_DOKTER = 10
    private val DENDA_NO_SHOW = 50000

    private fun generatePasienId(): String {
        return "P" + (pasienList.size + 1).toString().padStart(3, '0')
    }

    private fun generateDokterId(): String {
        return "D" + (dokterList.size + 1).toString().padStart(3, '0')
    }

    private fun generateTagihanId(): String {
        return "T" + (tagihanList.size + 1).toString().padStart(3, '0')
    }

    fun tambahPasien(nama: String, nomorTelepon: String?, alamat: String?, kategori: String){
        pasienList.add(Pasien(generatePasienId(), nama, alamat, nomorTelepon, kategori ))
    }

    fun tambahDokter(nama: String, tipe: String, spesialisasi: String? = null){
        when(tipe){
            "Umum" -> dokterList.add(DokterUmum(generateDokterId(), nama))
            "Spesialis" -> {
                if(spesialisasi == null) println("Spesialisasi perlu diisi")
                else dokterList.add(DokterSpesialis(generateDokterId(), nama, spesialisasi))
            }
            else -> println("Tipe Tidak Valid")
        }
    }

    fun buatJanji(pasienId: String, dokterId: String, jam: Int, pembayaran: Pembayaran): Boolean {
        // Validasi jam
        if (jam !in 0..23) {
            println("Jam janji harus antara 0 hingga 23.")
            return false
        }

        val pasien = pasienList.find { it.id == pasienId }
        val dokter = dokterList.find { it.id == dokterId }

        if (pasien == null) {
            println("Pasien dengan ID $pasienId tidak ditemukan.")
            return false
        }
        if (dokter == null) {
            println("Dokter dengan ID $dokterId tidak ditemukan.")
            return false
        }

        val bentrok = janjiList.any { it.dokter.id == dokterId && it.jam == jam }
        if (bentrok) {
            println("Janji bentrok: Dokter ${dokter.nama} sudah ada janji pada jam $jam.")
            return false
        }

        val janjiDokterHariIni = janjiList.count { it.dokter.id == dokterId && it.jam == jam }
        if (janjiDokterHariIni >= MAX_ANTRIAN_PER_DOKTER) {
            println("Antrian dokter sudah penuh.")
            return false
        }

        // Tambah janji baru
        val janji = JanjiTemu(pasien, dokter, jam)
        janjiList.add(janji)
        prosesPembayaran(janji, pembayaran)
        println("Janji temu berhasil dibuat: Pasien ${pasien.nama} dengan Dokter ${dokter.nama} jam $jam.")
        return true
    }

    fun prosesPembayaran(janji: JanjiTemu, pembayaran: Pembayaran) {
        val biayaDasar = janji.dokter.biayaDasar
        val totalBayar = pembayaran.hitungBiaya(biayaDasar)
        val tagihan = Tagihan(generateTagihanId(), janji, totalBayar,pembayaran.javaClass.simpleName)
        tagihanList.add(tagihan)
    }

    fun laporanHarian() {
        println("=== Laporan Harian Klinik ===")

        // Hitung janji per dokter
        val janjiPerDokter = janjiList.groupingBy { it.dokter }.eachCount()

        // Total pemasukan (sum semua tagihan)
        var totalPemasukan = janjiList.sumOf { janji ->
            janji.dokter.biayaDasar
        }

        val totalDenda = tagihanList.sumOf { it.dendaNoShow }
        totalPemasukan += totalDenda

        println("Total Janji per Dokter:")
        janjiPerDokter.forEach { (dokter, jumlah) ->
            println("- ${dokter.nama}: $jumlah janji")
        }

        println("Total Pemasukan Kas: Rp$totalPemasukan")

        // 3 dokter dengan janji terbanyak
        val top3Dokter = janjiPerDokter.entries.sortedByDescending { it.value }.take(3)
        println("3 Dokter dengan Janji Terbanyak:")
        top3Dokter.forEachIndexed { index, entry ->
            println("${index + 1}. ${entry.key.nama} (${entry.value} janji)")
        }

        println("=============================")

        // Tambahan: Export laporan ringkas dalam bentuk tabel
        println("\n=== Tabel Ringkas Laporan Klinik ===")
        println(String.format("%-20s | %-12s | %-15s", "Nama Dokter", "Jumlah Janji", "Total Pemasukan"))
        println("-".repeat(53))

        // Grup tagihan per dokter
        val tagihanPerDokter = tagihanList.groupBy { it.janjiTemu.dokter }

        dokterList.forEach { dokter ->
            val jumlahJanji = janjiPerDokter[dokter] ?: 0
            val totalBiayaDasar = dokter.biayaDasar * jumlahJanji
            val totalDendaDokter = tagihanPerDokter[dokter]?.sumOf { it.dendaNoShow } ?: 0
            val totalPemasukanDokter = totalBiayaDasar + totalDendaDokter

            println(String.format("%-20s | %-12d | Rp %-13d", dokter.nama, jumlahJanji, totalPemasukanDokter))
        }

        println("-".repeat(53))
    }

    fun getTagihanByPasienId(pasienId: String): List<Tagihan> {
        return tagihanList.filter { it.janjiTemu.pasien.id == pasienId }
    }

    fun lihatAntrian(dokterId: String) {
        val janjiPrioritas = janjiList
            .filter { it.dokter.id == dokterId }
            .sortedWith(
                compareByDescending<JanjiTemu> { it.pasien.kategori.equals("Lansia", ignoreCase = true) }
                    .thenBy { it.jam }
            )

        if (janjiPrioritas.isEmpty()) {
            println("Tidak ada antrian untuk dokter dengan ID $dokterId.")
            return
        }

        println("Antrian janji untuk dokter ID $dokterId:")
        janjiPrioritas.forEachIndexed { index, janji ->
            println("${index + 1}. Jam: ${janji.jam} - Pasien: ${janji.pasien.nama} (Kategori: ${janji.pasien.kategori})")
        }
    }

    fun batalkanJanji(pasienId: String, dokterId: String): Boolean {
        val janji = janjiList.find {
            it.pasien.id == pasienId && it.dokter.id == dokterId && it.status == StatusJanji.Aktif
        }

        if (janji == null) {
            println("Janji tidak ditemukan atau sudah dibatalkan.")
            return false
        }

        // Langsung beri status NoShow dan denda
        janji.status = StatusJanji.NoShow
        val tagihanDenda = Tagihan(
            generateTagihanId(),
            janji,
            DENDA_NO_SHOW,
            "Denda NoShow",
            DENDA_NO_SHOW
        )
        tagihanList.add(tagihanDenda)
        println("Janji dibatalkan dan dikenakan denda sebesar Rp $DENDA_NO_SHOW.")
        return true
    }

    fun getPasienList() = pasienList.toList()
    fun getDokterList() = dokterList.toList()
    fun getJanjiList() = janjiList.toList()
    fun getTagihanList() = tagihanList.toList()
}