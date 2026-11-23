package dao;

// DAO melempar kesalahan ini saat data yang hendak diganti atau dihapus tidak ditemukan.
public class DataAksesException extends RuntimeException {
    public DataAksesException(String pesan) {
        // Teruskan pesan ke RuntimeException agar dialog bisa membacanya lewat getMessage().
        super(pesan);
    }
}
