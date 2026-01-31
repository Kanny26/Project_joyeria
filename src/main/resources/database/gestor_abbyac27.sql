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

INSERT INTO Usuario (
    nombre,
    pass,
    estado,
    fecha_creacion
) VALUES (
    'AdminKS',
    '$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.'	,
    1,
    NOW()
);

INSERT INTO rol (rol_id, cargo, usuario_id, nombre)
VALUES (1, 'administrador', 1, 'AdminKS');


UPDATE Usuario 
SET pass = '$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.' 
WHERE usuario_id = 1;

select * from Usuario;
select * from rol;
select * from correo_usuario;
select * from telefono_usuario;

-- Primero eliminar relaciones (si existen)
DELETE FROM Rol WHERE usuario_id = 4;
DELETE FROM Correo_Usuario WHERE usuario_id = 0;
DELETE FROM Telefono_Usuario WHERE usuario_id = 4;

-- Luego eliminar el usuario
DELETE FROM Usuario WHERE usuario_id = 4;

SELECT 
    u.usuario_id,
    u.nombre,
    u.estado,
    u.documento,
    u.fecha_creacion,
    r.cargo,
    GROUP_CONCAT(DISTINCT t.telefono) AS telefonos,
    GROUP_CONCAT(DISTINCT c.email) AS correos
FROM Usuario u
LEFT JOIN Rol r ON u.usuario_id = r.usuario_id
LEFT JOIN Telefono_Usuario t ON u.usuario_id = t.usuario_id
LEFT JOIN Correo_Usuario c ON u.usuario_id = c.usuario_id
GROUP BY 
    u.usuario_id,
    u.nombre,
    u.estado,
    u.documento,
    u.fecha_creacion,
    r.cargo;
    
ALTER TABLE Desempeno_Vendedor 
ADD COLUMN observaciones TEXT NULL AFTER periodo;


ALTER TABLE Categoria ADD icono VARCHAR(255);
ALTER TABLE producto ADD imagen VARCHAR(255);
ALTER TABLE Producto
ADD COLUMN precio_venta DECIMAL(10,2) NOT NULL AFTER precio_unitario;

INSERT INTO Categoria (nombre, icono) VALUES
('Anillos', 'anillos.png'),
('Topitos', 'topitos.png'),
('Aretes largos', 'aretes_largos.png'),
('Conjuntos', 'juegos.png'),
('Earcuff', 'earcuff.png'),
('Rosarios', 'rosario.png'),
('Collares', 'collar.png'),
('Tobilleras', 'tobillera.png'),
('Dijes', 'dijes.png'),
('Denarios', 'denarios.png'),
('Pulseras', 'pulsera.png'),
('Manillas', 'manillas.png'),
('Materiales', 'materiales.png');

SELECT categoria_id, nombre FROM Categoria;
SELECT producto_id, nombre, categoria_id FROM Producto;

SELECT COUNT(*) FROM Categoria;
SELECT * FROM Categoria;


CREATE TABLE Material (
    material_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);


INSERT INTO  Material (nombre) VALUES 
('acero inoxidable'),
('plata'),
('oro laminado'),
('mostacilla'),
('hilo coreano'),
('hilo encerado'), 
('nylon'),
('dijes'),
('perlas varias');

SELECT * FROM Material;
SELECT * FROM Producto;
SELECT * FROM usuario;
SELECT * FROM compra;
SELECT * FROM detalle_compra;
SELECT COUNT(*) FROM rol;
SELECT producto_id, imagen FROM producto;

DELETE FROM compra WHERE compra_id = 1;
