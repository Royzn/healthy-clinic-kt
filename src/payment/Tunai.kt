package payment

class Tunai: Pembayaran {
    override fun hitungBiaya(dasar: Int): Int = dasar
}