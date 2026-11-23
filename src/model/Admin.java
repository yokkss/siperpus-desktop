package model;

public class Admin extends Akun {
    public Admin(String nama, String password) {
        super(nama, password);
    }

    @Override
    public boolean admin() { return true; }
}
