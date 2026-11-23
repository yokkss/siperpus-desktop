package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DetailPeminjaman {
    public final Buku book;
    private LocalDate returned;

    DetailPeminjaman(Buku book) { this.book = book; }
    public LocalDate returned() { return returned; }

    public void kembalikan(LocalDate tanggal) {
        if (returned != null) throw new IllegalArgumentException("Buku ini sudah dikembalikan.");
        returned = tanggal;
    }

    public long fine(Peminjaman pinjaman, LocalDate tanggal) {
        long hari = ChronoUnit.DAYS.between(pinjaman.due(), tanggal);
        return Math.max(0, hari) * 10_000L;
    }
}
