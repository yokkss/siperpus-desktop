# SIPERPUS Desktop dengan JavaFX

Dari PowerShell di folder ini pada Windows 64-bit, jalankan dengan JDK 24 atau lebih baru.

```powershell
java Siperpus.java
```

SIPERPUS akan terbuka di jendela desktop. Program mengunduh JavaFX ke folder `lib/javafx` jika diperlukan, jadi unduhan pertama memerlukan koneksi internet. Aplikasi memakai data demo di memori.

Buka `src/SiperpusDesktop.java` dan baca method `start()` untuk melihat cara aplikasi membuka jendela. `src/MenuTransaksi.java` menangani pilihan buku, lalu `src/service/PeminjamanService.java` memeriksa stok dan batas pinjam.

Katalog bisa dibuka tanpa login. Akun demo Admin memakai `admin` dan Petugas memakai `petugas` sebagai username sekaligus password.

Untuk mencoba pengembalian satu buku, masuk sebagai Petugas, pilih menu **Pengembalian**, lalu pilih salah satu buku dari transaksi 1. Dengan tanggal kembali hari ini, buku contoh itu memiliki denda Rp30.000. Buku lainnya tetap dipinjam sampai dikembalikan.
