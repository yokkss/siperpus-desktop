import javax.swing.SwingUtilities;
import view.FormPerpustakaan;

public class Siperpus {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FormPerpustakaan().setVisible(true));
    }
}
