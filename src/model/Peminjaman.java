package model;

import java.time.LocalDate;
import java.util.List;

public class Peminjaman {
    public static final int DURASI_HARI = 2;
    public final int id;
    public final Anggota member;
    public final LocalDate start;
    public final List<DetailPeminjaman> items;

    public Peminjaman(int id, Anggota member, LocalDate start, List<Buku> buku) {
        this.id = id;
        this.member = member;
        this.start = start;
        this.items = buku.stream().map(DetailPeminjaman::new).toList();
    }

    public LocalDate due() { return start.plusDays(DURASI_HARI); }
}
