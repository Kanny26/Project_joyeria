-- ======================================================
-- BASE DE DATOS
-- ======================================================
DROP DATABASE IF EXISTS gestor_abbyac27;
CREATE DATABASE gestor_abbyac27;
USE gestor_abbyac27;

-- ======================================================
-- TABLA USUARIO
-- ======================================================
CREATE TABLE Usuario (
    usuario_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    pass VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL,
    documento VARCHAR(50) UNIQUE,
    fecha_registro DATE,
    fecha_inicio DATE,
    minimo_compra DECIMAL(10,2)
);

CREATE TABLE Telefono_Usuario (
    telefono_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    telefono VARCHAR(50) NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Correo_Usuario (
    correo_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

-- ======================================================
-- ROLES Y PERMISOS
-- ======================================================
CREATE TABLE Rol (
    rol_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cargo ENUM('vendedor','administrador','proveedor','cliente') NOT NULL,
    nombre VARCHAR(255) UNIQUE NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Permiso (
    permiso_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
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
    recuperacion_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNSIGNED NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    fecha_solicitud DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    estado BOOLEAN DEFAULT 1,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

-- ======================================================
-- CATEGORÍAS Y MATERIALES
-- ======================================================
CREATE TABLE Subcategoria (
    subcategoria_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE Categoria (
    categoria_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    icono VARCHAR(255),
    subcategoria_id INT UNSIGNED,
    FOREIGN KEY (subcategoria_id) REFERENCES Subcategoria(subcategoria_id)
);

CREATE TABLE Material (
    material_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE Usuario_Material (
    usuario_id INT UNSIGNED NOT NULL,
    material_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (usuario_id, material_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    FOREIGN KEY (material_id) REFERENCES Material(material_id)
);

-- ======================================================
-- PRODUCTOS
-- ======================================================
CREATE TABLE Producto (
    producto_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    stock INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    fecha_registro DATE NOT NULL,
    imagen VARCHAR(255),
    material_id INT UNSIGNED NOT NULL,
    categoria_id INT UNSIGNED NOT NULL,
    usuario_proveedor_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (material_id) REFERENCES Material(material_id),
    FOREIGN KEY (categoria_id) REFERENCES Categoria(categoria_id),
    FOREIGN KEY (usuario_proveedor_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Inventario_Movimiento (
    movimiento_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    producto_id INT UNSIGNED NOT NULL,
    tipo ENUM('entrada','salida','ajuste') NOT NULL,
    estado ENUM('activo','inactivo'),
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL,
    referencia VARCHAR(255),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

-- ======================================================
-- COMPRAS
-- ======================================================
CREATE TABLE Compra (
    compra_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_proveedor_id INT UNSIGNED NOT NULL,
    fecha_compra DATE NOT NULL,
    fecha_entrega DATE NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (usuario_proveedor_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Detalle_Compra (
    detalle_compra_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    compra_id INT UNSIGNED NOT NULL,
    producto_id INT UNSIGNED NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

-- ======================================================
-- VENTAS (SIN CRÉDITO)
-- ======================================================
CREATE TABLE venta_factura (
    venta_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNSIGNED NOT NULL,
    usuario_cliente_id INT UNSIGNED NOT NULL,
    fecha_emision DATE NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    FOREIGN KEY (usuario_cliente_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Detalle_Venta (
    detalle_venta_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id INT UNSIGNED NOT NULL,
    producto_id INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venta_id) REFERENCES venta_factura(venta_id),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

-- ======================================================
-- PAGOS
-- ======================================================
CREATE TABLE Metodo_Pago (
    metodo_pago_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO Metodo_Pago (nombre) VALUES
('efectivo'),
('tarjeta'),
('transferencia');

CREATE TABLE Pago_Venta (
    pago_venta_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id INT UNSIGNED NOT NULL,
    metodo_pago_id INT UNSIGNED NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    fecha DATETIME NOT NULL,
    estado ENUM('pendiente','confirmado','rechazado') NOT NULL,
    FOREIGN KEY (venta_id) REFERENCES venta_factura(venta_id),
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
);

CREATE TABLE Pago_Compra (
    pago_compra_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    compra_id INT UNSIGNED NOT NULL,
    metodo_pago_id INT UNSIGNED NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    fecha DATETIME NOT NULL,
    estado ENUM('pendiente','confirmado','rechazado') NOT NULL,
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id),
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
);

-- ======================================================
-- CRÉDITOS (SOLO PARA COMPRAS)
-- ======================================================
CREATE TABLE Credito_Compra (
    credito_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    compra_id INT UNSIGNED NOT NULL,
    monto_total DECIMAL(12,2) NOT NULL,
    saldo_pendiente DECIMAL(12,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado ENUM('activo','pagado','vencido') NOT NULL,
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
);

CREATE TABLE Abono_Credito_Compra (
    abono_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    credito_id INT UNSIGNED NOT NULL,
    metodo_pago_id INT UNSIGNED NOT NULL,
    monto_abono DECIMAL(12,2) NOT NULL,
    fecha DATETIME NOT NULL,
    estado ENUM('pendiente','confirmado') NOT NULL,
    FOREIGN KEY (credito_id) REFERENCES Credito_Compra(credito_id),
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
);

-- ======================================================
-- INSERT INICIAL ADMIN
-- ======================================================
INSERT INTO Usuario (nombre, pass, estado, fecha_creacion)
VALUES ('AdminKS','$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.',1,NOW());
select *  from Usuario;

INSERT INTO Rol (cargo, usuario_id, nombre)
VALUES ('administrador', 1, 'AdminKS');

INSERT INTO Categoria (nombre, icono) VALUES
('Anillos','anillos.png'),
('Topitos','topitos.png'),
('Aretes largos','aretes_largos.png'),
('Conjuntos','juegos.png'),
('Earcuff','earcuff.png'),
('Rosarios','rosario.png'),
('Collares','collar.png'),
('Tobilleras','tobillera.png'),
('Dijes','dijes.png'),
('Denarios','denarios.png'),
('Pulseras','pulsera.png'),
('Manillas','manillas.png'),
('Materiales','materiales.png');

INSERT INTO Material (nombre) VALUES
('Acero inoxidable'),
('Plata'),
('Oro laminado'),
('Mostacilla'),
('Hilo coreano'),
('Hilo encerado'),
('Nylon'),
('Dijes'),
('Perlas varias');

ALTER TABLE Producto 
ADD COLUMN codigo VARCHAR(10) NOT NULL AFTER producto_id;

ALTER TABLE Producto 
    ADD COLUMN imagen_data LONGBLOB AFTER imagen,
    ADD COLUMN imagen_tipo VARCHAR(50) AFTER imagen_data;

select * from Producto;
SELECT producto_id, nombre, imagen, imagen_tipo,
       LENGTH(imagen_data) AS tamanio_bytes
FROM Producto;

delete from producto where producto_id = 4;