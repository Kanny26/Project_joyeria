-- ======================================================
-- BASE DE DATOS: gestor_abbyac27
-- Normalizada hasta 3FN
-- Imágenes guardadas como MEDIUMBLOB desde backend Java
-- ======================================================
DROP DATABASE IF EXISTS gestor_abbyac27;
CREATE DATABASE gestor_abbyac27
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE gestor_abbyac27;

-- ======================================================
-- MÓDULO 1: USUARIOS DEL SISTEMA (Administrador y Vendedor)
-- Solo quienes tienen login acceden al sistema
-- ======================================================
CREATE TABLE Usuario (
    usuario_id     INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(255)  NOT NULL,
    pass           VARCHAR(255)  NOT NULL,           -- bcrypt hash
    estado         BOOLEAN       NOT NULL DEFAULT 1,
    fecha_creacion DATETIME      NOT NULL DEFAULT NOW()
);

CREATE TABLE Telefono_Usuario (
    telefono_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    telefono    VARCHAR(50)  NOT NULL,
    usuario_id  INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
        ON DELETE CASCADE
);

CREATE TABLE Correo_Usuario (
    correo_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
        ON DELETE CASCADE
);

-- ======================================================
-- MÓDULO 2: ROLES Y PERMISOS (solo para usuarios del sistema)
-- ======================================================
-- CORRECCIÓN: La relación Usuario→Rol va desde Usuario,
-- no desde Rol hacia Usuario. Se usa tabla intermedia
-- para permitir que un usuario tenga múltiples roles.
CREATE TABLE Rol (
    rol_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cargo  ENUM('vendedor','administrador') NOT NULL  -- solo roles con login
);

CREATE TABLE Usuario_Rol (
    usuario_id INT UNSIGNED NOT NULL,
    rol_id     INT UNSIGNED NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id)     REFERENCES Rol(rol_id)         ON DELETE CASCADE
);

CREATE TABLE Permiso (
    permiso_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    descripcion TEXT
);

CREATE TABLE Rol_Permiso (
    rol_id     INT UNSIGNED NOT NULL,
    permiso_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id)     REFERENCES Rol(rol_id)         ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES Permiso(permiso_id) ON DELETE CASCADE
);

CREATE TABLE Recuperacion_Contrasena (
    recuperacion_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id       INT UNSIGNED NOT NULL,
    token            VARCHAR(255) UNIQUE NOT NULL,
    fecha_solicitud  DATETIME     NOT NULL,
    fecha_expiracion DATETIME     NOT NULL,
    estado           BOOLEAN      NOT NULL DEFAULT 1,  -- 1=vigente, 0=usado
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
        ON DELETE CASCADE
);

-- ======================================================
-- MÓDULO 3: PROVEEDORES (registro informativo, sin login)
-- ======================================================
CREATE TABLE Proveedor (
    proveedor_id   INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(255)  NOT NULL,
    documento      VARCHAR(50)   UNIQUE NOT NULL,
    fecha_registro DATE          NOT NULL DEFAULT (CURDATE()),
    fecha_inicio   DATE,
    estado         BOOLEAN       NOT NULL DEFAULT 1
);

CREATE TABLE Telefono_Proveedor (
    telefono_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    telefono     VARCHAR(50)  NOT NULL,
    proveedor_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id)
        ON DELETE CASCADE
);

CREATE TABLE Correo_Proveedor (
    correo_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email        VARCHAR(255) NOT NULL,
    proveedor_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id)
        ON DELETE CASCADE
);

-- ======================================================
-- MÓDULO 4: CLIENTES (registro informativo, sin login)
-- ======================================================
CREATE TABLE Cliente (
    cliente_id     INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(255)  NOT NULL,
    documento      VARCHAR(50)   UNIQUE,
    fecha_registro DATE          NOT NULL DEFAULT (CURDATE()),
    minimo_compra  DECIMAL(10,2),
    estado         BOOLEAN       NOT NULL DEFAULT 1
);

CREATE TABLE Telefono_Cliente (
    telefono_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    telefono    VARCHAR(50)  NOT NULL,
    cliente_id  INT UNSIGNED NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_id)
        ON DELETE CASCADE
);

CREATE TABLE Correo_Cliente (
    correo_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    cliente_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_id)
        ON DELETE CASCADE
);

-- ======================================================
-- MÓDULO 5: CATEGORÍAS Y MATERIALES
-- ======================================================
-- CORRECCIÓN: Subcategoria no depende de Categoria,
-- es Categoria quien puede tener una Subcategoria opcional.
-- Se mantiene igual porque la relación ya era correcta.
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
        ON DELETE SET NULL
);

CREATE TABLE Material (
    material_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL
);

-- CORRECCIÓN: Renombrada de Usuario_Material a Proveedor_Material
-- porque son los Proveedores quienes suministran materiales,
-- no los usuarios del sistema (Admins/Vendedores).
CREATE TABLE Proveedor_Material (
    proveedor_id INT UNSIGNED NOT NULL,
    material_id  INT UNSIGNED NOT NULL,
    PRIMARY KEY (proveedor_id, material_id),
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id) ON DELETE CASCADE,
    FOREIGN KEY (material_id)  REFERENCES Material(material_id)   ON DELETE CASCADE
);

-- ======================================================
-- MÓDULO 6: PRODUCTOS
-- imagen      → nombre original del archivo (opcional)
-- imagen_data → MEDIUMBLOB, bytes enviados desde Java
-- imagen_tipo → MIME type: 'image/jpeg', 'image/png', etc.
-- ======================================================
CREATE TABLE Producto (
    producto_id     INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(10)   NOT NULL UNIQUE,
    nombre          VARCHAR(255)  NOT NULL,
    descripcion     VARCHAR(500),
    stock           INT           NOT NULL DEFAULT 0,
    estado          BOOLEAN       NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(10,2) NOT NULL,
    precio_venta    DECIMAL(10,2) NOT NULL,
    fecha_registro  DATE          NOT NULL DEFAULT (CURDATE()),
    imagen          VARCHAR(255),
    imagen_data     MEDIUMBLOB,
    imagen_tipo     VARCHAR(50),
    material_id     INT UNSIGNED  NOT NULL,
    categoria_id    INT UNSIGNED  NOT NULL,
    subcategoria_id INT UNSIGNED,
    proveedor_id    INT UNSIGNED  NOT NULL,            -- FK a Proveedor real
    FOREIGN KEY (material_id)    REFERENCES Material(material_id),
    FOREIGN KEY (categoria_id)   REFERENCES Categoria(categoria_id),
    FOREIGN KEY (subcategoria_id)REFERENCES Subcategoria(subcategoria_id)
        ON DELETE SET NULL,
    FOREIGN KEY (proveedor_id)   REFERENCES Proveedor(proveedor_id)
);

-- ======================================================
-- MÓDULO 7: INVENTARIO
-- ======================================================
CREATE TABLE Inventario_Movimiento (
    movimiento_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    producto_id   INT UNSIGNED NOT NULL,
    usuario_id    INT UNSIGNED NOT NULL,               -- quién registró el movimiento
    tipo          ENUM('entrada','salida','ajuste') NOT NULL,
    cantidad      INT          NOT NULL,
    fecha         DATETIME     NOT NULL DEFAULT NOW(),
    referencia    VARCHAR(255),                        -- ej: "COMPRA-001", "VENTA-005"
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id),
    FOREIGN KEY (usuario_id)  REFERENCES Usuario(usuario_id)
);

-- ======================================================
-- MÓDULO 8: COMPRAS (Joyería → Proveedor)
-- ======================================================
CREATE TABLE Compra (
    compra_id    INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    proveedor_id INT UNSIGNED  NOT NULL,
    usuario_id   INT UNSIGNED  NOT NULL,               -- vendedor/admin que registró
    fecha_compra DATE          NOT NULL,
    fecha_entrega DATE         NOT NULL,
    -- CORRECCIÓN 3FN: total se elimina porque es SUM(cantidad*precio_unitario)
    -- Si tu profe exige el campo por practicidad, descomenta la línea:
    -- total     DECIMAL(10,2) GENERATED ALWAYS AS STORED (...) [no soportado fácil en MySQL con subquery]
    -- Se recomienda calcularlo desde Java al momento de mostrar.
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id),
    FOREIGN KEY (usuario_id)   REFERENCES Usuario(usuario_id)
);

CREATE TABLE Detalle_Compra (
    detalle_compra_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    compra_id         INT UNSIGNED  NOT NULL,
    producto_id       INT UNSIGNED  NOT NULL,
    precio_unitario   DECIMAL(10,2) NOT NULL,
    cantidad          INT           NOT NULL,
    -- CORRECCIÓN 3FN: subtotal es derivado (precio_unitario * cantidad),
    -- se calcula en el backend Java. Eliminado para evitar redundancia.
    FOREIGN KEY (compra_id)   REFERENCES Compra(compra_id)   ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

-- ======================================================
-- MÓDULO 9: VENTAS (Joyería → Cliente)
-- ======================================================
CREATE TABLE Venta (
    venta_id   INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNSIGNED  NOT NULL,                 -- vendedor que realizó la venta
    cliente_id INT UNSIGNED  NOT NULL,
    fecha_emision DATE        NOT NULL DEFAULT (CURDATE()),
    -- CORRECCIÓN 3FN: total derivado de detalles, se calcula en Java.
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_id)
);

CREATE TABLE Detalle_Venta (
    detalle_venta_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    venta_id         INT UNSIGNED  NOT NULL,
    producto_id      INT UNSIGNED  NOT NULL,
    cantidad         INT           NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,
    -- CORRECCIÓN 3FN: subtotal es derivado, se calcula en Java.
    FOREIGN KEY (venta_id)    REFERENCES Venta(venta_id)       ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
);

-- ======================================================
-- MÓDULO 10: MÉTODOS DE PAGO (catálogo simple)
-- CORRECCIÓN: Metodo_pago era una tabla híbrida con FKs a venta
-- y compra al mismo tiempo (violaba diseño relacional limpio)
-- y duplicaba datos con Pago_Venta y Pago_credito.
-- Se reemplaza por un catálogo de métodos y tablas de pago separadas.
-- ======================================================
CREATE TABLE Metodo_Pago (
    metodo_pago_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre         ENUM('efectivo','tarjeta') NOT NULL
);

-- Pagos de ventas
CREATE TABLE Pago_Venta (
    pago_venta_id  INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    venta_id       INT UNSIGNED  NOT NULL,
    metodo_pago_id INT UNSIGNED  NOT NULL,
    monto          DECIMAL(12,2) NOT NULL,
    fecha          DATETIME      NOT NULL DEFAULT NOW(),
    estado         ENUM('pendiente','confirmado','rechazado') NOT NULL DEFAULT 'pendiente',
    FOREIGN KEY (venta_id)       REFERENCES Venta(venta_id)             ON DELETE CASCADE,
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
);

-- Pagos de compras
CREATE TABLE Pago_Compra (
    pago_compra_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    compra_id      INT UNSIGNED  NOT NULL,
    metodo_pago_id INT UNSIGNED  NOT NULL,
    monto          DECIMAL(12,2) NOT NULL,
    fecha          DATETIME      NOT NULL DEFAULT NOW(),
    estado         ENUM('pendiente','confirmado','rechazado') NOT NULL DEFAULT 'pendiente',
    FOREIGN KEY (compra_id)      REFERENCES Compra(compra_id)           ON DELETE CASCADE,
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
);

-- ======================================================
-- MÓDULO 11: CRÉDITOS
-- CORRECCIÓN: Pago_credito duplicaba campos de Metodo_pago.
-- Se unifica en Abono_Credito con FK limpia a Metodo_Pago.
-- ======================================================
CREATE TABLE Credito_Compra (
    credito_id        INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    compra_id         INT UNSIGNED  NOT NULL UNIQUE,   -- 1 crédito por compra
    monto_total       DECIMAL(12,2) NOT NULL,
    saldo_pendiente   DECIMAL(12,2) NOT NULL,
    fecha_inicio      DATE          NOT NULL,
    fecha_vencimiento DATE          NOT NULL,
    estado            ENUM('activo','pagado','vencido') NOT NULL DEFAULT 'activo',
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
);

-- CORRECCIÓN: Se fusionaron Abono_Credito_Compra + Pago_credito
-- en una sola tabla sin campos duplicados.
CREATE TABLE Abono_Credito (
    abono_id       INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    credito_id     INT UNSIGNED  NOT NULL,
    metodo_pago_id INT UNSIGNED  NOT NULL,
    monto_abono    DECIMAL(12,2) NOT NULL,
    fecha          DATETIME      NOT NULL DEFAULT NOW(),
    estado         ENUM('pendiente','confirmado') NOT NULL DEFAULT 'pendiente',
    FOREIGN KEY (credito_id)     REFERENCES Credito_Compra(credito_id),
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
);

-- ======================================================
-- MÓDULO 12: POSTVENTA / POSTCOMPRA
-- ======================================================
CREATE TABLE Caso_Postventa (
    caso_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id   INT UNSIGNED NOT NULL,
    tipo       ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad   INT  NOT NULL,
    motivo     TEXT,
    fecha      DATE NOT NULL DEFAULT (CURDATE()),
    estado     ENUM('en_proceso','aprobado','cancelado') NOT NULL DEFAULT 'en_proceso',
    FOREIGN KEY (venta_id) REFERENCES Venta(venta_id)
);

CREATE TABLE Caso_Postcompra (
    caso_id   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    compra_id INT UNSIGNED NOT NULL,
    tipo      ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad  INT  NOT NULL,
    motivo    TEXT,
    fecha     DATE NOT NULL DEFAULT (CURDATE()),
    estado    ENUM('en_proceso','aprobado','cancelado') NOT NULL DEFAULT 'en_proceso',
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
);

-- Historial de cambios de estado (auditoría)
CREATE TABLE Historial_Caso_Postventa (
    historial_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    caso_id      INT UNSIGNED NOT NULL,
    estado       ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha        DATETIME NOT NULL DEFAULT NOW(),
    observacion  TEXT,
    usuario_id   INT UNSIGNED NOT NULL,               -- quién hizo el cambio
    FOREIGN KEY (caso_id)    REFERENCES Caso_Postventa(caso_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

CREATE TABLE Historial_Caso_Postcompra (
    historial_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    caso_id      INT UNSIGNED NOT NULL,
    estado       ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha        DATETIME     NOT NULL DEFAULT NOW(),
    observacion  TEXT,
    usuario_id   INT UNSIGNED NOT NULL,
    FOREIGN KEY (caso_id)    REFERENCES Caso_Postcompra(caso_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

-- ======================================================
-- MÓDULO 13: DESEMPEÑO DE VENDEDORES
-- CORRECCIÓN 3FN: comision_ganada es derivada
-- (ventas_totales * comision_porcentaje / 100), se elimina.
-- El backend Java la calcula al momento de mostrar el reporte.
-- ======================================================
CREATE TABLE Desempeno_Vendedor (
    desempeno_id        INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    usuario_id          INT UNSIGNED  NOT NULL,
    ventas_totales      DECIMAL(12,2) NOT NULL,
    comision_porcentaje DECIMAL(5,2)  NOT NULL,
    periodo             DATE          NOT NULL,        -- primer día del mes evaluado
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
);

-- ======================================================
-- DATOS INICIALES
-- ======================================================

-- Métodos de pago base
INSERT INTO Metodo_Pago (nombre) VALUES ('efectivo'), ('tarjeta');

-- Roles del sistema
INSERT INTO Rol (cargo) VALUES ('administrador'), ('vendedor');

-- Usuario administrador
INSERT INTO Usuario (nombre, pass, estado, fecha_creacion)
VALUES ('AdminKS', '$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.', 1, NOW());

-- Asignar rol administrador al usuario 1
INSERT INTO Usuario_Rol (usuario_id, rol_id) VALUES (1, 1);

-- Proveedor (registro informativo, sin login)
INSERT INTO Proveedor (nombre, documento, fecha_registro, fecha_inicio, estado)
VALUES ('Joyeria luz de sol', '900123456', CURDATE(), '2025-02-12', 1);

-- Categorías
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

-- Materiales
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

-- Subcategorías
INSERT INTO Subcategoria (nombre) VALUES
('Matrimonio'),
('15 Años'),
('Compromiso'),
('Aniversario'),
('Graduación'),
('Cumpleaños'),
('Bautizo'),
('Primera Comunión'),
('Confirmación'),
('Día de la Madre'),
('Día del Padre'),
('Navidad'),
('San Valentín'),
('Eventos Empresariales'),
('Uso Diario');

-- Productos (imagen_data = NULL → el backend Java lo llena con setBytes())
INSERT INTO Producto
    (codigo, nombre, descripcion, stock, precio_unitario, precio_venta,
     fecha_registro, imagen, imagen_data, imagen_tipo,
     material_id, categoria_id, subcategoria_id, proveedor_id)
VALUES
('ANI01','Anillo Aura Dorada',       'Anillo de oro 18K ideal para matrimonio.',  10,  15000,  32000, CURDATE(),'Anillo_aura_dorada.jpg',        NULL,NULL,1,1, 1,1),
('ANI02','Anillo Brillo Soberano',   'Diseño elegante para 15 años.',              8, 300000, 480000, CURDATE(),'Anillo_Brillo_Soberano.jpg',     NULL,NULL,1,1, 3,1),
('ANI03','Anillo Destello Celestial','Plata con acabado brillante.',               6, 280000, 450000, CURDATE(),'anillo_destello_celestial.jpg',  NULL,NULL,3,1, 3,1),
('ANI04','Anillo Destello Imperial', 'Plata 925 estilo minimalista.',             15,  90000, 180000, CURDATE(),'Anillo_destello_imperial.jpg',   NULL,NULL,2,1, 6,1),
('ANI05','Anillo Destello Lila',     'Diseño clásico para eventos especiales.',    5, 400000, 650000, CURDATE(),'anillo_destello_lila.jpg',       NULL,NULL,1,1, 1,1),
('ANI06','Anillo Dulce Reino',       'Modelo romántico para aniversario.',        12, 250000, 390000, CURDATE(),'anillo_dulce_reino.jpg',         NULL,NULL,1,1, 4,1),
('ANI07','Anillo Eclipse Real',      'Titanio resistente estilo masculino.',      20, 120000, 240000, CURDATE(),'Anillo_Eclipse_Real.jpg',        NULL,NULL,5,1, 6,1),
('ANI08','Anillo Mi Tesoro',         'Acabado brillante en oro blanco.',           7, 310000, 500000, CURDATE(),'Anillo_mi_tesoro.jpg',           NULL,NULL,3,1, 3,1),
('ANI09','Anillo flor tulipan',      'Ideal para 15 años.',                        9, 270000, 420000, CURDATE(),'Anillo_tulipan.jpg',             NULL,NULL,1,1, 2,1),
('ANI10','Anillo princesa valiente', 'Modelo elegante para uso diario.',          18, 150000, 280000, CURDATE(),'Anillo_valiente.jpg',            NULL,NULL,4,1,15,1);

-- ======================================================
-- CONSULTAS DE VERIFICACIÓN
-- ======================================================
SELECT * FROM Usuario;
SELECT * FROM Rol;
SELECT * FROM Usuario_Rol;
SELECT * FROM Proveedor;
SELECT * FROM Cliente;
SELECT * FROM Categoria;
SELECT * FROM Subcategoria;
SELECT * FROM Material;
SELECT
    producto_id, codigo, nombre, stock, precio_venta,
    imagen_tipo, ROUND(LENGTH(imagen_data)/1024,1) AS kb
FROM Producto;
SELECT * FROM Metodo_Pago;
SELECT * FROM Compra;
SELECT * FROM Detalle_Compra;
SELECT * FROM Venta;