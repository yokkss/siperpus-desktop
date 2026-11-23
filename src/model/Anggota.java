package model;

public class Anggota {
    private String id;
    private String name;
    private String phone;

    public Anggota(String id, String name, String phone) {
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

    @Override
    public String toString() { return id + " · " + name; }
}
