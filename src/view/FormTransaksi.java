package view;

import model.*;
import util.DataDemo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

class FormTransaksi {
    private final FormPerpustakaan form;
    private final DataDemo data;
    private final DialogForm dialog;

    FormTransaksi(FormPerpustakaan form) {
        this.form = form;
        data = form.data;
        dialog = form.dialog;
    }

    JPanel halamanPeminjaman() {
        TabelData tabel = tabelPinjaman(data.pinjaman.semua());
        tabel.tambahPencarian("Cari anggota atau transaksi", 0, 1);
        if (!form.adalahAdmin()) {
            tabel.tombol.add(dialog.tombol("Buat peminjaman", e -> buatPeminjaman()));
        }
        return tabel;
    }

    private void buatPeminjaman() {
        JComboBox<Anggota> pilihanAnggota = new JComboBox<>();
        for (Anggota anggota : data.anggota.semua()) pilihanAnggota.addItem(anggota);

        JComboBox<Buku> pertama = new JComboBox<>();
        JComboBox<Buku> kedua = new JComboBox<>();
        JComboBox<Buku> ketiga = new JComboBox<>();
        kedua.addItem(null);
        ketiga.addItem(null);
        for (Buku buku : data.buku.semua()) {
            if (data.transaksi.available(buku) > 0) {
                pertama.addItem(buku);
                kedua.addItem(buku);
                ketiga.addItem(buku);
            }
        }
        if (pertama.getItemCount() == 0) throw new IllegalArgumentException("Semua buku sedang dipinjam.");
        LocalDate jatuhTempo = LocalDate.now().plusDays(Peminjaman.DURASI_HARI);
        String[] label = {"Anggota", "Buku pertama", "Buku kedua (opsional)", "Buku ketiga (opsional)", "Jatuh tempo"};
        if (!dialog.isi("Buat peminjaman", label, pilihanAnggota, pertama, kedua, ketiga, new JLabel(jatuhTempo.toString()))) return;

        Anggota anggota = (Anggota) pilihanAnggota.getSelectedItem();
        List<Buku> pilihan = new ArrayList<>();
        pilihan.add((Buku) pertama.getSelectedItem());
        if (kedua.getSelectedItem() != null) pilihan.add((Buku) kedua.getSelectedItem());
        if (ketiga.getSelectedItem() != null) pilihan.add((Buku) ketiga.getSelectedItem());

        // Service memeriksa stok dan batas pinjam sebelum transaksi disimpan.
        Peminjaman pinjaman = data.transaksi.borrow(anggota, pilihan);
        form.muatUlang();
        dialog.info("Transaksi " + pinjaman.id + " tersimpan. Jatuh tempo " + pinjaman.due() + ".");
    }

    private TabelData tabelPinjaman(List<Peminjaman> daftar) {
        List<Peminjaman> terbaru = new ArrayList<>(daftar);
        Collections.reverse(terbaru);
        TabelData tabel = new TabelData("Transaksi", "Anggota", "Pinjam", "Jatuh tempo", "Buku", "Status");
        for (Peminjaman pinjaman : terbaru) {
            tabel.tambahBaris(pinjaman.id, pinjaman.member.name(), pinjaman.start, pinjaman.due(),
                    pinjaman.items.size(), status(pinjaman));
        }
        tabel.tombol.add(dialog.tombol("Detail transaksi", e -> detailTransaksi(terbaru.get(tabel.indeksPilihan()))));
        return tabel;
    }

    private String status(Peminjaman pinjaman) {
        int kembali = 0;
        for (DetailPeminjaman detail : pinjaman.items) {
            if (detail.returned() != null) kembali++;
        }
        if (kembali == 0) return "Aktif";
        if (kembali == pinjaman.items.size()) return "Selesai";
        return "Kembali sebagian";
    }

    private String rupiah(long jumlah) {
        return "Rp" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(jumlah);
    }

    private void detailTransaksi(Peminjaman pinjaman) {
        String pesan = pinjaman.member + "\nJatuh tempo " + pinjaman.due() + "\n";
        for (DetailPeminjaman detail : pinjaman.items) {
            String keterangan = "Dipinjam";
            if (detail.returned() != null) {
                long denda = detail.fine(pinjaman, detail.returned());
                keterangan = "Kembali " + detail.returned() + " · " + rupiah(denda);
            }
            pesan += "\n" + detail.book.title() + " · " + keterangan;
        }
        dialog.info(pesan);
    }

    // Satu baris pengembalian mewakili satu buku dalam sebuah transaksi.
    private static class BukuDipinjam {
        final Peminjaman pinjaman;
        final DetailPeminjaman detail;

        BukuDipinjam(Peminjaman pinjaman, DetailPeminjaman detail) {
            this.pinjaman = pinjaman;
            this.detail = detail;
        }
    }

    JPanel halamanPengembalian() {
        TabelData tabel = new TabelData("Transaksi", "Anggota", "Buku aktif", "Jatuh tempo", "Denda hari ini");
        List<BukuDipinjam> aktif = new ArrayList<>();
        for (Peminjaman pinjaman : data.pinjaman.semua()) {
            for (DetailPeminjaman detail : pinjaman.items) {
                if (detail.returned() == null) {
                    aktif.add(new BukuDipinjam(pinjaman, detail));
                    tabel.tambahBaris(pinjaman.id, pinjaman.member.name(), detail.book.title(),
                            pinjaman.due(), rupiah(detail.fine(pinjaman, LocalDate.now())));
                }
            }
        }
        tabel.tambahPencarian("Cari pinjaman aktif", 0, 1, 2);
        if (!form.adalahAdmin()) {
            tabel.tombol.add(dialog.tombol("Kembalikan buku", e -> kembalikanBuku(aktif.get(tabel.indeksPilihan()))));
        }
        return tabel;
    }

    private void kembalikanBuku(BukuDipinjam pilihan) {
        Peminjaman pinjaman = pilihan.pinjaman;
        DetailPeminjaman detail = pilihan.detail;
        JTextField isian = new JTextField(LocalDate.now().toString(), 15);
        if (!dialog.isi("Pengembalian · " + detail.book.title(), new String[]{"Tanggal kembali (YYYY-MM-DD)"}, isian)) return;
        LocalDate tanggal = bacaTanggal(isian);
        if (tanggal.isBefore(pinjaman.start) || tanggal.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal kembali di luar periode pinjam.");
        }
        String denda = rupiah(detail.fine(pinjaman, tanggal));
        if (dialog.konfirmasi("Kembalikan " + detail.book.title() + " dengan denda " + denda + "?")) {
            data.transaksi.giveBack(pinjaman, detail, tanggal);
            form.muatUlang();
        }
    }

    private LocalDate bacaTanggal(JTextField isian) {
        try {
            return LocalDate.parse(isian.getText().strip());
        } catch (DateTimeParseException salah) {
            throw new IllegalArgumentException("Gunakan format tanggal YYYY-MM-DD.");
        }
    }

    JPanel dashboard() {
        int total = 0;
        int dipinjam = 0;
        for (Buku buku : data.buku.semua()) {
            total += buku.stock();
            dipinjam += data.transaksi.borrowed(buku);
        }
        JPanel kartu = new JPanel(new GridLayout(1, 3, 10, 0));
        kartu.add(kartuJumlah("Total eksemplar", total));
        kartu.add(kartuJumlah("Anggota", data.anggota.semua().size()));
        kartu.add(kartuJumlah("Sedang dipinjam", dipinjam));
        TabelData tabel = tabelPinjaman(data.pinjaman.semua());
        tabel.filter.add(new JLabel("Transaksi terbaru"));
        JPanel panel = new JPanel(new BorderLayout(0, 18));
        panel.setOpaque(false);
        panel.add(kartu, BorderLayout.NORTH);
        panel.add(tabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel kartuJumlah(String nama, int jumlah) {
        JPanel kartu = new JPanel(new GridLayout(2, 1));
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(new EmptyBorder(12, 16, 12, 16));
        kartu.add(new JLabel(nama));
        JLabel angka = new JLabel(String.valueOf(jumlah));
        angka.setFont(new Font("SansSerif", Font.BOLD, 25));
        kartu.add(angka);
        return kartu;
    }

    JPanel halamanLaporan() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JTextField awal = new JTextField(LocalDate.now().minusDays(30).toString(), 9);
        JTextField akhir = new JTextField(LocalDate.now().toString(), 9);
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filter.add(new JLabel("Tanggal pinjam"));
        filter.add(awal);
        filter.add(new JLabel("s.d."));
        filter.add(akhir);
        JPanel hasil = new JPanel(new BorderLayout());
        filter.add(dialog.tombol("Terapkan", e -> tampilkanLaporan(hasil, bacaTanggal(awal), bacaTanggal(akhir))));
        panel.add(filter, BorderLayout.NORTH);
        panel.add(hasil, BorderLayout.CENTER);
        tampilkanLaporan(hasil, bacaTanggal(awal), bacaTanggal(akhir));
        return panel;
    }

    private void tampilkanLaporan(JPanel hasil, LocalDate awal, LocalDate akhir) {
        if (akhir.isBefore(awal)) throw new IllegalArgumentException("Tanggal akhir mendahului tanggal awal.");
        List<Peminjaman> periode = new ArrayList<>();
        int bukuDipinjam = 0;
        int terlambat = 0;
        for (Peminjaman pinjaman : data.pinjaman.semua()) {
            if (pinjaman.start.isBefore(awal) || pinjaman.start.isAfter(akhir)) continue;
            periode.add(pinjaman);
            bukuDipinjam += pinjaman.items.size();
            for (DetailPeminjaman detail : pinjaman.items) {
                LocalDate tanggal = detail.returned();
                if (tanggal == null) tanggal = LocalDate.now();
                if (detail.fine(pinjaman, tanggal) > 0) terlambat++;
            }
        }
        String ringkasan = periode.size() + " transaksi   ·   " + bukuDipinjam + " buku dipinjam   ·   " + terlambat + " buku terlambat";
        hasil.removeAll();
        hasil.add(tabelPinjaman(periode), BorderLayout.CENTER);
        hasil.add(new JLabel(ringkasan), BorderLayout.SOUTH);
        hasil.revalidate();
        hasil.repaint();
    }
}
