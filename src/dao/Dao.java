package dao;

import java.util.List;

// Interface ini menentukan operasi yang harus disediakan oleh kelas penyimpan data.
// T mewakili tipe data. Pada Dao<Buku>, misalnya, tambah menerima satu objek Buku.
public interface Dao<T> {
    List<T> semua();
    void tambah(T data);
    void ganti(T lama, T baru);
    void hapus(T data);
}
