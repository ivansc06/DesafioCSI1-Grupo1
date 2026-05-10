package primetechfinal;

import javax.swing.SwingUtilities;
import primetechfinal.db.ConexionDB;

public class HiloCarga extends Thread {

    private final PantallaCarga splash;

    public HiloCarga(PantallaCarga splash) {
        this.splash = splash;
    }

    @Override
    public void run() {
        try {
            splash.setProgreso(20, "Iniciando...");
            Thread.sleep(400);

            splash.setProgreso(50, "Conectando con la base de datos...");
            ConexionDB.getConexionApp().close();
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
    }
}
