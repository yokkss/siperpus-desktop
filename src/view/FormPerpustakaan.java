package view;

import model.Akun;
import util.DataDemo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FormPerpustakaan extends JFrame implements ActionListener {
    final DataDemo data = new DataDemo();
    final DialogForm dialog = new DialogForm(this);
    final JPanel menu = new JPanel();
    private final JPanel isi = new JPanel(new BorderLayout(0, 18));
    private final JLabel judul = new JLabel();
    private final FormMasterData master = new FormMasterData(this);
    private final FormTransaksi transaksi = new FormTransaksi(this);
    Akun pengguna;

    public FormPerpustakaan() {
        super("SIPERPUS · Sistem Informasi Perpustakaan");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(790, 520));
        setSize(1100, 690);
        setLocationRelativeTo(null);

        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(new Color(41, 44, 43));
        menu.setBorder(new EmptyBorder(30, 15, 18, 15));
        menu.setPreferredSize(new Dimension(198, 0));
        add(menu, BorderLayout.WEST);

        isi.setBackground(new Color(249, 248, 245));
        isi.setBorder(new EmptyBorder(30, 30, 26, 30));
        judul.setFont(new Font("Serif", Font.BOLD, 38));
        add(isi, BorderLayout.CENTER);
        bukaHalaman("Katalog buku");
    }

    void bukaHalaman(String nama) {
        judul.setText(nama);
        susunMenu();
        isi.removeAll();
        isi.add(judul, BorderLayout.NORTH);

        JPanel halaman;
        switch (nama) {
            case "Katalog buku":
            case "Data buku": halaman = master.halamanBuku(); break;
            case "Data anggota": halaman = master.halamanAnggota(); break;
            case "Kategori": halaman = master.halamanKategori(); break;
            case "Kelola pengguna": halaman = master.halamanAkun(); break;
            case "Dashboard": halaman = transaksi.dashboard(); break;
            case "Peminjaman": halaman = transaksi.halamanPeminjaman(); break;
            case "Pengembalian": halaman = transaksi.halamanPengembalian(); break;
            case "Laporan": halaman = transaksi.halamanLaporan(); break;
            default: throw new IllegalArgumentException("Halaman tidak dikenal: " + nama);
        }
        isi.add(halaman, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    void muatUlang() {
        bukaHalaman(judul.getText());
    }

    boolean adalahAdmin() {
        return pengguna != null && pengguna.admin();
    }

    private void susunMenu() {
        menu.removeAll();
        JLabel merek = new JLabel("SIPERPUS");
        merek.setFont(new Font("SansSerif", Font.BOLD, 23));
        merek.setForeground(Color.WHITE);
        menu.add(merek);

        String namaPeran = "Katalog publik";
        if (pengguna != null) namaPeran = pengguna.label() + " · " + pengguna.name();
        JLabel peran = new JLabel(namaPeran);
        peran.setForeground(Color.LIGHT_GRAY);
        menu.add(peran);
        menu.add(Box.createVerticalStrut(34));

        if (pengguna == null) {
            tambahMenu("Katalog buku");
            menu.add(Box.createVerticalGlue());
            tambahMenu("Login staf");
            return;
        }
        tambahMenu("Dashboard");
        tambahMenu("Data buku");
        tambahMenu("Data anggota");
        if (adalahAdmin()) tambahMenu("Kategori");
        tambahMenu("Peminjaman");
        tambahMenu("Pengembalian");
        tambahMenu("Laporan");
        if (adalahAdmin()) tambahMenu("Kelola pengguna");
        menu.add(Box.createVerticalGlue());
        tambahMenu("Logout");
    }

    private void tambahMenu(String nama) {
        JButton tombol = new JButton(nama);
        tombol.setMaximumSize(new Dimension(210, 39));
        tombol.setHorizontalAlignment(SwingConstants.LEFT);
        tombol.setForeground(Color.WHITE);
        tombol.setBackground(new Color(52, 55, 54));
        if (nama.equals(judul.getText())) tombol.setBackground(new Color(107, 44, 48));
        tombol.setBorder(new EmptyBorder(10, 14, 10, 14));
        tombol.addActionListener(this);
        menu.add(tombol);
        menu.add(Box.createVerticalStrut(4));
    }

    // Swing memanggil method ini ketika salah satu tombol menu diklik.
    @Override
    public void actionPerformed(ActionEvent event) {
        String pilihan = event.getActionCommand();
        if (pilihan.equals("Login staf")) {
            login();
        } else if (pilihan.equals("Logout")) {
            pengguna = null;
            bukaHalaman("Katalog buku");
        } else {
            bukaHalaman(pilihan);
        }
    }

    private void login() {
        JTextField username = new JTextField(24);
        JPasswordField password = new JPasswordField();
        JLabel contoh = new JLabel("admin / admin     ·     petugas / petugas");
        String[] label = {"Username", "Password", "Akun contoh (username / password)"};
        if (!dialog.isi("Login staf · data demo", label, username, password, contoh)) return;

        String nama = username.getText().strip();
        String sandi = new String(password.getPassword());
        for (Akun akun : data.akun.semua()) {
            if (akun.name().equals(nama) && akun.cocok(sandi)) {
                pengguna = akun;
                bukaHalaman("Dashboard");
                return;
            }
        }
        dialog.info("Username atau password salah.");
    }
}
