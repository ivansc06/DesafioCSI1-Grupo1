package primetechfinal;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.SwingUtilities;

public class PrimeTech {

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();//lanzamos flatlaf, mejora la ui solo con esta linea

        
        javax.swing.UIManager.put("TabbedPane.contentBorderInsets", new java.awt.Insets(0, 0, 0, 0));
        javax.swing.UIManager.put("TabbedPane.showContentSeparator", false);

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true); //forma de abrir pantalla gastando menos memoria
        });
    }
}
