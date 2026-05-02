package primetechfinal;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.SwingUtilities;

public class PrimeTech {

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();

        // quitamos el borde blanco del contenido del JTabbedPane
        javax.swing.UIManager.put("TabbedPane.contentBorderInsets", new java.awt.Insets(0, 0, 0, 0));
        javax.swing.UIManager.put("TabbedPane.showContentSeparator", false);

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true); //forma de abrir pantalla gastando menos memoria
        });
    }
}
