package service;

import dao.Dao;
import model.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import static util.Validasi.require;

public class PeminjamanService {
    private final Dao<Buku> buku;
    private final Dao<Anggota> anggota;
    private final Dao<Peminjaman> pinjaman;

    public PeminjamanService(Dao<Buku> buku, Dao<Anggota> anggota, Dao<Peminjaman> pinjaman) {
        this.buku = buku;
        this.anggota = anggota;
        this.pinjaman = pinjaman;
    }

    public int borrowed(Buku buku) {
        return (int) pinjaman.semua().stream().flatMap(p -> p.items.stream())
                .filter(i -> i.book == buku && i.returned() == null).count();
    }

    public int available(Buku buku) { return buku.stock() - borrowed(buku); }

    public long active(Anggota anggota) {
        return pinjaman.semua().stream().filter(p -> p.member == anggota)
                .flatMap(p -> p.items.stream()).filter(i -> i.returned() == null).count();
    }

    public Peminjaman borrow(Anggota peminjam, List<Buku> pilihan) {
        return borrow(peminjam, pilihan, LocalDate.now());
    }

    public Peminjaman borrow(Anggota peminjam, List<Buku> pilihan, LocalDate tanggal) {
        require(anggota.semua().contains(peminjam), "Pilih anggota yang terdaftar.");
        require(!pilihan.isEmpty(), "Tambahkan minimal satu buku.");
        require(active(peminjam) + pilihan.size() <= 3, "Batas anggota adalah 3 buku aktif.");
        for (Buku b : pilihan) {
            require(buku.semua().contains(b) && available(b) >= Collections.frequency(pilihan, b),
                    "Stok tidak mencukupi untuk " + b + ".");
        }
        Peminjaman baru = new Peminjaman(pinjaman.semua().size() + 1, peminjam, tanggal, pilihan);
        pinjaman.tambah(baru);
        return baru;
    }

    public void giveBack(Peminjaman transaksi, DetailPeminjaman item, LocalDate tanggal) {
        require(pinjaman.semua().contains(transaksi) && transaksi.items.contains(item), "Detail transaksi tidak ditemukan.");
        require(!tanggal.isBefore(transaksi.start) && !tanggal.isAfter(LocalDate.now()),
                "Tanggal kembali harus di antara tanggal pinjam dan hari ini.");
        item.kembalikan(tanggal);
    }
}
