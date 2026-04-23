package primetech;

import javax.swing.SwingUtilities;

public class PrimeTech {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
