# Lógica de desarrollo — Prime Tech Systems

**Alumno:** Jose Luis Perez  
**Módulo:** Programación — 1º DAM  
**Proyecto:** Aplicación de gestión para tienda de informática

---

## 1. Arranque de la aplicación

El punto de entrada es `PrimeTech.java`. Antes de abrir ninguna ventana, configuro FlatLaf y todos los estilos globales de la interfaz mediante `UIManager.put()` — botones redondeados en cian, fuente Roboto en tablas, colores de selección, etc. Es importante hacerlo aquí, antes de que se cree cualquier componente, para que los estilos se apliquen a toda la aplicación.

Después lanzo la `PantallaCarga` dentro de `SwingUtilities.invokeLater()`, que garantiza que todo lo visual se ejecuta en el hilo de Swing (EDT). Desde ahí arranco un `HiloCarga`, que es un hilo separado que:

1. Actualiza la barra de progreso en la pantalla de carga
2. Fuerza la inicialización del pool de conexiones de HikariCP llamando a `ConexionDB.getConexionApp().close()`
3. Cuando termina, cierra la pantalla de carga y abre el `LoginFrame` devolviendo el control al EDT con `SwingUtilities.invokeLater()`

La razón de usar un hilo separado es que conectar con la base de datos es una operación lenta. Si lo hiciera en el EDT, la pantalla de carga se congelaría y la barra de progreso no avanzaría.

---

## 2. Gestión de la conexión a la base de datos

`ConexionDB` gestiona dos pools de conexiones HikariCP:

- **poolApp** — para empleados normales (vendedores, técnicos). Solo tiene permisos de SELECT, INSERT, UPDATE y DELETE
- **poolAdmin** — para administradores. Tiene todos los privilegios

Los pools se inicializan una sola vez cuando la clase se carga en memoria, gracias al bloque `static {}`. La configuración (URL, usuario, contraseña) se lee de `config.properties`, que está dentro del JAR y nunca se sube al repositorio.

El método `getConexion()` decide qué pool usar según el cargo del empleado en sesión. Si todavía no hay sesión (por ejemplo durante la pantalla de carga), se usa `getConexionApp()` directamente.

---

## 3. Sistema de sesión

`Sesion` es una clase con un único atributo estático `empleadoActual`. Funciona como un registro global del empleado que está usando la aplicación en ese momento.

- Al hacer login correctamente → `Sesion.iniciar(empleado)`
- Al cerrar sesión o por inactividad → `Sesion.cerrar()`
- Cualquier parte de la aplicación puede consultar `Sesion.getEmpleado()` para saber quién está logueado

Esto permite que `ConexionDB` sepa en tiempo real si debe dar una conexión de admin o de app, sin necesidad de pasar el empleado como parámetro por todo el código.

---

## 4. Patrón DAO

Cada entidad del sistema (Producto, Venta, Cliente, Empleado) tiene su propia clase DAO que concentra todas las consultas SQL de esa entidad. La pantalla principal instancia los DAO una sola vez como variables de clase:

```java
private ProductoDAO productoDAO = new ProductoDAO();
private VentaDAO ventaDAO = new VentaDAO();
private ClienteDAO clienteDAO = new ClienteDAO();
```

Así no se crea un objeto nuevo cada vez que se necesita hacer una consulta. Los DAO piden una conexión al pool de HikariCP, ejecutan la consulta y devuelven la conexión al pool automáticamente.

---

## 5. Navegación entre paneles

En lugar de usar un `JTabbedPane`, la pantalla principal tiene cuatro paneles (`pnlProductos`, `pnlVentas`, `pnlClientes`, `pnlDashboard`) que ocupan el mismo espacio. Al iniciar solo se muestra `pnlProductos`; los demás están ocultos.

El método `mostrarPanel()` oculta todos y muestra solo el que corresponde:

```java
private void mostrarPanel(JPanel panel) {
    pnlProductos.setVisible(false);
    pnlVentas.setVisible(false);
    pnlClientes.setVisible(false);
    pnlDashboard.setVisible(false);
    panel.setVisible(true);
}
```

Los labels de navegación del lateral llaman a este método en su evento `mouseClicked`.

---

## 6. Lógica de creación y edición

Para productos, ventas y clientes uso el mismo dialog tanto para crear como para editar. La variable `productoActual` (y sus equivalentes para venta y cliente) actúa como indicador:

- Si es `null` → estamos creando uno nuevo
- Si tiene valor → estamos editando ese registro

Al pulsar guardar, el código comprueba esta variable para decidir si llamar a `insertar()` o `actualizar()` del DAO correspondiente.

---

## 7. Estilo visual de las tablas

Las tablas tienen tres capas de estilo:

1. **UIManager global** — define la cabecera (azul pizarra, texto blanco), el color de selección (cian) y la fuente (Roboto)
2. **Viewport del scrollpane** — el área vacía debajo de los datos sale oscura `[30, 30, 40]`
3. **Renderer personalizado** — controla el color de cada celda individualmente

Para ventas y clientes hay un `DefaultTableCellRenderer` que fuerza fondo blanco y texto oscuro en filas no seleccionadas. Para productos hay un renderer más complejo que colorea las filas según el nivel de stock:

| Stock | Color |
|---|---|
| < 5 | Naranja (crítico) |
| 5 – 9 | Amarillo (bajo) |
| 10 – 25 | Blanco (normal) |
| > 25 | Verde (bien abastecido) |

---

## 8. Búsqueda en tiempo real

Cada buscador tiene un evento `keyReleased` que llama al método de búsqueda del DAO correspondiente cada vez que el usuario suelta una tecla. El método consulta la base de datos con el texto actual y recarga la tabla con los resultados. Los botones que antes hacían la búsqueda ahora limpian el buscador y recargan la tabla completa.

---

## 9. Inactividad y cierre de sesión automático

Al abrir la pantalla principal se arranca un `javax.swing.Timer` configurado a 3 minutos. Cualquier movimiento de ratón o pulsación de teclado reinicia el timer. Si el timer llega a cero sin actividad, cierra la sesión automáticamente y vuelve al login.

---

## 10. CI/CD con GitHub Actions

El repositorio tiene dos workflows:

**ci.yml** — se ejecuta en cada push a `master`. Levanta un MySQL, crea la base de datos y ejecuta los tests de integración con Ant. Si algún test falla, el push se marca como fallido.

**release.yml** — se dispara al crear un tag `vX.Y`. Compila el proyecto, copia las dependencias necesarias a `dist/lib/`, genera scripts de arranque `.bat` y `.sh`, y publica un ZIP en GitHub Releases. Los scripts usan `-cp` en lugar de `-jar` para evitar el límite de 72 caracteres por línea del `MANIFEST.MF`.
