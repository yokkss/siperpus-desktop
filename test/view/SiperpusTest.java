package view;

import util.DataDemo;
import java.time.LocalDate;
import java.util.List;
import javax.swing.SwingUtilities;

class SiperpusTest {
    public static void main(String[] args) throws Exception {
        var db = new DataDemo();
        var loan = db.pinjaman.semua().getFirst();
        var item = loan.items.getFirst();
        LocalDate today = LocalDate.now();
        assert item.fine(loan, loan.due()) == 0;
        assert item.fine(loan, today) == 30000;
        int available = db.transaksi.available(item.book);
        db.transaksi.giveBack(loan, item, today);
        assert db.transaksi.available(item.book) == available + 1;
        assert db.transaksi.active(loan.member) == 1;
        assert loan.items.get(1).returned() == null;
        try {
            db.transaksi.giveBack(loan, item, today);
            throw new AssertionError("Pengembalian ganda diterima");
        } catch (IllegalArgumentException expected) {}
        db.transaksi.borrow(loan.member, List.of(item.book, item.book));
        try {
            db.transaksi.borrow(loan.member, List.of(item.book));
            throw new AssertionError("Batas 3 buku terlewati");
        } catch (IllegalArgumentException expected) {}
        try {
            db.transaksi.borrow(db.anggota.semua().get(1), List.of(item.book, db.buku.semua().get(4)));
            throw new AssertionError("Peminjaman stok kosong diterima");
        } catch (IllegalArgumentException expected) {}
        assert db.transaksi.active(db.anggota.semua().get(1)) == 0;
        SwingUtilities.invokeAndWait(() -> {
            var app = new FormPerpustakaan();
            try {
                app.setVisible(true);
                assert app.isShowing();
                periksaPilihanSetelahPencarian();
                for (var user : app.data.akun.semua()) {
                    app.pengguna = user;
                    app.bukaHalaman("Dashboard");
                    for (String page : List.of("Dashboard", "Data buku", "Data anggota",
                            "Peminjaman", "Pengembalian", "Laporan")) klikMenu(app, page);
                    if (user.admin()) {
                        klikMenu(app, "Kategori");
                        klikMenu(app, "Kelola pengguna");
                    } else {
                        for (var component : app.menu.getComponents())
                            if (component instanceof javax.swing.JButton b)
                                assert !List.of("Kategori", "Kelola pengguna").contains(b.getText());
                    }
                    klikMenu(app, "Logout");
                    assert app.pengguna == null;
                }
            } finally { app.dispose(); }
        });
        System.out.println("Alur pinjam, kembali, stok, denda, dan halaman Swing lulus.");
    }

    private static void klikMenu(FormPerpustakaan app, String nama) {
        for (var komponen : app.menu.getComponents()) {
            if (komponen instanceof javax.swing.JButton tombol && tombol.getText().equals(nama)) {
                tombol.doClick(0);
                return;
            }
        }
        throw new AssertionError("Menu tidak ditemukan: " + nama);
    }

    private static void periksaPilihanSetelahPencarian() {
        TabelData panel = new TabelData("Judul");
        panel.tambahBaris("Java");
        panel.tambahBaris("Basis Data");
        panel.tambahPencarian("Cari", 0);
        for (var komponen : panel.filter.getComponents()) {
            if (komponen instanceof javax.swing.JTextField kata) kata.setText("basis");
        }
        for (var komponen : panel.getComponents()) {
            if (komponen instanceof javax.swing.JScrollPane scroll) {
                var tabel = (javax.swing.JTable) scroll.getViewport().getView();
                assert tabel.getRowCount() == 1;
                tabel.setRowSelectionInterval(0, 0);
                assert panel.indeksPilihan() == 1;
                return;
            }
        }
        throw new AssertionError("Tabel pencarian tidak ditemukan.");
    }
}
