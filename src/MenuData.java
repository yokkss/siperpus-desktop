import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Admin;
import model.Akun;
import model.Anggota;
import model.Buku;
import model.DetailPeminjaman;
import model.Peminjaman;
import model.Petugas;
import util.DataDemo;

import java.util.Locale;

class MenuData {
    private final SiperpusDesktop aplikasi;
    private final DataDemo data;

    MenuData(SiperpusDesktop aplikasi) {
        this.aplikasi = aplikasi;
        data = aplikasi.data;
    }

    void simpanBuku(Buku buku) {
        TextField isbn = new TextField();
        TextField judul = new TextField();
        TextField penulis = new TextField();
        TextField stok = new TextField("1");
        ComboBox<String> kategori = aplikasi.kotakPilihan(data.kategori.semua());
        if (buku != null) {
            isbn.setText(buku.isbn());
            judul.setText(buku.title());
            penulis.setText(buku.author());
            stok.setText(String.valueOf(buku.stock()));
            kategori.setValue(buku.category());
        }
        if (!aplikasi.formulir("Data buku", null, new String[]{"ISBN", "Judul", "Penulis", "Kategori", "Total eksemplar"},
                isbn, judul, penulis, kategori, stok)) return;

        String kode = aplikasi.wajib(isbn);
        String nama = aplikasi.wajib(judul);
        String pengarang = aplikasi.wajib(penulis);
        String jenis = kategori.getValue();
        if (jenis == null) throw new IllegalArgumentException("Pilih kategori buku.");
        if (data.buku.isbnTerpakai(kode, buku)) throw new IllegalArgumentException("ISBN sudah terdaftar.");

        int jumlah;
        try {
            jumlah = Integer.parseInt(stok.getText().strip());
        } catch (NumberFormatException masalah) {
            throw new IllegalArgumentException("Isi total eksemplar dengan angka bulat.");
        }
        int minimum = buku == null ? 0 : data.transaksi.borrowed(buku);
        if (jumlah < minimum) throw new IllegalArgumentException("Total eksemplar minimal " + minimum + ".");

        if (buku == null) data.buku.tambah(new Buku(kode, nama, pengarang, jenis, jumlah));
        else buku.ubah(kode, nama, pengarang, jenis, jumlah);
        aplikasi.katalog();
    }

    void hapusBuku(Buku buku) {
        for (Peminjaman pinjaman : data.pinjaman.semua()) {
            for (DetailPeminjaman detail : pinjaman.items) {
                if (detail.book == buku) throw new IllegalArgumentException("Buku sudah tercatat dalam transaksi.");
            }
        }
        if (aplikasi.konfirmasi("Hapus buku " + buku.title() + "?")) {
            data.buku.hapus(buku);
            aplikasi.katalog();
        }
    }

    void anggota() {
        TextField cari = new TextField();
        cari.setPromptText("Cari nama atau nomor anggota");
        ListView<Anggota> daftar = new ListView<>();
        isiAnggota(daftar, "");
        cari.textProperty().addListener((nilai, lama, baru) -> isiAnggota(daftar, baru));

        HBox tombol = new HBox(8, aplikasi.tombol("Detail anggota", () -> {
            Anggota anggota = aplikasi.pilihan(daftar);
            aplikasi.tampilkan("Detail anggota", anggota + "\nKontak: " + anggota.phone()
                    + "\nBuku aktif: " + data.transaksi.active(anggota));
        }));
        if (aplikasi.admin()) {
            tombol.getChildren().addAll(aplikasi.tombol("Tambah anggota", () -> simpanAnggota(null)),
                    aplikasi.tombol("Ubah", () -> simpanAnggota(aplikasi.pilihan(daftar))),
                    aplikasi.tombol("Hapus", () -> hapusAnggota(aplikasi.pilihan(daftar))));
        }
        VBox isi = new VBox(12, cari, daftar, tombol);
        VBox.setVgrow(daftar, Priority.ALWAYS);
        aplikasi.halaman("Data anggota", isi);
    }

    private void isiAnggota(ListView<Anggota> daftar, String pencarian) {
        String kata = pencarian.strip().toLowerCase(Locale.ROOT);
        daftar.getItems().clear();
        for (Anggota anggota : data.anggota.semua()) {
            if (anggota.toString().toLowerCase(Locale.ROOT).contains(kata)) {
                daftar.getItems().add(anggota);
            }
        }
    }

    private void simpanAnggota(Anggota anggota) {
        TextField nomor = new TextField(anggota == null ? "" : anggota.id());
        TextField nama = new TextField(anggota == null ? "" : anggota.name());
        TextField kontak = new TextField(anggota == null ? "" : anggota.phone());
        if (!aplikasi.formulir("Data anggota", null, new String[]{"Nomor", "Nama", "Kontak"}, nomor, nama, kontak)) return;
        String id = aplikasi.wajib(nomor);
        String namaAnggota = aplikasi.wajib(nama);
        String telepon = aplikasi.wajib(kontak);
        for (Anggota lain : data.anggota.semua()) {
            if (lain != anggota && lain.id().equalsIgnoreCase(id)) {
                throw new IllegalArgumentException("Nomor anggota sudah digunakan.");
            }
        }
        if (anggota == null) data.anggota.tambah(new Anggota(id, namaAnggota, telepon));
        else anggota.ubah(id, namaAnggota, telepon);
        anggota();
    }

    private void hapusAnggota(Anggota anggota) {
        for (Peminjaman pinjaman : data.pinjaman.semua()) {
            if (pinjaman.member == anggota) throw new IllegalArgumentException("Anggota punya riwayat peminjaman.");
        }
        if (aplikasi.konfirmasi("Hapus anggota " + anggota + "?")) {
            data.anggota.hapus(anggota);
            anggota();
        }
    }

    void kategori() {
        ListView<String> daftar = new ListView<>();
        daftar.getItems().addAll(data.kategori.semua());
        HBox tombol = new HBox(8, aplikasi.tombol("Tambah kategori", () -> simpanKategori(null)),
                aplikasi.tombol("Ubah", () -> simpanKategori(aplikasi.pilihan(daftar))),
                aplikasi.tombol("Hapus", () -> hapusKategori(aplikasi.pilihan(daftar))));
        VBox isi = new VBox(12, daftar, tombol);
        VBox.setVgrow(daftar, Priority.ALWAYS);
        aplikasi.halaman("Kategori", isi);
    }

    private void simpanKategori(String lama) {
        TextField nama = new TextField(lama == null ? "" : lama);
        if (!aplikasi.formulir("Kategori", null, new String[]{"Nama kategori"}, nama)) return;
        String baru = aplikasi.wajib(nama);
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
        kategori();
    }

    private void hapusKategori(String pilihan) {
        for (Buku buku : data.buku.semua()) {
            if (buku.category().equals(pilihan)) throw new IllegalArgumentException("Kategori masih dipakai buku.");
        }
        if (aplikasi.konfirmasi("Hapus kategori " + pilihan + "?")) {
            data.kategori.hapus(pilihan);
            kategori();
        }
    }

    void akun() {
        ListView<Akun> daftar = new ListView<>();
        daftar.getItems().addAll(data.akun.semua());
        daftar.setCellFactory(tabel -> new ListCell<>() {
            @Override
            protected void updateItem(Akun akun, boolean kosong) {
                super.updateItem(akun, kosong);
                setText(kosong || akun == null ? null : akun.name() + " · " + akun.label());
            }
        });
        HBox tombol = new HBox(8, aplikasi.tombol("Tambah akun", this::tambahAkun),
                aplikasi.tombol("Ubah peran", () -> aturAkun(aplikasi.pilihan(daftar), true)),
                aplikasi.tombol("Hapus", () -> aturAkun(aplikasi.pilihan(daftar), false)));
        VBox isi = new VBox(12, daftar, tombol);
        VBox.setVgrow(daftar, Priority.ALWAYS);
        aplikasi.halaman("Kelola pengguna", isi);
    }

    private void tambahAkun() {
        TextField nama = new TextField();
        PasswordField sandi = new PasswordField();
        ComboBox<String> peran = new ComboBox<>();
        peran.getItems().addAll("Petugas", "Admin");
        peran.setValue("Petugas");
        if (!aplikasi.formulir("Tambah akun demo", null, new String[]{"Username", "Password", "Peran"}, nama, sandi, peran)) return;
        String username = aplikasi.wajib(nama);
        String password = aplikasi.wajib(sandi);
        for (Akun akun : data.akun.semua()) {
            if (akun.name().equalsIgnoreCase(username)) throw new IllegalArgumentException("Username sudah digunakan.");
        }
        if ("Admin".equals(peran.getValue())) data.akun.tambah(new Admin(username, password));
        else data.akun.tambah(new Petugas(username, password));
        akun();
    }

    private void aturAkun(Akun akun, boolean ubah) {
        if (aplikasi.akunSendiri(akun)) throw new IllegalArgumentException("Akun ini sedang digunakan.");
        if (ubah) {
            Akun baru = akun.gantiPeran();
            if (aplikasi.konfirmasi("Ubah peran " + akun.name() + " menjadi " + baru.label() + "?")) {
                data.akun.ganti(akun, baru);
                akun();
            }
        } else if (aplikasi.konfirmasi("Hapus akun " + akun.name() + "?")) {
            data.akun.hapus(akun);
            akun();
        }
    }
}
