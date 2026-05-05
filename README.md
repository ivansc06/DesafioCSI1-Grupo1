# 🖥️ Prime Tech Systems

![CI](https://github.com/ivansc06/DesafioCSI1-Grupo1/actions/workflows/ci.yml/badge.svg)
![Release](https://img.shields.io/github/v/release/ivansc06/DesafioCSI1-Grupo1?label=release&color=brightgreen)
![Java](https://img.shields.io/badge/Java-8-orange?logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![License](https://img.shields.io/badge/license-MIT-green)

Aplicación de gestión para una tienda de informática desarrollada en Java Swing como proyecto de 1º DAM.

---

## ✨ Funcionalidades

- 📦 **Gestión de productos** — altas, bajas, ediciones y búsqueda. Alerta visual de stock bajo
- 💰 **Gestión de ventas** — registro de ventas con carrito, método de pago y cliente asociado
- 👥 **Gestión de clientes** — particulares y empresas con todos sus datos
- 📊 **Dashboard** — gráfica de ventas de los últimos 7 días con JFreeChart
- 📄 **Facturas HTML** — generación automática de facturas al realizar una venta
- 📧 **Envío de facturas por email** — envío automático al cliente vía Gmail SMTP
- 📁 **Exportar a Excel** — exportación de productos, ventas y clientes con Apache POI
- 🔒 **Sistema de login** — autenticación con BCrypt, bloqueo por intentos fallidos y roles

---

## 🛠️ Tecnologías

| Tecnología | Uso |
|---|---|
| Java 8 + Swing | Interfaz gráfica |
| MySQL 8.0 | Base de datos |
| HikariCP 4.0.3 | Pool de conexiones |
| FlatLaf 3.4 | Look & Feel moderno |
| Apache POI 5.3.0 | Exportación a Excel |
| JavaMail 2.0.1 | Envío de emails |
| JFreeChart 1.5.4 | Gráficas del dashboard |
| Log4j 2.25.4 | Sistema de logs |
| JUnit 4.13.2 | Tests de integración |
| GitHub Actions | CI/CD |

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

**3. Crear los usuarios de MySQL**
```sql
CREATE USER 'tienda_admin'@'localhost' IDENTIFIED BY 'admin_pass';
GRANT ALL PRIVILEGES ON tienda_informatica.* TO 'tienda_admin'@'localhost';

CREATE USER 'tienda_app'@'localhost' IDENTIFIED BY 'app_pass';
GRANT SELECT, INSERT, UPDATE, DELETE ON tienda_informatica.* TO 'tienda_app'@'localhost';

FLUSH PRIVILEGES;
```

**4. Configurar el email** *(opcional)*

Crea el archivo `Java/PrimeTechFinal/src/primetechfinal/email.properties`:
```properties
email.host=smtp.gmail.com
email.port=587
email.usuario=tucorreo@gmail.com
email.clave=tucontrasena_de_aplicacion
email.nombre=Prime Tech Systems
```

**5. Abrir y ejecutar**

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

## 📁 Estructura del proyecto

```
DesafioCSI1-Grupo1/
├── Base_De_Datos/          # Scripts SQL
├── Java/
│   ├── librerias/          # JARs de dependencias
│   └── PrimeTechFinal/     # Proyecto NetBeans
│       ├── src/            # Código fuente
│       └── test/           # Tests de integración
└── .github/workflows/      # CI con GitHub Actions
```

---

## 👥 Autores

Proyecto desarrollado por el **Grupo 1** — 1º DAM

Markel, Javier, Joselu e Iván.
