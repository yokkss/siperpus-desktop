package model;

public class Admin extends Akun {
    public Admin(String nama, String password) {
        // super(...) memanggil constructor Akun untuk mengisi nama dan password admin.
        super(nama, password);
    }

    // Form memanggil admin() untuk menentukan apakah menu Kelola pengguna ditampilkan.
    @Override
    public boolean admin() { return true; }
}
