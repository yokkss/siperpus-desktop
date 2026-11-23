package dao;

import java.util.List;

public interface Dao<T> {
    List<T> semua();
    void tambah(T data);
    void ganti(T lama, T baru);
    void hapus(T data);
}
