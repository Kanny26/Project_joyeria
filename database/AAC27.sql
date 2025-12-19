-- -----------------------------------
-- BASE DE DATOS PRINCIPAL
-- -----------------------------------
DROP DATABASE IF EXISTS gestor_abbyac27;
CREATE DATABASE gestor_abbyac27;
USE gestor_abbyac27;

-- -----------------------------------
-- USUARIOS, ROLES Y PERMISOS
-- -----------------------------------
CREATE TABLE Usuario (
    usuario_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    pass VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL, -- del usuario
    documento VARCHAR(50) UNIQUE NULL,
    fecha_registro DATE NULL, -- del cliente
    fecha_inicio DATE NULL, -- del proveedor
    minimo_compra DECIMAL(10,2) NULL
);

CREATE TABLE Rol (
    rol_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cargo ENUM('vendedor', 'administrador','proveedor', 'cliente') NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Permiso (
    permiso_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT
);

CREATE TABLE Rol_Permiso (
    rol_id INT UNSIGNED NOT NULL,
    permiso_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES Rol(rol_id),
    FOREIGN KEY (permiso_id) REFERENCES Permiso(permiso_id)
);

CREATE TABLE Recuperacion_Contrasena (
    recuperacion_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNSIGNED NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    fecha_solicitud DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Telefono_Usuario (
    telefono_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    telefono VARCHAR(50) NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Correo_Usuario (
    correo_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

-- -----------------------------------
-- SUBCATEGORIAS, CATEGORIAS, MATERIALES Y PRODUCTOS
-- -----------------------------------
CREATE TABLE Subcategoria (
    subcategoria_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE Categoria (
    categoria_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    subcategoria_id INT UNSIGNED,
    FOREIGN KEY (subcategoria_id) REFERENCES Subcategoria(subcategoria_id)
);

CREATE TABLE Material (
    material_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE Producto (
    producto_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    stock INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    fecha_registro DATE NOT NULL,
    material_id INT UNSIGNED NOT NULL,
    categoria_id INT UNSIGNED NOT NULL,
    usuario_proveedor_id INT UNSIGNED NOT NULL,

    FOREIGN KEY (usuario_proveedor_id) REFERENCES Usuario(usuario_id),
    FOREIGN KEY (material_id) REFERENCES Material(material_id),
    FOREIGN KEY (categoria_id) REFERENCES Categoria(categoria_id)
);

CREATE TABLE Inventario_Movimiento (
    movimiento_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    producto_id INT UNSIGNED NOT NULL,
    tipo ENUM('entrada','salida','ajuste') NOT NULL,
    estado ENUM ('activo', 'inactivo'),
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL,
    referencia VARCHAR(255),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

CREATE TABLE Usuario_Material (
    usuario_id INT UNSIGNED NOT NULL,
    material_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (usuario_id, material_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    FOREIGN KEY (material_id) REFERENCES Material(material_id)
);

-- -----------------------------------
-- COMPRAS 
-- -----------------------------------
CREATE TABLE Compra (
    compra_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_proveedor_id INT UNSIGNED NOT NULL,
    fecha_compra DATE NOT NULL,
    fecha_entrega DATE NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (usuario_proveedor_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Detalle_Compra (
    detalle_compra_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    compra_id INT UNSIGNED NOT NULL,
    producto_id INT UNSIGNED NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

-- -----------------------------------
-- VENTAS
-- -----------------------------------
CREATE TABLE venta_factura (
    venta_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNSIGNED NOT NULL,  -- vendedor
    usuario_cliente_id INT UNSIGNED NOT NULL, -- cliente ahora usuario
    fecha_emision DATE NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    FOREIGN KEY (usuario_cliente_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Detalle_Venta (
    detalle_venta_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venta_id INT UNSIGNED NOT NULL,
    producto_id INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venta_id) REFERENCES venta_factura(venta_id),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

-- -----------------------------------
-- POSTVENTA (CLIENTE) Y POSTCOMPRA (PROVEEDOR)
-- -----------------------------------
CREATE TABLE Caso_Postventa_Cliente (
    caso_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venta_id INT UNSIGNED NOT NULL,
    tipo ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad INT NOT NULL,
    motivo TEXT,
    fecha DATE NOT NULL,
    estado ENUM('en_proceso','aprobado','cancelado'),
    FOREIGN KEY (venta_id) REFERENCES venta_factura(venta_id)
);

CREATE TABLE Caso_Postcompra_Usuario (
    caso_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    compra_id INT UNSIGNED NOT NULL,
    tipo ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad INT NOT NULL,
    motivo TEXT,
    fecha DATE NOT NULL,
    estado ENUM('en_proceso','aprobado','cancelado'),
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
);

CREATE TABLE Estado_Caso_Cliente (
    estado_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    caso_id INT UNSIGNED NOT NULL,
    estado ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha DATETIME NOT NULL,
    observacion TEXT,
    FOREIGN KEY (caso_id) REFERENCES Caso_Postventa_Cliente(caso_id)
);

CREATE TABLE Estado_Caso_Proveedor (
    estado_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    caso_id INT UNSIGNED NOT NULL,
    estado ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha DATETIME NOT NULL,
    observacion TEXT,
    FOREIGN KEY (caso_id) REFERENCES Caso_Postcompra_Usuario(caso_id)
);

-- -----------------------------------
-- DESEMPEÑO
-- -----------------------------------
CREATE TABLE Desempeno_Vendedor (
    desempeno_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNSIGNED NOT NULL,
    ventas_totales DECIMAL(12,2) NOT NULL,
    comision_porcentaje DECIMAL(5,2) NOT NULL,
    comision_ganada DECIMAL(12,2) NOT NULL,
    periodo DATE NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

-- -----------------------------------
-- PAGOS
-- -----------------------------------
CREATE TABLE Metodo_pago (
    metodo_pago_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venta_id INT UNSIGNED,
    compra_id INT UNSIGNED,
    monto DECIMAL(12,2) NOT NULL,
    metodo ENUM('efectivo','tarjeta') NOT NULL,
    fecha DATE NOT NULL,
    estado ENUM('pendiente','confirmado','rechazado') NOT NULL,
    FOREIGN KEY (venta_id) REFERENCES venta_factura(venta_id),
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
);

CREATE TABLE Pago_credito (
    pago_credito_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    metodo_pago_id INT UNSIGNED NOT NULL,
    credito_id INT UNSIGNED NOT NULL,
    monto_abono DECIMAL(12,2) NOT NULL,
    fecha DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    metodo ENUM('efectivo','tarjeta') NOT NULL,
    estado ENUM('pendiente','pagado','vencido') NOT NULL,
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_pago(metodo_pago_id)
);

INSERT INTO Usuario (nombre, pass, fecha_creacion, documento, fecha_registro, fecha_inicio, minimo_compra)
VALUES
('Vendedor prueba','pass123', '2024-06-07', NULL, '2025-01-01', NULL, NULL),    -- usuario_id = 1
('Proveedor prueba','pass123', '2024-06-07', NULL, NULL, '2020-06-01', 100.000),  -- usuario_id = 2
('Cliente prueba','pass123', '2024-06-07', '12345678', '2025-11-01', NULL, NULL); -- usuario_id = 3
SELECT * FROM Usuario;

INSERT INTO Subcategoria (nombre) VALUES
('Anillos de 15 años');
SELECT * FROM Subcategoria;

INSERT INTO Categoria (nombre, subcategoria_id) VALUES
('Aretes largos', null),
('Cadenas', null),
('Pulseras', null),
('Anillos', 1),
('Tobilleras', null);
SELECT * FROM Categoria; 

INSERT INTO Material (nombre) VALUES
('Plata'),
('Oro laminado'),
('Cuero');
SELECT * FROM Material; 

INSERT INTO Producto (producto_id,nombre, descripcion, stock, precio_unitario, fecha_registro, material_id, categoria_id, usuario_proveedor_id)
VALUES
(1,'Aretes Perla Plata', 'Aretes con perla en plata', 0, 45000.00, '2025-11-20', 1, 1, 2),
(2, 'Cadena Oro Laminado', 'Cadena para hombre', 15, 120000.00, '2025-11-20', 2, 2, 2),
(3, 'Pulsera Corazón', 'Pulsera delicada', 30, 35000.00, '2025-11-20', 3, 3, 2),
(4, 'Anillo Ajustable Plateado', 'Anillo unisex ajustable', 25, 28000.00, '2025-11-20', 1, 4, 2),
(5, 'Tobillera Estrella Dorada', 'Tobillera con estrella', 18, 42000.00, '2025-11-20', 2, 5, 2);
SELECT * FROM Producto;

-- vendedor = usuario_id 1, cliente = usuario_id 3
INSERT INTO venta_factura (usuario_id, usuario_cliente_id, fecha_emision, total)
VALUES
(1, 3, '2025-12-01', 90000.00); -- venta_id = 1
SELECT * FROM venta_factura;

INSERT INTO Detalle_Venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
VALUES
(1, 1, 2, 45000.00, 90000.00);
SELECT * FROM Detalle_venta;

INSERT INTO Inventario_Movimiento (producto_id, tipo, estado, cantidad, fecha, referencia)
VALUES
(1, 'salida', 'activo', 2, '2025-09-12', 'Venta #1');
SELECT * FROM Inventario_Movimiento;

CREATE INDEX idx_producto_nombre ON Producto(nombre);

CREATE INDEX idx_venta_usuario_cliente ON venta_factura(usuario_cliente_id);

CREATE INDEX idx_movimiento_fecha ON Inventario_Movimiento(fecha);

CREATE INDEX idx_compra_usuario_proveedor ON Compra(usuario_proveedor_id);

CREATE INDEX idx_caso_postventa_tipo ON Caso_Postventa_Cliente(tipo);

SHOW INDEX FROM Producto;
SHOW INDEX FROM venta_factura;
SHOW INDEX FROM Inventario_Movimiento;
SHOW INDEX FROM Compra;
SHOW INDEX FROM Caso_Postventa_Cliente;

-- Productos sin stock
CREATE VIEW producto_sin_stock AS
SELECT producto_id, nombre
FROM Producto
WHERE stock = 0;

-- Usuarios
CREATE VIEW vista_usuarios AS
SELECT usuario_id, nombre, pass, fecha_creacion, documento,
       fecha_registro, fecha_inicio, minimo_compra
FROM Usuario;

-- Categorías con subcategorías
CREATE VIEW vista_categorias AS
SELECT Categoria.categoria_id,
       Categoria.nombre AS categoria,
       Subcategoria.nombre AS subcategoria
FROM Categoria
JOIN Subcategoria ON Categoria.subcategoria_id = Subcategoria.subcategoria_id;

-- Productos con detalle (material, categoría, proveedor)
CREATE VIEW vista_productos_detalle AS
SELECT Producto.producto_id,
       Producto.nombre,
       Producto.descripcion,
       Producto.stock,
       Producto.precio_unitario,
       Producto.fecha_registro,
       Material.nombre AS material,
       Categoria.nombre AS categoria,
       Usuario.nombre AS proveedor
FROM Producto
JOIN Material ON Producto.material_id = Material.material_id
JOIN Categoria ON Producto.categoria_id = Categoria.categoria_id
JOIN Usuario ON Producto.usuario_proveedor_id = Usuario.usuario_id;

CREATE VIEW vista_ventas_detalle AS
SELECT 
    venta_factura.venta_id AS venta_id,
    venta_factura.fecha_emision AS factura_fecha_emision,
    venta_factura.total AS factura_total,
    usuario_vendedor.usuario_id AS vendedor_id,
    usuario_vendedor.nombre AS vendedor_nombre,
    usuario_cliente.usuario_id AS cliente_id,
    usuario_cliente.nombre  AS cliente_nombre,
    Detalle_Venta.detalle_venta_id AS detalle_venta_id,
    Detalle_Venta.producto_id AS producto_id,
    Producto.nombre AS producto_nombre,
    Producto.descripcion AS producto_descripcion,
    Detalle_Venta.cantidad AS detalle_cantidad,
    Detalle_Venta.precio_unitario AS detalle_precio_unitario,
    Detalle_Venta.subtotal AS detalle_subtotal
FROM venta_factura
JOIN Usuario AS usuario_vendedor ON venta_factura.usuario_id = usuario_vendedor.usuario_id
JOIN Usuario AS usuario_cliente ON venta_factura.usuario_cliente_id = usuario_cliente.usuario_id
JOIN Detalle_Venta ON venta_factura.venta_id = Detalle_Venta.detalle_venta_id
JOIN Producto ON Detalle_Venta.producto_id = Producto.producto_id;


-- Productos sin stock
SELECT * FROM producto_sin_stock;

-- Usuarios
SELECT * FROM vista_usuarios;

-- Categorías
SELECT * FROM vista_categorias;

-- Productos con detalle
SELECT * FROM vista_productos_detalle;

-- Detalles de las ventas
SELECT * FROM vista_ventas_detalle;
