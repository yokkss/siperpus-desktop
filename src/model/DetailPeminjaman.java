package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DetailPeminjaman {
    public final Buku book;
    // Tanggal kembali bernilai null selama eksemplar ini masih dipinjam.
    private LocalDate returned;

    DetailPeminjaman(Buku book) { this.book = book; }
    public LocalDate returned() { return returned; }

    public void kembalikan(LocalDate tanggal) {
        if (returned != null) throw new IllegalArgumentException("Buku ini sudah dikembalikan.");
        returned = tanggal;
    }

    public long fine(Peminjaman pinjaman, LocalDate tanggal) {
        long hari = ChronoUnit.DAYS.between(pinjaman.due(), tanggal);
        // Kembali lebih awal atau tepat waktu dikenai Rp0. Telat 3 hari dikenai Rp30.000 untuk buku ini.
        return Math.max(0, hari) * 10_000L;
    }
}
