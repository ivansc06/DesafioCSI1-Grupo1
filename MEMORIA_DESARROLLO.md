# Memoria de Desarrollo — Prime Tech Systems

**Alumno:** Jose Luis Perez  
**Módulo:** Programación — 1º DAM  
**Proyecto:** Aplicación de gestión para tienda de informática

---

## 1. Descripción del proyecto

He desarrollado una aplicación de escritorio en Java Swing llamada **Prime Tech Systems**, pensada para gestionar una tienda de informática. La aplicación permite controlar el inventario de productos, registrar ventas, gestionar clientes y visualizar estadísticas. Todo funciona contra una base de datos MySQL local.

---

## 2. Tecnologías utilizadas

Desde el principio tuve claro que quería usar Java con Swing para la interfaz, ya que es lo que estábamos viendo en clase. Para la base de datos elegí MySQL porque ya lo conocía. A lo largo del desarrollo fui añadiendo librerías externas según las necesitaba:

- **HikariCP** para el pool de conexiones a la base de datos, evitando abrir y cerrar conexiones constantemente
- **FlatLaf** para mejorar el aspecto visual de la interfaz sin tener que diseñar componentes desde cero
- **Apache POI** para exportar datos a Excel
- **JavaMail** para enviar facturas por correo electrónico
- **JFreeChart** para mostrar gráficas en el dashboard
- **ZXing** para generar códigos QR en las facturas
- **Log4j2** para registrar logs de acceso y errores
- **BCrypt** para cifrar las contraseñas de los empleados

---

## 3. Base de datos

Diseñé la base de datos con varias tablas: productos, clientes, ventas, líneas de venta y empleados. Creé dos usuarios de MySQL con distintos permisos:

- `tienda_admin` con todos los privilegios, para tareas de administración
- `tienda_app` solo con SELECT, INSERT, UPDATE y DELETE, que es el usuario que usa la aplicación

Para facilitar el despliegue, escribí un script SQL (`Crear_Base_De_Datos_Final.sql`) que crea toda la estructura desde cero, incluyendo datos de ejemplo y triggers.

---

## 4. Estructura de la aplicación

Organicé el código siguiendo el patrón DAO (Data Access Object):

- `dao/` — clases que se comunican con la base de datos (ProductoDAO, VentaDAO, ClienteDAO...)
- `model/` — clases del modelo (Producto, Venta, Cliente...)
- `sesion/` — gestión de sesión del empleado logueado
- `util/` — utilidades: generación de facturas HTML, envío de email, exportación a Excel, generación de QR

La pantalla principal (`Pantalla.java`) es la más grande del proyecto y contiene toda la lógica de la interfaz.

---

## 5. Desarrollo de la interfaz

### Navegación sin pestañas

Al principio usé un `JTabbedPane` para separar las secciones, pero decidí quitarlo para tener más control sobre el diseño. Lo reemplacé por cuatro paneles (`pnlProductos`, `pnlVentas`, `pnlClientes`, `pnlDashboard`) que ocupan el mismo espacio. Al iniciar la aplicación solo se muestra el panel de productos; los demás están ocultos. Creé un método `mostrarPanel()` que oculta todos y muestra solo el que corresponde, y lo llamo desde los eventos de los labels de navegación del lateral.

### Mejoras visuales con FlatLaf

Apliqué FlatLaf como Look & Feel para modernizar la interfaz. Configuré los estilos globales en `PrimeTech.java`, antes de abrir cualquier ventana, para que se apliquen a toda la aplicación:

- Botones redondeados con fondo cian y texto blanco
- Selección de filas de tabla en cian
- Scrollbar discreta
- Sin borde de foco en los componentes

Para los campos de búsqueda apliqué el estilo directamente sobre cada componente (no con UIManager) para no afectar al formulario de login.

### Estilo de las tablas

Las tablas tienen la cabecera en azul pizarra con texto blanco. Las filas con datos salen en blanco con texto oscuro. El área vacía debajo de los datos sale en el mismo color oscuro que el fondo de la aplicación. Para conseguir esto tuve que combinar `setBackground()` en la tabla con un renderer personalizado en ventas y clientes, porque el renderer por defecto heredaba el color del fondo.

### Formato de datos en la tabla de ventas

Los datos que llegaban de la base de datos no se mostraban bien. Las fechas aparecían en formato ISO (`2026-05-06T11:35:38`) y los totales sin formato (`249.0`). Lo corregí aplicando un `DateTimeFormatter` con el patrón `dd/MM/yyyy HH:mm` y formateando el total con `String.format("%.2f €", total)`.

---

## 6. CI/CD con GitHub Actions

Configuré dos workflows en GitHub Actions:

**CI (`ci.yml`):** se ejecuta automáticamente en cada push a `master`. Levanta un contenedor de MySQL, crea la base de datos, compila el proyecto con Ant y ejecuta los tests de integración. Así siempre sé si algo está roto antes de que llegue a producción.

**Release (`release.yml`):** se dispara al crear un tag con formato `vX.Y`. Compila el proyecto, copia exactamente las dependencias necesarias a `dist/lib/`, genera scripts de arranque para Windows (`.bat`) y Linux (`.sh`), empaqueta todo en un ZIP y lo publica como release en GitHub.

Los scripts de arranque usan `-cp "PrimeTechFinal.jar:lib/*"` en lugar de `-jar` porque el manifiesto del JAR tiene un límite de 72 caracteres por línea que impide incluir todos los JARs del classpath.

---

## 7. Problemas encontrados

Durante el desarrollo me encontré con varios problemas que tuve que resolver:

- **Conflicto de librerías de log4j:** al copiar todas las dependencias con `find`, se copiaban JARs extra del paquete de log4j que entraban en conflicto entre sí. Lo resolví especificando cada JAR por su ruta exacta.
- **Permisos en GitHub Actions:** al crear el release me daba error 403 porque faltaba el permiso `contents: write` en el workflow.
- **Formato YAML con heredocs:** al intentar crear el `.bat` con un heredoc dentro del YAML, la sintaxis de `@echo off` rompía el parser de YAML. Lo solucioné usando `printf` en su lugar.
- **Panels de navegación desalineados:** al mover los paneles dentro del diseñador de NetBeans, algunos tenían tamaños distintos. Tuve que igualarlos manualmente usando la opción "Same Size" del diseñador.
- **Renderer de tablas:** el `setBackground()` sobre la tabla no afectaba a las celdas porque el renderer por defecto tenía prioridad. Tuve que crear un renderer personalizado que forzara el fondo blanco en las filas con datos.

---

## 8. Funcionalidades implementadas

- Login con BCrypt y bloqueo automático por intentos fallidos
- Cierre de sesión automático por inactividad (3 minutos)
- CRUD completo de productos, ventas y clientes
- Alerta visual en productos con stock bajo (fila naranja cuando stock < 5)
- Dashboard con gráfica de barras (ventas últimos 7 días) y tarta (top 5 productos)
- Generación de facturas HTML con código QR incrustado
- Envío de facturas por email vía Gmail SMTP
- Exportación de datos a Excel
- Sistema de logs con Log4j2
- CI/CD automático con GitHub Actions
