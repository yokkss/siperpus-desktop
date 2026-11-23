import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Siperpus {
    private static final String VERSI_FX = "26.0.1";
    private static final Path FOLDER_FX = Path.of("lib", "javafx");

    public static void main(String[] args) {
        if (!Files.isRegularFile(Path.of("src", "SiperpusDesktop.java"))) {
            System.err.println("Jalankan java Siperpus.java dari folder FP non swing.");
            System.exit(1);
        }

        try {
            siapkanJavaFX();
            // Jalankan aplikasi desktop dengan lokasi JavaFX yang sudah disiapkan.
            String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
            Process aplikasi = new ProcessBuilder(java, "--enable-native-access=javafx.graphics",
                    "--module-path", FOLDER_FX.toString(), "--add-modules", "javafx.controls",
                    "src/SiperpusDesktop.java").inheritIO().start();
            System.exit(aplikasi.waitFor());
        } catch (IOException masalah) {
            System.err.println("SIPERPUS gagal dibuka: " + masalah.getMessage());
            System.exit(1);
        } catch (InterruptedException masalah) {
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }

    private static void siapkanJavaFX() throws IOException {
        Files.createDirectories(FOLDER_FX);
        String[] modul = {"base", "graphics", "controls"};
        for (String nama : modul) {
            String paket = "javafx-" + nama;
            String namaFile = paket + "-" + VERSI_FX + "-win.jar";
            Path tujuan = FOLDER_FX.resolve(namaFile);
            if (Files.isRegularFile(tujuan)) continue;

            System.out.println("Mengunduh JavaFX " + nama + "...");
            URI alamat = URI.create("https://repo.maven.apache.org/maven2/org/openjfx/"
                    + paket + "/" + VERSI_FX + "/" + namaFile);
            URLConnection koneksi = alamat.toURL().openConnection();
            koneksi.setConnectTimeout(10_000);
            koneksi.setReadTimeout(30_000);

            // Pindahkan ke nama .jar setelah unduhan selesai agar file separuh jadi tidak dipakai.
            Path sementara = Files.createTempFile(FOLDER_FX, paket, ".part");
            try (InputStream isi = koneksi.getInputStream()) {
                Files.copy(isi, sementara, StandardCopyOption.REPLACE_EXISTING);
                Files.move(sementara, tujuan, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(sementara);
            }
        }
    }
}
