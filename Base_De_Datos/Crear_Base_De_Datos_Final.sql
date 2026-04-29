-- ============================================================
--  BASE DE DATOS: tienda_informatica
--  Versión final - toda la lógica se gestiona desde JavaFX
--  Tablas: 7
-- ============================================================

CREATE DATABASE IF NOT EXISTS tienda_informatica
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tienda_informatica;

-- ------------------------------------------------------------
-- TABLA: productos
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS productos (
    id_producto   INT            NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(200)   NOT NULL,
    descripcion   TEXT,
    precio_compra DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    precio_venta  DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    stock         INT            NOT NULL DEFAULT 0,
    PRIMARY KEY (id_producto)
);

-- ------------------------------------------------------------
-- TABLA: empleados
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS empleados (
    id_empleado  INT          NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100) NOT NULL,
    apellidos    VARCHAR(150),
    cargo        ENUM('vendedor','tecnico','admin','gerente') NOT NULL DEFAULT 'vendedor',
    email        VARCHAR(150) NOT NULL UNIQUE,
    contraseña   VARCHAR(200) NOT NULL,
    PRIMARY KEY (id_empleado)
);

-- ------------------------------------------------------------
-- SUPERCLASE: clientes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente  INT          NOT NULL AUTO_INCREMENT,
    tipo        ENUM('particular','empresa') NOT NULL,
    telefono    VARCHAR(20),
    email       VARCHAR(150) UNIQUE,
    direccion   VARCHAR(255),
    PRIMARY KEY (id_cliente)
);

-- ------------------------------------------------------------
-- SUBCLASE: clientes_particular
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes_particular (
    id_cliente  INT          NOT NULL,
    nombre      VARCHAR(100) NOT NULL,
    apellidos   VARCHAR(150),
    dni         VARCHAR(20)  UNIQUE,
    PRIMARY KEY (id_cliente),
    CONSTRAINT fk_particular_cliente FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ------------------------------------------------------------
-- SUBCLASE: clientes_empresa
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes_empresa (
    id_cliente      INT          NOT NULL,
    razon_social    VARCHAR(200) NOT NULL,
    cif             VARCHAR(20)  UNIQUE,
    contacto_nombre VARCHAR(150),
    PRIMARY KEY (id_cliente),
    CONSTRAINT fk_empresa_cliente FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ------------------------------------------------------------
-- TABLA: ventas
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ventas (
    id_venta     INT            NOT NULL AUTO_INCREMENT,
    id_cliente   INT,
    id_empleado  INT            NOT NULL,
    fecha_venta  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total        DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    metodo_pago  ENUM('efectivo','tarjeta','transferencia') NOT NULL DEFAULT 'efectivo',
    PRIMARY KEY (id_venta),
    CONSTRAINT fk_venta_cliente  FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)   ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_venta_empleado FOREIGN KEY (id_empleado)
        REFERENCES empleados(id_empleado) ON UPDATE CASCADE
);

-- ------------------------------------------------------------
-- TABLA: detalle_ventas
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id_detalle      INT            NOT NULL AUTO_INCREMENT,
    id_venta        INT            NOT NULL,
    id_producto     INT            NOT NULL,
    cantidad        INT            NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2)  NOT NULL,
    subtotal        DECIMAL(10,2)  GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    PRIMARY KEY (id_detalle),
    CONSTRAINT fk_detalle_venta    FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta)       ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto) ON UPDATE CASCADE
);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

-- Productos de ejemplo
INSERT INTO productos (nombre, precio_compra, precio_venta, stock) VALUES
    ('Portátil Lenovo IdeaPad',    450.00, 699.00, 10),
    ('Ratón inalámbrico Logitech',  15.00,  29.99, 50),
    ('Teclado mecánico RGB',        40.00,  79.99, 30),
    ('Monitor 24" Full HD',        150.00, 249.00, 15),
    ('SSD 1TB Samsung',             60.00, 109.99, 25);

-- Admin (contraseña: admin1234)
-- IMPORTANTE: sustituir 'admin1234' por el hash BCrypt generado desde Java
INSERT INTO empleados (nombre, apellidos, cargo, email, contraseña)
    VALUES ('Admin', '', 'admin', 'admin@tienda.com', 'admin1234');

-- 3 empleados normales (contraseña: 12345)
-- IMPORTANTE: sustituir '12345' por el hash BCrypt generado desde Java
INSERT INTO empleados (nombre, apellidos, cargo, email, contraseña) VALUES
    ('José Luis', 'Pérez', 'tecnico',  'joseluisperez@tienda.local', '12345'),
    ('Javier',    'Maza',  'vendedor', 'javiermaza@tienda.local',    '12345'),
    ('Iván',      'López', 'gerente',  'ivan@tienda.local',          '12345'),
    ('Markel',      'Corbaton', 'gerente',  'markel@tienda.local',          '12345');

-- Cliente particular de ejemplo
INSERT INTO clientes (tipo, telefono, email, direccion)
    VALUES ('particular', '612345678', 'ana@gmail.com', 'Calle Mayor 1, Zaragoza');
INSERT INTO clientes_particular (id_cliente, nombre, apellidos, dni)
    VALUES (LAST_INSERT_ID(), 'Ana', 'García López', '12345678A');

-- Cliente empresa de ejemplo
INSERT INTO clientes (tipo, telefono, email, direccion)
    VALUES ('empresa', '976111222', 'compras@techcorp.es', 'Polígono Industrial 5, Zaragoza');
INSERT INTO clientes_empresa (id_cliente, razon_social, cif, contacto_nombre)
    VALUES (LAST_INSERT_ID(), 'TechCorp SL', 'B12345678', 'Pedro Martínez');
    
    
-- Vamos a crear dos campos en la tabla empleados para implementar un trigger que contabilice los inicios de sesion fallidos, y si 
-- llega a 5 bloquee al empleado
ALTER TABLE empleados
    ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado         TINYINT(1) NOT NULL DEFAULT 0;
-- TRIGER DE CONTEO DE INTENTOS FALLIDOS :
-- Crear el trigger
DELIMITER //
CREATE TRIGGER trg_bloquear_cuenta
BEFORE UPDATE ON empleados
FOR EACH ROW
BEGIN
    IF NEW.intentos_fallidos >= 5 THEN
        SET NEW.bloqueado = 1;
    END IF;
END //
DELIMITER ;

-- ============================================================
-- TRIGGERS
-- ============================================================

DELIMITER $$

-- ------------------------------------------------------------
-- TRIGGER 1: Actualizar total de la venta al INSERTAR una línea
-- ------------------------------------------------------------
CREATE TRIGGER trg_actualizar_total_insert
AFTER INSERT ON detalle_ventas
FOR EACH ROW
BEGIN
    UPDATE ventas
    SET total = (SELECT SUM(subtotal) FROM detalle_ventas WHERE id_venta = NEW.id_venta)
    WHERE id_venta = NEW.id_venta;
END$$

-- ------------------------------------------------------------
-- TRIGGER 2: Al ELIMINAR una línea de venta:
--            - Restaurar el stock del producto
--            - Recalcular el total de la venta
-- (fusionado en un único trigger porque MySQL solo permite
--  un trigger por evento/tabla)
-- ------------------------------------------------------------
CREATE TRIGGER trg_delete_detalle
AFTER DELETE ON detalle_ventas
FOR EACH ROW
BEGIN
    -- Devolvemos las unidades al stock del producto
    UPDATE productos
    SET stock = stock + OLD.cantidad
    WHERE id_producto = OLD.id_producto;

    -- Recalculamos el total de la venta (0 si ya no quedan líneas)
    UPDATE ventas
    SET total = IFNULL(
        (SELECT SUM(subtotal) FROM detalle_ventas WHERE id_venta = OLD.id_venta),
        0.00
    )
    WHERE id_venta = OLD.id_venta;
END$$

-- ------------------------------------------------------------
-- TRIGGER 3: Impedir stock negativo y descontar stock al vender
-- Se ejecuta ANTES de insertar para poder cancelar la operación
-- ------------------------------------------------------------
CREATE TRIGGER trg_stock_al_vender
BEFORE INSERT ON detalle_ventas
FOR EACH ROW
BEGIN
    DECLARE v_stock INT;

    SELECT stock INTO v_stock
    FROM productos WHERE id_producto = NEW.id_producto;

    IF v_stock < NEW.cantidad THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stock insuficiente para el producto solicitado';
    END IF;

    UPDATE productos
    SET stock = stock - NEW.cantidad
    WHERE id_producto = NEW.id_producto;
END$$

DELIMITER ;

-- ============================================================
-- USUARIOS MySQL
-- ============================================================

-- 1. Usuario administrador (desarrollo y mantenimiento)
--    Tiene todos los permisos sobre la BD
CREATE USER IF NOT EXISTS 'tienda_admin'@'localhost' IDENTIFIED BY 'admin_pass';
GRANT ALL PRIVILEGES ON tienda_informatica.* TO 'tienda_admin'@'localhost';

-- 2. Usuario de la aplicación JavaFX (solo permisos necesarios)
--    No puede borrar tablas ni hacer operaciones destructivas
CREATE USER IF NOT EXISTS 'tienda_app'@'localhost' IDENTIFIED BY 'app_pass';
GRANT SELECT, INSERT, UPDATE ON tienda_informatica.* TO 'tienda_app'@'localhost';

-- Aplicar los cambios de permisos
FLUSH PRIVILEGES;
