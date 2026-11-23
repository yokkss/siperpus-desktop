import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Anggota;
import model.Buku;
import model.DetailPeminjaman;
import model.Peminjaman;
import util.DataDemo;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class MenuTransaksi {
    private final SiperpusDesktop aplikasi;
    private final DataDemo data;

    MenuTransaksi(SiperpusDesktop aplikasi) {
        this.aplikasi = aplikasi;
        data = aplikasi.data;
    }

    void dashboard() {
        long eksemplar = 0;
        long dipinjam = 0;
        for (Buku buku : data.buku.semua()) {
            eksemplar += buku.stock();
            dipinjam += data.transaksi.borrowed(buku);
        }
        Label ringkasan = new Label(eksemplar + " eksemplar    ·    " + data.anggota.semua().size()
                + " anggota    ·    " + dipinjam + " sedang dipinjam");
        VBox isi = new VBox(12, ringkasan, new Label("Transaksi terbaru"), daftarTransaksi(data.pinjaman.semua()));
        VBox.setVgrow(isi.getChildren().get(2), Priority.ALWAYS);
        aplikasi.halaman("Dashboard", isi);
    }

    void peminjaman() {
        VBox daftar = daftarTransaksi(data.pinjaman.semua());
        if (!aplikasi.admin()) daftar.getChildren().add(aplikasi.tombol("Buat peminjaman", this::pinjam));
        aplikasi.halaman("Peminjaman", daftar);
    }

    private VBox daftarTransaksi(List<Peminjaman> transaksi) {
        ListView<Peminjaman> daftar = new ListView<>();
        daftar.setPlaceholder(new Label("Belum ada transaksi."));
        for (int i = transaksi.size() - 1; i >= 0; i--) {
            daftar.getItems().add(transaksi.get(i));
        }
        daftar.setCellFactory(tabel -> new ListCell<>() {
            @Override
            protected void updateItem(Peminjaman pinjaman, boolean kosong) {
                super.updateItem(pinjaman, kosong);
                if (kosong || pinjaman == null) {
                    setText(null);
                    return;
                }
                setText("#" + pinjaman.id + "  ·  " + pinjaman.member.name() + "  ·  "
                        + pinjaman.items.size() + " buku  |  Jatuh tempo: " + pinjaman.due());
            }
        });
        VBox panel = new VBox(10, daftar, aplikasi.tombol("Detail transaksi", () -> detail(aplikasi.pilihan(daftar))));
        VBox.setVgrow(daftar, Priority.ALWAYS);
        return panel;
    }

    private void detail(Peminjaman pinjaman) {
        StringBuilder pesan = new StringBuilder();
        pesan.append("Tanggal pinjam: ").append(pinjaman.start).append("\n");
        for (DetailPeminjaman detail : pinjaman.items) {
            pesan.append("\n").append(detail.book.title()).append(" · ");
            if (detail.returned() == null) {
                pesan.append("Dipinjam");
            } else {
                pesan.append("Kembali ").append(detail.returned())
                        .append(" · ").append(rupiah(detail.fine(pinjaman, detail.returned())));
            }
        }
        aplikasi.tampilkan("Transaksi #" + pinjaman.id, pesan.toString());
    }

    private void pinjam() {
        if (data.anggota.semua().isEmpty()) throw new IllegalArgumentException("Tambahkan anggota terlebih dahulu.");
        List<Buku> tersedia = new ArrayList<>();
        for (Buku buku : data.buku.semua()) {
            if (data.transaksi.available(buku) > 0) tersedia.add(buku);
        }
        if (tersedia.isEmpty()) throw new IllegalArgumentException("Tidak ada buku tersedia.");

        ComboBox<Anggota> anggota = aplikasi.kotakPilihan(data.anggota.semua());
        ComboBox<Buku> pertama = aplikasi.kotakPilihan(tersedia);
        ComboBox<Buku> kedua = aplikasi.kotakPilihan(tersedia);
        ComboBox<Buku> ketiga = aplikasi.kotakPilihan(tersedia);
        LocalDate jatuhTempo = LocalDate.now().plusDays(Peminjaman.DURASI_HARI);
        String[] label = {"Anggota", "Buku pertama", "Buku kedua (opsional)", "Buku ketiga (opsional)"};
        if (!aplikasi.formulir("Buat peminjaman", "Jatuh tempo: " + jatuhTempo, label,
                anggota, pertama, kedua, ketiga)) return;

        // Kotak pilihan yang belum diisi menghasilkan null.
        Buku bukuPertama = pertama.getValue();
        Buku bukuKedua = kedua.getValue();
        Buku bukuKetiga = ketiga.getValue();
        Peminjaman pinjaman = data.transaksi.borrow(anggota.getValue(), bukuPertama, bukuKedua, bukuKetiga);
        peminjaman();
        aplikasi.tampilkan("Peminjaman disimpan", "Transaksi #" + pinjaman.id + " · Jatuh tempo " + pinjaman.due());
    }

    void pengembalian() {
        ListView<BukuAktif> daftar = new ListView<>();
        daftar.setPlaceholder(new Label("Tidak ada buku yang masih dipinjam."));
        for (Peminjaman pinjaman : data.pinjaman.semua()) {
            for (DetailPeminjaman detail : pinjaman.items) {
                if (detail.returned() == null) daftar.getItems().add(new BukuAktif(pinjaman, detail));
            }
        }
        VBox isi = new VBox(12, daftar);
        VBox.setVgrow(daftar, Priority.ALWAYS);
        if (!aplikasi.admin()) {
            isi.getChildren().add(aplikasi.tombol("Kembalikan buku", () -> kembalikan(aplikasi.pilihan(daftar))));
        }
        aplikasi.halaman("Pengembalian", isi);
    }

    private void kembalikan(BukuAktif buku) {
        DatePicker isian = new DatePicker(LocalDate.now());
        isian.setEditable(false);
        if (!aplikasi.formulir("Pengembalian", buku.detail.book.title(), new String[]{"Tanggal kembali"}, isian)) return;
        LocalDate tanggal = isian.getValue();
        if (tanggal == null || tanggal.isBefore(buku.pinjaman.start) || tanggal.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal harus di antara tanggal pinjam dan hari ini.");
        }
        long denda = buku.detail.fine(buku.pinjaman, tanggal);
        if (aplikasi.konfirmasi("Kembalikan " + buku.detail.book.title() + " dengan denda " + rupiah(denda) + "?")) {
            data.transaksi.giveBack(buku.pinjaman, buku.detail, tanggal);
            pengembalian();
        }
    }

    void laporan() {
        DatePicker awal = new DatePicker(LocalDate.now().minusDays(30));
        DatePicker akhir = new DatePicker(LocalDate.now());
        awal.setEditable(false);
        akhir.setEditable(false);
        VBox hasil = new VBox(10);
        HBox filter = new HBox(8, new Label("Dari"), awal, new Label("sampai"), akhir,
                aplikasi.tombol("Terapkan", () -> tampilkanLaporan(hasil, awal.getValue(), akhir.getValue())));
        VBox isi = new VBox(12, filter, hasil);
        VBox.setVgrow(hasil, Priority.ALWAYS);
        tampilkanLaporan(hasil, awal.getValue(), akhir.getValue());
        aplikasi.halaman("Laporan", isi);
    }

    private void tampilkanLaporan(VBox hasil, LocalDate awal, LocalDate akhir) {
        if (awal == null || akhir == null || akhir.isBefore(awal)) {
            throw new IllegalArgumentException("Pilih rentang tanggal yang benar.");
        }
        List<Peminjaman> periode = new ArrayList<>();
        int total = 0;
        int terlambat = 0;
        LocalDate hariIni = LocalDate.now();
        for (Peminjaman pinjaman : data.pinjaman.semua()) {
            if (pinjaman.start.isBefore(awal) || pinjaman.start.isAfter(akhir)) continue;
            periode.add(pinjaman);
            for (DetailPeminjaman detail : pinjaman.items) {
                total++;
                LocalDate tanggal = detail.returned() == null ? hariIni : detail.returned();
                if (detail.fine(pinjaman, tanggal) > 0) terlambat++;
            }
        }
        Label ringkasan = new Label(periode.size() + " transaksi  ·  " + total
                + " buku dipinjam  ·  " + terlambat + " buku terlambat");
        VBox daftar = daftarTransaksi(periode);
        VBox.setVgrow(daftar, Priority.ALWAYS);
        hasil.getChildren().setAll(ringkasan, daftar);
    }

    private class BukuAktif {
        final Peminjaman pinjaman;
        final DetailPeminjaman detail;

        BukuAktif(Peminjaman pinjaman, DetailPeminjaman detail) {
            this.pinjaman = pinjaman;
            this.detail = detail;
        }

        @Override
        public String toString() {
            return "#" + pinjaman.id + "  ·  " + pinjaman.member.name() + "  ·  " + detail.book.title()
                    + "  |  Denda hari ini: " + rupiah(detail.fine(pinjaman, LocalDate.now()));
        }
    }

    private String rupiah(long jumlah) {
        return "Rp" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(jumlah);
    }
}
