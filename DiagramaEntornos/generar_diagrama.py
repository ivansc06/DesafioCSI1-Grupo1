from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, HRFlowable
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT

OUTPUT = r"C:\Users\dpjos\Documents\DiagramaClases_PrimeTechFinal.pdf"

doc = SimpleDocTemplate(OUTPUT, pagesize=A4,
    leftMargin=15*mm, rightMargin=15*mm,
    topMargin=15*mm, bottomMargin=15*mm)

W = A4[0] - 30*mm

TITLE = ParagraphStyle("t",  fontSize=15, fontName="Helvetica-Bold", alignment=TA_CENTER, spaceAfter=4)
PACK  = ParagraphStyle("p",  fontSize=9,  fontName="Helvetica-Bold", alignment=TA_CENTER, textColor=colors.white)
CNAME = ParagraphStyle("cn", fontSize=8,  fontName="Helvetica-Bold", alignment=TA_CENTER)
ATTR  = ParagraphStyle("a",  fontSize=7,  fontName="Helvetica",      alignment=TA_LEFT, leading=10)
NOTE  = ParagraphStyle("n",  fontSize=7,  fontName="Helvetica-Oblique", alignment=TA_LEFT, textColor=colors.grey)
ARROW = ParagraphStyle("ar", fontSize=7,  fontName="Helvetica", alignment=TA_LEFT,
                        textColor=colors.HexColor("#555555"), leading=10,
                        backColor=colors.HexColor("#f8f9fa"),
                        borderColor=colors.HexColor("#bdc3c7"), borderWidth=1, borderPadding=4)
FLUJO = ParagraphStyle("fl", fontSize=7,  fontName="Courier", leading=11,
                        backColor=colors.HexColor("#f8f9fa"),
                        borderColor=colors.HexColor("#2c3e50"), borderWidth=1, borderPadding=6)
NOTA2 = ParagraphStyle("n2", fontSize=7,  fontName="Helvetica", leading=11,
                        borderColor=colors.HexColor("#bdc3c7"), borderWidth=1, borderPadding=6,
                        backColor=colors.HexColor("#f8f9fa"))

C_BLUE = colors.HexColor("#2c3e50")
C_GRN  = colors.HexColor("#27ae60")
C_GRAY = colors.HexColor("#7f8c8d")
C_TEAL = colors.HexColor("#16a085")
C_PURP = colors.HexColor("#8e44ad")
C_RED  = colors.HexColor("#c0392b")
BG     = colors.HexColor("#f4f6f7")
BRD    = colors.HexColor("#bdc3c7")

# ── helpers ──────────────────────────────────────────────────────────

def cls(nombre, attrs, mets, hc, w):
    rows = [[Paragraph(nombre, CNAME)]]
    if attrs: rows.append([Paragraph(attrs, ATTR)])
    if mets:  rows.append([Paragraph(mets,  ATTR)])
    st = [
        ("BOX",           (0,0), (-1,-1), 1,   BRD),
        ("LINEBELOW",     (0,0), ( 0, 0), 1,   BRD),
        ("BACKGROUND",    (0,0), ( 0, 0), hc),
        ("TEXTCOLOR",     (0,0), ( 0, 0), colors.white),
        ("FONTNAME",      (0,0), ( 0, 0), "Helvetica-Bold"),
        ("FONTSIZE",      (0,0), ( 0, 0), 8),
        ("ALIGN",         (0,0), (-1,-1), "CENTER"),
        ("VALIGN",        (0,0), (-1,-1), "TOP"),
        ("TOPPADDING",    (0,0), (-1,-1), 3),
        ("BOTTOMPADDING", (0,0), (-1,-1), 3),
        ("LEFTPADDING",   (0,0), (-1,-1), 4),
        ("RIGHTPADDING",  (0,0), (-1,-1), 4),
    ]
    if len(rows) > 1:
        st.append(("BACKGROUND", (0,1), (-1,-1), BG))
    if len(rows) > 2:
        st.append(("LINEABOVE",  (0,2), ( 0, 2), 0.5, BRD))
    t = Table(rows, colWidths=[w])
    t.setStyle(TableStyle(st))
    return t

def phdr(texto, color):
    t = Table([[Paragraph(texto, PACK)]], colWidths=[W])
    t.setStyle(TableStyle([
        ("BACKGROUND",    (0,0), (-1,-1), color),
        ("BOX",           (0,0), (-1,-1), 1, color),
        ("TOPPADDING",    (0,0), (-1,-1), 5),
        ("BOTTOMPADDING", (0,0), (-1,-1), 4),
    ]))
    return t

def row(*items_and_widths):
    items  = items_and_widths[0]
    widths = items_and_widths[1]
    t = Table([items], colWidths=widths, hAlign="LEFT")
    t.setStyle(TableStyle([
        ("VALIGN",       (0,0), (-1,-1), "TOP"),
        ("LEFTPADDING",  (0,0), (-1,-1), 1),
        ("RIGHTPADDING", (0,0), (-1,-1), 1),
    ]))
    return t

def sp(n=2): return Spacer(1, n*mm)

# ═════════════════════════════════════════════════════════════════════
story = []

story.append(Paragraph("Diagrama de Clases — PrimeTechFinal", TITLE))
story.append(HRFlowable(width=W, thickness=2, color=C_BLUE))
story.append(sp(4))

# ── primetech (UI) ──────────────────────────────────────────────────
story.append(phdr("primetech  (UI — Swing)", C_BLUE))
story.append(sp(2))
story.append(row(
    [cls("PrimeTech", "", "+ main(args[])", C_BLUE, 38*mm),
     cls("LoginFrame  extends JFrame",
         "- txtEmail : JTextField\n- txtPassword : JPasswordField\n- lblError : JLabel\n- btnIniciarSesion : JButton",
         "+ iniciarSesion()", C_BLUE, 70*mm),
     cls("Pantalla  extends JFrame",
         "- lblEmpleado : JLabel\n- btnClientes : JButton\n- btnEmpleados : JButton\n- btnProductos : JButton\n- btnVentas : JButton\n- btnCerrarSesion : JButton",
         "", C_BLUE, 67*mm)],
    [38*mm, 70*mm, 67*mm]))
story.append(sp(1))
story.append(Paragraph(
    "PrimeTech ──→ LoginFrame (lanza)     |     LoginFrame ──→ Sesion.iniciar()  ──→  Pantalla (abre)", ARROW))
story.append(sp(2))
story.append(Paragraph("  Dialogs — crear con la paleta de NetBeans:", NOTE))
story.append(sp(1))
story.append(row(
    [cls("ClientesDialog\nextends JDialog",  "- tabla : JTable", "+ cargarTabla()", C_GRAY, 42*mm),
     cls("EmpleadosDialog\nextends JDialog", "- tabla : JTable", "+ cargarTabla()", C_GRAY, 42*mm),
     cls("ProductosDialog\nextends JDialog", "- tabla : JTable", "+ cargarTabla()", C_GRAY, 42*mm),
     cls("VentasDialog\nextends JDialog",    "- tabla : JTable", "+ cargarTabla()", C_GRAY, 49*mm)],
    [42*mm, 42*mm, 42*mm, 49*mm]))
story.append(sp(4))

# ── primetech.sesion ────────────────────────────────────────────────
story.append(phdr("primetech.sesion", C_TEAL))
story.append(sp(2))
story.append(row(
    [cls("Sesion",
         "- empleadoActual : Empleado  {static}",
         "+ iniciar(emp : Empleado)  {static}\n+ cerrar()  {static}\n+ getEmpleado() : Empleado  {static}\n+ haySession() : boolean  {static}\n+ esAdmin() : boolean  {static}",
         C_TEAL, 100*mm),
     Paragraph(
         "<b>Patron: clase de utilidad estatica</b><br/><br/>"
         "* Constructor private - nunca se instancia<br/>"
         "* Todos los miembros son static<br/>"
         "* Actua como variable global segura<br/><br/>"
         "Usada por: LoginFrame, Pantalla,<br/>"
         "todos los Dialogs y ConexionDB",
         ParagraphStyle("b2", fontSize=7, fontName="Helvetica", leading=11,
                        borderColor=C_TEAL, borderWidth=1, borderPadding=6,
                        backColor=colors.HexColor("#e8f8f5")))],
    [100*mm, 75*mm]))
story.append(sp(4))

# ── primetech.model ─────────────────────────────────────────────────
story.append(phdr("primetech.model", C_GRN))
story.append(sp(2))
story.append(row(
    [cls("Empleado",
         "- idEmpleado : int\n- nombre : String\n- apellidos : String\n- cargo : String\n- email : String\n- contrasena : String",
         "+ getNombreCompleto()\n+ getters / setters", C_GRN, 43*mm),
     cls("Cliente",
         "- idCliente : int\n- tipo : String\n- telefono / email\n- direccion : String\n[ Particular ]\n- nombre, apellidos, dni\n[ Empresa ]\n- razonSocial, cif\n- contactoNombre",
         "+ getNombreVisible()\n+ getters / setters", C_GRN, 46*mm),
     cls("Producto",
         "- idProducto : int\n- nombre : String\n- descripcion : String\n- precioCompra : double\n- precioVenta : double\n- stock : int",
         "+ getters / setters", C_GRN, 43*mm),
     cls("Venta",
         "- idVenta : int\n- idCliente : int\n- idEmpleado : int\n- fechaVenta : LocalDateTime\n- total : double\n- metodoPago : String\n- nombreCliente : String\n- detalles : List<DetalleVenta>",
         "+ getters / setters", C_GRN, 43*mm)],
    [43*mm, 46*mm, 43*mm, 43*mm]))
story.append(sp(1))
story.append(row(
    [cls("DetalleVenta",
         "- idDetalle : int\n- idVenta : int\n- idProducto : int\n- nombreProducto : String\n- cantidad : int\n- precioUnitario : double",
         "+ getSubtotal() : double\n+ getters / setters", C_GRN, 55*mm),
     Paragraph(
         "<b>Venta  1 ─────── *  DetalleVenta</b><br/><br/>"
         "Una Venta contiene una lista de DetalleVenta.<br/>"
         "Cada DetalleVenta referencia un Producto por id.",
         ParagraphStyle("r2", fontSize=7, fontName="Helvetica", leading=11,
                        borderColor=C_GRN, borderWidth=1, borderPadding=6,
                        backColor=colors.HexColor("#eafaf1")))],
    [55*mm, 120*mm]))
story.append(sp(4))

# ── primetech.dao ───────────────────────────────────────────────────
story.append(phdr("primetech.dao", C_PURP))
story.append(sp(2))
story.append(row(
    [cls("EmpleadoDAO", "",
         "+ login(email, pass) : Empleado\n+ buscarPorEmail() : Empleado\n+ listarTodos() : List\n+ insertar(Empleado)\n+ actualizar(Empleado)\n+ actualizarContrasena()\n+ eliminar(id)", C_PURP, 44*mm),
     cls("ClienteDAO", "",
         "+ listarTodos() : List\n+ listarParticulares() : List\n+ listarEmpresas() : List\n+ insertarParticular(Cliente)\n+ insertarEmpresa(Cliente)\n+ eliminar(id)", C_PURP, 44*mm),
     cls("ProductoDAO", "",
         "+ listarTodos() : List\n+ buscarPorNombre() : List\n+ buscarPorId() : Producto\n+ insertar(Producto)\n+ actualizar(Producto)\n+ eliminar(id)", C_PURP, 44*mm),
     cls("VentaDAO", "",
         "+ registrarVenta() : int\n+ listarTodas() : List\n+ cargarDetalles() : List\n+ cargarVentaCompleta()\n+ totalVentasHoy() : double\n+ ventasMes() : int\n+ productoMasVendido() : String", C_PURP, 43*mm)],
    [44*mm, 44*mm, 44*mm, 43*mm]))
story.append(sp(1))
story.append(Paragraph(
    "Todos los DAOs ──→ ConexionDB.getConexion()     |     EmpleadoDAO ──→ BCrypt (hashpw / checkpw)", ARROW))
story.append(sp(4))

# ── primetech.db ────────────────────────────────────────────────────
story.append(phdr("primetech.db", C_RED))
story.append(sp(2))
story.append(cls("ConexionDB",
    "- URL, USUARIO_APP, USUARIO_ADMIN, CLAVE_APP, CLAVE_ADMIN  {static}\n"
    "- conexionApp : Connection  {static}          - conexionAdmin : Connection  {static}",
    "+ getConexion() : Connection  {static}   ->  lee Sesion.cargo  ->  admin: tienda_admin  |  otros: tienda_app\n"
    "+ getConexionApp() : Connection  {static}        (uso durante el login, sin sesion activa)\n"
    "+ getConexionAdmin() : Connection  {static}      (uso exclusivo de HashearContrasenas)\n"
    "+ cerrar()  {static}",
    C_RED, W))
story.append(sp(4))

# ── primetech.util ──────────────────────────────────────────────────
story.append(phdr("primetech.util", C_GRAY))
story.append(sp(2))
story.append(row(
    [cls("BCrypt", "",
         "+ hashpw(password, salt) : String  {static}\n+ checkpw(plain, hashed) : boolean  {static}\n+ gensalt() : String  {static}",
         C_GRAY, 62*mm),
     cls("FacturaHTML", "",
         "+ generarYAbrir(venta, empresa) : Path  {static}\n- generarHTML() : String\n- formatEuro() : String\n- escape() : String",
         C_GRAY, 65*mm),
     cls("HashearContrasenas", "",
         "+ main(args[])  {static}\n+ migrarContrasenas()  {static}\n\n  EJECUTAR SOLO UNA VEZ",
         C_GRAY, 48*mm)],
    [62*mm, 65*mm, 48*mm]))
story.append(sp(5))

# ── Flujo principal ─────────────────────────────────────────────────
story.append(HRFlowable(width=W, thickness=1, color=C_BLUE))
story.append(sp(2))
story.append(Paragraph("Flujo principal de la aplicacion",
    ParagraphStyle("fh", fontSize=9, fontName="Helvetica-Bold", spaceAfter=3)))
story.append(Paragraph(
    "PrimeTech.main()  ->  LoginFrame  ->  EmpleadoDAO.login()  ->  ConexionDB.getConexionApp()  [sin sesion]<br/>"
    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    "->  Sesion.iniciar(empleado)<br/>"
    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    "->  Pantalla  ->  boton modulo  ->  new XxxDialog().setVisible(true)<br/>"
    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    "->  XxxDAO  ->  ConexionDB.getConexion()<br/>"
    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    "cargo=admin  ->  tienda_admin<br/>"
    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    "otro cargo  ->  tienda_app",
    FLUJO))

doc.build(story)
print("PDF generado en:", OUTPUT)
