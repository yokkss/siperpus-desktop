package model;

// Satu objek Anggota mewakili peminjam yang dipilih petugas saat membuat transaksi.
public class Anggota {
    private String id;
    private String name;
    private String phone;

    public Anggota(String id, String name, String phone) {
        // Constructor memakai method ubah() yang sama dengan form edit anggota.
        ubah(id, name, phone);
    }

    public void ubah(String id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String phone() { return phone; }

    // toString() menentukan teks yang ditampilkan saat objek anggota dicetak.
    @Override
    public String toString() { return id + " · " + name; }
}
