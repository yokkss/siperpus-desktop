package view;

import model.*;
import util.DataDemo;
import javax.swing.*;

class FormMasterData {
    private final FormPerpustakaan form;
    private final DataDemo data;
    private final DialogForm dialog;

    FormMasterData(FormPerpustakaan form) {
        this.form = form;
        data = form.data;
        dialog = form.dialog;
    }

    JPanel halamanBuku() {
        TabelData tabel = new TabelData("Judul", "Penulis", "Kategori", "Total", "Dipinjam", "Tersedia");
        for (Buku buku : data.buku.semua()) {
            tabel.tambahBaris(buku.title(), buku.author(), buku.category(), buku.stock(),
                    data.transaksi.borrowed(buku), data.transaksi.available(buku));
        }
        tabel.tambahPencarian("Cari judul, penulis, atau kategori", 0, 1, 2);
        tabel.tombol.add(dialog.tombol("Detail buku", e -> detailBuku(bukuPilihan(tabel))));
        if (form.adalahAdmin()) {
            tabel.tombol.add(dialog.tombol("Tambah", e -> isiBuku(null)));
            tabel.tombol.add(dialog.tombol("Ubah", e -> isiBuku(bukuPilihan(tabel))));
            tabel.tombol.add(dialog.tombol("Hapus", e -> hapusBuku(bukuPilihan(tabel))));
        }
        return tabel;
    }

    private Buku bukuPilihan(TabelData tabel) {
        return data.buku.semua().get(tabel.indeksPilihan());
    }

    private void detailBuku(Buku buku) {
        int tersedia = data.transaksi.available(buku);
        String status = tersedia > 0 ? "Tersedia" : "Sedang dipinjam";
        dialog.info(buku.title() + "\n" + buku.author() + " · " + buku.category()
                + "\nISBN " + buku.isbn() + "\n\nTotal " + buku.stock()
                + "  ·  Dipinjam " + data.transaksi.borrowed(buku)
                + "  ·  Tersedia " + tersedia + "\nStatus: " + status);
    }

    private void isiBuku(Buku buku) {
        JTextField isbn = new JTextField(24);
        JTextField judul = new JTextField();
        JTextField penulis = new JTextField();
        JComboBox<String> kategori = new JComboBox<>();
        for (String nama : data.kategori.semua()) kategori.addItem(nama);
        JSpinner stok = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
        if (buku != null) {
            isbn.setText(buku.isbn());
            judul.setText(buku.title());
            penulis.setText(buku.author());
            kategori.setSelectedItem(buku.category());
            stok.setValue(buku.stock());
        }
        String[] label = {"ISBN", "Judul", "Penulis", "Kategori", "Total eksemplar"};
        if (!dialog.isi("Data buku", label, isbn, judul, penulis, kategori, stok)) return;

        String nomor = dialog.wajib(isbn);
        String nama = dialog.wajib(judul);
        String pengarang = dialog.wajib(penulis);
        String jenis = (String) kategori.getSelectedItem();
        int total = (Integer) stok.getValue();
        if (jenis == null) throw new IllegalArgumentException("Tambahkan kategori terlebih dahulu.");
        if (data.buku.isbnTerpakai(nomor, buku)) throw new IllegalArgumentException("ISBN sudah terdaftar.");
        if (buku == null) {
            data.buku.tambah(new Buku(nomor, nama, pengarang, jenis, total));
        } else {
            if (total < data.transaksi.borrowed(buku)) {
                throw new IllegalArgumentException("Total di bawah jumlah yang sedang dipinjam.");
            }
            buku.ubah(nomor, nama, pengarang, jenis, total);
        }
        form.muatUlang();
    }

    private void hapusBuku(Buku buku) {
        for (Peminjaman pinjaman : data.pinjaman.semua()) {
            for (DetailPeminjaman detail : pinjaman.items) {
                if (detail.book == buku) throw new IllegalArgumentException("Buku sudah tercatat dalam transaksi.");
            }
        }
        if (dialog.konfirmasi("Hapus " + buku + "?")) {
            data.buku.hapus(buku);
            form.muatUlang();
        }
    }

    JPanel halamanAnggota() {
        TabelData tabel = new TabelData("Nomor anggota", "Nama", "Kontak", "Buku aktif");
        for (Anggota anggota : data.anggota.semua()) {
            tabel.tambahBaris(anggota.id(), anggota.name(), anggota.phone(), data.transaksi.active(anggota));
        }
        tabel.tambahPencarian("Cari anggota", 0, 1, 2);
        if (form.adalahAdmin()) {
            tabel.tombol.add(dialog.tombol("Tambah", e -> isiAnggota(null)));
            tabel.tombol.add(dialog.tombol("Ubah", e -> isiAnggota(anggotaPilihan(tabel))));
            tabel.tombol.add(dialog.tombol("Hapus", e -> hapusAnggota(anggotaPilihan(tabel))));
        }
        return tabel;
    }

    private Anggota anggotaPilihan(TabelData tabel) {
        return data.anggota.semua().get(tabel.indeksPilihan());
    }

    private void isiAnggota(Anggota anggota) {
        JTextField nomor = new JTextField(24);
        JTextField nama = new JTextField();
        JTextField kontak = new JTextField();
        if (anggota != null) {
            nomor.setText(anggota.id());
            nama.setText(anggota.name());
            kontak.setText(anggota.phone());
        }
        String[] label = {"Nomor anggota", "Nama", "Kontak"};
        if (!dialog.isi("Data anggota", label, nomor, nama, kontak)) return;
        String id = dialog.wajib(nomor);
        String namaAnggota = dialog.wajib(nama);
        String telepon = dialog.wajib(kontak);
        for (Anggota lain : data.anggota.semua()) {
            if (lain != anggota && lain.id().equalsIgnoreCase(id)) {
                throw new IllegalArgumentException("Nomor sudah digunakan.");
            }
        }
        if (anggota == null) data.anggota.tambah(new Anggota(id, namaAnggota, telepon));
        else anggota.ubah(id, namaAnggota, telepon);
        form.muatUlang();
    }

    private void hapusAnggota(Anggota anggota) {
        for (Peminjaman pinjaman : data.pinjaman.semua()) {
            if (pinjaman.member == anggota) throw new IllegalArgumentException("Anggota memiliki riwayat peminjaman.");
        }
        if (dialog.konfirmasi("Hapus " + anggota + "?")) {
            data.anggota.hapus(anggota);
            form.muatUlang();
        }
    }

    JPanel halamanKategori() {
        TabelData tabel = new TabelData("Kategori", "Jumlah judul");
        for (String nama : data.kategori.semua()) tabel.tambahBaris(nama, jumlahJudul(nama));
        tabel.tambahPencarian("Cari kategori", 0);
        tabel.tombol.add(dialog.tombol("Tambah", e -> isiKategori(null)));
        tabel.tombol.add(dialog.tombol("Ubah", e -> isiKategori(kategoriPilihan(tabel))));
        tabel.tombol.add(dialog.tombol("Hapus", e -> hapusKategori(kategoriPilihan(tabel))));
        return tabel;
    }

    private String kategoriPilihan(TabelData tabel) {
        return data.kategori.semua().get(tabel.indeksPilihan());
    }

    private int jumlahJudul(String kategori) {
        int jumlah = 0;
        for (Buku buku : data.buku.semua()) {
            if (buku.category().equals(kategori)) jumlah++;
        }
        return jumlah;
    }

    private void isiKategori(String lama) {
        JTextField isian = new JTextField(lama, 24);
        if (!dialog.isi("Kategori", new String[]{"Nama kategori"}, isian)) return;
        String baru = dialog.wajib(isian);
        for (String kategori : data.kategori.semua()) {
            if (!kategori.equals(lama) && kategori.equalsIgnoreCase(baru)) {
                throw new IllegalArgumentException("Kategori sudah ada.");
            }
        }
        if (lama == null) {
            data.kategori.tambah(baru);
        } else {
            data.kategori.ganti(lama, baru);
            for (Buku buku : data.buku.semua()) {
                if (buku.category().equals(lama)) buku.ubahKategori(baru);
            }
        }
        form.muatUlang();
    }

    private void hapusKategori(String kategori) {
        if (jumlahJudul(kategori) > 0) throw new IllegalArgumentException("Kategori masih dipakai buku.");
        if (dialog.konfirmasi("Hapus " + kategori + "?")) {
            data.kategori.hapus(kategori);
            form.muatUlang();
        }
    }

    JPanel halamanAkun() {
        TabelData tabel = new TabelData("Username", "Peran");
        for (Akun akun : data.akun.semua()) tabel.tambahBaris(akun.name(), akun.label());
        tabel.tombol.add(dialog.tombol("Tambah akun", e -> tambahAkun()));
        tabel.tombol.add(dialog.tombol("Ubah peran", e -> ubahPeran(akunPilihan(tabel))));
        tabel.tombol.add(dialog.tombol("Hapus akun", e -> hapusAkun(akunPilihan(tabel))));
        return tabel;
    }

    private Akun akunPilihan(TabelData tabel) {
        Akun akun = data.akun.semua().get(tabel.indeksPilihan());
        if (akun == form.pengguna) throw new IllegalArgumentException("Akun ini sedang digunakan.");
        return akun;
    }

    private void tambahAkun() {
        JTextField nama = new JTextField(24);
        JPasswordField password = new JPasswordField();
        JComboBox<String> peran = new JComboBox<>(new String[]{"Petugas", "Admin"});
        if (!dialog.isi("Tambah akun demo", new String[]{"Username", "Password demo", "Peran"}, nama, password, peran)) return;
        String username = dialog.wajib(nama);
        String sandi = new String(password.getPassword());
        if (sandi.isBlank()) throw new IllegalArgumentException("Isi password demo.");
        for (Akun akun : data.akun.semua()) {
            if (akun.name().equalsIgnoreCase(username)) throw new IllegalArgumentException("Username sudah digunakan.");
        }
        if (peran.getSelectedIndex() == 0) data.akun.tambah(new Petugas(username, sandi));
        else data.akun.tambah(new Admin(username, sandi));
        form.muatUlang();
    }

    private void ubahPeran(Akun akun) {
        Akun baru = akun.gantiPeran();
        if (dialog.konfirmasi("Ubah peran " + akun.name() + " menjadi " + baru.label() + "?")) {
            data.akun.ganti(akun, baru);
            form.muatUlang();
        }
    }

    private void hapusAkun(Akun akun) {
        if (dialog.konfirmasi("Hapus akun " + akun.name() + "?")) {
            data.akun.hapus(akun);
            form.muatUlang();
        }
    }
}
