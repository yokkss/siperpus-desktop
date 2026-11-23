package model;

// Admin dan Petugas memakai atribut dan method bersama dari kelas induk ini.
public abstract class Akun {
    private final String name;
    private final String password;

    protected Akun(String name, String password) {
        // this.name adalah atribut objek. name di sebelah kanan adalah parameter constructor.
        this.name = name;
        this.password = password;
    }

    public String name() { return name; }
    public boolean cocok(String masukan) { return password.equals(masukan); }
    // Setiap kelas turunan mengisi sendiri jawaban untuk admin().
    public abstract boolean admin();
    public String label() { return admin() ? "Admin" : "Petugas"; }

    // Buat akun dengan peran sebaliknya, memakai nama dan password akun saat ini.
    public Akun gantiPeran() {
        return admin() ? new Petugas(name, password) : new Admin(name, password);
    }
}
