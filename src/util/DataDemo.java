package util;

import dao.*;
import model.*;
import service.PeminjamanService;
import java.time.LocalDate;

public class DataDemo {
    public final BukuDao buku = new BukuDao();
    public final Dao<Anggota> anggota = new DaoMemori<>();
    public final Dao<String> kategori = new DaoMemori<>();
    public final Dao<Akun> akun = new DaoMemori<>();
    public final Dao<Peminjaman> pinjaman = new DaoMemori<>();
    // Menu dan service memakai daftar yang sama, jadi transaksi baru langsung terlihat di katalog.
    public final PeminjamanService transaksi = new PeminjamanService(buku, anggota, pinjaman);

    public DataDemo() {
        kategori.tambah("Teknologi");
        kategori.tambah("Sastra");
        kategori.tambah("Sejarah");
        akun.tambah(new Admin("admin", "admin"));
        akun.tambah(new Petugas("petugas", "petugas"));
        // new Buku(...) membuat objek buku dengan stok 5. buku.tambah(...) menyimpannya ke daftar.
        buku.tambah(new Buku("9786020000019","Dasar Pemrograman Java","R. Pratama","Teknologi",5));
        buku.tambah(new Buku("9786020000026","Struktur Data","Dewi Lestari Putri","Teknologi",3));
        buku.tambah(new Buku("9786020000033","Basis Data","Ayu Wulandari","Teknologi",4));
        buku.tambah(new Buku("9786020000040","Kumpulan Cerita Nusantara","Tim Pustaka","Sastra",2));
        buku.tambah(new Buku("9786020000057","Sejarah Kepulauan Indonesia","D. Santoso","Sejarah",1));
        anggota.tambah(new Anggota("A001","Nadia Putri","081200000001"));
        anggota.tambah(new Anggota("A002","Bima Saputra","081200000002"));
        // Contoh untuk latihan pengembalian: anggota pertama meminjam dua buku lima hari lalu.
        transaksi.borrow(anggota.semua().get(0), buku.semua().get(0), buku.semua().get(4), null,
                LocalDate.now().minusDays(5));
    }
}
