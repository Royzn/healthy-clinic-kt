import data_class.DokterSpesialis
import data_class.DokterUmum
import data_class.StatusJanji
import service.ClinicService
import payment.*

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    while (true) {
        println("\n=== Klinik Management System ===")
        println("1. Tambah Pasien")
        println("2. Tambah Dokter")
        println("3. Buat Janji Temu")
        println("4. Lihat Antrian Dokter")
        println("5. Tampilkan Laporan Harian")
        println("6. Lihat Tagihan Pasien")
        println("7. Batalkan Janji")
        println("8. Keluar")
        print("Pilih menu (1-8): ")

        when (val input = readlnOrNull()) {
            "1" -> {
                print("Nama pasien: ")
                val nama = readlnOrNull()?.trim() ?: ""

                var telepon: String? = null
                while (true) {
                    print("Nomor telepon (optional): ")
                    val input = readlnOrNull()?.trim()

                    if (input.isNullOrEmpty()) {
                        telepon = null
                        break
                    } else if (input.matches(Regex("^[0-9]{6,15}\$"))) {
                        telepon = input
                        break
                    } else {
                        println("Nomor telepon tidak valid! Hanya angka, 6-15 digit.")
                    }
                }

                print("Alamat (optional): ")
                val alamat = readlnOrNull()?.trim()

                println("Kategori pasien (1. Reguler, 2. Lansia): ")
                val kategoriInput = readlnOrNull()?.toIntOrNull()
                val kategori = when (kategoriInput) {
                    2 -> "Lansia"
                    else -> "Reguler"
                }

                ClinicService.tambahPasien(nama, telepon, alamat, kategori)
            }

            "2" -> {
                var nama: String
                while (true) {
                    print("Nama dokter: ")
                    nama = readlnOrNull()?.trim() ?: ""
                    if (nama.isNotEmpty()) break
                    println("Nama dokter tidak boleh kosong.")
                }

                var tipe: String
                while (true) {
                    print("Tipe dokter (Umum/Spesialis): ")
                    tipe = readlnOrNull()?.trim() ?: ""
                    if (tipe == "Umum" || tipe == "Spesialis") break
                    println("Tipe dokter harus tepat 'Umum' atau 'Spesialis'. (Case sensitive!)")
                }

                if (tipe == "Spesialis") {
                    var spesialisasi: String
                    while (true) {
                        print("Spesialisasi dokter: ")
                        spesialisasi = readlnOrNull()?.trim() ?: ""
                        if (spesialisasi.isNotEmpty()) break
                        println("Spesialisasi tidak boleh kosong.")
                    }
                    ClinicService.tambahDokter(nama, tipe, spesialisasi)
                } else {
                    ClinicService.tambahDokter(nama, tipe)
                }
            }

            "3" -> {
                val pasienList = ClinicService.getPasienList()
                if (pasienList.isEmpty()) {
                    println("Belum ada pasien terdaftar.")
                    continue
                }

                println("\n=== Daftar Pasien ===")
                pasienList.forEachIndexed { index, pasien ->
                    println("${index + 1}. ${pasien.nama} (ID: ${pasien.id})")
                }

                print("Pilih nomor pasien: ")
                val pasienInput = readlnOrNull()?.toIntOrNull()

                if (pasienInput == null || pasienInput !in 1..pasienList.size) {
                    println("Input pasien tidak valid.")
                    continue
                }
                val pasienId = pasienList[pasienInput - 1].id


                val dokterList = ClinicService.getDokterList()
                if (dokterList.isEmpty()) {
                    println("Belum ada dokter terdaftar.")
                    continue
                }

                println("\n=== Daftar Dokter ===")
                dokterList.forEachIndexed { index, dokter ->
                    val tipe = when (dokter) {
                        is DokterSpesialis -> "Spesialis (${dokter.spesialisasi})"
                        is DokterUmum -> "Umum"
                        else -> "Unknown"
                    }
                    println("${index + 1}. ${dokter.nama} (ID: ${dokter.id}), Tipe: $tipe")
                }

                print("Pilih nomor dokter: ")
                val dokterInput = readlnOrNull()?.toIntOrNull()

                if (dokterInput == null || dokterInput !in 1..dokterList.size) {
                    println("Input dokter tidak valid.")
                    continue
                }
                val dokterId = dokterList[dokterInput - 1].id

                print("Jam janji (0-23): ")
                val jamInput = readlnOrNull()
                val jam = jamInput?.toIntOrNull()

                if (jam == null) {
                    println("Jam janji tidak valid.")
                    continue
                }

                println("Pilih metode pembayaran:")
                println("1. Tunai")
                println("2. Asuransi BPJS")
                println("3. Asuransi Swasta")

                print("Masukkan pilihan (1-3): ")
                val pembayaranInput = readlnOrNull()?.toIntOrNull()

                val pembayaran = when(pembayaranInput) {
                    1 -> Tunai()
                    2 -> AsuransiBPJS()
                    3 -> AsuransiSwasta()
                    else -> {
                        println("Pilihan pembayaran tidak valid.")
                        continue
                    }
                }

                ClinicService.buatJanji(pasienId, dokterId, jam, pembayaran)
            }

            "4" ->{
                val dokterList = ClinicService.getDokterList()
                if (dokterList.isEmpty()) {
                    println("Belum ada dokter terdaftar.")
                    continue
                }

                println("\n=== Daftar Dokter ===")
                dokterList.forEachIndexed { index, dokter ->
                    val tipe = when (dokter) {
                        is DokterSpesialis -> "Spesialis (${dokter.spesialisasi})"
                        is DokterUmum -> "Umum"
                        else -> "Unknown"
                    }
                    println("${index + 1}. ${dokter.nama} (ID: ${dokter.id}), Tipe: $tipe")
                }

                print("Masukkan nomor dokter untuk lihat antrian: ")
                val inputDokter = readlnOrNull()?.toIntOrNull()

                if (inputDokter == null || inputDokter !in 1..dokterList.size) {
                    println("Input nomor dokter tidak valid.")
                    continue
                }

                val dokterId = dokterList[inputDokter - 1].id
                ClinicService.lihatAntrian(dokterId)
            }


            "5" -> {
                println()
                ClinicService.laporanHarian()
            }

            "6" -> {
                val pasienList = ClinicService.getPasienList()
                if (pasienList.isEmpty()) {
                    println("Belum ada pasien terdaftar.")
                    continue
                }

                println("\n=== Daftar Pasien ===")
                pasienList.forEachIndexed { index, pasien ->
                    println("${index + 1}. ${pasien.nama} (ID: ${pasien.id})")
                }

                print("Pilih nomor pasien untuk lihat tagihan: ")
                val input = readlnOrNull()?.toIntOrNull()

                if (input == null || input !in 1..pasienList.size) {
                    println("Input tidak valid.")
                    continue
                }

                val pasienId = pasienList[input - 1].id

                val tagihanPasien = ClinicService.getTagihanByPasienId(pasienId)
                if (tagihanPasien.isEmpty()) {
                    println("Pasien belum memiliki tagihan.")
                } else {
                    println("Tagihan untuk pasien ${tagihanPasien[0].janjiTemu.pasien.nama}:")
                    tagihanPasien.forEachIndexed { index, tagihan ->
                        val nominal = if (tagihan.tipePembayaran == "Denda") tagihan.dendaNoShow else tagihan.totalBayar
                        println("${index + 1}. ID Tagihan: ${tagihan.id}, Total Bayar: Rp $nominal, Metode Pembayaran: ${tagihan.tipePembayaran}")
                    }
                }
            }

            "7" -> {
                val pasienList = ClinicService.getPasienList()
                if (pasienList.isEmpty()) {
                    println("Belum ada pasien terdaftar.")
                    continue
                }

                println("\n=== Daftar Pasien ===")
                pasienList.forEachIndexed { index, pasien ->
                    println("${index + 1}. ${pasien.nama} (ID: ${pasien.id})")
                }

                print("Pilih nomor pasien: ")
                val pasienInput = readlnOrNull()?.toIntOrNull()
                if (pasienInput == null || pasienInput !in 1..pasienList.size) {
                    println("Input pasien tidak valid.")
                    continue
                }
                val pasienId = pasienList[pasienInput - 1].id

                val janjiListPasien = ClinicService.getJanjiList()
                    .filter { it.pasien.id == pasienId && it.status == StatusJanji.Aktif }

                if (janjiListPasien.isEmpty()) {
                    println("Pasien tidak memiliki janji aktif.")
                    continue
                }

                println("\n=== Janji Aktif Pasien ===")
                janjiListPasien.forEachIndexed { index, janji ->
                    println("${index + 1}. Dokter: ${janji.dokter.nama}")
                }

                print("Pilih nomor janji yang ingin dibatalkan: ")
                val janjiInput = readlnOrNull()?.toIntOrNull()
                if (janjiInput == null || janjiInput !in 1..janjiListPasien.size) {
                    println("Input janji tidak valid.")
                    continue
                }
                val janji = janjiListPasien[janjiInput - 1]

                ClinicService.batalkanJanji(janji)

            }

            "8" -> {
                println("Keluar dari program.")
                break
            }



            else -> {
                println("Pilihan tidak valid. Silakan coba lagi.")
            }
        }
    }
}
