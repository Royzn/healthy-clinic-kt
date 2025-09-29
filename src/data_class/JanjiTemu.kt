package data_class

class JanjiTemu(
    val pasien: Pasien,
    val dokter: Dokter,
    val jam: Int,
    var status: StatusJanji = StatusJanji.Aktif
)