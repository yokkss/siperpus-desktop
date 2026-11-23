package model;

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

    public String isbn() { return isbn; }
    public String title() { return title; }
    public String author() { return author; }
    public String category() { return category; }
    public int stock() { return stock; }

    @Override
    public String toString() { return title; }
}
