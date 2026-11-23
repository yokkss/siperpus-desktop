package model;

// Satu objek Buku mewakili satu judul. stock menyimpan total eksemplarnya.
public class Buku {
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int stock;

    public Buku(String isbn, String title, String author, String category, int stock) {
        ubah(isbn, title, author, category, stock);
    }

    public void ubah(String isbn, String title, String author, String category, int stock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.stock = stock;
    }

    public void ubahKategori(String category) {
        this.category = category;
    }

    // Method berikut memberi form akses baca ke atribut yang dideklarasikan private.
    public String isbn() { return isbn; }
    public String title() { return title; }
    public String author() { return author; }
    public String category() { return category; }
    public int stock() { return stock; }

    // Daftar pilihan mencetak objek Buku, lalu toString() menampilkan judulnya.
    @Override
    public String toString() { return title; }
}
