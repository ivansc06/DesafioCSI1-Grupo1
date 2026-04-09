-- ============================================================
--  BASE DE DATOS: tienda_informatica
--  Proyecto: Gestor de tienda de informática
--  Entidades: Productos, Clientes, Empleados, Ventas, Reparaciones
--  Equipo: José Luis Pérez, Javier Maza, Markel, Iván
-- ============================================================
 -- prueba git
CREATE DATABASE IF NOT EXISTS tienda_informatica
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
 
USE tienda_informatica;
 
-- ------------------------------------------------------------
-- TABLA: categorias
-- Categorías de productos (portátiles, periféricos, etc.)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
    id_categoria    INT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    PRIMARY KEY (id_categoria)
);
 
-- ------------------------------------------------------------
-- TABLA: proveedores
-- Proveedores de productos
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS proveedores (
    id_proveedor    INT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(150) NOT NULL,
    contacto        VARCHAR(100),
    telefono        VARCHAR(20),
    email           VARCHAR(150),
    direccion       VARCHAR(255),
    PRIMARY KEY (id_proveedor)
);
 
-- ------------------------------------------------------------
-- TABLA: productos
-- Catálogo de productos de la tienda
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS productos (
    id_producto     INT            NOT NULL AUTO_INCREMENT,
    id_categoria    INT            NOT NULL,
    id_proveedor    INT,
    nombre          VARCHAR(200)   NOT NULL,
    descripcion     TEXT,
    precio_compra   DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    precio_venta    DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    stock           INT            NOT NULL DEFAULT 0,
    stock_minimo    INT            NOT NULL DEFAULT 5,   -- alerta de reposición
    numero_serie    VARCHAR(100)   UNIQUE,
    activo          TINYINT(1)     NOT NULL DEFAULT 1,
    fecha_alta      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_producto),
    CONSTRAINT fk_prod_categoria FOREIGN KEY (id_categoria)
        REFERENCES categorias(id_categoria) ON UPDATE CASCADE,
    CONSTRAINT fk_prod_proveedor FOREIGN KEY (id_proveedor)
        REFERENCES proveedores(id_proveedor) ON DELETE SET NULL ON UPDATE CASCADE
);
 
-- ------------------------------------------------------------
-- TABLA: clientes
-- Clientes de la tienda
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente      INT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(150),
    dni             VARCHAR(20)  UNIQUE,
    telefono        VARCHAR(20),
    email           VARCHAR(150) UNIQUE,
    direccion       VARCHAR(255),
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo          TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_cliente)
);
 
-- ------------------------------------------------------------
-- TABLA: empleados
-- Empleados de la tienda
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS empleados (
    id_empleado     INT          NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(150),
    dni             VARCHAR(20)  UNIQUE,
    cargo           ENUM('vendedor','tecnico','admin','gerente') NOT NULL DEFAULT 'vendedor',
    telefono        VARCHAR(20),
    email           VARCHAR(150) UNIQUE,
    contraseña 		VARCHAR(200) NOT NULL,
    fecha_alta      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo          TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_empleado)
);
 
-- ------------------------------------------------------------
-- TABLA: ventas
-- Cabecera de cada venta realizada
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ventas (
    id_venta        INT            NOT NULL AUTO_INCREMENT,
    id_cliente      INT,                                  -- NULL = venta sin cliente registrado
    id_empleado     INT            NOT NULL,
    fecha_venta     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total           DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    descuento       DECIMAL(5,2)   NOT NULL DEFAULT 0.00, -- % de descuento aplicado
    metodo_pago     ENUM('efectivo','tarjeta','transferencia','otro') NOT NULL DEFAULT 'efectivo',
    observaciones   TEXT,
    PRIMARY KEY (id_venta),
    CONSTRAINT fk_venta_cliente  FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_venta_empleado FOREIGN KEY (id_empleado)
        REFERENCES empleados(id_empleado) ON UPDATE CASCADE
);
 
-- ------------------------------------------------------------
-- TABLA: detalle_ventas
-- Líneas de producto de cada venta
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id_detalle      INT            NOT NULL AUTO_INCREMENT,
    id_venta        INT            NOT NULL,
    id_producto     INT            NOT NULL,
    cantidad        INT            NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2)  NOT NULL,             -- precio en el momento de la venta
    subtotal        DECIMAL(10,2)  GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    PRIMARY KEY (id_detalle),
    CONSTRAINT fk_detalle_venta    FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta)    ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto) ON UPDATE CASCADE
);
 
-- ------------------------------------------------------------
-- TABLA: estados_reparacion
-- Estados posibles de una reparación
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estados_reparacion (
    id_estado   INT         NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50) NOT NULL UNIQUE,  -- 'recibido','en_diagnostico','en_reparacion','listo','entregado','cancelado'
    descripcion VARCHAR(255),
    PRIMARY KEY (id_estado)
);
 
-- ------------------------------------------------------------
-- TABLA: reparaciones
-- Reparaciones de equipos de clientes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reparaciones (
    id_reparacion   INT            NOT NULL AUTO_INCREMENT,
    id_cliente      INT            NOT NULL,
    id_empleado     INT            NOT NULL,              -- técnico responsable
    id_estado       INT            NOT NULL DEFAULT 1,
    dispositivo     VARCHAR(200)   NOT NULL,              -- descripción del equipo
    numero_serie    VARCHAR(100),
    problema        TEXT           NOT NULL,              -- descripción del problema
    diagnostico     TEXT,                                 -- diagnóstico del técnico
    solucion        TEXT,                                 -- solución aplicada
    coste_piezas    DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    coste_mano_obra DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    total           DECIMAL(10,2)  GENERATED ALWAYS AS (coste_piezas + coste_mano_obra) STORED,
    fecha_entrada   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_prevista  DATE,
    fecha_entrega   DATETIME,
    observaciones   TEXT,
    PRIMARY KEY (id_reparacion),
    CONSTRAINT fk_rep_cliente  FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente)   ON UPDATE CASCADE,
    CONSTRAINT fk_rep_empleado FOREIGN KEY (id_empleado)
        REFERENCES empleados(id_empleado) ON UPDATE CASCADE,
    CONSTRAINT fk_rep_estado   FOREIGN KEY (id_estado)
        REFERENCES estados_reparacion(id_estado) ON UPDATE CASCADE
);
 
-- ------------------------------------------------------------
-- TABLA: piezas_reparacion
-- Piezas/productos usados en una reparación
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS piezas_reparacion (
    id_pieza        INT            NOT NULL AUTO_INCREMENT,
    id_reparacion   INT            NOT NULL,
    id_producto     INT,                                  -- NULL si es pieza externa
    descripcion     VARCHAR(255)   NOT NULL,
    cantidad        INT            NOT NULL DEFAULT 1,
    coste_unitario  DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id_pieza),
    CONSTRAINT fk_pieza_reparacion FOREIGN KEY (id_reparacion)
        REFERENCES reparaciones(id_reparacion) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pieza_producto   FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto) ON DELETE SET NULL ON UPDATE CASCADE
);
 
-- ============================================================
-- DATOS INICIALES
-- ============================================================
 
-- Categorías
INSERT INTO categorias (nombre, descripcion) VALUES
    ('Portátiles',        'Ordenadores portátiles y ultrabooks'),
    ('Sobremesa',         'PCs de escritorio y all-in-one'),
    ('Componentes',       'CPU, RAM, discos, tarjetas gráficas'),
    ('Periféricos',       'Teclados, ratones, monitores, auriculares'),
    ('Redes',             'Routers, switches, cables, adaptadores'),
    ('Almacenamiento',    'HDD, SSD, memorias USB, tarjetas SD'),
    ('Software',          'Licencias y sistemas operativos'),
    ('Consumibles',       'Tintas, tóneres, papel, limpieza');
 
-- Estados de reparación
INSERT INTO estados_reparacion (nombre, descripcion) VALUES
    ('Recibido',        'Equipo recibido, pendiente de diagnóstico'),
    ('En diagnóstico',  'El técnico está evaluando el problema'),
    ('En reparación',   'Reparación en curso'),
    ('Esperando pieza', 'Pendiente de llegada de componente'),
    ('Listo',           'Reparación finalizada, pendiente de entrega'),
    ('Entregado',       'Equipo entregado al cliente'),
    ('Cancelado',       'Reparación cancelada');
 
-- Empleados de ejemplo
INSERT INTO empleados (nombre, apellidos, cargo, email, contraseña) VALUES
    ('José Luis', 'Pérez',  'tecnico',  'joseluisperez@tienda.local', SHA2('12345', 256)),
    ('Javier',    'Maza',   'vendedor', 'javiermaza@tienda.local', SHA2('12345', 256)),
    ('Markel',    '',        'tecnico',  'markel@tienda.local', SHA2('12345', 256)),
    ('Iván',      '',        'gerente',    'ivan@tienda.local', SHA2('12345', 256)),
    ('Admin', '', 'admin', 'admin@tienda.com', SHA2('12345', 256)); -- Esto guarda la contraseña '12345' encriptada
 
-- ============================================================
-- VISTAS ÚTILES
-- ============================================================
 
-- Stock bajo mínimo
CREATE OR REPLACE VIEW v_stock_bajo AS
    SELECT id_producto, nombre, stock, stock_minimo,
           (stock_minimo - stock) AS unidades_faltan
    FROM productos
    WHERE stock < stock_minimo AND activo = 1
    ORDER BY unidades_faltan DESC;
 
-- Ventas con cliente y empleado
CREATE OR REPLACE VIEW v_ventas_detalle AS
    SELECT v.id_venta, v.fecha_venta,
           CONCAT(c.nombre, ' ', IFNULL(c.apellidos,'')) AS cliente,
           CONCAT(e.nombre, ' ', IFNULL(e.apellidos,'')) AS empleado,
           v.total, v.descuento, v.metodo_pago
    FROM ventas v
    LEFT JOIN clientes  c ON v.id_cliente  = c.id_cliente
    JOIN  empleados e ON v.id_empleado = e.id_empleado
    ORDER BY v.fecha_venta DESC;
 
-- Reparaciones activas (no entregadas ni canceladas)
CREATE OR REPLACE VIEW v_reparaciones_activas AS
    SELECT r.id_reparacion, r.fecha_entrada, r.fecha_prevista,
           CONCAT(c.nombre, ' ', IFNULL(c.apellidos,'')) AS cliente,
           c.telefono,
           CONCAT(e.nombre, ' ', IFNULL(e.apellidos,'')) AS tecnico,
           r.dispositivo, r.problema, er.nombre AS estado, r.total
    FROM reparaciones r
    JOIN clientes          c  ON r.id_cliente  = c.id_cliente
    JOIN empleados         e  ON r.id_empleado = e.id_empleado
    JOIN estados_reparacion er ON r.id_estado   = er.id_estado
    WHERE er.nombre NOT IN ('Entregado','Cancelado')
    ORDER BY r.fecha_entrada ASC;
 
-- Productos más vendidos
CREATE OR REPLACE VIEW v_productos_mas_vendidos AS
    SELECT p.id_producto, p.nombre,
           SUM(dv.cantidad)  AS total_unidades,
           SUM(dv.subtotal)  AS total_ingresos
    FROM detalle_ventas dv
    JOIN productos p ON dv.id_producto = p.id_producto
    GROUP BY p.id_producto, p.nombre
    ORDER BY total_unidades DESC;
 
-- ============================================================
-- PROCEDIMIENTOS ALMACENADOS
-- ============================================================
 
DELIMITER $$
 
-- Registrar una venta y actualizar stock automáticamente
CREATE PROCEDURE sp_registrar_venta(
    IN p_id_cliente   INT,
    IN p_id_empleado  INT,
    IN p_metodo_pago  VARCHAR(20),
    IN p_descuento    DECIMAL(5,2)
)
BEGIN
    INSERT INTO ventas (id_cliente, id_empleado, metodo_pago, descuento)
    VALUES (p_id_cliente, p_id_empleado, p_metodo_pago, p_descuento);
    SELECT LAST_INSERT_ID() AS nueva_venta_id;
END$$
 
-- Añadir línea de producto a una venta y descontar stock
CREATE PROCEDURE sp_añadir_linea_venta(
    IN p_id_venta    INT,
    IN p_id_producto INT,
    IN p_cantidad    INT
)
BEGIN
    DECLARE v_precio DECIMAL(10,2);
    DECLARE v_stock  INT;
 
    SELECT precio_venta, stock INTO v_precio, v_stock
    FROM productos WHERE id_producto = p_id_producto;
 
    IF v_stock < p_cantidad THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stock insuficiente para el producto solicitado';
    END IF;
 
    INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario)
    VALUES (p_id_venta, p_id_producto, p_cantidad, v_precio);
 
    UPDATE productos SET stock = stock - p_cantidad
    WHERE id_producto = p_id_producto;
 
    UPDATE ventas
    SET total = (SELECT SUM(subtotal) FROM detalle_ventas WHERE id_venta = p_id_venta)
    WHERE id_venta = p_id_venta;
END$$
 
-- Cambiar el estado de una reparación
CREATE PROCEDURE sp_cambiar_estado_reparacion(
    IN p_id_reparacion INT,
    IN p_id_estado     INT
)
BEGIN
    UPDATE reparaciones SET id_estado = p_id_estado WHERE id_reparacion = p_id_reparacion;
 
    IF p_id_estado = 6 THEN  -- 6 = Entregado
        UPDATE reparaciones SET fecha_entrega = NOW()
        WHERE id_reparacion = p_id_reparacion;
    END IF;
END$$
 
DELIMITER ;
 
-- ============================================================
-- TRIGGERS
-- ============================================================
 
-- Alerta: insertar en log si el stock baja del mínimo tras una venta
CREATE TABLE IF NOT EXISTS log_stock_bajo (
    id_log      INT      NOT NULL AUTO_INCREMENT,
    id_producto INT      NOT NULL,
    stock_actual INT     NOT NULL,
    fecha       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_log)
);
 
DELIMITER $$
 
CREATE TRIGGER trg_stock_minimo
AFTER UPDATE ON productos
FOR EACH ROW
BEGIN
    IF NEW.stock < NEW.stock_minimo AND OLD.stock >= OLD.stock_minimo THEN
        INSERT INTO log_stock_bajo (id_producto, stock_actual)
        VALUES (NEW.id_producto, NEW.stock);
    END IF;
END$$
 
DELIMITER ;