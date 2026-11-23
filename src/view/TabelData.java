package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.regex.Pattern;

// Satu komponen tabel dipakai kembali di beberapa menu.
class TabelData extends JPanel {
    final JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
    final JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final DefaultTableModel model;
    private final JTable tabel;

    TabelData(String... kolom) {
        super(new BorderLayout(0, 8));
        model = new DefaultTableModel(kolom, 0);
        tabel = new JTable(model);
        tabel.setDefaultEditor(Object.class, null);
        tabel.setAutoCreateRowSorter(true);
        tabel.setRowHeight(38);
        tabel.setShowVerticalLines(false);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.getTableHeader().setReorderingAllowed(false);
        filter.setOpaque(false);
        tombol.setOpaque(false);
        setOpaque(false);
        add(filter, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(tombol, BorderLayout.SOUTH);
    }

    void tambahBaris(Object... nilai) {
        model.addRow(nilai);
    }

    int indeksPilihan() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            throw new IllegalArgumentException("Pilih satu baris pada tabel.");
        }
        // Setelah tabel diurutkan atau difilter, posisi di layar bisa berbeda.
        return tabel.convertRowIndexToModel(baris);
    }

    void tambahPencarian(String label, int... kolom) {
        JTextField kata = new JTextField(20);
        filter.add(new JLabel(label));
        filter.add(kata);
        TableRowSorter<DefaultTableModel> urutan = new TableRowSorter<>(model);
        tabel.setRowSorter(urutan);
        kata.getDocument().addDocumentListener(new DocumentListener() {
            private void cari() {
                String teks = Pattern.quote(kata.getText().strip());
                urutan.setRowFilter(RowFilter.regexFilter("(?iu)" + teks, kolom));
            }
            public void insertUpdate(DocumentEvent e) { cari(); }
            public void removeUpdate(DocumentEvent e) { cari(); }
            public void changedUpdate(DocumentEvent e) { cari(); }
        });
    }
}
