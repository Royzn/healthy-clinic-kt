package data_class

class DokterSpesialis(
    override val id: String,
    override val nama: String,
    val spesialisasi: String
): Dokter(id, nama) {
    override val biayaDasar: Int = 200_000
}