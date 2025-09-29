package data_class

class Tagihan(
    val id: String,
    val janjiTemu: JanjiTemu,
    val totalBayar: Int,
    val tipePembayaran: String,
    var dendaNoShow: Int = 0
)