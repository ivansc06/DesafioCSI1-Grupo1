package primetechfinal;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.SwingUtilities;

public class PrimeTech {

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true); //forma de abrir pantalla gastando menos memoria
        });
    }
}
