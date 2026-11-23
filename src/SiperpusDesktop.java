import dao.DataAksesException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Akun;
import model.Buku;
import util.DataDemo;

import java.util.List;
import java.util.Locale;

public class SiperpusDesktop extends Application {
    private static final String WARNA_DASAR = "-fx-base: white; -fx-accent: black;"
            + " -fx-focus-color: black; -fx-faint-focus-color: transparent;";

    final DataDemo data = new DataDemo();
    private final BorderPane jendela = new BorderPane();
    private Stage panggung;
    private Akun pengguna;
    private MenuData menuData;
    private MenuTransaksi transaksi;

    public static void main(String[] args) {
        launch(args);
    }

    // JavaFX memanggil start() setelah siap membuka jendela.
    @Override
    public void start(Stage panggung) {
        this.panggung = panggung;
        menuData = new MenuData(this);
        transaksi = new MenuTransaksi(this);
        jendela.setStyle(WARNA_DASAR);
        panggung.setTitle("SIPERPUS · Perpustakaan");
        panggung.setMinWidth(770);
        panggung.setMinHeight(500);
        panggung.setScene(new Scene(jendela, 1000, 620));
        katalog();
        panggung.show();
    }

    private void navigasi(String aktif) {
        VBox menu = new VBox(8);
        menu.setPadding(new Insets(22, 14, 18, 14));
        menu.setPrefWidth(190);
        menu.setStyle("-fx-background-color: black;");
        Label merek = new Label("SIPERPUS");
        merek.setStyle("-fx-text-fill: white; -fx-font-size: 23px; -fx-font-weight: bold;");
        Label peran = new Label(pengguna == null ? "Katalog publik" : pengguna.label() + " · " + pengguna.name());
        peran.setStyle("-fx-text-fill: white;");
        menu.getChildren().addAll(merek, peran, new Region());
        if (pengguna == null) {
            tambahMenu(menu, aktif, "Katalog buku", this::katalog);
            tambahMenu(menu, aktif, "Login staf", this::login);
        } else {
            tambahMenu(menu, aktif, "Dashboard", transaksi::dashboard);
            tambahMenu(menu, aktif, "Data buku", this::katalog);
            tambahMenu(menu, aktif, "Data anggota", menuData::anggota);
            tambahMenu(menu, aktif, "Peminjaman", transaksi::peminjaman);
            tambahMenu(menu, aktif, "Pengembalian", transaksi::pengembalian);
            tambahMenu(menu, aktif, "Laporan", transaksi::laporan);
            if (admin()) {
                tambahMenu(menu, aktif, "Kategori", menuData::kategori);
                tambahMenu(menu, aktif, "Kelola pengguna", menuData::akun);
            }
            Region ruang = new Region();
            VBox.setVgrow(ruang, Priority.ALWAYS);
            menu.getChildren().add(ruang);
            tambahMenu(menu, aktif, "Logout", () -> {
                pengguna = null;
                katalog();
            });
        }
        jendela.setLeft(menu);
    }

    private void tambahMenu(VBox menu, String aktif, String nama, Runnable tindakan) {
        Button tombol = tombol(nama, tindakan);
        tombol.setMaxWidth(Double.MAX_VALUE);
        // Balik warna tombol untuk menandai halaman yang sedang dibuka.
        if (nama.equals(aktif)) {
            tombol.setStyle("-fx-alignment: center-left; -fx-text-fill: black; -fx-background-color: white;");
        } else {
            tombol.setStyle("-fx-alignment: center-left; -fx-text-fill: white; -fx-background-color: black;");
        }
        menu.getChildren().add(tombol);
    }

    void halaman(String judul, Node isi) {
        navigasi(judul);
        Label nama = new Label(judul);
        nama.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: black;");
        VBox konten = new VBox(18, nama, isi);
        VBox.setVgrow(isi, Priority.ALWAYS);
        konten.setPadding(new Insets(25));
        konten.setStyle("-fx-background-color: white;");
        jendela.setCenter(konten);
    }

    boolean admin() {
        return pengguna != null && pengguna.admin();
    }

    boolean akunSendiri(Akun akun) {
        return pengguna == akun;
    }

    private void login() {
        TextField nama = new TextField();
        PasswordField password = new PasswordField();
        if (!formulir("Login staf", "Admin: admin / admin. Petugas: petugas / petugas.",
                new String[]{"Username", "Password"}, nama, password)) return;

        for (Akun akun : data.akun.semua()) {
            if (akun.name().equals(nama.getText().strip()) && akun.cocok(password.getText())) {
                pengguna = akun;
                transaksi.dashboard();
                return;
            }
        }
        throw new IllegalArgumentException("Username atau password salah.");
    }

    void katalog() {
        TextField cari = new TextField();
        cari.setPromptText("Cari judul, penulis, atau kategori");
        ListView<Buku> daftar = new ListView<>();
        daftar.setPlaceholder(new Label("Tidak ada buku yang cocok."));
        daftar.setCellFactory(tabel -> new ListCell<>() {
            @Override
            protected void updateItem(Buku buku, boolean kosong) {
                super.updateItem(buku, kosong);
                if (kosong || buku == null) {
                    setText(null);
                    return;
                }
                setText(buku.title() + "  ·  " + buku.author() + "  ·  " + buku.category()
                        + "  |  " + data.transaksi.available(buku) + " tersedia");
            }
        });
        isiKatalog(daftar, "");
        cari.textProperty().addListener((nilai, lama, baru) -> isiKatalog(daftar, baru));

        HBox aksi = new HBox(8, tombol("Detail buku", () -> detailBuku(pilihan(daftar))));
        if (admin()) {
            aksi.getChildren().addAll(tombol("Tambah buku", () -> menuData.simpanBuku(null)),
                    tombol("Ubah", () -> menuData.simpanBuku(pilihan(daftar))),
                    tombol("Hapus", () -> menuData.hapusBuku(pilihan(daftar))));
        }
        VBox isi = new VBox(12, cari, daftar, aksi);
        VBox.setVgrow(daftar, Priority.ALWAYS);
        halaman(pengguna == null ? "Katalog buku" : "Data buku", isi);
    }

    private void isiKatalog(ListView<Buku> daftar, String teks) {
        String kata = teks.strip().toLowerCase(Locale.ROOT);
        daftar.getItems().clear();
        for (Buku buku : data.buku.semua()) {
            if (buku.title().toLowerCase(Locale.ROOT).contains(kata)
                    || buku.author().toLowerCase(Locale.ROOT).contains(kata)
                    || buku.category().toLowerCase(Locale.ROOT).contains(kata)) {
                daftar.getItems().add(buku);
            }
        }
    }

    private void detailBuku(Buku buku) {
        tampilkan(buku.title(), buku.author() + " · " + buku.category() + "\nISBN " + buku.isbn()
                + "\nTotal: " + buku.stock() + " | Dipinjam: " + data.transaksi.borrowed(buku)
                + " | Tersedia: " + data.transaksi.available(buku));
    }

    Button tombol(String nama, Runnable tindakan) {
        Button tombol = new Button(nama);
        tombol.setOnAction(event -> {
            try {
                tindakan.run();
            } catch (IllegalArgumentException | DataAksesException masalah) {
                tampilkan("Periksa isian", masalah.getMessage());
            }
        });
        return tombol;
    }

    <T> T pilihan(ListView<T> daftar) {
        T dipilih = daftar.getSelectionModel().getSelectedItem();
        if (dipilih == null) throw new IllegalArgumentException("Pilih satu baris dulu.");
        return dipilih;
    }

    <T> ComboBox<T> kotakPilihan(List<T> data) {
        ComboBox<T> pilihan = new ComboBox<>();
        pilihan.getItems().addAll(data);
        pilihan.setMaxWidth(Double.MAX_VALUE);
        return pilihan;
    }

    boolean formulir(String judul, String petunjuk, String[] label, Control... isian) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(panggung);
        dialog.getDialogPane().setStyle(WARNA_DASAR);
        dialog.setTitle(judul);
        dialog.setHeaderText(petunjuk);
        GridPane kolom = new GridPane();
        kolom.setPadding(new Insets(12));
        kolom.setHgap(16);
        kolom.setVgap(10);
        for (int i = 0; i < label.length; i++) {
            Label nama = new Label(label[i]);
            nama.setLabelFor(isian[i]);
            kolom.addRow(i, nama, isian[i]);
        }
        dialog.getDialogPane().setContent(kolom);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    boolean konfirmasi(String pesan) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION, pesan, ButtonType.YES, ButtonType.NO);
        dialog.initOwner(panggung);
        dialog.getDialogPane().setStyle(WARNA_DASAR);
        dialog.setHeaderText(null);
        return dialog.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    void tampilkan(String judul, String pesan) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION, pesan);
        dialog.initOwner(panggung);
        dialog.getDialogPane().setStyle(WARNA_DASAR);
        dialog.setTitle(judul);
        dialog.setHeaderText(null);
        dialog.showAndWait();
    }

    String wajib(TextInputControl kolom) {
        String teks = kolom.getText().strip();
        if (teks.isEmpty()) throw new IllegalArgumentException("Lengkapi semua isian.");
        return teks;
    }
}
