-- ============================================================
-- BASE DE DATOS: gestor_abbyac27
-- Versión corregida: relación Producto-Subcategoría normalizada
-- ============================================================

SET FOREIGN_KEY_CHECKS=0;

DROP DATABASE IF EXISTS gestor_abbyac27;
CREATE DATABASE gestor_abbyac27 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestor_abbyac27;

-- ============================================================
-- MÓDULO 1: USUARIOS DEL SISTEMA
-- ============================================================
CREATE TABLE Usuario (
    usuario_id     INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(255)  NOT NULL,
    pass           VARCHAR(255)  NOT NULL,
    estado         BOOLEAN       NOT NULL DEFAULT 1,
    pass_temporal  TINYINT(1)    NOT NULL DEFAULT 1,
    fecha_creacion DATETIME      NOT NULL DEFAULT NOW()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Telefono_Usuario (
    telefono_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    telefono    VARCHAR(50)  NOT NULL,
    usuario_id  INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Correo_Usuario (
    correo_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    usuario_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 2: ROLES Y PERMISOS
-- ============================================================
CREATE TABLE Rol (
    rol_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    cargo  ENUM('superadministrador','administrador','vendedor') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Usuario_Rol (
    usuario_id INT UNSIGNED NOT NULL,
    rol_id     INT UNSIGNED NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id)     REFERENCES Rol(rol_id)         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Permiso (
    permiso_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Rol_Permiso (
    rol_id     INT UNSIGNED NOT NULL,
    permiso_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id)     REFERENCES Rol(rol_id)         ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES Permiso(permiso_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Recuperacion_Contrasena (
    recuperacion_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id          INT UNSIGNED NOT NULL,
    codigo_verificacion INT UNIQUE   NOT NULL,
    fecha_solicitud     DATETIME     NOT NULL,
    fecha_expiracion    DATETIME     NOT NULL,
    estado              BOOLEAN      NOT NULL DEFAULT 1,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 3: PROVEEDORES
-- ============================================================
CREATE TABLE Proveedor (
    proveedor_id   INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(255)  NOT NULL,
    documento      VARCHAR(50)   UNIQUE NOT NULL,
    fecha_registro DATE          NOT NULL DEFAULT (CURDATE()),
    fecha_inicio   DATE,
    minimo_compra  DECIMAL(10,2) NOT NULL,
    estado         BOOLEAN       NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Telefono_Proveedor (
    telefono_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    telefono     VARCHAR(50)  NOT NULL,
    proveedor_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Correo_Proveedor (
    correo_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email        VARCHAR(255) NOT NULL,
    proveedor_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 4: CLIENTES
-- ============================================================
CREATE TABLE Cliente (
    cliente_id     INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(255)  NOT NULL,
    documento      VARCHAR(50)   UNIQUE,
    fecha_registro DATE          NOT NULL DEFAULT (CURDATE()),
    minimo_compra  DECIMAL(10,2),
    estado         BOOLEAN       NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Telefono_Cliente (
    telefono_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    telefono    VARCHAR(50)  NOT NULL,
    cliente_id  INT UNSIGNED NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Correo_Cliente (
    correo_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    cliente_id INT UNSIGNED NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 5: CATEGORÍAS, SUBCATEGORÍAS Y MATERIALES
-- ============================================================
CREATE TABLE Categoria (
    categoria_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(255) NOT NULL,
    icono        VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Subcategoria (
    subcategoria_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Catálogo de combinaciones válidas (Categoria ↔ Subcategoria)
-- Define qué subcategorías son aplicables a cada categoría
CREATE TABLE Categoria_Subcategoria (
    categoria_id    INT UNSIGNED NOT NULL,
    subcategoria_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (categoria_id, subcategoria_id),
    FOREIGN KEY (categoria_id)    REFERENCES Categoria(categoria_id)    ON DELETE CASCADE,
    FOREIGN KEY (subcategoria_id) REFERENCES Subcategoria(subcategoria_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Material (
    material_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Proveedor_Material (
    proveedor_id INT UNSIGNED NOT NULL,
    material_id  INT UNSIGNED NOT NULL,
    PRIMARY KEY (proveedor_id, material_id),
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id) ON DELETE CASCADE,
    FOREIGN KEY (material_id)  REFERENCES Material(material_id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 6: PRODUCTOS
-- CORRECCIÓN: se elimina subcategoria_id de esta tabla.
-- La relación con subcategorías se maneja en Producto_Subcategoria.
-- ============================================================
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
    proveedor_id    INT UNSIGNED  NOT NULL,
    FOREIGN KEY (material_id)  REFERENCES Material(material_id),
    FOREIGN KEY (categoria_id) REFERENCES Categoria(categoria_id),
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- NUEVA TABLA: Producto_Subcategoria
-- Un producto pertenece a UNA categoría (en Producto)
-- y puede tener VARIAS subcategorías (aquí).
-- Solo se permiten combos válidos según Categoria_Subcategoria.
-- ============================================================
CREATE TABLE Producto_Subcategoria (
    producto_id     INT UNSIGNED NOT NULL,
    subcategoria_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (producto_id, subcategoria_id),
    FOREIGN KEY (producto_id)     REFERENCES Producto(producto_id)          ON DELETE CASCADE,
    FOREIGN KEY (subcategoria_id) REFERENCES Subcategoria(subcategoria_id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 7: INVENTARIO
-- ============================================================
CREATE TABLE Inventario_Movimiento (
    movimiento_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    producto_id   INT UNSIGNED NOT NULL,
    usuario_id    INT UNSIGNED NULL,
    tipo          ENUM('entrada','salida','ajuste') NOT NULL,
    cantidad      INT          NOT NULL,
    fecha         DATETIME     NOT NULL DEFAULT NOW(),
    referencia    VARCHAR(255),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id),
    FOREIGN KEY (usuario_id)  REFERENCES Usuario(usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 8: COMPRAS
-- ============================================================
CREATE TABLE Compra (
    compra_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    proveedor_id  INT UNSIGNED NOT NULL,
    fecha_compra  DATE         NOT NULL,
    fecha_entrega DATE         NOT NULL,
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Detalle_Compra (
    detalle_compra_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    compra_id         INT UNSIGNED  NOT NULL,
    producto_id       INT UNSIGNED  NOT NULL,
    precio_unitario   DECIMAL(10,2) NOT NULL,
    cantidad          INT           NOT NULL,
    FOREIGN KEY (compra_id)   REFERENCES Compra(compra_id)    ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 9: VENTAS
-- ============================================================
CREATE TABLE Venta (
    venta_id      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id    INT UNSIGNED NOT NULL,
    cliente_id    INT UNSIGNED NOT NULL,
    fecha_emision DATE         NOT NULL DEFAULT (CURDATE()),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Detalle_Venta (
    detalle_venta_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    venta_id         INT UNSIGNED  NOT NULL,
    producto_id      INT UNSIGNED  NOT NULL,
    cantidad         INT           NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venta_id)    REFERENCES Venta(venta_id)        ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 10: MÉTODOS DE PAGO
-- ============================================================
CREATE TABLE Metodo_Pago (
    metodo_pago_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Pago_Venta (
    pago_venta_id  INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    venta_id       INT UNSIGNED  NOT NULL,
    metodo_pago_id INT UNSIGNED  NOT NULL,
    monto          DECIMAL(12,2) NOT NULL,
    fecha          DATETIME      NOT NULL DEFAULT NOW(),
    estado         ENUM('pendiente','confirmado') NOT NULL DEFAULT 'pendiente',
    FOREIGN KEY (venta_id)       REFERENCES Venta(venta_id)              ON DELETE CASCADE,
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Pago_Compra (
    pago_compra_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    compra_id      INT UNSIGNED  NOT NULL,
    metodo_pago_id INT UNSIGNED  NOT NULL,
    monto          DECIMAL(12,2) NOT NULL,
    fecha          DATETIME      NOT NULL DEFAULT NOW(),
    estado         ENUM('pendiente','confirmado','rechazado') NOT NULL DEFAULT 'pendiente',
    FOREIGN KEY (compra_id)      REFERENCES Compra(compra_id)            ON DELETE CASCADE,
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 11: CRÉDITOS
-- ============================================================
CREATE TABLE Credito_Compra (
    credito_id        INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    compra_id         INT UNSIGNED  NOT NULL UNIQUE,
    monto_total       DECIMAL(12,2) NOT NULL,
    saldo_pendiente   DECIMAL(12,2) NOT NULL,
    fecha_inicio      DATE          NOT NULL,
    fecha_vencimiento DATE          NOT NULL,
    estado            ENUM('activo','pagado','vencido') NOT NULL DEFAULT 'activo',
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Abono_Credito (
    abono_id       INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    credito_id     INT UNSIGNED  NOT NULL,
    metodo_pago_id INT UNSIGNED  NOT NULL,
    monto_abono    DECIMAL(12,2) NOT NULL,
    fecha          DATETIME      NOT NULL DEFAULT NOW(),
    estado         ENUM('pendiente','confirmado') NOT NULL DEFAULT 'pendiente',
    FOREIGN KEY (credito_id)     REFERENCES Credito_Compra(credito_id),
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 12: POSTVENTA
-- ============================================================
CREATE TABLE Caso_Postventa (
    caso_id  INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id INT UNSIGNED NOT NULL,
    tipo     ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad INT          NOT NULL,
    motivo   TEXT,
    fecha    DATE         NOT NULL DEFAULT (CURDATE()),
    estado   ENUM('en_proceso','aprobado','cancelado') NOT NULL DEFAULT 'en_proceso',
    FOREIGN KEY (venta_id) REFERENCES Venta(venta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Historial_Caso_Postventa (
    historial_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    caso_id      INT UNSIGNED NOT NULL,
    estado       ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha        DATETIME     NOT NULL DEFAULT NOW(),
    observacion  TEXT,
    usuario_id   INT UNSIGNED NOT NULL,
    FOREIGN KEY (caso_id)    REFERENCES Caso_Postventa(caso_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- MÓDULO 13: AUDITORÍA
-- ============================================================
CREATE TABLE Auditoria_Log (
    log_id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id       INT UNSIGNED,
    accion           VARCHAR(100) NOT NULL,
    entidad          VARCHAR(50),
    entidad_id       INT UNSIGNED,
    datos_anteriores JSON,
    datos_nuevos     JSON,
    direccion_ip     VARCHAR(45),
    fecha_hora       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE SET NULL,
    INDEX idx_usuario_fecha (usuario_id, fecha_hora),
    INDEX idx_accion        (accion),
    INDEX idx_entidad       (entidad),
    INDEX idx_fecha         (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;


-- ============================================================
-- INSERCIÓN DE DATOS
-- ============================================================

-- Métodos de Pago
INSERT INTO Metodo_Pago (nombre) VALUES
('Efectivo'),
('Tarjeta de Crédito'),
('Tarjeta de Débito'),
('PSE'),
('Nequi'),
('Daviplata'),
('A la Mano'),
('Bancolombia (Transferencia)'),
('Nu (Nubank)'),
('Transfiya'),
('Efecty / La Perla'),
('Bold'),
('Contraentrega (Interrapidísimo)'),
('Contraentrega (Servientrega)'),
('Wompi / Link de Pago');

-- Materiales
-- ID:  1               2              3       4                  5
INSERT INTO Material (nombre) VALUES
('Plata Ley 950'), ('Covergold'), ('Rodio'), ('Acero Inoxidable'), ('Murano'),
-- ID:  6              7         8                    9           10
('Mostacilla'), ('Miyuki'), ('Perlas de imitación'), ('Herrajes'), ('Hilo Chino'),
-- ID:  11       12       13                  14                  15
('Nylon'), ('Lana'), ('Balines de Acero'), ('Balines de Rodio'), ('Balines de Covergold');

-- Permisos
INSERT INTO Permiso (nombre, descripcion) VALUES
('usuarios_crear',           'Crear nuevos usuarios del sistema'),         -- 1
('usuarios_editar',          'Modificar datos de usuarios'),               -- 2
('usuarios_eliminar',        'Eliminar usuarios del sistema'),             -- 3
('productos_crear',          'Registrar nuevos productos'),                -- 4
('productos_editar',         'Actualizar precios y stock'),                -- 5
('productos_eliminar',       'Desactivar productos'),                      -- 6
('ventas_registrar',         'Crear nuevas ventas'),                       -- 7
('ventas_consultar',         'Ver historial de ventas'),                   -- 8
('compras_registrar',        'Registrar compras a proveedores'),           -- 9
('inventario_ajustar',       'Realizar ajustes de inventario'),            -- 10
('reportes_ver',             'Acceder a reportes del sistema'),            -- 11
('creditos_aprobar',         'Aprobar créditos a clientes'),               -- 12
('postventa_gestionar',      'Gestionar cambios y devoluciones'),          -- 13
('desempeno_ver',            'Ver métricas de vendedores'),                -- 14
('configuracion_sistema',    'Acceder a configuración global'),            -- 15
('gestionar_vendedores',     'Crear y editar usuarios con rol Vendedor'),  -- 16
('gestionar_administradores','Crear y editar usuarios con rol Administrador'); -- 17

-- Roles
-- ID: 1=superadministrador  2=administrador  3=vendedor
INSERT INTO Rol (cargo) VALUES
('superadministrador'),
('administrador'),
('vendedor');

-- ============================================================
-- USUARIOS
-- ============================================================
INSERT INTO Usuario (nombre, pass, estado, pass_temporal) VALUES
('AdminKS',        '$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.', 1, 0), -- 1
('Marlen Becerra', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 2
('Carlos Ruiz',    '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 3
('Stephany Moreno','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 4
('Felipe Mora',    '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 5
('Gabriela Silva', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 6
('Héctor Díaz',    '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 7
('Isabella Vega',  '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 8
('Javier Ortiz',   '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 9
('Karen López',    '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 10
('Luis Castro',    '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 11
('María Pérez',    '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 12
('Nicolás Silva',  '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 13
('Olga Ruiz',      '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1), -- 14
('Pablo Díaz',     '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1); -- 15

-- usuario 1 = superadministrador | 2 = administrador | 3-15 = vendedor
INSERT IGNORE INTO Usuario_Rol (usuario_id, rol_id) VALUES
(1, 1),
(2, 2),
(3, 3),(4, 3),(5, 3),(6, 3),(7, 3),(8, 3),
(9, 3),(10,3),(11,3),(12,3),(13,3),(14,3),(15,3);

INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES
('3027131281',1),('3153084721',2),('3123456789',3),('3027131490',4),('3145678901',5),
('3156789012',6),('3167890123',7),('3178901234',8),('3189012345',9),('3190123456',10),
('3201234567',11),('3212345678',12),('3223456789',13),('3234567890',14),('3245678901',15);

INSERT INTO Correo_Usuario (email, usuario_id) VALUES
('marlenbe211@gmail.com',1),('santiago.morenob500@gmail.com',2),('carlos.ruiz@abbyac27.com',3),
('stephanymoreno1826@gmail.com',4),('felipe.andrade@abbyac27.com',5),('gabriela.silva@abbyac27.com',6),
('hector.diaz@abbyac27.com',7),('isabella.ramirez@abbyac27.com',8),('javier.ortiz@abbyac27.com',9),
('karen.lopez@abbyac27.com',10),('luis.castro@abbyac27.com',11),('maria.perez@abbyac27.com',12),
('nicolas.gomez@abbyac27.com',13),('olga.ruiz@abbyac27.com',14),('pablo.diaz@abbyac27.com',15);

-- ============================================================
-- PROVEEDORES
-- ============================================================
INSERT INTO Proveedor (nombre, documento, fecha_inicio, minimo_compra, estado) VALUES
('Joyeria Aurora',                  '900987654-1', '2024-01-15', 5000000.00,  1), -- 1
('Joyeria samaritans',              '800111222-3', '2024-03-10', 2000000.00,  1), -- 2
('Suministros Joyeros S.A.S',       '901555444-5', '2023-11-20', 10000000.00, 1), -- 3
('Perlas del Caribe Ltda',          '890333222-1', '2025-01-05', 1500000.00,  1), -- 4
('Herrajes diamante',               '900777888-9', '2024-06-12', 500000.00,   1), -- 5
('Distribuidora Plata asaleya',     '800444555-2', '2024-08-22', 3000000.00,  1), -- 6
('Distri acero fatima',             '901222333-0', '2023-05-30', 0.00,        1), -- 7
('Empaques Elegance',               '890666777-4', '2024-02-14', 1000000.00,  1), -- 8
('Insumos Joyeros del Eje',         '900111000-8', '2024-09-01', 2500000.00,  1), -- 9
('Brillo Eterno Mayorista',         '800999888-7', '2024-10-10', 6000000.00,  1), -- 10
('Cristales Glam',                  '901333444-6', '2025-02-01', 800000.00,   1), -- 11
('Willy Johns',                     '890888999-5', '2023-08-15', 4000000.00,  1), -- 12
('Relojería Continental',           '900444333-2', '2024-04-18', 1200000.00,  1), -- 13
('Relojeria y accesorios delaida',  '800222111-0', '2024-11-30', 2000000.00,  1), -- 14
('Importaciones Zafiro',            '901666777-3', '2025-01-20', 7500000.00,  1); -- 15

INSERT INTO Telefono_Proveedor (telefono, proveedor_id) VALUES
('6014445566',1),('3101234567',1),('6028889900',2),('3159876543',2),('3001112233',3),
('3204445555',4),('3116667777',5),('3182223344',6),('3015556677',7),('3129990000',8),
('3141112222',9),('3173334444',10),('3007778888',11),('3134445555',12),('3162221111',13);

INSERT INTO Correo_Proveedor (email, proveedor_id) VALUES
('contacto@joyeriaaurora.com',1),('ventas@joyeriasamaritans.co',2),
('gerencia@suministrosjoyeros.com.co',3),('pedidos@perlasdelcaribe.com',4),
('info@herrajesdiamante.com',5),('comercial@plataasaleya.com',6),
('ventas@distriacerofatima.co',7),('comercial@eleganceestuches.com',8),
('gerencia@insumosdeleje.co',9),('mayorista@brilloeterno.com',10),
('pedidos@glamcristales.com',11),('willyjohns@atencion.com',12),
('importaciones@continental.com',13),('delaida@accesorios.com',14),
('importaciones@zafiro.com',15);

-- ============================================================
-- CLIENTES
-- ============================================================
INSERT INTO Cliente (nombre, documento, minimo_compra, estado) VALUES
('María Fernanda Castro',  '1234567890', 50000.00,  1), -- 1
('Juan Pablo Ramírez',     '2345678901', 100000.00, 1), -- 2
('Ana Lucía Gómez',        '3456789012', 75000.00,  1), -- 3
('Carlos Andrés Díaz',     '4567890123', 200000.00, 1), -- 4
('Laura Sofía Martínez',   '5678901234', 80000.00,  1), -- 5
('Diego Fernando López',   '6789012345', 150000.00, 1), -- 6
('Valeria Alejandra Ruiz', '7890123456', 90000.00,  1), -- 7
('Sebastián Mora Silva',   '8901234567', 120000.00, 1), -- 8
('Camila Andrea Pérez',    '9012345678', 60000.00,  1), -- 9
('Andrés Felipe Torres',   '1023456789', 180000.00, 1), -- 10
('Daniela Paola Vega',     '1123456780', 70000.00,  1), -- 11
('Mateo Alejandro Herrera','1223456781', 130000.00, 1), -- 12
('Sofía Isabel Mendoza',   '1323456782', 85000.00,  1), -- 13
('Santiago David Castro',  '1423456783', 160000.00, 1), -- 14
('Isabella María Ortiz',   '1523456784', 95000.00,  1); -- 15

INSERT INTO Telefono_Cliente (telefono, cliente_id) VALUES
('3101112222',1),('3112223333',2),('3123334444',3),('3134445555',4),('3145556666',5),
('3156667777',6),('3167778888',7),('3178889999',8),('3189990000',9),('3190001111',10),
('3201112222',11),('3212223333',12),('3223334444',13),('3234445555',14),('3245556666',15);

INSERT INTO Correo_Cliente (email, cliente_id) VALUES
('maria.castro@email.com',1),('juan.ramirez@email.com',2),('ana.gomez@email.com',3),
('carlos.diaz@email.com',4),('laura.martinez@email.com',5),('diego.lopez@email.com',6),
('valeria.ruiz@email.com',7),('sebastian.mora@email.com',8),('camila.perez@email.com',9),
('andres.torres@email.com',10),('daniela.vega@email.com',11),('mateo.herrera@email.com',12),
('sofia.mendoza@email.com',13),('santiago.castro@email.com',14),('isabella.ortiz@email.com',15);

-- ============================================================
-- CATEGORÍAS
-- ID: 1=Anillos 2=Topitos 3=Aretes Largos 4=Conjuntos 5=Earcuff
--     6=Rosarios 7=Collares 8=Tobilleras 9=Dijes 10=Denarios
--     11=Pulseras 12=Manillas 13=Relojes 14=Estuches 15=Accesorios en lana
-- ============================================================
INSERT INTO Categoria (nombre, icono) VALUES
('Anillos',            'anillos.png'),       -- 1
('Topitos',            'topitos.png'),       -- 2
('Aretes Largos',      'aretes_largos.png'), -- 3
('Conjuntos',          'juegos.png'),        -- 4
('Earcuff',            'earcuff.png'),       -- 5
('Rosarios',           'rosario.png'),       -- 6
('Collares',           'collar.png'),        -- 7
('Tobilleras',         'tobillera.png'),     -- 8
('Dijes',              'dijes.png'),         -- 9
('Denarios',           'denarios.png'),      -- 10
('Pulseras',           'pulsera.png'),       -- 11
('Manillas',           'manillas.png'),      -- 12
('Relojes',            'reloj.png'),         -- 13
('Estuches',           'estuches.png'),      -- 14
('Accesorios en lana', 'lanas.png');         -- 15

-- ============================================================
-- SUBCATEGORÍAS
-- ID: 1=Matrimonio 2=Compromiso 3=15 Años 4=Grados 5=Religioso
--     6=Aniversario 7=Cumpleaños 8=Día de la Madre 9=Amor y Amistad
--     10=Navidad 11=Personalizados 12=Uso Diario 13=Parejas
--     14=Protección/Amuletos 15=Infantil/Bebés
-- ============================================================
INSERT INTO Subcategoria (nombre) VALUES
('Matrimonio'),           -- 1
('Compromiso'),           -- 2
('15 Años'),              -- 3
('Grados'),               -- 4
('Religioso'),            -- 5
('Aniversario'),          -- 6
('Cumpleaños'),           -- 7
('Día de la Madre'),      -- 8
('Amor y Amistad'),       -- 9
('Navidad'),              -- 10
('Personalizados'),       -- 11
('Uso Diario'),           -- 12
('Parejas'),              -- 13
('Protección / Amuletos'),-- 14
('Infantil / Bebés');     -- 15

-- ============================================================
-- CATÁLOGO: combinaciones válidas Categoría ↔ Subcategoría
-- Esto define qué subcategorías puede tener cada categoría.
-- ============================================================
INSERT INTO Categoria_Subcategoria (categoria_id, subcategoria_id) VALUES
-- Anillos (1): Matrimonio, Compromiso, 15 Años, Aniversario, Uso Diario, Parejas
(1,1),(1,2),(1,3),(1,6),(1,12),(1,13),
-- Topitos (2): Uso Diario, Personalizados
(2,12),(2,11),
-- Aretes Largos (3): 15 Años, Matrimonio, Cumpleaños, Día de la Madre
(3,3),(3,1),(3,7),(3,8),
-- Conjuntos (4): Matrimonio, 15 Años, Grados
(4,1),(4,3),(4,4),
-- Earcuff (5): Uso Diario, Personalizados, Amor y Amistad
(5,12),(5,11),(5,9),
-- Rosarios (6): Religioso, Protección/Amuletos
(6,5),(6,14),
-- Collares (7): Matrimonio, 15 Años, Día de la Madre, Amor y Amistad, Personalizados
(7,1),(7,3),(7,8),(7,9),(7,11),
-- Tobilleras (8): Uso Diario, Infantil/Bebés
(8,12),(8,15),
-- Dijes (9): Personalizados, Cumpleaños, Navidad, Amor y Amistad
(9,11),(9,7),(9,10),(9,9),
-- Denarios (10): Religioso, Protección/Amuletos
(10,5),(10,14),
-- Pulseras (11): 15 Años, Amor y Amistad, Parejas, Personalizados, Uso Diario
(11,3),(11,9),(11,13),(11,11),(11,12),
-- Manillas (12): Parejas, Uso Diario, Personalizados, Protección/Amuletos
(12,13),(12,12),(12,11),(12,14),
-- Relojes (13): Uso Diario, Personalizados, Parejas
(13,12),(13,11),(13,13),
-- Estuches (14): Matrimonio, 15 Años, Personalizados
(14,1),(14,3),(14,11),
-- Accesorios en lana (15): Infantil/Bebés, Navidad, Uso Diario
(15,15),(15,10),(15,12);

-- ============================================================
-- PROVEEDOR_MATERIAL
-- ============================================================
INSERT INTO Proveedor_Material (proveedor_id, material_id) VALUES
(1,3),(1,4),(1,5),(2,9),(2,11),(3,5),(3,6),(4,10),(4,2),
(5,14),(5,15),(6,2),(7,3),(7,4),(8,13);

-- ============================================================
-- PRODUCTOS
-- CORRECCIÓN: sin subcategoria_id (se registra en Producto_Subcategoria)
--
-- Referencia material_id:
--   1=Plata Ley 950  2=Covergold  3=Rodio  4=Acero Inoxidable  5=Murano
--   6=Mostacilla  7=Miyuki  11=Nylon  14=Balines de Rodio
--
-- Referencia categoria_id:
--   1=Anillos  2=Topitos  3=Aretes Largos  4=Conjuntos  5=Earcuff
--   6=Rosarios  7=Collares  8=Tobilleras  9=Dijes  10=Denarios
--   11=Pulseras  12=Manillas
-- ============================================================
INSERT INTO Producto
    (codigo, nombre, descripcion, stock, precio_unitario, precio_venta,
     fecha_registro, imagen, imagen_data, imagen_tipo,
     material_id, categoria_id, proveedor_id)
VALUES
-- prod 1: Anillos / Covergold / Joyeria samaritans
('ANI001','Anillo Plata Liso',
 'Anillo en Plata Ley 950 fabricado en taller.',
 10, 45000.00, 95000.00, CURDATE(), 'anillo_plata.jpg', NULL, NULL, 2, 1, 2),

-- prod 2: Anillos / Covergold / Joyeria samaritans
('ANI002','Anillo Brillo 15 Años',
 'Diseño elegante con circonia para quinceañeras.',
 8, 300000.00, 480000.00, CURDATE(), 'anillo_brillo.jpg', NULL, NULL, 2, 1, 2),

-- prod 3: Anillos / Covergold / Suministros Joyeros
('ANI003','Anillo Compromiso Celestial',
 'Plata 925 con diamante sintético, estilo minimalista.',
 6, 280000.00, 450000.00, CURDATE(), 'anillo_celestial.jpg', NULL, NULL, 2, 1, 3),

-- prod 4: Aretes Largos / Murano / Joyeria samaritans
('ARE001','Aretes Murano',
 'Aretes largos fabricados con murano y plata.',
 15, 12000.00, 35000.00, CURDATE(), 'aretes_murano.jpg', NULL, NULL, 5, 3, 2),

-- prod 5: Earcuff / Nylon / Herrajes diamante
('ARE002','Aretes Cristal Swarovski',
 'Aretes earcuff con cristales austriacos.',
 5, 400000.00, 650000.00, CURDATE(), 'aretes_cristal.jpg', NULL, NULL, 11, 5, 5),

-- prod 6: Collares / Covergold / Joyeria samaritans
('COL001','Collar Dije Covergold',
 'Collar corto con dije de corazón en covergold.',
 12, 15000.00, 35000.00, CURDATE(), 'collar_cover.jpg', NULL, NULL, 2, 7, 2),

-- prod 7: Rosarios / Plata Ley 950 / Joyeria Aurora
('COL002','Rosario en Plata',
 'Rosario de Plata Ley 950 tejido a mano.',
 5, 80000.00, 180000.00, CURDATE(), 'rosario_plata.jpg', NULL, NULL, 1, 6, 1),

-- prod 8: Pulseras / Nylon / Joyeria samaritans
('PUL001','Pulsera Hilo Chino',
 'Pulsera tejida en hilo chino con balín de plata.',
 30, 8000.00, 25000.00, CURDATE(), 'pulsera_hilo.jpg', NULL, NULL, 11, 11, 2),

-- prod 9: Manillas / Miyuki / Suministros Joyeros
('PUL002','Manilla Miyuki',
 'Manilla tejida en Miyuki con herrajes de rodio.',
 10, 25000.00, 60000.00, CURDATE(), 'manilla_miyuki.jpg', NULL, NULL, 7, 12, 3),

-- prod 10: Manillas / Balines de Rodio / Insumos Joyeros del Eje
('MAN001','Manilla Titanio Hombre',
 'Manilla resistente de titanio para uso diario.',
 18, 150000.00, 280000.00, CURDATE(), 'manilla_titanio.jpg', NULL, NULL, 14, 12, 9),

-- prod 11: Dijes / Acero Inoxidable / Brillo Eterno Mayorista
('DIJ001','Dije Corazón Oro',
 'Dije en forma de corazón.',
 14, 180000.00, 350000.00, CURDATE(), 'dije_corazon.jpg', NULL, NULL, 4, 9, 10),

-- prod 12: Denarios / Plata Ley 950 / Joyeria Aurora
('DEN001','Denario Plata',
 'Denario de Plata Ley 950 con balines.',
 11, 35000.00, 75000.00, CURDATE(), 'denario_plata.jpg', NULL, NULL, 1, 10, 1),

-- prod 13: Topitos / Covergold / Perlas del Caribe
('TOP001','Topitos Covergold',
 'Topitos básicos en covergold para uso diario.',
 25, 5000.00, 15000.00, CURDATE(), 'topitos_cover.jpg', NULL, NULL, 2, 2, 4),

-- prod 14: Conjuntos / Rodio / Relojería Continental
('CON001','Conjunto Boda Completo',
 'Set de anillo, aretes y collar para novia.',
 3, 850000.00, 1500000.00, CURDATE(), 'conjunto_boda.jpg', NULL, NULL, 3, 4, 13),

-- prod 15: Tobilleras / Covergold / Relojeria y accesorios delaida
('TOB001','Tobillera Verano Plata',
 'Tobillera delicada de plata con detalles de perlas.',
 16, 130000.00, 260000.00, CURDATE(), 'tobillera_verano.jpg', NULL, NULL, 2, 8, 14);

-- ============================================================
-- PRODUCTO_SUBCATEGORIA
-- Asigna las subcategorías específicas de cada producto.
-- Solo se usan combos que existen en Categoria_Subcategoria.
--
-- Referencia subcategoria_id:
--   1=Matrimonio  2=Compromiso  3=15 Años  4=Grados  5=Religioso
--   6=Aniversario  7=Cumpleaños  8=Día de la Madre  9=Amor y Amistad
--   10=Navidad  11=Personalizados  12=Uso Diario  13=Parejas
--   14=Protección/Amuletos  15=Infantil/Bebés
-- ============================================================
INSERT INTO Producto_Subcategoria (producto_id, subcategoria_id) VALUES
-- prod 1 (Anillo Plata Liso)        → Compromiso, Aniversario, Uso Diario
(1, 2),(1, 6),(1, 12),
-- prod 2 (Anillo Brillo 15 Años)    → 15 Años
(2, 3),
-- prod 3 (Anillo Compromiso Celest.)→ Compromiso, Matrimonio
(3, 2),(3, 1),
-- prod 4 (Aretes Murano)            → Cumpleaños, Día de la Madre
(4, 7),(4, 8),
-- prod 5 (Aretes Cristal Swarovski) → Uso Diario, Amor y Amistad
(5, 12),(5, 9),
-- prod 6 (Collar Dije Covergold)    → Amor y Amistad, Personalizados
(6, 9),(6, 11),
-- prod 7 (Rosario en Plata)         → Religioso, Protección/Amuletos
(7, 5),(7, 14),
-- prod 8 (Pulsera Hilo Chino)       → Uso Diario, Amor y Amistad
(8, 12),(8, 9),
-- prod 9 (Manilla Miyuki)           → Personalizados, Parejas
(9, 11),(9, 13),
-- prod 10 (Manilla Titanio Hombre)  → Uso Diario, Parejas
(10, 12),(10, 13),
-- prod 11 (Dije Corazón Oro)        → Amor y Amistad, Cumpleaños, Personalizados
(11, 9),(11, 7),(11, 11),
-- prod 12 (Denario Plata)           → Religioso, Protección/Amuletos
(12, 5),(12, 14),
-- prod 13 (Topitos Covergold)       → Uso Diario, Personalizados
(13, 12),(13, 11),
-- prod 14 (Conjunto Boda Completo)  → Matrimonio, 15 Años, Grados
(14, 1),(14, 3),(14, 4),
-- prod 15 (Tobillera Verano Plata)  → Infantil/Bebés, Uso Diario
(15, 15),(15, 12);

-- ============================================================
-- COMPRAS
-- ============================================================
INSERT INTO Compra (proveedor_id, fecha_compra, fecha_entrega) VALUES
(1,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(2,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
(3,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
(4,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY)),
(5,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(6,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 DAY)),
(7,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
(8,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
(9,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(10, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY)),
(11, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
(12, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
(13, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(14, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY)),
(15, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY));

INSERT INTO Detalle_Compra (compra_id, producto_id, precio_unitario, cantidad) VALUES
(1,1,150000.00,20),(2,2,300000.00,10),(3,3,280000.00,5),(4,4,90000.00,15),
(5,5,400000.00,8),(6,6,250000.00,12),(7,7,120000.00,30),(8,8,310000.00,4),
(9,9,270000.00,10),(10,10,150000.00,25),(11,11,180000.00,15),(12,12,220000.00,8),
(13,13,80000.00,50),(14,14,850000.00,3),(15,15,130000.00,20);

-- ============================================================
-- INVENTARIO
-- ============================================================
INSERT INTO Inventario_Movimiento (producto_id, usuario_id, tipo, cantidad, referencia) VALUES
(1,1,'entrada',20,'COMPRA-001'),(2,2,'entrada',10,'COMPRA-002'),(3,3,'entrada',5,'COMPRA-003'),
(4,4,'entrada',15,'COMPRA-004'),(5,5,'entrada',8,'COMPRA-005'),(6,6,'entrada',12,'COMPRA-006'),
(7,7,'entrada',30,'COMPRA-007'),(8,8,'entrada',4,'COMPRA-008'),(9,9,'entrada',10,'COMPRA-009'),
(10,10,'entrada',25,'COMPRA-010'),(11,11,'entrada',15,'COMPRA-011'),(12,12,'entrada',8,'COMPRA-012'),
(13,13,'entrada',50,'COMPRA-013'),(14,14,'entrada',3,'COMPRA-014'),(15,15,'entrada',20,'COMPRA-015');

-- ============================================================
-- VENTAS
-- ============================================================
INSERT INTO Venta (usuario_id, cliente_id, fecha_emision) VALUES
(2,1,CURDATE()),(3,2,CURDATE()),(4,3,CURDATE()),(5,4,CURDATE()),(6,5,CURDATE()),
(7,6,CURDATE()),(8,7,CURDATE()),(9,8,CURDATE()),(10,9,CURDATE()),(11,10,CURDATE()),
(12,11,CURDATE()),(13,12,CURDATE()),(14,13,CURDATE()),(15,14,CURDATE()),(2,15,CURDATE());

INSERT INTO Detalle_Venta (venta_id, producto_id, cantidad, precio_unitario) VALUES
(1,1,1,320000.00),(2,2,1,480000.00),(3,3,1,450000.00),(4,4,2,180000.00),
(5,5,1,650000.00),(6,6,1,390000.00),(7,7,1,240000.00),(8,8,1,500000.00),
(9,9,1,420000.00),(10,10,1,280000.00),(11,11,1,350000.00),(12,12,1,400000.00),
(13,13,3,150000.00),(14,14,1,1500000.00),(15,15,1,260000.00);

-- ============================================================
-- PAGOS
-- ============================================================
INSERT INTO Pago_Venta (venta_id, metodo_pago_id, monto, estado) VALUES
(1,1,320000.00,'confirmado'),(2,2,480000.00,'confirmado'),(3,3,450000.00,'confirmado'),
(4,5,360000.00,'confirmado'),(5,6,650000.00,'confirmado'),(6,7,390000.00,'confirmado'),
(7,1,240000.00,'confirmado'),(8,2,500000.00,'confirmado'),(9,8,420000.00,'confirmado'),
(10,1,280000.00,'confirmado'),(11,3,350000.00,'confirmado'),(12,4,400000.00,'confirmado'),
(13,1,450000.00,'confirmado'),(14,2,1500000.00,'pendiente'),(15,5,260000.00,'confirmado');

INSERT INTO Pago_Compra (compra_id, metodo_pago_id, monto, estado) VALUES
(1,4,3000000.00,'confirmado'),(2,7,3000000.00,'confirmado'),(3,1,1400000.00,'confirmado'),
(4,5,1350000.00,'confirmado'),(5,2,3200000.00,'confirmado'),(6,3,3000000.00,'confirmado'),
(7,1,3600000.00,'confirmado'),(8,4,1240000.00,'confirmado'),(9,6,2700000.00,'confirmado'),
(10,7,3750000.00,'confirmado'),(11,8,2700000.00,'confirmado'),(12,1,1760000.00,'confirmado'),
(13,2,4000000.00,'confirmado'),(14,4,2550000.00,'pendiente'),(15,5,2600000.00,'confirmado');

-- ============================================================
-- CRÉDITOS
-- ============================================================
INSERT INTO Credito_Compra (compra_id, monto_total, saldo_pendiente, fecha_inicio, fecha_vencimiento, estado) VALUES
(1,3000000.00,0.00,        CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'pagado'),
(2,3000000.00,1500000.00,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'activo'),
(3,1400000.00,0.00,        CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'pagado'),
(4,1350000.00,1350000.00,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'activo'),
(5,3200000.00,0.00,        CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'pagado'),
(6,3000000.00,3000000.00,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'activo'),
(7,3600000.00,0.00,        CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'pagado'),
(8,1240000.00,620000.00,   CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'activo'),
(9,2700000.00,0.00,        CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'pagado'),
(10,3750000.00,3750000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'activo'),
(11,2700000.00,0.00,       CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'pagado'),
(12,1760000.00,880000.00,  CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'activo'),
(13,4000000.00,0.00,       CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'pagado'),
(14,2550000.00,2550000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'activo'),
(15,2600000.00,0.00,       CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY),'pagado');

INSERT INTO Abono_Credito (credito_id, metodo_pago_id, monto_abono, estado) VALUES
(1,1,3000000.00,'confirmado'),(2,2,1500000.00,'confirmado'),(3,1,1400000.00,'confirmado'),
(4,5,0.00,'pendiente'),(5,3,3200000.00,'confirmado'),(6,7,0.00,'pendiente'),
(7,1,3600000.00,'confirmado'),(8,4,620000.00,'confirmado'),(9,6,2700000.00,'confirmado'),
(10,8,0.00,'pendiente'),(11,2,2700000.00,'confirmado'),(12,3,880000.00,'confirmado'),
(13,1,4000000.00,'confirmado'),(14,5,0.00,'pendiente'),(15,7,2600000.00,'confirmado');

-- ============================================================
-- POSTVENTA
-- ============================================================
INSERT INTO Caso_Postventa (venta_id, tipo, cantidad, motivo, fecha, estado) VALUES
(1,'cambio',1,    'Talla incorrecta del anillo',       CURDATE(),'aprobado'),
(2,'devolucion',1,'Cliente cambió de opinión',          CURDATE(),'aprobado'),
(3,'reclamo',1,   'Pieza llegó con pequeño detalle',    CURDATE(),'en_proceso'),
(4,'cambio',2,    'Prefiere otro diseño de aretes',     CURDATE(),'aprobado'),
(5,'devolucion',1,'Problema con el cierre',             CURDATE(),'en_proceso'),
(6,'reclamo',1,   'Demora en la entrega',               CURDATE(),'cancelado'),
(7,'cambio',1,    'Color no era el esperado',           CURDATE(),'aprobado'),
(8,'devolucion',1,'Error en el pedido',                 CURDATE(),'aprobado'),
(9,'reclamo',1,   'Empaque dañado',                     CURDATE(),'en_proceso'),
(10,'cambio',1,   'Talla de manilla incorrecta',        CURDATE(),'aprobado'),
(11,'devolucion',1,'Cliente insatisfecho',              CURDATE(),'cancelado'),
(12,'reclamo',1,  'Producto diferente al mostrado',     CURDATE(),'en_proceso'),
(13,'cambio',3,   'Cambio de modelo en topitos',        CURDATE(),'aprobado'),
(14,'devolucion',1,'Cancelación de evento',             CURDATE(),'en_proceso'),
(15,'reclamo',1,  'Detalle en el broche',               CURDATE(),'aprobado');

INSERT INTO Historial_Caso_Postventa (caso_id, estado, observacion, usuario_id) VALUES
(1,'en_proceso','Caso recibido para revisión',2),
(1,'aprobado',  'Autorizado cambio de talla',2),
(2,'en_proceso','Solicitud de devolución recibida',3),
(2,'aprobado',  'Devolución aprobada, reembolso procesado',3),
(3,'en_proceso','Reclamo en evaluación técnica',4),
(4,'en_proceso','Cambio de aretes solicitado',5),
(4,'aprobado',  'Nuevos aretes enviados al cliente',5),
(5,'en_proceso','Revisión de garantía en proceso',6),
(6,'en_proceso','Reclamo por demora registrado',7),
(6,'cancelado', 'Cliente aceptó compensación',7),
(7,'en_proceso','Solicitud de cambio de color',8),
(7,'aprobado',  'Cambio aprobado, nuevo producto enviado',8),
(8,'en_proceso','Error de pedido confirmado',9),
(8,'aprobado',  'Producto correcto enviado',9),
(9,'en_proceso','Reclamo por empaque dañado',10);

-- ============================================================
-- ROL_PERMISO
-- ============================================================
-- Superadministrador (1): todos los permisos
INSERT INTO Rol_Permiso (rol_id, permiso_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),
(1,10),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17);

-- Administrador (2): todos excepto gestionar_administradores (17)
INSERT INTO Rol_Permiso (rol_id, permiso_id) VALUES
(2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),(2,9),
(2,10),(2,11),(2,12),(2,13),(2,14),(2,15),(2,16);

-- Vendedor (3): solo permisos operativos básicos
INSERT INTO Rol_Permiso (rol_id, permiso_id) VALUES
(3,4),(3,5),(3,7),(3,8),(3,11),(3,13);

-- ============================================================
-- RECUPERACIÓN DE CONTRASEÑA
-- ============================================================
INSERT INTO Recuperacion_Contrasena
    (usuario_id, codigo_verificacion, fecha_solicitud, fecha_expiracion, estado)
VALUES
    (2, 452810, NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE), 1);

COMMIT;

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================
-- CONSULTAS DE VERIFICACIÓN
-- ============================================================

-- 1. Ver roles asignados
SELECT u.nombre, r.cargo
FROM Usuario u
INNER JOIN Usuario_Rol ur ON u.usuario_id = ur.usuario_id
INNER JOIN Rol r          ON ur.rol_id    = r.rol_id;

-- 2. Ver permisos por rol
SELECT r.cargo, p.nombre
FROM Rol r
INNER JOIN Rol_Permiso rp ON r.rol_id     = rp.rol_id
INNER JOIN Permiso p      ON rp.permiso_id = p.permiso_id
ORDER BY r.rol_id, p.permiso_id;

-- 3. Ver productos con su categoría y subcategorías asignadas
SELECT
    p.codigo,
    p.nombre                        AS producto,
    c.nombre                        AS categoria,
    GROUP_CONCAT(s.nombre ORDER BY s.nombre SEPARATOR ', ') AS subcategorias
FROM Producto p
INNER JOIN Categoria          c  ON p.categoria_id    = c.categoria_id
LEFT  JOIN Producto_Subcategoria ps ON p.producto_id  = ps.producto_id
LEFT  JOIN Subcategoria          s  ON ps.subcategoria_id = s.subcategoria_id
GROUP BY p.producto_id, p.codigo, p.nombre, c.nombre
ORDER BY p.codigo;

-- 4. Ver combinaciones válidas del catálogo
SELECT c.nombre AS categoria, s.nombre AS subcategoria_disponible
FROM Categoria_Subcategoria cs
INNER JOIN Categoria   c ON cs.categoria_id    = c.categoria_id
INNER JOIN Subcategoria s ON cs.subcategoria_id = s.subcategoria_id
ORDER BY c.nombre, s.nombre;

Select * from gestor_abbyac27.Caso_postventa;