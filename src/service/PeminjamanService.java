package service;

import dao.Dao;
import model.*;
import java.time.LocalDate;
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
        // Telusuri detail semua transaksi, lalu hitung eksemplar judul ini yang tanggal kembalinya masih kosong.
        int jumlah = 0;
        for (Peminjaman transaksi : pinjaman.semua()) {
            for (DetailPeminjaman detail : transaksi.items) {
                if (detail.book == buku && detail.returned() == null) jumlah++;
            }
        }
        return jumlah;
    }

    public int available(Buku buku) { return buku.stock() - borrowed(buku); }

    public long active(Anggota anggota) {
        long jumlah = 0;
        for (Peminjaman transaksi : pinjaman.semua()) {
            if (transaksi.member != anggota) continue;
            for (DetailPeminjaman detail : transaksi.items) {
                if (detail.returned() == null) jumlah++;
            }
        }
        return jumlah;
    }

    // Contoh overloading: versi ini memakai tanggal hari ini. Data demo mengisi tanggal sendiri.
    public Peminjaman borrow(Anggota peminjam, Buku bukuPertama, Buku bukuKedua, Buku bukuKetiga) {
        return borrow(peminjam, bukuPertama, bukuKedua, bukuKetiga, LocalDate.now());
    }

    public Peminjaman borrow(Anggota peminjam, Buku bukuPertama, Buku bukuKedua, Buku bukuKetiga,
                             LocalDate tanggal) {
        require(anggota.semua().contains(peminjam), "Pilih anggota yang terdaftar.");
        require(bukuPertama != null, "Tambahkan minimal satu buku.");
        int jumlahPilihan = 1;
        if (bukuKedua != null) jumlahPilihan++;
        if (bukuKetiga != null) jumlahPilihan++;

        // Jika sudah memegang 2 buku, anggota masih boleh menambah 1 buku.
        require(active(peminjam) + jumlahPilihan <= 3, "Batas anggota adalah 3 buku aktif.");

        periksaStok(bukuPertama, bukuKedua, bukuKetiga);
        periksaStok(bukuKedua, bukuPertama, bukuKetiga);
        periksaStok(bukuKetiga, bukuPertama, bukuKedua);

        Peminjaman baru = new Peminjaman(pinjaman.semua().size() + 1, peminjam, tanggal,
                bukuPertama, bukuKedua, bukuKetiga);
        pinjaman.tambah(baru);
        return baru;
    }

    private void periksaStok(Buku pilihan, Buku pilihanLainA, Buku pilihanLainB) {
        if (pilihan == null) return;

        // Judul yang dipilih dua kali membutuhkan dua eksemplar.
        int jumlah = 1;
        if (pilihan == pilihanLainA) jumlah++;
        if (pilihan == pilihanLainB) jumlah++;
        require(buku.semua().contains(pilihan) && available(pilihan) >= jumlah,
                "Stok tidak mencukupi untuk " + pilihan + ".");
    }

    public void giveBack(Peminjaman transaksi, DetailPeminjaman item, LocalDate tanggal) {
        require(pinjaman.semua().contains(transaksi) && transaksi.items.contains(item), "Detail transaksi tidak ditemukan.");
        require(!tanggal.isBefore(transaksi.start) && !tanggal.isAfter(LocalDate.now()),
                "Tanggal kembali harus di antara tanggal pinjam dan hari ini.");
        // Catat tanggal pada detail yang dipilih. Setelah itu borrowed() tidak lagi menghitung eksemplar ini.
        item.kembalikan(tanggal);
    }
}
