package data_class

class DokterUmum(
    override val id: String,
    override val nama: String
): Dokter(id, nama) {
    override val biayaDasar: Int = 100_000
}