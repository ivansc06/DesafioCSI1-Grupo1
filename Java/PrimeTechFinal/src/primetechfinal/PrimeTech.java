package primetechfinal;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.SwingUtilities;
import java.awt.Color;

public class PrimeTech {

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();//lanzamos flatlaf, mejora la ui solo con esta linea

        // redondear los botones con FlatLaf (mayor numero = mas redondeado)
        javax.swing.UIManager.put("Button.arc", 999);

        // botones en cian con texto blanco
        javax.swing.UIManager.put("Button.background", new Color(0, 204, 255));
        javax.swing.UIManager.put("Button.foreground", Color.WHITE);
        javax.swing.UIManager.put("Button.hoverBackground", new Color(0, 170, 210));

        // redondear campos de texto
        javax.swing.UIManager.put("TextComponent.arc", 10);

        // fuente Roboto para las tablas
        javax.swing.UIManager.put("Table.font", new java.awt.Font("Roboto", java.awt.Font.PLAIN, 13));
        javax.swing.UIManager.put("TableHeader.font", new java.awt.Font("Roboto", java.awt.Font.BOLD, 13));

        // filas de tabla en blanco con texto oscuro
        javax.swing.UIManager.put("Table.background", Color.WHITE);
        javax.swing.UIManager.put("Table.foreground", new Color(30, 30, 40));
        javax.swing.UIManager.put("Table.gridColor", new Color(210, 210, 220));
        javax.swing.UIManager.put("TableHeader.background", new Color(50, 90, 150));
        javax.swing.UIManager.put("TableHeader.foreground", Color.WHITE);

        // seleccion de tabla en cian
        javax.swing.UIManager.put("Table.selectionBackground", new Color(0, 204, 255));
        javax.swing.UIManager.put("Table.selectionForeground", Color.WHITE);

        // scroll discreto
        javax.swing.UIManager.put("ScrollBar.thumb", new Color(80, 80, 100));

        // quitar borde de foco azul al hacer clic
        javax.swing.UIManager.put("Component.focusWidth", 0);

        SwingUtilities.invokeLater(() -> {
            PantallaCarga splash = new PantallaCarga();
            splash.setLocationRelativeTo(null); // centrar en pantalla
            splash.setVisible(true);

            // hilo en segundo plano para no bloquear la interfaz
            new Thread(() -> {
                try {
                    splash.setProgreso(20, "Iniciando...");
                    Thread.sleep(400);

                    splash.setProgreso(50, "Conectando con la base de datos...");
                    primetechfinal.db.ConexionDB.getConexionApp().close(); // inicializa el pool de HikariCP
                    Thread.sleep(400);

                    splash.setProgreso(80, "Cargando interfaz...");
                    Thread.sleep(400);

                    splash.setProgreso(100, "Listo");
                    Thread.sleep(300);

                    SwingUtilities.invokeLater(() -> {
                        splash.dispose();
                        new LoginFrame().setVisible(true);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
}
