# SIPERPUS Desktop

Jalankan dari folder proyek dengan JDK 22 atau lebih baru (diuji dengan JDK 26).

```powershell
java src/Siperpus.java
```

Katalog terbuka untuk pengunjung. Klik **Login staf** untuk memakai akun demo `admin / admin` atau `petugas / petugas`. Data disimpan di memori selama jendela aplikasi aktif.

| Lokasi | Isi |
|---|---|
| `src/model/` | Buku, anggota, akun dan peran, peminjaman, serta detail tiap buku. |
| `src/dao/` | Kontrak DAO, penyimpanan di memori, pencarian ISBN, dan kesalahan akses data. |
| `src/service/` | Aturan transaksi, cek stok dan batas buku aktif, serta proses pengembalian. |
| `src/view/` | Form Swing untuk pengunjung, petugas, dan admin. |
| `src/util/` | Inisialisasi data contoh dan validasi. |
| `test/view/` | Pengujian transaksi dan halaman Swing. |
| `lib/`, `sql/` | Folder cadangan untuk driver dan skrip database pada tahap berikutnya. |
