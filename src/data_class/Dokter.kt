package data_class

open class Dokter(
    open val id: String,
    open val nama: String,
) {
    open val biayaDasar: Int = 100_000
}