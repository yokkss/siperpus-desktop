package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Satu transaksi menghubungkan satu anggota dengan semua buku yang dipinjam bersamaan.
public class Peminjaman {
    public static final int DURASI_HARI = 2;
    public final int id;
    public final Anggota member;
    public final LocalDate start;
    public final List<DetailPeminjaman> items;

    public Peminjaman(int id, Anggota member, LocalDate start,
                       Buku bukuPertama, Buku bukuKedua, Buku bukuKetiga) {
        this.id = id;
        this.member = member;
        this.start = start;
        // Buat satu DetailPeminjaman untuk tiap buku agar masing-masing bisa dikembalikan sendiri.
        List<DetailPeminjaman> detailBuku = new ArrayList<>();
        detailBuku.add(new DetailPeminjaman(bukuPertama));
        if (bukuKedua != null) detailBuku.add(new DetailPeminjaman(bukuKedua));
        if (bukuKetiga != null) detailBuku.add(new DetailPeminjaman(bukuKetiga));
        this.items = List.copyOf(detailBuku);
    }

    // Misalnya, pinjam tanggal 10 berarti jatuh tempo tanggal 12.
    public LocalDate due() { return start.plusDays(DURASI_HARI); }
}
