-- ======================================================
-- 1. ESTRUCTURA DE LA BASE DE DATOS (DDL) - CORREGIDA
-- ======================================================
DROP DATABASE IF EXISTS gestor_abbyac27;
CREATE DATABASE gestor_abbyac27;
USE gestor_abbyac27;

-- MÓDULO: USUARIOS Y CONTACTO
CREATE TABLE Usuario (
    usuario_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(255) NOT NULL,
    pass          VARCHAR(255) NOT NULL,
    estado        BOOLEAN NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL,
    documento     VARCHAR(50) UNIQUE,
    fecha_registro DATE,
    fecha_inicio  DATE,
    minimo_compra DECIMAL(10,2)
);

CREATE TABLE Telefono_Usuario (
    telefono_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    telefono    VARCHAR(50)  NOT NULL,
    usuario_id  INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Correo_Usuario (
    correo_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

-- MÓDULO: ROLES Y SEGURIDAD
CREATE TABLE Rol (
    rol_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cargo      ENUM('vendedor','administrador','proveedor','cliente') NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Permiso (
    permiso_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    descripcion TEXT
);

-- RF: Rol_Permiso corregida (PK compuesta, no solo rol_id)
CREATE TABLE Rol_Permiso (
    rol_id     INT UNSIGNED NOT NULL,
    permiso_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id)     REFERENCES Rol(rol_id),
    FOREIGN KEY (permiso_id) REFERENCES Permiso(permiso_id)
);

CREATE TABLE Recuperacion_Contrasena (
    recuperacion_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id       INT UNSIGNED NOT NULL,
    token            VARCHAR(255) UNIQUE NOT NULL,
    fecha_solicitud  DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    estado           BOOLEAN DEFAULT 1,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

-- MÓDULO: PRODUCTOS Y CATEGORÍAS
CREATE TABLE Subcategoria (
    subcategoria_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(255) NOT NULL
);

CREATE TABLE Categoria (
    categoria_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(255) NOT NULL,
    icono           VARCHAR(255),
    subcategoria_id INT UNSIGNED,
    FOREIGN KEY (subcategoria_id) REFERENCES Subcategoria(subcategoria_id)
);

CREATE TABLE Material (
    material_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL
);

CREATE TABLE Usuario_Material (
    usuario_id  INT UNSIGNED NOT NULL,
    material_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (usuario_id, material_id),
    FOREIGN KEY (usuario_id)  REFERENCES Usuario(usuario_id),
    FOREIGN KEY (material_id) REFERENCES Material(material_id)
);

-- *** CORRECCIÓN PRINCIPAL: se agregan codigo, imagen_data, imagen_tipo ***
CREATE TABLE Producto (
    producto_id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(10)     NOT NULL,       -- ej: ANI01
    nombre              VARCHAR(255)    NOT NULL,
    descripcion         VARCHAR(500),
    stock               INT             NOT NULL DEFAULT 0,
    precio_unitario     DECIMAL(10,2)   NOT NULL,
    precio_venta        DECIMAL(10,2)   NOT NULL,
    fecha_registro      DATE            NOT NULL,
    imagen              VARCHAR(255),                   -- nombre del archivo
    imagen_data         MEDIUMBLOB,                     -- bytes de la imagen en BD
    imagen_tipo         VARCHAR(50),                    -- MIME type, ej: image/jpeg
    material_id         INT UNSIGNED    NOT NULL,
    categoria_id        INT UNSIGNED    NOT NULL,
    usuario_proveedor_id INT UNSIGNED   NOT NULL,
    FOREIGN KEY (material_id)          REFERENCES Material(material_id),
    FOREIGN KEY (categoria_id)         REFERENCES Categoria(categoria_id),
    FOREIGN KEY (usuario_proveedor_id) REFERENCES Usuario(usuario_id)
);

-- MÓDULO: TRANSACCIONES (COMPRAS, VENTAS, PAGOS)
CREATE TABLE Inventario_Movimiento (
    movimiento_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    producto_id   INT UNSIGNED NOT NULL,
    tipo          ENUM('entrada','salida','ajuste') NOT NULL,
    estado        ENUM('activo','inactivo'),
    cantidad      INT NOT NULL,
    fecha         DATETIME NOT NULL,
    referencia    VARCHAR(255),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

CREATE TABLE Compra (
    compra_id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_proveedor_id INT UNSIGNED NOT NULL,
    fecha_compra         DATE         NOT NULL,
    fecha_entrega        DATE         NOT NULL,
    total                DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (usuario_proveedor_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Detalle_Compra (
    detalle_compra_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    compra_id         INT UNSIGNED    NOT NULL,
    producto_id       INT UNSIGNED    NOT NULL,
    precio_unitario   DECIMAL(10,2)   NOT NULL,
    cantidad          INT             NOT NULL,
    subtotal          DECIMAL(10,2)   NOT NULL,
    FOREIGN KEY (compra_id)   REFERENCES Compra(compra_id),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

CREATE TABLE venta_factura (
    venta_id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id        INT UNSIGNED NOT NULL,
    usuario_cliente_id INT UNSIGNED NOT NULL,
    fecha_emision     DATE         NOT NULL,
    total             DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (usuario_id)         REFERENCES Usuario(usuario_id),
    FOREIGN KEY (usuario_cliente_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Detalle_Venta (
    detalle_venta_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id         INT UNSIGNED NOT NULL,
    producto_id      INT UNSIGNED NOT NULL,
    cantidad         INT          NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,
    subtotal         DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venta_id)    REFERENCES venta_factura(venta_id),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

CREATE TABLE Metodo_pago (
    metodo_pago_id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venta_id       INT UNSIGNED,
    compra_id      INT UNSIGNED,
    monto          DECIMAL(12,2) NOT NULL,
    metodo         ENUM('efectivo','tarjeta') NOT NULL,
    fecha          DATE NOT NULL,
    estado         ENUM('pendiente','confirmado','rechazado') NOT NULL,
    FOREIGN KEY (venta_id)  REFERENCES venta_factura(venta_id),
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
);

CREATE TABLE Pago_Venta (
    pago_venta_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id       INT UNSIGNED NOT NULL,
    metodo_pago_id INT UNSIGNED NOT NULL,
    monto          DECIMAL(12,2) NOT NULL,
    fecha          DATETIME NOT NULL,
    estado         ENUM('pendiente','confirmado','rechazado') NOT NULL,
    FOREIGN KEY (venta_id)       REFERENCES venta_factura(venta_id),
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_pago(metodo_pago_id)
);

-- MÓDULO: CRÉDITOS
CREATE TABLE Credito_Compra (
    credito_id       INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    compra_id        INT UNSIGNED NOT NULL,
    monto_total      DECIMAL(12,2) NOT NULL,
    saldo_pendiente  DECIMAL(12,2) NOT NULL,
    fecha_inicio     DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado           ENUM('activo','pagado','vencido') NOT NULL,
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
);

CREATE TABLE Abono_Credito_Compra (
    abono_id       INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    credito_id     INT UNSIGNED NOT NULL,
    metodo_pago_id INT UNSIGNED NOT NULL,
    monto_abono    DECIMAL(12,2) NOT NULL,
    fecha          DATETIME NOT NULL,
    estado         ENUM('pendiente','confirmado') NOT NULL,
    FOREIGN KEY (credito_id)     REFERENCES Credito_Compra(credito_id),
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_pago(metodo_pago_id)
);

-- POSTVENTA / POSTCOMPRA
CREATE TABLE Caso_Postventa_Cliente (
    caso_id  INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    venta_id INT UNSIGNED NOT NULL,
    tipo     ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad INT NOT NULL,
    motivo   TEXT,
    fecha    DATE NOT NULL,
    estado   ENUM('en_proceso','aprobado','cancelado'),
    FOREIGN KEY (venta_id) REFERENCES venta_factura(venta_id)
);

CREATE TABLE Caso_Postcompra_Usuario (
    caso_id   INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    compra_id INT UNSIGNED NOT NULL,
    tipo      ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad  INT NOT NULL,
    motivo    TEXT,
    fecha     DATE NOT NULL,
    estado    ENUM('en_proceso','aprobado','cancelado'),
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
);

CREATE TABLE Estado_Caso_Cliente (
    estado_id   INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    caso_id     INT UNSIGNED NOT NULL,
    estado      ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha       DATETIME NOT NULL,
    observacion TEXT,
    FOREIGN KEY (caso_id) REFERENCES Caso_Postventa_Cliente(caso_id)
);

CREATE TABLE Estado_Caso_Proveedor (
    estado_id   INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    caso_id     INT UNSIGNED NOT NULL,
    estado      ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha       DATETIME NOT NULL,
    observacion TEXT,
    FOREIGN KEY (caso_id) REFERENCES Caso_Postcompra_Usuario(caso_id)
);

-- DESEMPEÑO
CREATE TABLE Desempeno_Vendedor (
    desempeno_id        INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id          INT UNSIGNED NOT NULL,
    ventas_totales      DECIMAL(12,2) NOT NULL,
    comision_porcentaje DECIMAL(5,2)  NOT NULL,
    comision_ganada     DECIMAL(12,2) NOT NULL,
    periodo             DATE NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Pago_credito (
    pago_credito_id   INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    metodo_pago_id    INT UNSIGNED NOT NULL,
    credito_id        INT UNSIGNED NOT NULL,
    monto_abono       DECIMAL(12,2) NOT NULL,
    fecha             DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    metodo            ENUM('efectivo','tarjeta') NOT NULL,
    estado            ENUM('pendiente','pagado','vencido') NOT NULL,
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_pago(metodo_pago_id)
);

-- ======================================================
-- INSERT INICIAL
-- ======================================================
INSERT INTO Usuario (nombre, pass, estado, fecha_creacion)
VALUES ('AdminKS','$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.',1,NOW());

INSERT INTO Rol (cargo, usuario_id)
VALUES ('administrador', 1);

INSERT INTO Categoria (nombre, icono) VALUES
('Anillos',       'anillos.png'),
('Topitos',       'topitos.png'),
('Aretes largos', 'aretes_largos.png'),
('Conjuntos',     'juegos.png'),
('Earcuff',       'earcuff.png'),
('Rosarios',      'rosario.png'),
('Collares',      'collar.png'),
('Tobilleras',    'tobillera.png'),
('Dijes',         'dijes.png'),
('Denarios',      'denarios.png'),
('Pulseras',      'pulsera.png'),
('Manillas',      'manillas.png'),
('Materiales',    'materiales.png');

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


select * from gestor_abbyac27.Compra;
select *from gestor_abbyac27.Detalle_Compra;
select * from gestor_abbyac27.Usuario;
select * from gestor_abbyac27.Producto;
SELECT stock FROM Producto WHERE producto_id = 1;
select * from gestor_abbyac27.rol;