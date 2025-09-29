package payment

interface Pembayaran {
    fun hitungBiaya(dasar: Int): Int
}