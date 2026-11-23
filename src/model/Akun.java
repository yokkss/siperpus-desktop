package model;

public abstract class Akun {
    private final String name;
    private final String password;

    protected Akun(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String name() { return name; }
    public boolean cocok(String masukan) { return password.equals(masukan); }
    public abstract boolean admin();
    public String label() { return admin() ? "Admin" : "Petugas"; }

    public Akun gantiPeran() {
        return admin() ? new Petugas(name, password) : new Admin(name, password);
    }
}
