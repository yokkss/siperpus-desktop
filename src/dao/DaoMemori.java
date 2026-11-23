package dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DaoMemori<T> implements Dao<T> {
    // Setiap objek DaoMemori punya daftar sendiri. Isinya tersimpan selama aplikasi berjalan.
    private final List<T> data = new ArrayList<>();

    public List<T> semua() {
        // Pemanggil bisa membaca daftar ini. Untuk menambah isinya, gunakan tambah().
        return Collections.unmodifiableList(data);
    }

    public void tambah(T nilai) {
        data.add(nilai);
    }

    public void ganti(T lama, T baru) {
        // Cari posisi data lama agar penggantinya menempati posisi yang sama.
        int index = data.indexOf(lama);
        if (index < 0) throw new DataAksesException("Data yang akan diubah tidak ditemukan.");
        data.set(index, baru);
    }

    public void hapus(T nilai) {
        if (!data.remove(nilai)) throw new DataAksesException("Data yang akan dihapus tidak ditemukan.");
    }
}
