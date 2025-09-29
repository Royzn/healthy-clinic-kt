package data_class

class Pasien(
    val id: String,       // Unique identifier (e.g., patient number)
    val nama: String,     // Patient name
    val alamat: String?,  // Optional address
    val noTelepon: String?, // Optional phone number
    val kategori: String
)