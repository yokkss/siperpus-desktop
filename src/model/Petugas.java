package model;

public class Petugas extends Akun {
    public Petugas(String nama, String password) {
        super(nama, password);
    }

    @Override
    public boolean admin() { return false; }
}
