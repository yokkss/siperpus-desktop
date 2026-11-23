package dao;

import model.Buku;

// BukuDao mewarisi semua(), tambah(), ganti(), dan hapus() untuk objek Buku.
public class BukuDao extends DaoMemori<Buku> {
    public boolean isbnTerpakai(String isbn, Buku sedangDiubah) {
        // Saat mengedit buku, ISBN miliknya sendiri boleh tetap dipakai.
        // Hasilnya true jika ada buku lain dengan ISBN yang sama.
        for (Buku buku : semua()) {
            if (buku != sedangDiubah && buku.isbn().equals(isbn)) {
                return true;
            }
        }
        return false;
    }
}
