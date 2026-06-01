/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package primetechfinal;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import primetechfinal.dao.ClienteDAO;
import primetechfinal.dao.ProductoDAO;
import primetechfinal.dao.VentaDAO;
import primetechfinal.model.Cliente;
import primetechfinal.model.DetalleVenta;
import primetechfinal.model.Producto;
import primetechfinal.model.Venta;
import primetechfinal.sesion.Sesion;
import primetechfinal.util.EnviarEmail;
import primetechfinal.util.FacturaHTML;
/**
 *
 * @author dpjos
 */
public class Pantalla extends javax.swing.JFrame {
    
    private static final Logger logger = Logger.getLogger(Pantalla.class.getName());
    private Producto productoActual = null; //Guarda el producto que se está editando.
    //Si es null significa que estamos creando uno nuevo. Si tiene valor significa que estamos editando ese producto
    
    private Cliente clienteActual = null; // null = creando, con valor = editando
    private ClienteDAO clienteDAO = new ClienteDAO();
    private ProductoDAO productoDAO = new ProductoDAO();//instanciamos para no hacer new ProductoDAO cada vez que lo utilicemos
    private VentaDAO ventaDAO = new VentaDAO();
    private Venta ventaActual = null; // null = nueva venta, con valor = editando
    // listas paralelas al combobox de clientes y productos en el dialog de venta
    private List<Cliente> clientesCombo = new ArrayList<>();
    private List<Producto> productosCombo = new ArrayList<>();

    // timer que cierra la sesion si el usuario lleva 3 minutos sin actividad
    private javax.swing.Timer timerInactividad;
    private static final int MINUTOS_INACTIVIDAD = 3;

    public Pantalla() {
        initComponents();
        setLocationRelativeTo(null);

        // fondo blanco en los botones del header para que no hereden el cian del UIManager
        btnCerrarPantalla.setBackground(Color.WHITE);
        btnMinimizarPantalla.setBackground(Color.WHITE);

        // cerrar la aplicacion
        btnCerrarPantalla.addActionListener(e -> System.exit(0));

        // minimizar la ventana
        btnMinimizarPantalla.addActionListener(e -> setState(JFrame.ICONIFIED));

        // hacer la ventana arrastrable desde el header
        final int[] posInicial = new int[2];
        pnlHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                posInicial[0] = e.getXOnScreen();
                posInicial[1] = e.getYOnScreen();
            }
        });
        pnlHeader.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int deltaX = e.getXOnScreen() - posInicial[0];
                int deltaY = e.getYOnScreen() - posInicial[1];
                setLocation(getLocation().x + deltaX, getLocation().y + deltaY);
                posInicial[0] = e.getXOnScreen();
                posInicial[1] = e.getYOnScreen();
            }
        });

        //con esto solo mostramos el panel productos
        pnlVentas.setVisible(false);
        pnlClientes.setVisible(false);
        pnlDashboard.setVisible(false);
        lblEmpleado.setText(Sesion.getEmpleado().getNombreCompleto()
                        + " · " + Sesion.getEmpleado().getCargo());//necesario para saber que empleado esta conectado

        // estilo oscuro solo para los buscadores de Pantalla, sin afectar al Login
        Color fondoBuscador = new Color(45, 45, 60);
        Color textoBuscador = new Color(200, 200, 210);
        for (JTextField txt : new JTextField[]{txtBuscarProducto, txtBuscarVenta, txtBuscarCliente}) {
            txt.setBackground(fondoBuscador);
            txt.setForeground(textoBuscador);
            txt.setCaretColor(Color.WHITE);
        }

        

        

        // color oscuro que se verá en el área vacía debajo de las filas de datos
        Color fondoTablaVacia = new Color(30, 30, 40);

        // renderer personalizado para ventas y clientes:
        // swing pinta cada celda llamando a getTableCellRendererComponent, que devuelve
        // un componente visual. aqui sobreescribimos ese metodo para controlar el color.
        DefaultTableCellRenderer rendererBlanco = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // primero dejamos que el renderer por defecto aplique fuente, bordes, etc.
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    // fila normal (no seleccionada): fondo blanco y texto oscuro
                    setBackground(Color.WHITE);
                    setForeground(new Color(30, 30, 40));
                }
                // si la fila esta seleccionada, super() ya aplica el cian del UIManager
                return this;
            }
        };

        // setBackground en la tabla solo afecta al area vacia (sin celdas),
        // porque el renderer tiene prioridad sobre las celdas con datos
        tblProductos.setBackground(fondoTablaVacia);
        tblVentas.setBackground(fondoTablaVacia);
        tblVentas.setDefaultRenderer(Object.class, rendererBlanco);   // aplicamos el renderer a todas las celdas de tipo Object
        tblClientes.setBackground(fondoTablaVacia);
        tblClientes.setDefaultRenderer(Object.class, rendererBlanco); // tblProductos no necesita este renderer porque ya tiene el suyo para las filas naranjas

        cargarTablaProductos();//asi cargarmos las tablas nada mas empezar el programa
        cargarTablaClientes();
        cargarTablaVentas();
        cargarEstadisticas();
        cargarGraficaDashboard();
        iniciarTimerInactividad();

        // aviso de stock critico al arrancar — productos con menos de 5 unidades
        try {
            List<Producto> criticos = productoDAO.listarStockBajo(5);
            if (!criticos.isEmpty()) {
                StringBuilder sb = new StringBuilder("Productos con stock crítico (menos de 5 unidades):\n\n");
                for (Producto p : criticos) {
                    sb.append("  • ").append(p.getNombre()).append(" — ").append(p.getStock()).append(" ud.\n");
                }
                JOptionPane.showMessageDialog(this, sb.toString(), "Stock bajo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            // no interrumpimos el arranque si falla el aviso
        }
        
        // Cambia 'jLabelBanner' por el nombre de variable exacto que le diste a tu JLabel superior
        int anchoReal = lblsuperior.getWidth();
        int altoReal = lblsuperior.getHeight();

        // En NetBeans a veces el layout da 0 antes de renderizar la ventana por primera vez
        if (anchoReal == 0) anchoReal = 1010; // Pon aquí el ancho aproximado que tiene en tu diseñador
        if (altoReal == 0) altoReal = 100;
            
        // Reemplaza la ruta por la ubicación exacta de tu archivo dentro de tu source package
        lblsuperior.setIcon(calibrarBanner("/primetechfinal/fotos/superiorrecortado (1).png", anchoReal, altoReal));
    }

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        DialogProductos = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtPrecioCompra = new javax.swing.JTextField();
        txtPrecioVenta = new javax.swing.JTextField();
        txtStock = new javax.swing.JTextField();
        lblGuardarProducto = new javax.swing.JLabel();
        lblCancelarProducto = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        txtDescripcion = new javax.swing.JTextArea();
        lblFondoProductos = new javax.swing.JLabel();
        DialogClientes = new javax.swing.JDialog();
        cmbTipoCliente = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtNombreCliente = new javax.swing.JTextField();
        lblApellidos = new javax.swing.JLabel();
        txtApellidos = new javax.swing.JTextField();
        lblDni = new javax.swing.JLabel();
        txtDni = new javax.swing.JTextField();
        lblRazonSocial = new javax.swing.JLabel();
        txtRazonSocial = new javax.swing.JTextField();
        lblCif = new javax.swing.JLabel();
        txtCif = new javax.swing.JTextField();
        lblContacto = new javax.swing.JLabel();
        txtContacto = new javax.swing.JTextField();
        lblTelefono = new javax.swing.JLabel();
        txtTelefonoCliente = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtEmailCliente = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txtDireccionCliente = new javax.swing.JTextField();
        lblGuardarClientes = new javax.swing.JLabel();
        lblCancelarClientes = new javax.swing.JLabel();
        lblFondoClientes = new javax.swing.JLabel();
        DialogVenta = new javax.swing.JDialog();
        jLabel17 = new javax.swing.JLabel();
        cmbClienteVenta = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        cmbMetodoPago = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        cmbProductoVenta = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        txtCantidadVenta = new javax.swing.JTextField();
        btnAgregarVenta = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblCarrito = new javax.swing.JTable();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        lblGuardarVenta = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblFondoVentas = new javax.swing.JLabel();
        lblCerrarSesion = new javax.swing.JLabel();
        lblEmpleado = new javax.swing.JLabel();
        pnlContenido = new javax.swing.JPanel();
        pnlProductos = new javax.swing.JPanel();
        txtBuscarProducto = new javax.swing.JTextField();
        btnBuscarProducto = new javax.swing.JButton();
        btnNuevoProducto = new javax.swing.JButton();
        btnEditarProducto = new javax.swing.JButton();
        btnEliminarProducto = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductos = new javax.swing.JTable();
        pnlVentas = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        lblVentasHoy = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        lblVentasMes = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        lblProductoTop = new javax.swing.JLabel();
        btnNuevaVenta = new javax.swing.JButton();
        btnVerFactura = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblVentas = new javax.swing.JTable();
        txtBuscarVenta = new javax.swing.JTextField();
        btnBuscarVenta = new javax.swing.JButton();
        btnEditarVenta = new javax.swing.JButton();
        btnEliminarVenta = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        pnlClientes = new javax.swing.JPanel();
        btnNuevoCliente = new javax.swing.JButton();
        txtBuscarCliente = new javax.swing.JTextField();
        btnBuscarCliente = new javax.swing.JButton();
        btnEditarCliente = new javax.swing.JButton();
        btnEliminarProducto1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        pnlDashboard = new javax.swing.JPanel();
        btnExportarExcel = new javax.swing.JButton();
        lblNavDashboard = new javax.swing.JLabel();
        lblNavVentas = new javax.swing.JLabel();
        lblNavProductos = new javax.swing.JLabel();
        lblNavClientes = new javax.swing.JLabel();
        lblsuperior = new javax.swing.JLabel();
        lblderecho = new javax.swing.JLabel();
        pnlHeader = new javax.swing.JPanel();
        btnCerrarPantalla = new javax.swing.JButton();
        btnMinimizarPantalla = new javax.swing.JButton();

        DialogProductos.setMaximumSize(new java.awt.Dimension(877, 717));
        DialogProductos.setMinimumSize(new java.awt.Dimension(877, 717));
        DialogProductos.setPreferredSize(new java.awt.Dimension(877, 717));
        DialogProductos.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Nombre:");
        DialogProductos.getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 230, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Descripción:");
        DialogProductos.getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 340, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Precio Compra:");
        DialogProductos.getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 550, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Precio Venta:");
        DialogProductos.getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 230, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Stock:");
        DialogProductos.getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 430, -1, -1));
        DialogProductos.getContentPane().add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 230, 160, 30));
        DialogProductos.getContentPane().add(txtPrecioCompra, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 550, 110, 30));
        DialogProductos.getContentPane().add(txtPrecioVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 230, 120, 30));
        DialogProductos.getContentPane().add(txtStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 430, 120, 30));

        lblGuardarProducto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblGuardarProducto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblGuardarProductoMouseClicked(evt);
            }
        });
        DialogProductos.getContentPane().add(lblGuardarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 626, 80, 40));

        lblCancelarProducto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblCancelarProducto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCancelarProductoMouseClicked(evt);
            }
        });
        DialogProductos.getContentPane().add(lblCancelarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 626, 70, 40));

        txtDescripcion.setColumns(20);
        txtDescripcion.setRows(5);
        jScrollPane6.setViewportView(txtDescripcion);

        DialogProductos.getContentPane().add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(84, 410, 260, -1));

        lblFondoProductos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/fondodialogproductos.png"))); // NOI18N
        DialogProductos.getContentPane().add(lblFondoProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 720));

        DialogClientes.setMaximumSize(new java.awt.Dimension(932, 751));
        DialogClientes.setMinimumSize(new java.awt.Dimension(932, 751));
        DialogClientes.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cmbTipoCliente.setFont(new java.awt.Font("Segoe UI", 2, 18)); // NOI18N
        cmbTipoCliente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "particular", "empresa" }));
        cmbTipoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTipoClienteActionPerformed(evt);
            }
        });
        DialogClientes.getContentPane().add(cmbTipoCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 180, 150, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Tipo:");
        DialogClientes.getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 170, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Nombre:");
        DialogClientes.getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 540, -1, -1));
        DialogClientes.getContentPane().add(txtNombreCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 540, 150, -1));

        lblApellidos.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblApellidos.setForeground(new java.awt.Color(255, 255, 255));
        lblApellidos.setText("Apellidos:");
        DialogClientes.getContentPane().add(lblApellidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 180, -1, -1));
        DialogClientes.getContentPane().add(txtApellidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 190, 150, -1));

        lblDni.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblDni.setForeground(new java.awt.Color(255, 255, 255));
        lblDni.setText("DNI:");
        DialogClientes.getContentPane().add(lblDni, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 270, -1, -1));
        DialogClientes.getContentPane().add(txtDni, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 280, 150, -1));

        lblRazonSocial.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblRazonSocial.setForeground(new java.awt.Color(255, 255, 255));
        lblRazonSocial.setText("Razón Social:");
        DialogClientes.getContentPane().add(lblRazonSocial, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 360, -1, -1));
        DialogClientes.getContentPane().add(txtRazonSocial, new org.netbeans.lib.awtextra.AbsoluteConstraints(720, 370, 150, -1));

        lblCif.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblCif.setForeground(new java.awt.Color(255, 255, 255));
        lblCif.setText("CIF:");
        DialogClientes.getContentPane().add(lblCif, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 460, -1, -1));
        DialogClientes.getContentPane().add(txtCif, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 470, 150, -1));

        lblContacto.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblContacto.setForeground(new java.awt.Color(255, 255, 255));
        lblContacto.setText("Contacto:");
        DialogClientes.getContentPane().add(lblContacto, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 530, -1, -1));
        DialogClientes.getContentPane().add(txtContacto, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 540, 150, -1));

        lblTelefono.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTelefono.setForeground(new java.awt.Color(255, 255, 255));
        lblTelefono.setText("Teléfono:");
        DialogClientes.getContentPane().add(lblTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 270, -1, -1));
        DialogClientes.getContentPane().add(txtTelefonoCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 280, 140, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Email:");
        DialogClientes.getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 360, -1, -1));
        DialogClientes.getContentPane().add(txtEmailCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 370, 140, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Dirección:");
        DialogClientes.getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 460, -1, -1));
        DialogClientes.getContentPane().add(txtDireccionCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 470, 140, -1));

        lblGuardarClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblGuardarClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblGuardarClientesMouseClicked(evt);
            }
        });
        DialogClientes.getContentPane().add(lblGuardarClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 656, 70, 40));

        lblCancelarClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblCancelarClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCancelarClientesMouseClicked(evt);
            }
        });
        DialogClientes.getContentPane().add(lblCancelarClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 656, 70, 40));

        lblFondoClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/fondodialogclientes.png"))); // NOI18N
        DialogClientes.getContentPane().add(lblFondoClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        DialogVenta.setMinimumSize(new java.awt.Dimension(1077, 745));
        DialogVenta.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Cliente:");
        DialogVenta.getContentPane().add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 260, -1, -1));

        cmbClienteVenta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        cmbClienteVenta.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " " }));
        DialogVenta.getContentPane().add(cmbClienteVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 270, 170, -1));

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("Método de Pago:");
        DialogVenta.getContentPane().add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 340, -1, -1));

        cmbMetodoPago.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        cmbMetodoPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "efectivo", "tarjeta", "transferencia", " " }));
        DialogVenta.getContentPane().add(cmbMetodoPago, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 340, 170, -1));

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setText("Producto:");
        DialogVenta.getContentPane().add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 410, -1, -1));

        cmbProductoVenta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        DialogVenta.getContentPane().add(cmbProductoVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 410, 170, -1));

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("Cantidad:");
        DialogVenta.getContentPane().add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 470, -1, -1));

        txtCantidadVenta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        txtCantidadVenta.setText("1");
        DialogVenta.getContentPane().add(txtCantidadVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 470, 170, -1));

        btnAgregarVenta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnAgregarVenta.setText("Agregar");
        btnAgregarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarVentaActionPerformed(evt);
            }
        });
        DialogVenta.getContentPane().add(btnAgregarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 580, 150, 50));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("CARRITO");
        DialogVenta.getContentPane().add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 150, -1, -1));

        tblCarrito.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Producto", "Cantidad", "P. Unit", "Subtotal"
            }
        ));
        jScrollPane4.setViewportView(tblCarrito);

        DialogVenta.getContentPane().add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 220, 500, 350));

        jLabel22.setFont(new java.awt.Font("Segoe UI", 3, 36)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Total:");
        DialogVenta.getContentPane().add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 580, -1, -1));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 3, 36)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("0.00 €");
        DialogVenta.getContentPane().add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 580, -1, -1));

        lblGuardarVenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblGuardarVenta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblGuardarVentaMouseClicked(evt);
            }
        });
        DialogVenta.getContentPane().add(lblGuardarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 650, 90, 40));

        jLabel7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });
        DialogVenta.getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 650, 80, 40));

        lblFondoVentas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/fondodialogventas.png"))); // NOI18N
        DialogVenta.getContentPane().add(lblFondoVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, -1));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblCerrarSesion.setForeground(new java.awt.Color(0, 102, 255));
        lblCerrarSesion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/cerrar-sesion.png"))); // NOI18N
        lblCerrarSesion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblCerrarSesion.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblCerrarSesionMouseClicked(evt);
            }
        });
        getContentPane().add(lblCerrarSesion, new org.netbeans.lib.awtextra.AbsoluteConstraints(1200, 50, -1, -1));

        lblEmpleado.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblEmpleado.setForeground(new java.awt.Color(153, 255, 153));
        lblEmpleado.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        getContentPane().add(lblEmpleado, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 40, 220, 40));

        pnlContenido.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlProductos.setBackground(new java.awt.Color(30, 30, 40));
        pnlProductos.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        txtBuscarProducto.setToolTipText("Buscar Producto");
        txtBuscarProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarProductoKeyReleased(evt);
            }
        });
        pnlProductos.add(txtBuscarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 22, 190, 30));

        btnBuscarProducto.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnBuscarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/borrar.png"))); // NOI18N
        btnBuscarProducto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarProductoActionPerformed(evt);
            }
        });
        pnlProductos.add(btnBuscarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(990, 10, 50, 50));

        btnNuevoProducto.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnNuevoProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/nuevoproducto.png"))); // NOI18N
        btnNuevoProducto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevoProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoProductoActionPerformed(evt);
            }
        });
        pnlProductos.add(btnNuevoProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 110, 80, 80));

        btnEditarProducto.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEditarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/editarproducto.png"))); // NOI18N
        btnEditarProducto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarProductoActionPerformed(evt);
            }
        });
        pnlProductos.add(btnEditarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 240, 80, 80));

        btnEliminarProducto.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEliminarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/eliminarproducto.png"))); // NOI18N
        btnEliminarProducto.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarProductoActionPerformed(evt);
            }
        });
        pnlProductos.add(btnEliminarProducto, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 360, 80, 80));

        tblProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nombre", "Descripción", "Precio C", "Precio V", "Stock"
            }
        ));
        jScrollPane1.setViewportView(tblProductos);

        pnlProductos.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 950, 440));

        pnlContenido.add(pnlProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 520));

        pnlVentas.setBackground(new java.awt.Color(30, 30, 40));
        pnlVentas.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel13.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Ventas Hoy:");
        pnlVentas.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        lblVentasHoy.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        lblVentasHoy.setForeground(new java.awt.Color(0, 204, 255));
        lblVentasHoy.setText("0.00 €");
        pnlVentas.add(lblVentasHoy, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, -1, -1));

        jLabel14.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("Ventas este mes:");
        pnlVentas.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 20, -1, -1));

        lblVentasMes.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        lblVentasMes.setForeground(new java.awt.Color(0, 204, 255));
        lblVentasMes.setText("0");
        pnlVentas.add(lblVentasMes, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 20, -1, -1));

        jLabel15.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Más vendido:");
        pnlVentas.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, -1, -1));

        lblProductoTop.setFont(new java.awt.Font("Roboto", 1, 18)); // NOI18N
        lblProductoTop.setForeground(new java.awt.Color(0, 204, 255));
        lblProductoTop.setText("-");
        pnlVentas.add(lblProductoTop, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, 560, 20));

        btnNuevaVenta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnNuevaVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/nuevaventa.png"))); // NOI18N
        btnNuevaVenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevaVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevaVentaActionPerformed(evt);
            }
        });
        pnlVentas.add(btnNuevaVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 80, 100, 70));

        btnVerFactura.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnVerFactura.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/factura.png"))); // NOI18N
        btnVerFactura.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVerFactura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerFacturaActionPerformed(evt);
            }
        });
        pnlVentas.add(btnVerFactura, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 430, 100, 70));

        tblVentas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Cliente", "Fecha", "Total", "Método Pago"
            }
        ));
        jScrollPane3.setViewportView(tblVentas);

        pnlVentas.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 940, 420));

        txtBuscarVenta.setToolTipText("Buscar Venta");
        txtBuscarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarVentaActionPerformed(evt);
            }
        });
        txtBuscarVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarVentaKeyReleased(evt);
            }
        });
        pnlVentas.add(txtBuscarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 20, 220, 40));

        btnBuscarVenta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnBuscarVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/borrar.png"))); // NOI18N
        btnBuscarVenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarVentaActionPerformed(evt);
            }
        });
        pnlVentas.add(btnBuscarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(1000, 20, 50, 50));

        btnEditarVenta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEditarVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/editarventa.png"))); // NOI18N
        btnEditarVenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarVentaActionPerformed(evt);
            }
        });
        pnlVentas.add(btnEditarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 170, 100, 70));

        btnEliminarVenta.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEliminarVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/eliminarventa.png"))); // NOI18N
        btnEliminarVenta.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarVentaActionPerformed(evt);
            }
        });
        pnlVentas.add(btnEliminarVenta, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 260, 100, 70));

        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/enviarcorreo.png"))); // NOI18N
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        pnlVentas.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 350, 100, 70));

        pnlContenido.add(pnlVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 520));

        pnlClientes.setBackground(new java.awt.Color(30, 30, 40));
        pnlClientes.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnNuevoCliente.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnNuevoCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/nuevocliente.png"))); // NOI18N
        btnNuevoCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnNuevoCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoClienteActionPerformed(evt);
            }
        });
        pnlClientes.add(btnNuevoCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 110, 90, -1));

        txtBuscarCliente.setToolTipText("Buscar Cliente");
        txtBuscarCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarClienteKeyReleased(evt);
            }
        });
        pnlClientes.add(txtBuscarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 30, 190, -1));

        btnBuscarCliente.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBuscarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/borrar.png"))); // NOI18N
        btnBuscarCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarClienteActionPerformed(evt);
            }
        });
        pnlClientes.add(btnBuscarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(930, 20, 60, -1));

        btnEditarCliente.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/editarcliente.png"))); // NOI18N
        btnEditarCliente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEditarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarClienteActionPerformed(evt);
            }
        });
        pnlClientes.add(btnEditarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 230, 90, -1));

        btnEliminarProducto1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEliminarProducto1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/eliminarcliente.png"))); // NOI18N
        btnEliminarProducto1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEliminarProducto1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarProducto1ActionPerformed(evt);
            }
        });
        pnlClientes.add(btnEliminarProducto1, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 340, 90, -1));

        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Tipo", "Nombre/Razón Social", "Teléfono", "Email"
            }
        ));
        jScrollPane2.setViewportView(tblClientes);

        pnlClientes.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 900, 380));

        pnlContenido.add(pnlClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 520));

        pnlDashboard.setBackground(new java.awt.Color(30, 30, 40));
        pnlDashboard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlContenido.add(pnlDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1080, 520));

        getContentPane().add(pnlContenido, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 140, 1080, 520));

        btnExportarExcel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnExportarExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/excel.png"))); // NOI18N
        btnExportarExcel.setText("Exportar");
        btnExportarExcel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnExportarExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportarExcelActionPerformed(evt);
            }
        });
        getContentPane().add(btnExportarExcel, new org.netbeans.lib.awtextra.AbsoluteConstraints(1100, 540, 170, -1));

        lblNavDashboard.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblNavDashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblNavDashboardMouseClicked(evt);
            }
        });
        getContentPane().add(lblNavDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 480, 180, 40));

        lblNavVentas.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblNavVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblNavVentasMouseClicked(evt);
            }
        });
        getContentPane().add(lblNavVentas, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 310, 180, 40));

        lblNavProductos.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblNavProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblNavProductosMouseClicked(evt);
            }
        });
        getContentPane().add(lblNavProductos, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 360, 180, 40));

        lblNavClientes.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblNavClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblNavClientesMouseClicked(evt);
            }
        });
        getContentPane().add(lblNavClientes, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 420, 180, 40));

        lblsuperior.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/superiorrecortado (1).png"))); // NOI18N
        getContentPane().add(lblsuperior, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 1280, 100));

        lblderecho.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/derechofinal.png"))); // NOI18N
        getContentPane().add(lblderecho, new org.netbeans.lib.awtextra.AbsoluteConstraints(1080, 140, 200, 520));

        pnlHeader.setBackground(new java.awt.Color(255, 255, 255));
        pnlHeader.setCursor(new java.awt.Cursor(java.awt.Cursor.MOVE_CURSOR));
        pnlHeader.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnCerrarPantalla.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/cerrar.png"))); // NOI18N
        btnCerrarPantalla.setBorder(null);
        btnCerrarPantalla.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCerrarPantalla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarPantallaActionPerformed(evt);
            }
        });
        pnlHeader.add(btnCerrarPantalla, new org.netbeans.lib.awtextra.AbsoluteConstraints(1240, 0, 40, 40));

        btnMinimizarPantalla.setIcon(new javax.swing.ImageIcon(getClass().getResource("/primetechfinal/fotos/minimizar.png"))); // NOI18N
        btnMinimizarPantalla.setBorder(null);
        btnMinimizarPantalla.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMinimizarPantalla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMinimizarPantallaActionPerformed(evt);
            }
        });
        pnlHeader.add(btnMinimizarPantalla, new org.netbeans.lib.awtextra.AbsoluteConstraints(1192, 0, 40, 40));

        getContentPane().add(pnlHeader, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1280, 40));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbTipoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTipoClienteActionPerformed
         actualizarVisibilidadCampos();
    }//GEN-LAST:event_cmbTipoClienteActionPerformed

    private void btnAgregarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarVentaActionPerformed
        int idxProducto = cmbProductoVenta.getSelectedIndex();
        if (idxProducto < 0 || idxProducto >= productosCombo.size()) return;
        try {
            int cantidad = Integer.parseInt(txtCantidadVenta.getText());
            if (cantidad <= 0) { JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que 0."); return; }
            // obtenemos el producto directamente por índice, sin buscar por nombre
            Producto p = productosCombo.get(idxProducto);
            double subtotal = cantidad * p.getPrecioVenta();
            DefaultTableModel m = (DefaultTableModel) tblCarrito.getModel();
            m.addRow(new Object[]{p.getNombre(), cantidad, p.getPrecioVenta(), subtotal});
            recalcularTotal();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número.");
        }
    }//GEN-LAST:event_btnAgregarVentaActionPerformed

    private void lblCerrarSesionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCerrarSesionMouseClicked
        int confirm = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?");
        if (confirm != JOptionPane.YES_OPTION) return;//mostramos una confirmación
        Sesion.cerrar();
        new LoginFrame().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_lblCerrarSesionMouseClicked

    private void btnExportarExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportarExcelActionPerformed
        javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
        selector.setDialogTitle("Guardar Excel");
        selector.setSelectedFile(new java.io.File("informe_primetech.xlsx"));

        int resultado = selector.showSaveDialog(this);
        if (resultado == javax.swing.JFileChooser.APPROVE_OPTION) {
            String ruta = selector.getSelectedFile().getAbsolutePath();
            if (!ruta.endsWith(".xlsx")) ruta += ".xlsx";
            try {
                primetechfinal.util.ExportarExcel.exportarTodo(ruta);
                JOptionPane.showMessageDialog(this, "Excel exportado correctamente en:\n" + ruta);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnExportarExcelActionPerformed

    private void btnEliminarProducto1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarProducto1ActionPerformed
        int fila = tblClientes.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar cliente?");
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            clienteDAO.eliminar((int) tblClientes.getValueAt(fila, 0));
            cargarTablaClientes();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnEliminarProducto1ActionPerformed

    private void btnEditarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarClienteActionPerformed
        int fila = tblClientes.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente.");
            return;
        }
        clienteActual = new Cliente();
        clienteActual.setIdCliente((int) tblClientes.getValueAt(fila, 0));
        clienteActual.setTipo((String) tblClientes.getValueAt(fila, 1));
        limpiarCamposCliente();
        // cargamos tipo en el combobox
        cmbTipoCliente.setSelectedItem(clienteActual.getTipo());
        actualizarVisibilidadCampos();
        // cargamos nombre visible en el campo correspondiente
        if (clienteActual.getTipo().equals("particular")) {
            // separamos nombre y apellidos del nombre visible (columna 2)
            txtNombreCliente.setText((String) tblClientes.getValueAt(fila, 2));
        } else {
            txtRazonSocial.setText((String) tblClientes.getValueAt(fila, 2));
        }
        txtTelefonoCliente.setText((String) tblClientes.getValueAt(fila, 3));
        txtEmailCliente.setText((String) tblClientes.getValueAt(fila, 4));
        DialogClientes.setVisible(true);
    }//GEN-LAST:event_btnEditarClienteActionPerformed

    private void btnBuscarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarClienteActionPerformed
        txtBuscarCliente.setText("");
        cargarTablaClientes();
    }//GEN-LAST:event_btnBuscarClienteActionPerformed

    private void btnNuevoClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoClienteActionPerformed
        clienteActual = null;
        limpiarCamposCliente();
        cmbTipoCliente.setSelectedIndex(0); // selecciona particular por defecto
        actualizarVisibilidadCampos();
        DialogClientes.setVisible(true);
    }//GEN-LAST:event_btnNuevoClienteActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int fila = tblVentas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una venta primero.");
            return;
        }

        int idVenta = (int) tblVentas.getValueAt(fila, 0);

        try {
            Venta venta = ventaDAO.cargarVentaCompleta(idVenta);
            EnviarEmail.enviarFactura(venta, "Prime Tech Systems");
            JOptionPane.showMessageDialog(this, "Factura enviada correctamente a " + venta.getEmailCliente());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al enviar el email: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btnEliminarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarVentaActionPerformed
        int fila = tblVentas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una venta.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar la venta seleccionada?");
        if (confirm != JOptionPane.YES_OPTION) return;//panel de confirmacion
        try {
            ventaDAO.eliminar((int) tblVentas.getValueAt(fila, 0));
            cargarTablaVentas();
            cargarTablaProductos(); // el stock se restaura al eliminar una venta
            cargarEstadisticas();
            cargarGraficaDashboard(); // actualizamos el dashboard al eliminar una venta
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnEliminarVentaActionPerformed

    private void btnEditarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarVentaActionPerformed
        int fila = tblVentas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una venta.");
            return;
        }
        try {
            int id = (int) tblVentas.getValueAt(fila, 0);
            ventaActual = ventaDAO.cargarVentaCompleta(id);
            if (ventaActual == null) return;//si no hay venta volvemos, quizas es redundante, pero por si acaso
            // preparamos el dialog con los datos actuales
            limpiarDialogVenta();
            // seleccionamos el cliente en el combobox buscando por ID en clientesCombo
            if (ventaActual.getIdCliente() > 0) {
                for (int i = 0; i < clientesCombo.size(); i++) {
                    if (clientesCombo.get(i).getIdCliente() == ventaActual.getIdCliente()) {
                        cmbClienteVenta.setSelectedIndex(i + 1); // +1 porque índice 0 es "Sin cliente"
                        break;
                    }
                }
            }
            // método de pago
            cmbMetodoPago.setSelectedItem(ventaActual.getMetodoPago());
            // cargamos los productos del carrito
            DefaultTableModel m = (DefaultTableModel) tblCarrito.getModel();
            m.setRowCount(0);
            for (DetalleVenta d : ventaActual.getDetalles()) {
                m.addRow(new Object[]{
                    d.getNombreProducto(), d.getCantidad(),
                    d.getPrecioUnitario(), d.getCantidad() * d.getPrecioUnitario()
                });
            }
            recalcularTotal();
            DialogVenta.setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnEditarVentaActionPerformed

    private void btnBuscarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarVentaActionPerformed
        txtBuscarVenta.setText("");
        cargarTablaVentas();
    }//GEN-LAST:event_btnBuscarVentaActionPerformed

    private void txtBuscarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBuscarVentaActionPerformed

    private void btnVerFacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerFacturaActionPerformed
        int fila = tblVentas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una venta.");
            return;
        }
        try {
            int id = (int) tblVentas.getValueAt(fila, 0);
            Venta v = ventaDAO.cargarVentaCompleta(id);
            FacturaHTML.generarYAbrir(v, "PRIME TECH SYSTEMS");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnVerFacturaActionPerformed

    private void btnNuevaVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevaVentaActionPerformed
        ventaActual = null; // indicamos que es venta nueva
        limpiarDialogVenta();
        DialogVenta.setVisible(true);
    }//GEN-LAST:event_btnNuevaVentaActionPerformed

    private void btnEliminarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarProductoActionPerformed
        int fila = tblProductos.getSelectedRow();//obtenemos fila seleccionada
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto.");//si no hay nada seleccionado
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar producto?");//confirmacion para eliminar producto
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            productoDAO.eliminar((int) tblProductos.getValueAt(fila, 0));//le pasamos el id, y el metodo.eliminar ya sabe eliminar toda la fila
            cargarTablaProductos();//actualizamos tabla
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnEliminarProductoActionPerformed

    private void btnEditarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarProductoActionPerformed
        int fila = tblProductos.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto.");
            return;
        }
        productoActual = new Producto();
        productoActual.setIdProducto((int) tblProductos.getValueAt(fila, 0));
        limpiarCamposProducto();
        txtNombre.setText((String) tblProductos.getValueAt(fila, 1));
        txtDescripcion.setText((String) tblProductos.getValueAt(fila, 2));
        txtPrecioCompra.setText(String.valueOf(tblProductos.getValueAt(fila, 3)));
        txtPrecioVenta.setText(String.valueOf(tblProductos.getValueAt(fila, 4)));
        txtStock.setText(String.valueOf(tblProductos.getValueAt(fila, 5)));
        DialogProductos.setVisible(true);
    }//GEN-LAST:event_btnEditarProductoActionPerformed

    private void btnNuevoProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoProductoActionPerformed
        // ponemos null para indicar que estamos creando un producto nuevo
        productoActual = null;
        limpiarCamposProducto();
        DialogProductos.setVisible(true);
    }//GEN-LAST:event_btnNuevoProductoActionPerformed

    private void btnBuscarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarProductoActionPerformed
        txtBuscarProducto.setText("");
        cargarTablaProductos();
    }//GEN-LAST:event_btnBuscarProductoActionPerformed

    private void lblNavVentasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNavVentasMouseClicked
        mostrarPanel(pnlVentas);
    }//GEN-LAST:event_lblNavVentasMouseClicked

    private void lblNavProductosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNavProductosMouseClicked
        mostrarPanel(pnlProductos);
    }//GEN-LAST:event_lblNavProductosMouseClicked

    private void lblNavClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNavClientesMouseClicked
        mostrarPanel(pnlClientes);
    }//GEN-LAST:event_lblNavClientesMouseClicked

    private void lblNavDashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblNavDashboardMouseClicked
        mostrarPanel(pnlDashboard);
    }//GEN-LAST:event_lblNavDashboardMouseClicked

    private void btnCerrarPantallaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarPantallaActionPerformed
        // cerrar
        System.exit(0);
    }//GEN-LAST:event_btnCerrarPantallaActionPerformed

    private void btnMinimizarPantallaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMinimizarPantallaActionPerformed
        // minimizar
        setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_btnMinimizarPantallaActionPerformed

    private void txtBuscarProductoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarProductoKeyReleased
        try {
            DefaultTableModel m = (DefaultTableModel) tblProductos.getModel();
            m.setRowCount(0);
            for (Producto p : productoDAO.buscarPorNombre(txtBuscarProducto.getText())) {
                m.addRow(new Object[]{
                    p.getIdProducto(), p.getNombre(), p.getDescripcion(),
                    p.getPrecioCompra(), p.getPrecioVenta(), p.getStock()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_txtBuscarProductoKeyReleased

    private void txtBuscarVentaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarVentaKeyReleased
        try {
            DefaultTableModel m = (DefaultTableModel) tblVentas.getModel();
            m.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Venta v : ventaDAO.buscarPorCliente(txtBuscarVenta.getText())) {
                String fecha = v.getFechaVenta() != null ? v.getFechaVenta().format(fmt) : "-";
                String total = String.format("%.2f €", v.getTotal());
                m.addRow(new Object[]{
                    v.getIdVenta(), v.getNombreCliente(),
                    fecha, total, v.getMetodoPago()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_txtBuscarVentaKeyReleased

    private void txtBuscarClienteKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarClienteKeyReleased
        try {
            DefaultTableModel m = (DefaultTableModel) tblClientes.getModel();
            m.setRowCount(0);
            for (Cliente c : clienteDAO.buscarPorNombre(txtBuscarCliente.getText())) {
                m.addRow(new Object[]{
                    c.getIdCliente(), c.getTipo(),
                    c.getNombreVisible(), c.getTelefono(), c.getEmail()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_txtBuscarClienteKeyReleased

    private void lblGuardarProductoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblGuardarProductoMouseClicked
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        String precioCompraStr = txtPrecioCompra.getText().trim();
        String precioVentaStr = txtPrecioVenta.getText().trim();
        String stockStr = txtStock.getText().trim();

        if (nombre.isEmpty() || precioCompraStr.isEmpty() || precioVentaStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Rellena todos los campos obligatorios.");
            return;
        }

        try {
            double precioCompra = Double.parseDouble(precioCompraStr);
            double precioVenta  = Double.parseDouble(precioVentaStr);
            int stock           = Integer.parseInt(stockStr);

            Producto p = new Producto();
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setPrecioCompra(precioCompra);
            p.setPrecioVenta(precioVenta);
            p.setStock(stock);

            if (productoActual == null) {
                // NUEVO producto
                productoDAO.insertar(p);
                JOptionPane.showMessageDialog(this, "Producto añadido correctamente.");
            } else {
                // EDITAR producto existente
                p.setIdProducto(productoActual.getIdProducto());
                productoDAO.actualizar(p);
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.");
            }

            cargarTablaProductos();
            limpiarCamposProducto();
            DialogProductos.setVisible(false);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Precio y stock deben ser números válidos.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage());
        }
    }//GEN-LAST:event_lblGuardarProductoMouseClicked

    private void lblCancelarProductoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCancelarProductoMouseClicked
        limpiarCamposProducto();
        DialogProductos.setVisible(false);
    }//GEN-LAST:event_lblCancelarProductoMouseClicked

    private void lblGuardarClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblGuardarClientesMouseClicked
        try {
        Cliente c = clienteActual != null ? clienteActual : new Cliente();
        c.setTelefono(txtTelefonoCliente.getText());
        c.setEmail(txtEmailCliente.getText());
        c.setDireccion(txtDireccionCliente.getText());
        boolean esParticular = cmbTipoCliente.getSelectedItem().equals("particular");
        if (esParticular) {
            c.setNombre(txtNombreCliente.getText());
            c.setApellidos(txtApellidos.getText());
            c.setDni(txtDni.getText());
            if (clienteActual != null) clienteDAO.actualizarParticular(c);
            else                       clienteDAO.insertarParticular(c);
        } else {
            c.setRazonSocial(txtRazonSocial.getText());
            c.setCif(txtCif.getText());
            c.setContactoNombre(txtContacto.getText());
            if (clienteActual != null) clienteDAO.actualizarEmpresa(c);
            else                       clienteDAO.insertarEmpresa(c);
        }
        DialogClientes.setVisible(false);
        cargarTablaClientes();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_lblGuardarClientesMouseClicked

    private void lblCancelarClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblCancelarClientesMouseClicked
        DialogClientes.setVisible(false);
    }//GEN-LAST:event_lblCancelarClientesMouseClicked

    private void lblGuardarVentaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblGuardarVentaMouseClicked
        DefaultTableModel m = (DefaultTableModel) tblCarrito.getModel();
        if (m.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Añade productos al carrito.");
            return;
        }
        try {
            Venta v = ventaActual != null ? ventaActual : new Venta();
            // cliente: usamos clientesCombo por índice (índice 0 = Sin cliente)
            int idxCliente = cmbClienteVenta.getSelectedIndex();
            if (idxCliente > 0 && idxCliente - 1 < clientesCombo.size()) {
                v.setIdCliente(clientesCombo.get(idxCliente - 1).getIdCliente());
            } else {
                v.setIdCliente(0); // sin cliente
            }
            v.setIdEmpleado(Sesion.getEmpleado().getIdEmpleado());
            v.setMetodoPago(cmbMetodoPago.getSelectedItem().toString());
            // reconstruimos los detalles desde el carrito
            v.getDetalles().clear();
            for (int i = 0; i < m.getRowCount(); i++) {
                String nombre = (String) m.getValueAt(i, 0);
                int cantidad  = (int)    m.getValueAt(i, 1);
                double precio = (double) m.getValueAt(i, 2);
                // buscamos el producto por nombre exacto (guardamos getNombre() en el carrito)
                List<Producto> lista = productoDAO.buscarPorNombre(nombre);
                if (!lista.isEmpty()) {
                    v.getDetalles().add(new DetalleVenta(lista.get(0).getIdProducto(), nombre, cantidad, precio));
                }
            }
            if (ventaActual != null) ventaDAO.actualizar(v);//decidimos si actualiza o inserta
            else                     ventaDAO.registrarVenta(v);
            DialogVenta.setVisible(false);
            cargarTablaVentas();
            cargarTablaProductos(); // el stock cambia al registrar o editar una venta
            cargarEstadisticas();
            cargarGraficaDashboard(); // actualizamos el dashboard con los nuevos datos
            JOptionPane.showMessageDialog(this, ventaActual != null ? "Venta actualizada correctamente." : "Venta registrada correctamente.");//operacion ternaria ahorramos espacio, mejor que hacer if else
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }//GEN-LAST:event_lblGuardarVentaMouseClicked

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        DialogVenta.setVisible(false);
    }//GEN-LAST:event_jLabel7MouseClicked

    private void cargarTablaProductos() {
        try {
            DefaultTableModel m = (DefaultTableModel) tblProductos.getModel();
            m.setRowCount(0);
            for (Producto p : productoDAO.listarTodos()) {
                m.addRow(new Object[]{
                    p.getIdProducto(), p.getNombre(), p.getDescripcion(),
                    p.getPrecioCompra(), p.getPrecioVenta(), p.getStock()
                });
            }
            //Tambien me he ayudado de la ia
            // pintamos las filas segun el nivel de stock (columna 5):
            //   rojo/naranja  stock < 5  (critico, hay que reponer ya)
            //   amarillo      stock < 10 (bajo, avisar pronto)
            //   blanco        stock normal
            //   verde         stock > 25 (bien abastecido)
            //   rojo          stock < 5 (critico)
            tblProductos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
                    Object stockVal = table.getValueAt(row, 5);
                    if (stockVal != null) {
                        int stock = ((Number) stockVal).intValue();
                        if (!isSelected) {
                            if (stock < 5) {
                                c.setBackground(new Color(220, 60, 60));   // rojo: critico
                            } else if (stock < 10) {
                                c.setBackground(new Color(255, 245, 130)); // amarillo: stock bajo
                            } else if (stock > 25) {
                                c.setBackground(new Color(180, 230, 180)); // verde: bien abastecido
                            } else {
                                c.setBackground(Color.WHITE);              // normal
                            }
                            c.setForeground(Color.BLACK);
                        } else {
                            // fila seleccionada: usamos los colores del UIManager (cian)
                            c.setBackground(table.getSelectionBackground());
                            c.setForeground(table.getSelectionForeground());
                        }
                    }
                    return c;
                }
            });

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    private void cargarTablaClientes() {
        try {
            DefaultTableModel m = (DefaultTableModel) tblClientes.getModel();
            m.setRowCount(0);
            for (Cliente c : clienteDAO.listarTodos()) {
                m.addRow(new Object[]{
                    c.getIdCliente(), c.getTipo(),
                    c.getNombreVisible(), c.getTelefono(), c.getEmail()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
         }
    }
    private void cargarTablaVentas() {
        try {
            DefaultTableModel m = (DefaultTableModel) tblVentas.getModel();
            m.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Venta v : ventaDAO.listarTodas()) {
                String fecha = v.getFechaVenta() != null ? v.getFechaVenta().format(fmt) : "-";
                String total = String.format("%.2f €", v.getTotal());
                m.addRow(new Object[]{
                    v.getIdVenta(), v.getNombreCliente(),
                    fecha, total, v.getMetodoPago()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
        private void cargarEstadisticas() {
        try {
            lblVentasHoy.setText(String.format("%.2f €", ventaDAO.totalVentasHoy()));
            lblVentasMes.setText(String.valueOf(ventaDAO.ventasMes()));
            lblProductoTop.setText(ventaDAO.productoMasVendido());
        } catch (SQLException ex) {
            lblVentasHoy.setText("Error");
            }
    }
    // genera la grafica de barras con las ventas de los ultimos 7 dias y la mete en pnlDashboard
    private void cargarGraficaDashboard() {
        try {
            // --- GRAFICA DE BARRAS: ventas de los ultimos 7 dias ---
            LinkedHashMap<String, Double> datosBarras = ventaDAO.ventasUltimos7Dias();
            org.jfree.data.category.DefaultCategoryDataset datasetBarras = new org.jfree.data.category.DefaultCategoryDataset();
            for (Map.Entry<String, Double> entry : datosBarras.entrySet()) {
                datasetBarras.addValue(entry.getValue(), "Ventas", entry.getKey());
            }
            org.jfree.chart.JFreeChart chartBarras = org.jfree.chart.ChartFactory.createBarChart(
                "Ventas últimos 7 días", "Día", "Total (€)", datasetBarras);
            org.jfree.chart.plot.CategoryPlot plot = chartBarras.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            ((org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer())
                .setSeriesPaint(0, new Color(0, 204, 255));

            // --- GRAFICA DE TARTA: top 5 productos mas vendidos ---
            LinkedHashMap<String, Integer> datosTarta = ventaDAO.top5ProductosMasVendidos();
            org.jfree.data.general.DefaultPieDataset datasetTarta = new org.jfree.data.general.DefaultPieDataset();
            for (Map.Entry<String, Integer> entry : datosTarta.entrySet()) {
                datasetTarta.setValue(entry.getKey(), entry.getValue());
            }
            org.jfree.chart.JFreeChart chartTarta = org.jfree.chart.ChartFactory.createPieChart(
                "Top 5 productos más vendidos", datasetTarta, true, true, false);
            chartTarta.getPlot().setBackgroundPaint(Color.WHITE);

            // ponemos las dos graficas lado a lado en el panel del dashboard
            org.jfree.chart.ChartPanel panelBarras = new org.jfree.chart.ChartPanel(chartBarras);
            org.jfree.chart.ChartPanel panelTarta  = new org.jfree.chart.ChartPanel(chartTarta);

            pnlDashboard.setLayout(new java.awt.GridLayout(1, 2));
            pnlDashboard.removeAll();
            pnlDashboard.add(panelBarras);
            pnlDashboard.add(panelTarta);
            pnlDashboard.revalidate();
            pnlDashboard.repaint();

        } catch (SQLException ex) {
            logger.warning("Error al cargar las graficas del dashboard: " + ex.getMessage());
        }
    }

    private void limpiarCamposProducto() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecioCompra.setText("");//ASI NO REPETIMOS CODIGO
        txtPrecioVenta.setText("");
        txtStock.setText("");
    }
    private void limpiarCamposCliente() {
        txtNombreCliente.setText("");
        txtApellidos.setText("");
        txtDni.setText("");
        txtRazonSocial.setText("");
        txtCif.setText("");
        txtContacto.setText("");
        txtTelefonoCliente.setText("");
        txtEmailCliente.setText("");
        txtDireccionCliente.setText("");
    }
    private void actualizarVisibilidadCampos() {
        boolean esParticular = cmbTipoCliente.getSelectedItem().equals("particular");
        lblApellidos.setVisible(esParticular);
        txtApellidos.setVisible(esParticular);
        lblDni.setVisible(esParticular);
        txtDni.setVisible(esParticular);
        lblRazonSocial.setVisible(!esParticular);//muestra o los campos de particulares o los de empresa según el combobox
        txtRazonSocial.setVisible(!esParticular);
        lblCif.setVisible(!esParticular);
        txtCif.setVisible(!esParticular);
        lblContacto.setVisible(!esParticular);
        txtContacto.setVisible(!esParticular);
    }
    private void limpiarDialogVenta() {
        try {
            // cargamos clientes en el combobox y en la lista paralela (índice 0 = Sin cliente)
            clientesCombo.clear();
            cmbClienteVenta.removeAllItems();
            cmbClienteVenta.addItem("Sin cliente");
            for (Cliente c : clienteDAO.listarTodos()) {
                clientesCombo.add(c);
                cmbClienteVenta.addItem(c.getNombreVisible()); // nombre legible, no toString()
            }
            // cargamos productos en el combobox y en la lista paralela
            productosCombo.clear();
            cmbProductoVenta.removeAllItems();
            for (Producto p : productoDAO.listarTodos()) {
                productosCombo.add(p);
                cmbProductoVenta.addItem(p.getNombre()); // nombre legible, no toString()
            }
            // limpiamos carrito
            ((DefaultTableModel) tblCarrito.getModel()).setRowCount(0);
            jLabel23.setText("0.00 €");
            txtCantidadVenta.setText("1");
            cmbMetodoPago.setSelectedIndex(0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
    }
    private void recalcularTotal() {
        // suma todos los subtotales del carrito y actualiza el label
        DefaultTableModel m = (DefaultTableModel) tblCarrito.getModel();
        double total = 0;
        for (int i = 0; i < m.getRowCount(); i++) {
            total += (double) m.getValueAt(i, 3);
        }
        jLabel23.setText(String.format("%.2f €", total));
    }
    private void mostrarPanel(JPanel panel) {
        pnlProductos.setVisible(false);
        pnlVentas.setVisible(false);
        pnlClientes.setVisible(false);
        pnlDashboard.setVisible(false);
        panel.setVisible(true);
    }
    // arranca el timer de inactividad y registra los listeners de raton y teclado
    private void iniciarTimerInactividad() {
        timerInactividad = new javax.swing.Timer(MINUTOS_INACTIVIDAD * 60 * 1000, e -> cerrarSesionPorInactividad());
        timerInactividad.setRepeats(false);
        timerInactividad.start();

        // cualquier movimiento de raton o tecla resetea el timer
        java.awt.event.AWTEventListener activityListener = event -> resetearTimer();
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
            activityListener,
            java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK |
            java.awt.AWTEvent.MOUSE_EVENT_MASK |
            java.awt.AWTEvent.KEY_EVENT_MASK
        );
    }

    // reinicia el contador de inactividad cada vez que hay actividad
    private void resetearTimer() {
        if (timerInactividad != null) {
            timerInactividad.restart();
        }
    }

    // cierra la sesion y vuelve al login cuando se agota el tiempo de inactividad
    private void cerrarSesionPorInactividad() {
        timerInactividad.stop();
        Sesion.cerrar();
        JOptionPane.showMessageDialog(this,
            "Sesión cerrada por inactividad.",
            "Sesión expirada",
            JOptionPane.WARNING_MESSAGE);
        new LoginFrame().setVisible(true);
        this.dispose();
    }

    public ImageIcon calibrarBanner(String rutaImagen, int widthLabel, int heightLabel) {
        //AYUDADO CON IA para darle formato original a la imagen
        // Carga la imagen original
        java.net.URL imgURL = getClass().getResource(rutaImagen);
        if (imgURL == null) {
            return null; // Por si acaso la ruta falla
        }
        ImageIcon iconoOriginal = new ImageIcon(imgURL);
        Image img = iconoOriginal.getImage();

        int anchoOriginal = img.getWidth(null);
        int altoOriginal = img.getHeight(null);

        // Calculamos la proporción idónea basándonos en el ancho disponible
        double proporcion = (double) widthLabel / anchoOriginal;

        // El nuevo alto respetará de forma estricta la relación de aspecto original
        int nuevoAncho = widthLabel;
        int nuevoAlto = (int) (altoOriginal * proporcion);

        // Si por algún motivo el alto calculado supera el del JLabel, ajustamos por el alto
        if (nuevoAlto > heightLabel) {
            proporcion = (double) heightLabel / altoOriginal;
            nuevoAncho = (int) (anchoOriginal * proporcion);
            nuevoAlto = heightLabel;
        }

        Image imgEscalada = img.getScaledInstance(nuevoAncho, nuevoAlto, Image.SCALE_SMOOTH);
        return new ImageIcon(imgEscalada);
    }
   
    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(() -> new Pantalla().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog DialogClientes;
    private javax.swing.JDialog DialogProductos;
    private javax.swing.JDialog DialogVenta;
    private javax.swing.JButton btnAgregarVenta;
    private javax.swing.JButton btnBuscarCliente;
    private javax.swing.JButton btnBuscarProducto;
    private javax.swing.JButton btnBuscarVenta;
    private javax.swing.JButton btnCerrarPantalla;
    private javax.swing.JButton btnEditarCliente;
    private javax.swing.JButton btnEditarProducto;
    private javax.swing.JButton btnEditarVenta;
    private javax.swing.JButton btnEliminarProducto;
    private javax.swing.JButton btnEliminarProducto1;
    private javax.swing.JButton btnEliminarVenta;
    private javax.swing.JButton btnExportarExcel;
    private javax.swing.JButton btnMinimizarPantalla;
    private javax.swing.JButton btnNuevaVenta;
    private javax.swing.JButton btnNuevoCliente;
    private javax.swing.JButton btnNuevoProducto;
    private javax.swing.JButton btnVerFactura;
    private javax.swing.JComboBox<String> cmbClienteVenta;
    private javax.swing.JComboBox<String> cmbMetodoPago;
    private javax.swing.JComboBox<String> cmbProductoVenta;
    private javax.swing.JComboBox<String> cmbTipoCliente;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JLabel lblApellidos;
    private javax.swing.JLabel lblCancelarClientes;
    private javax.swing.JLabel lblCancelarProducto;
    private javax.swing.JLabel lblCerrarSesion;
    private javax.swing.JLabel lblCif;
    private javax.swing.JLabel lblContacto;
    private javax.swing.JLabel lblDni;
    private javax.swing.JLabel lblEmpleado;
    private javax.swing.JLabel lblFondoClientes;
    private javax.swing.JLabel lblFondoProductos;
    private javax.swing.JLabel lblFondoVentas;
    private javax.swing.JLabel lblGuardarClientes;
    private javax.swing.JLabel lblGuardarProducto;
    private javax.swing.JLabel lblGuardarVenta;
    private javax.swing.JLabel lblNavClientes;
    private javax.swing.JLabel lblNavDashboard;
    private javax.swing.JLabel lblNavProductos;
    private javax.swing.JLabel lblNavVentas;
    private javax.swing.JLabel lblProductoTop;
    private javax.swing.JLabel lblRazonSocial;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblVentasHoy;
    private javax.swing.JLabel lblVentasMes;
    private javax.swing.JLabel lblderecho;
    private javax.swing.JLabel lblsuperior;
    private javax.swing.JPanel pnlClientes;
    private javax.swing.JPanel pnlContenido;
    private javax.swing.JPanel pnlDashboard;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlProductos;
    private javax.swing.JPanel pnlVentas;
    private javax.swing.JTable tblCarrito;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTable tblProductos;
    private javax.swing.JTable tblVentas;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtBuscarCliente;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtBuscarVenta;
    private javax.swing.JTextField txtCantidadVenta;
    private javax.swing.JTextField txtCif;
    private javax.swing.JTextField txtContacto;
    private javax.swing.JTextArea txtDescripcion;
    private javax.swing.JTextField txtDireccionCliente;
    private javax.swing.JTextField txtDni;
    private javax.swing.JTextField txtEmailCliente;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtNombreCliente;
    private javax.swing.JTextField txtPrecioCompra;
    private javax.swing.JTextField txtPrecioVenta;
    private javax.swing.JTextField txtRazonSocial;
    private javax.swing.JTextField txtStock;
    private javax.swing.JTextField txtTelefonoCliente;
    // End of variables declaration//GEN-END:variables
}
