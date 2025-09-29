package payment

class AsuransiSwasta: Pembayaran {
    override fun hitungBiaya(dasar: Int): Int {
        val potonganDiskon = dasar * 70 / 100
        return dasar - potonganDiskon
    }
}