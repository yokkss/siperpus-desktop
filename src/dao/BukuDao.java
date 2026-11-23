package dao;

import model.Buku;

public class BukuDao extends DaoMemori<Buku> {
    public boolean isbnTerpakai(String isbn, Buku sedangDiubah) {
        return semua().stream().anyMatch(b -> b != sedangDiubah && b.isbn().equals(isbn));
    }
}
