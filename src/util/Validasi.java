package util;

public final class Validasi {
    private Validasi() {}

    // require dipanggil dengan sebuah syarat. Jika syarat salah, hentikan proses dan kirim pesan ke pemanggil.
    public static void require(boolean valid, String pesan) {
        if (!valid) throw new IllegalArgumentException(pesan);
    }
}
