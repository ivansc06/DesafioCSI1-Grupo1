package primetechfinal;

import javax.swing.SwingUtilities;

public class PrimeTech {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true); //forma de abrir pantalla gastando menos memoria
        });
    }
}
