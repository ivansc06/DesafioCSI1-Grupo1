# 🖥️ Prime Tech Systems

![CI](https://github.com/ivansc06/DesafioCSI1-Grupo1/actions/workflows/ci.yml/badge.svg)
![Release](https://img.shields.io/github/v/release/ivansc06/DesafioCSI1-Grupo1?label=release&color=brightgreen)
![Java](https://img.shields.io/badge/Java-8-orange?logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![License](https://img.shields.io/badge/license-MIT-green)

Aplicación de gestión para una tienda de informática desarrollada en Java Swing como proyecto de 1º DAM.

---

## ✨ Funcionalidades

- 📦 **Gestión de productos** — altas, bajas, ediciones y búsqueda. Alerta visual (fila naranja) cuando el stock baja de 5 unidades
- 💰 **Gestión de ventas** — registro de ventas con carrito, método de pago y cliente asociado
- 👥 **Gestión de clientes** — particulares y empresas con todos sus datos de contacto
- 📊 **Dashboard** — gráfica de barras (ventas últimos 7 días) y gráfica de tarta (top 5 productos más vendidos) con JFreeChart. Se actualiza automáticamente al registrar o eliminar una venta
- 📄 **Facturas HTML** — generación automática de facturas con diseño profesional al realizar una venta, con código QR incrustado
- 🔲 **Código QR** — cada factura incluye un QR con el resumen de la compra generado con ZXing
- 📧 **Envío de facturas por email** — envío de la factura HTML al cliente vía Gmail SMTP
- 📁 **Exportar a Excel** — exportación de productos, ventas y clientes con Apache POI
- 🔒 **Sistema de login** — autenticación con BCrypt, bloqueo automático por intentos fallidos y roles de empleado
- 🔐 **Conexión SSL** — comunicación cifrada con la base de datos MySQL mediante SSL
- ⏱️ **Bloqueo por inactividad** — la sesión se cierra automáticamente tras 3 minutos sin actividad
- 📝 **Sistema de logs** — registro de accesos, errores y operaciones con Log4j2

---

## 🛠️ Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java + Swing | 8 | Interfaz gráfica |
| MySQL | 8.0 | Base de datos |
| HikariCP | 4.0.3 | Pool de conexiones con SSL |
| FlatLaf | 3.4 | Look & Feel moderno |
| Apache POI | 5.3.0 | Exportación a Excel |
| JavaMail | 2.0.1 | Envío de emails |
| JFreeChart | 1.5.4 | Gráficas del dashboard |
| ZXing | 3.5.3 | Generación de códigos QR |
| Log4j | 2.25.4 | Sistema de logs |
| JUnit | 4.13.2 | Tests de integración |
| GitHub Actions | — | CI/CD automático |

---

## 🚀 Instalación y configuración

### Requisitos
- Java 8 o superior
- MySQL 8.0
- Apache NetBeans (recomendado) o Ant

### Pasos

**1. Clonar el repositorio**
```bash
git clone https://github.com/ivansc06/DesafioCSI1-Grupo1.git
```

**2. Crear la base de datos**
```bash
mysql -u root -p < Base_De_Datos/Crear_Base_De_Datos_Final.sql
```

**3. Verificar que SSL está activo en MySQL**

```sql
SHOW VARIABLES LIKE '%ssl%';
```

> La conexión usa `useSSL=true` con `verifyServerCertificate=false` — adecuado para entornos locales con certificado autofirmado. MySQL 8.0 tiene SSL activado por defecto.

**4. Crear los usuarios de MySQL**
```sql
CREATE USER 'tienda_admin'@'localhost' IDENTIFIED BY 'admin_pass';
GRANT ALL PRIVILEGES ON tienda_informatica.* TO 'tienda_admin'@'localhost';

CREATE USER 'tienda_app'@'localhost' IDENTIFIED BY 'app_pass';
GRANT SELECT, INSERT, UPDATE, DELETE ON tienda_informatica.* TO 'tienda_app'@'localhost';

FLUSH PRIVILEGES;
```

**5. Configurar el email** *(opcional)*

Crea el archivo `Java/PrimeTechFinal/src/primetechfinal/email.properties`:
```properties
email.host=smtp.gmail.com
email.port=587
email.usuario=tucorreo@gmail.com
email.clave=tucontrasena_de_aplicacion
email.nombre=Prime Tech Systems
```

> ⚠️ Este fichero está en `.gitignore` y nunca se sube al repositorio para proteger las credenciales.

**6. Abrir y ejecutar**

Abre el proyecto `Java/PrimeTechFinal` en NetBeans y ejecuta con F6.

---

## 🧪 Tests

Los tests de integración se ejecutan automáticamente en cada push a `master` mediante GitHub Actions con una base de datos MySQL real.

Para ejecutarlos localmente:
```bash
cd Java/PrimeTechFinal
ant test -Dplatforms.JDK_1.8.home=$JAVA_HOME
```

---

## 📦 Releases

Cada vez que se crea un tag `vX.Y`, el workflow `release.yml` se dispara automáticamente y:

1. Levanta un contenedor MySQL y crea la base de datos
2. Compila el proyecto con Ant generando el JAR
3. Copia exactamente las dependencias necesarias a `dist/lib/`
4. Genera scripts de arranque `PrimeTech.bat` (Windows) y `PrimeTech.sh` (Linux/Mac)
5. Empaqueta todo en un ZIP y lo publica en [Releases](https://github.com/ivansc06/DesafioCSI1-Grupo1/releases)

Los scripts usan `java -cp "PrimeTechFinal.jar:lib/*"` en lugar de `-jar` para evitar el límite de 72 caracteres por línea del `MANIFEST.MF`.

### Cómo ejecutar la release

1. Descarga el ZIP de la última release y descomprímelo
2. Asegúrate de tener **Java 8 o superior** instalado
3. Configura la base de datos y el `email.properties` (ver sección de instalación)
4. Ejecuta el programa:
   - **Windows**: doble clic en `PrimeTech.bat`
   - **Linux/Mac**: `./PrimeTech.sh`

> ⚠️ No muevas el JAR fuera de la carpeta — necesita la carpeta `lib/` que viene dentro del ZIP.

### Crear una nueva release

```bash
git tag v1.0
git push origin v1.0
```

---

## 📁 Estructura del proyecto

```
DesafioCSI1-Grupo1/
├── Base_De_Datos/              # Scripts SQL
├── Java/
│   ├── librerias/              # JARs de dependencias
│   └── PrimeTechFinal/         # Proyecto NetBeans
│       ├── src/
│       │   └── primetechfinal/
│       │       ├── dao/        # Acceso a base de datos
│       │       ├── model/      # Clases del modelo
│       │       ├── sesion/     # Gestión de sesión
│       │       └── util/       # Facturas, QR, email, Excel
│       └── test/               # Tests de integración
└── .github/workflows/
    ├── ci.yml                  # Tests automáticos en cada push
    └── release.yml             # Publica el JAR al crear un tag
```

---

## 👥 Autores

Proyecto desarrollado por el **Grupo 1** — 1º DAM

Markel, Javier, Joselu e Iván.
