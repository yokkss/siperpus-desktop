package model;

// Petugas mewarisi cara menyimpan nama dan memeriksa password dari Akun.
public class Petugas extends Akun {
    public Petugas(String nama, String password) {
        super(nama, password);
    }

    // Saat objeknya Petugas, pemanggilan admin() menghasilkan false.
    @Override
    public boolean admin() { return false; }
}
