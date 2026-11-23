package view;

import dao.DataAksesException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

class DialogForm {
    private final JFrame jendela;

    DialogForm(JFrame jendela) {
        this.jendela = jendela;
    }

    JButton tombol(String teks, ActionListener aksi) {
        JButton tombol = new JButton(teks);
        tombol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    aksi.actionPerformed(event);
                } catch (IllegalArgumentException | DataAksesException masalah) {
                    JOptionPane.showMessageDialog(jendela, masalah.getMessage(),
                            "Periksa isian", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        return tombol;
    }

    boolean isi(String judul, String[] label, JComponent... isian) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 2, 5));
        for (int i = 0; i < label.length; i++) {
            panel.add(new JLabel(label[i]));
            panel.add(isian[i]);
        }
        int jawaban = JOptionPane.showConfirmDialog(jendela, panel, judul,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return jawaban == JOptionPane.OK_OPTION;
    }

    boolean konfirmasi(String pesan) {
        int jawaban = JOptionPane.showConfirmDialog(jendela, pesan, "Konfirmasi", JOptionPane.YES_NO_OPTION);
        return jawaban == JOptionPane.YES_OPTION;
    }

    void info(String pesan) {
        JOptionPane.showMessageDialog(jendela, pesan, "SIPERPUS", JOptionPane.INFORMATION_MESSAGE);
    }

    String wajib(JTextField isian) {
        String teks = isian.getText().strip();
        if (teks.isEmpty()) throw new IllegalArgumentException("Lengkapi semua isian.");
        return teks;
    }
}
