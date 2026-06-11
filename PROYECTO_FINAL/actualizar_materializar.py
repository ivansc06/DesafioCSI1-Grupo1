import fitz

path = "MATERIALIZAR.pdf"
out  = "MATERIALIZAR_new.pdf"
doc  = fitz.open(path)

PW, PH = 595.27, 841.89
ML, MR = 62.7, 532.6
MT = 95
MB = 782

BLUE  = (50/255, 89/255, 122/255)
DARK  = (34/255, 34/255, 34/255)
GRAY  = (136/255, 136/255, 136/255)
WHITE = (1, 1, 1)

NEW_TOTAL = 14  # 11 existentes + 3 nuevas

# ── wrap text ────────────────────────────────────────────────
def wrap(text, width_pt, fs=10, fn="helv"):
    words = text.split()
    lines, cur, cw = [], [], 0
    sp = fitz.get_text_length(" ", fontname=fn, fontsize=fs)
    for w in words:
        ww = fitz.get_text_length(w, fontname=fn, fontsize=fs)
        if cur and cw + sp + ww > width_pt:
            lines.append(" ".join(cur)); cur = [w]; cw = ww
        else:
            cw = cw + sp + ww if cur else ww; cur.append(w)
    if cur:
        lines.append(" ".join(cur))
    return lines

# ── footer ───────────────────────────────────────────────────
def footer(page, pnum, total):
    page.insert_text((ML - 6, 803),
        "Prime Tech Systems - Materializar",
        fontname="helv", fontsize=9, color=GRAY)
    t = f"Pagina {pnum} de {total}"
    tw = fitz.get_text_length(t, fontname="helv", fontsize=9)
    page.insert_text((MR - tw, 806), t,
        fontname="helv", fontsize=9, color=GRAY)

# ── writer ───────────────────────────────────────────────────
class W:
    def __init__(self, pg, y=None):
        self.pg = pg
        self.y = y or MT

    def h1(self, text):
        self.y += 8
        self.pg.insert_text((ML, self.y + 18), text,
            fontname="hebo", fontsize=16, color=BLUE)
        self.y += 30

    def h2(self, text):
        self.y += 10
        self.pg.insert_text((ML, self.y + 13), text,
            fontname="hebo", fontsize=12, color=BLUE)
        self.y += 22

    def body(self, text, indent=0):
        for line in wrap(text, MR - ML - indent, 10, "helv"):
            if self.y + 14 > MB:
                break
            self.pg.insert_text((ML + indent, self.y + 11), line,
                fontname="helv", fontsize=10, color=DARK)
            self.y += 14
        self.y += 4

    def bullet(self, text):
        bx, tx = ML, ML + 14
        for i, line in enumerate(wrap(text, MR - tx, 10, "helv")):
            if self.y + 14 > MB:
                break
            if i == 0:
                self.pg.insert_text((bx, self.y + 11), chr(8226),
                    fontname="helv", fontsize=10, color=DARK)
            self.pg.insert_text((tx, self.y + 11), line,
                fontname="helv", fontsize=10, color=DARK)
            self.y += 14
        self.y += 2

    def sp(self, n=6):
        self.y += n


# ══════════════════════════════════════════════════════════════
# 1. FOOTERS EXISTENTES: "de 11" → "de 14"
# ══════════════════════════════════════════════════════════════
for i in range(len(doc)):
    pg = doc[i]
    targets = []
    for b in pg.get_text("dict")["blocks"]:
        if "lines" not in b:
            continue
        for line in b["lines"]:
            for span in line["spans"]:
                if ("gina" in span["text"]) and "de 11" in span["text"]:
                    targets.append(span)
    for span in targets:
        bb = span["bbox"]
        pg.add_redact_annot(
            fitz.Rect(bb[0] - 1, bb[1] - 1, bb[2] + 5, bb[3] + 1),
            fill=WHITE)
    pg.apply_redactions()
    for span in targets:
        new_t = span["text"].replace("de 11", "de 14")
        pg.insert_text(
            (span["bbox"][0], span["bbox"][3] - 1),
            new_t, fontname="helv", fontsize=9, color=GRAY)
    if targets:
        print(f"  Footer p.{i+1}: actualizado")

# ══════════════════════════════════════════════════════════════
# 2. ÍNDICE (página 2): añadir secciones 18, 19, 20
# ══════════════════════════════════════════════════════════════
pg2 = doc[1]
entries = [
    ("18. Forma de programacion",         "12"),
    ("19. Tipos de configuraciones",       "13"),
    ("20. Ejecucion en multiplataforma",   "14"),
]
ey = 537
for text, num in entries:
    pg2.insert_text((93, ey + 11), text,
        fontname="helv", fontsize=10, color=DARK)
    nw = fitz.get_text_length(num, fontname="helv", fontsize=10)
    pg2.insert_text((491.8 - nw, ey + 12), num,
        fontname="helv", fontsize=10, color=DARK)
    ey += 24
print("Indice actualizado")

# ══════════════════════════════════════════════════════════════
# 3. PÁGINA 12 — Sección 18: Forma de programación
# ══════════════════════════════════════════════════════════════
doc.insert_page(-1, width=PW, height=PH)
w = W(doc[-1])

w.h1("18. Forma de programacion")
w.body(
    "El proyecto combina tres paradigmas: orientado a objetos, orientado a "
    "eventos y concurrente. Esta combinacion es la habitual en aplicaciones "
    "de escritorio Java con Swing."
)

w.h2("18.1 Orientado a objetos (OOP)")
w.body(
    "Cada entidad del negocio tiene su clase modelo y su DAO asociado. "
    "La interfaz nunca accede directamente a la base de datos; "
    "lo hace siempre a traves del DAO correspondiente."
)
w.bullet(
    "Encapsulacion: toda la SQL esta dentro de los DAOs. "
    "Pantalla.java solo llama a metodos como productoDAO.insertar(p)."
)
w.bullet(
    "Herencia: los renderers extienden DefaultTableCellRenderer y "
    "los listeners de arrastre extienden MouseAdapter y MouseMotionAdapter."
)
w.bullet(
    "Composicion: Pantalla.java instancia los DAOs una sola vez como "
    "variables de clase para reutilizarlos en todas las operaciones."
)
w.bullet(
    "Polimorfismo: getNombreVisible() devuelve nombre+apellidos para "
    "particulares y razonSocial para empresas."
)

w.h2("18.2 Orientado a eventos (Swing)")
w.body(
    "La interfaz reacciona a acciones del usuario mediante listeners. "
    "Cualquier modificacion de componentes visuales debe ocurrir "
    "en el hilo EDT (Event Dispatch Thread) de Swing."
)
w.bullet("ActionListener en botones: confirmar guardar, eliminar o exportar.")
w.bullet("MouseListener en labels del sidebar: navegacion entre paneles.")
w.bullet(
    "KeyListener (keyReleased) en buscadores: filtra la tabla en "
    "tiempo real sin necesidad de pulsar Enter."
)
w.bullet(
    "SwingUtilities.invokeLater() garantiza que actualizaciones visuales "
    "iniciadas desde hilos secundarios se ejecuten en el EDT."
)

w.h2("18.3 Programacion concurrente")
w.body(
    "Varias operaciones se ejecutan en hilos separados para no bloquear la interfaz."
)
w.bullet(
    "HiloCarga extiende Thread. Realiza la conexion inicial a la BD "
    "mientras la pantalla de carga muestra el progreso en la barra."
)
w.bullet(
    "javax.swing.Timer (inactividad): se configura con setRepeats(false) "
    "y cierra la sesion tras 3 minutos sin actividad."
)
w.bullet(
    "HikariCP gestiona un pool interno de conexiones con maximo 5 "
    "simultaneas para tienda_app y 3 para tienda_admin."
)

footer(doc[-1], 12, NEW_TOTAL)
print("Pagina 12 generada")

# ══════════════════════════════════════════════════════════════
# 4. PÁGINA 13 — Sección 19: Tipos de configuraciones
# ══════════════════════════════════════════════════════════════
doc.insert_page(-1, width=PW, height=PH)
w = W(doc[-1])

w.h1("19. Tipos de configuraciones")
w.body(
    "La aplicacion usa cinco fuentes de configuracion distintas, cada una "
    "con una responsabilidad concreta. Los archivos con credenciales estan "
    "en .gitignore y nunca se suben al repositorio."
)

w.h2("19.1 config.properties (dentro del JAR)")
w.body(
    "Contiene la URL JDBC, el usuario y la contrasena de los dos usuarios "
    "de base de datos (tienda_app y tienda_admin). Se carga con "
    "getClass().getResourceAsStream() al arrancar la aplicacion."
)

w.h2("19.2 email.properties (local, gitignoreado)")
w.body(
    "Contiene las credenciales de Gmail (correo y contrasena de aplicacion) "
    "para el envio de facturas via SMTP. Solo existe localmente; "
    "el repositorio no contiene ninguna credencial de correo."
)

w.h2("19.3 log4j2.xml")
w.body(
    "Configura Log4j2 con dos appenders: consola (desarrollo) y archivo "
    "con rotacion diaria en logs/primetech.log (produccion). "
    "La carpeta logs/ esta en .gitignore para no acumular logs en el repo."
)

w.h2("19.4 UIManager — estilos visuales (FlatLaf)")
w.body(
    "No es un archivo externo. Es un bloque de UIManager.put() en "
    "PrimeTech.java que se ejecuta antes de crear cualquier componente Swing. "
    "Define colores de botones, fuentes de tabla, cabeceras y scrollbar "
    "de forma global para toda la aplicacion."
)

w.h2("19.5 HikariCP — pool de conexiones (ConexionDB.java)")
w.body(
    "Configurado mediante HikariConfig en el bloque static de ConexionDB. "
    "Los parametros clave son: maximumPoolSize (5 para app, 3 para admin), "
    "minimumIdle 0 (sin conexiones abiertas en reposo) y "
    "connectionTimeout de 30 segundos."
)

footer(doc[-1], 13, NEW_TOTAL)
print("Pagina 13 generada")

# ══════════════════════════════════════════════════════════════
# 5. PÁGINA 14 — Sección 20: Ejecución en multiplataforma
# ══════════════════════════════════════════════════════════════
doc.insert_page(-1, width=PW, height=PH)
w = W(doc[-1])

w.h1("20. Ejecucion en multiplataforma")
w.body(
    "El proyecto esta escrito en Java puro. El bytecode compilado (.class "
    "empaquetado en el JAR) es identico en cualquier sistema operativo; "
    "la JVM instalada en cada maquina lo ejecuta de forma nativa sin recompilar."
)

w.h2("20.1 La JVM como capa de abstraccion")
w.body(
    "Todas las dependencias (HikariCP, BCrypt, Log4j2, JFreeChart, "
    "Apache POI, ZXing, Jakarta Mail) son JARs estandar sin binarios nativos. "
    "Con Java 11 o superior y MySQL 8.0 accesible, la aplicacion "
    "funciona en Windows, Linux y macOS sin ninguna adaptacion."
)

w.h2("20.2 Scripts de arranque — Windows y Linux/Mac")
w.body(
    "El workflow release.yml de GitHub Actions genera automaticamente "
    "dos scripts al publicar una nueva version:"
)
w.bullet(
    "PrimeTech.bat (Windows): separador de classpath punto y coma. "
    "java --enable-native-access=ALL-UNNAMED "
    "-cp \"PrimeTechFinal.jar;lib/*\" primetechfinal.PrimeTech"
)
w.bullet(
    "PrimeTech.sh (Linux/Mac): separador de classpath dos puntos. "
    "java --enable-native-access=ALL-UNNAMED "
    "-cp \"PrimeTechFinal.jar:lib/*\" primetechfinal.PrimeTech"
)
w.body(
    "Se usa java -cp en lugar de java -jar porque el MANIFEST.MF tiene "
    "un limite de 72 caracteres por linea que impide incluir todos los "
    "JARs en la cabecera Class-Path."
)

w.h2("20.3 Instalador nativo con jpackage (Windows)")
w.body(
    "Para Windows se dispone ademas de un instalador generado con jpackage "
    "usando la opcion --type app-image. Empaqueta la aplicacion junto con "
    "una JVM propia eliminando la necesidad de tener Java instalado "
    "previamente en el equipo destino."
)
w.bullet(
    "El script crear_exe.bat en la raiz del proyecto Java automatiza "
    "el proceso: compila, genera el JAR gordo y ejecuta jpackage."
)
w.bullet(
    "El resultado es PrimeTechSystems.exe dentro de la carpeta "
    "instalador/PrimeTechSystems/."
)

w.h2("20.4 Requisitos minimos del sistema")
w.bullet("Java 11 o superior (solo si se usa el ZIP de GitHub Releases).")
w.bullet(
    "MySQL Server 8.0 con el script "
    "Crear_Base_De_Datos_Final.sql ejecutado previamente."
)
w.bullet(
    "Acceso a internet solo si se quiere enviar facturas por email (opcional)."
)
w.bullet(
    "En Linux/Mac: ejecutar chmod +x PrimeTech.sh antes de la "
    "primera ejecucion."
)

footer(doc[-1], 14, NEW_TOTAL)
print("Pagina 14 generada")

# ══════════════════════════════════════════════════════════════
# 6. GUARDAR
# ══════════════════════════════════════════════════════════════
doc.save(out)
print(f"\nGuardado: {out}  |  Total paginas: {len(doc)}")
