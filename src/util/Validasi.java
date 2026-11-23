package util;

public final class Validasi {
    private Validasi() {}

    public static void require(boolean valid, String pesan) {
        if (!valid) throw new IllegalArgumentException(pesan);
    }
}
