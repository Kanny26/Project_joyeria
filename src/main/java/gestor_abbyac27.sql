-- BASE DE DATOS: gestor_abbyac27 
-- 1. Configuración inicial para evitar errores de llaves foráneas durante la carga
SET FOREIGN_KEY_CHECKS=0;

-- 2. Eliminación y creación de BD
DROP DATABASE IF EXISTS gestor_abbyac27;
CREATE DATABASE gestor_abbyac27 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestor_abbyac27;

-- 3. Inicio de Transacción (Para velocidad y seguridad)
START TRANSACTION;

-- ==========================================
-- MÓDULO 1: USUARIOS DEL SISTEMA
-- ==========================================
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

-- ==========================================
-- MÓDULO 2: ROLES Y PERMISOS (CORREGIDO)
-- ==========================================
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
    recuperacion_id      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id           INT UNSIGNED NOT NULL,
    codigo_verificacion  INT UNIQUE NOT NULL,
    fecha_solicitud      DATETIME     NOT NULL,
    fecha_expiracion     DATETIME     NOT NULL,
    estado               BOOLEAN      NOT NULL DEFAULT 1,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- MÓDULO 3: PROVEEDORES
-- ==========================================
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

-- ==========================================
-- MÓDULO 4: CLIENTES
-- ==========================================
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

-- ==========================================
-- MÓDULO 5: CATEGORÍAS Y MATERIALES
-- ==========================================
CREATE TABLE Subcategoria (
    subcategoria_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Categoria (
    categoria_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(255) NOT NULL,
    icono           VARCHAR(255),
    subcategoria_id INT UNSIGNED,
    FOREIGN KEY (subcategoria_id) REFERENCES Subcategoria(subcategoria_id) ON DELETE SET NULL
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

-- ==========================================
-- MÓDULO 6: PRODUCTOS
-- ==========================================
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
    proveedor_id    INT UNSIGNED  NOT NULL,
    FOREIGN KEY (material_id)     REFERENCES Material(material_id),
    FOREIGN KEY (categoria_id)    REFERENCES Categoria(categoria_id),
    FOREIGN KEY (subcategoria_id) REFERENCES Subcategoria(subcategoria_id) ON DELETE SET NULL,
    FOREIGN KEY (proveedor_id)    REFERENCES Proveedor(proveedor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- MÓDULO 7: INVENTARIO
-- ==========================================
CREATE TABLE Inventario_Movimiento (
    movimiento_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    producto_id   INT UNSIGNED NOT NULL,
    usuario_id    INT UNSIGNED NOT NULL,
    tipo          ENUM('entrada','salida','ajuste') NOT NULL,
    cantidad      INT          NOT NULL,
    fecha         DATETIME     NOT NULL DEFAULT NOW(),
    referencia    VARCHAR(255),
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id),
    FOREIGN KEY (usuario_id)  REFERENCES Usuario(usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- MÓDULO 8: COMPRAS
-- ==========================================
CREATE TABLE Compra (
    compra_id     INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    proveedor_id  INT UNSIGNED  NOT NULL,
    usuario_id    INT UNSIGNED  NOT NULL,
    fecha_compra  DATE          NOT NULL,
    fecha_entrega DATE          NOT NULL,
    FOREIGN KEY (proveedor_id) REFERENCES Proveedor(proveedor_id),
    FOREIGN KEY (usuario_id)   REFERENCES Usuario(usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Detalle_Compra (
    detalle_compra_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    compra_id         INT UNSIGNED  NOT NULL,
    producto_id       INT UNSIGNED  NOT NULL,
    precio_unitario   DECIMAL(10,2) NOT NULL,
    cantidad          INT           NOT NULL,
    FOREIGN KEY (compra_id)   REFERENCES Compra(compra_id)   ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- MÓDULO 9: VENTAS
-- ==========================================
CREATE TABLE Venta (
    venta_id      INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    usuario_id    INT UNSIGNED  NOT NULL,
    cliente_id    INT UNSIGNED  NOT NULL,
    fecha_emision DATE          NOT NULL DEFAULT (CURDATE()),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id),
    FOREIGN KEY (cliente_id) REFERENCES Cliente(cliente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Detalle_Venta (
    detalle_venta_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    venta_id         INT UNSIGNED  NOT NULL,
    producto_id      INT UNSIGNED  NOT NULL,
    cantidad         INT           NOT NULL,
    precio_unitario  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (venta_id)    REFERENCES Venta(venta_id)       ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES Producto(producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- MÓDULO 10: MÉTODOS DE PAGO
-- ==========================================
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
    estado         ENUM('pendiente','confirmado','rechazado') NOT NULL DEFAULT 'pendiente',
    FOREIGN KEY (venta_id)       REFERENCES Venta(venta_id)             ON DELETE CASCADE,
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Pago_Compra (
    pago_compra_id INT UNSIGNED  AUTO_INCREMENT PRIMARY KEY,
    compra_id      INT UNSIGNED  NOT NULL,
    metodo_pago_id INT UNSIGNED  NOT NULL,
    monto          DECIMAL(12,2) NOT NULL,
    fecha          DATETIME      NOT NULL DEFAULT NOW(),
    estado         ENUM('pendiente','confirmado','rechazado') NOT NULL DEFAULT 'pendiente',
    FOREIGN KEY (compra_id)      REFERENCES Compra(compra_id)           ON DELETE CASCADE,
    FOREIGN KEY (metodo_pago_id) REFERENCES Metodo_Pago(metodo_pago_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- MÓDULO 11: CRÉDITOS
-- ==========================================
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

-- ==========================================
-- MÓDULO 12: POSTVENTA / POSTCOMPRA
-- ==========================================
CREATE TABLE Caso_Postventa (
    caso_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    venta_id   INT UNSIGNED NOT NULL,
    tipo       ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad   INT  NOT NULL,
    motivo     TEXT,
    fecha      DATE NOT NULL DEFAULT (CURDATE()),
    estado     ENUM('en_proceso','aprobado','cancelado') NOT NULL DEFAULT 'en_proceso',
    FOREIGN KEY (venta_id) REFERENCES Venta(venta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Caso_Postcompra (
    caso_id   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    compra_id INT UNSIGNED NOT NULL,
    tipo      ENUM('cambio','devolucion','reclamo') NOT NULL,
    cantidad  INT  NOT NULL,
    motivo    TEXT,
    fecha     DATE NOT NULL DEFAULT (CURDATE()),
    estado    ENUM('en_proceso','aprobado','cancelado') NOT NULL DEFAULT 'en_proceso',
    FOREIGN KEY (compra_id) REFERENCES Compra(compra_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Historial_Caso_Postventa (
    historial_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    caso_id      INT UNSIGNED NOT NULL,
    estado       ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha        DATETIME NOT NULL DEFAULT NOW(),
    observacion  TEXT,
    usuario_id   INT UNSIGNED NOT NULL,
    FOREIGN KEY (caso_id)    REFERENCES Caso_Postventa(caso_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Historial_Caso_Postcompra (
    historial_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    caso_id      INT UNSIGNED NOT NULL,
    estado       ENUM('en_proceso','aprobado','cancelado') NOT NULL,
    fecha        DATETIME     NOT NULL DEFAULT NOW(),
    observacion  TEXT,
    usuario_id   INT UNSIGNED NOT NULL,
    FOREIGN KEY (caso_id)    REFERENCES Caso_Postcompra(caso_id),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Auditoria_Log (
    log_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT UNSIGNED,
    accion VARCHAR(100) NOT NULL,
    entidad VARCHAR(50),
    entidad_id INT UNSIGNED,
    datos_anteriores JSON,
    datos_nuevos JSON,
    direccion_ip VARCHAR(45),
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(usuario_id) ON DELETE SET NULL,
    INDEX idx_usuario_fecha (usuario_id, fecha_hora),
    INDEX idx_accion (accion),
    INDEX idx_entidad (entidad),
    INDEX idx_fecha (fecha_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Crear la vista
CREATE OR REPLACE VIEW Vista_Auditoria_Admin AS
SELECT
    al.log_id,
    u.nombre AS usuario_nombre,
    al.accion,
    al.entidad,
    al.entidad_id,
    al.datos_anteriores,
    al.datos_nuevos,
    al.direccion_ip,
    al.fecha_hora
FROM Auditoria_Log al
LEFT JOIN Usuario u ON al.usuario_id = u.usuario_id;

-- ==========================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ==========================================
CREATE INDEX idx_producto_busqueda ON Producto(nombre, codigo, categoria_id);
CREATE INDEX idx_inventario_fecha ON Inventario_Movimiento(fecha, producto_id);
CREATE INDEX idx_venta_cliente ON Venta(cliente_id, fecha_emision);
CREATE INDEX idx_compra_proveedor ON Compra(proveedor_id, fecha_compra);

-- ==========================================
-- INSERCIÓN DE DATOS
-- ==========================================

-- Métodos de Pago
INSERT INTO Metodo_Pago (nombre) VALUES
('Efectivo'), ('Tarjeta de Crédito'), ('Tarjeta de Débito'), ('PSE'), ('Nequi'),
('Daviplata'), ('A la Mano'), ('Bancolombia (Transferencia)'), ('Nu (Nubank)'),
('Transfiya'), ('Efecty / La Perla'), ('Bold'), ('Contraentrega (Interrapidísimo)'),
('Contraentrega (Servientrega)'), ('Wompi / Link de Pago');

-- Materiales
INSERT INTO Material (nombre) VALUES
('Plata Ley 950'), ('Covergold'), ('Rodio'), ('Acero Inoxidable'), ('Murano'),
('Mostacilla'), ('Miyuki'), ('Perlas de imitación'), ('Herrajes'), ('Hilo Chino'),
('Nylon'), ('Lana'), ('Balines de Acero'), ('Balines de Rodio'), ('Balines de Covergold');

-- Subcategorías
INSERT INTO Subcategoria (nombre) VALUES
('Matrimonio'), ('Compromiso'), ('15 Años'), ('Grados'), ('Religioso'), ('Aniversario'),
('Cumpleaños'), ('Día de la Madre'), ('Amor y Amistad'), ('Navidad'), ('Personalizados'),
('Uso Diario'), ('Parejas'), ('Protección / Amuletos'), ('Infantil / Bebés');

-- ==========================================
-- PERMISOS (CORREGIDO - AGREGADOS 2 NUEVOS)
-- ==========================================
INSERT INTO Permiso (nombre, descripcion) VALUES
('usuarios_crear', 'Crear nuevos usuarios del sistema'),
('usuarios_editar', 'Modificar datos de usuarios'),
('usuarios_eliminar', 'Eliminar usuarios del sistema'),
('productos_crear', 'Registrar nuevos productos'),
('productos_editar', 'Actualizar precios y stock'),
('productos_eliminar', 'Desactivar productos'),
('ventas_registrar', 'Crear nuevas ventas'),
('ventas_consultar', 'Ver historial de ventas'),
('compras_registrar', 'Registrar compras a proveedores'),
('inventario_ajustar', 'Realizar ajustes de inventario'),
('reportes_ver', 'Acceder a reportes del sistema'),
('creditos_aprobar', 'Aprobar créditos a clientes'),
('postventa_gestionar', 'Gestionar cambios y devoluciones'),
('desempeno_ver', 'Ver métricas de vendedores'),
('configuracion_sistema', 'Acceder a configuración global'),
('gestionar_vendedores', 'Crear y editar usuarios con rol Vendedor'),
('gestionar_administradores', 'Crear y editar usuarios con rol Administrador');

-- rol_id 1 = superadministrador
-- rol_id 2 = administrador
-- rol_id 3 = vendedor
INSERT INTO Rol (cargo) VALUES
('superadministrador'),
('administrador'),
('vendedor');

-- ==========================================
-- USUARIOS
-- ==========================================
INSERT INTO Usuario (nombre, pass, estado, pass_temporal) VALUES
('AdminKS', '$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.', 1, 0),
('Marlen Becerra', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Carlos Ruiz', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Stephany Moreno', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Felipe Mora', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Gabriela Silva', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Héctor Díaz', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Isabella Vega', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Javier Ortiz', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Karen López', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Luis Castro', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('María Pérez', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Nicolás Silva', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Olga Ruiz', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1),
('Pablo Díaz', '$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi', 1, 1);

-- ==========================================
-- USUARIO_ROL (CORREGIDO - JERARQUÍA CORRECTA)
-- ==========================================
-- usuario_id 1 (AdminKS) = rol_id 1 (superadministrador)
-- usuario_id 2 (Marlen) = rol_id 2 (administrador)
-- usuario_id 3-15 = rol_id 3 (vendedor)
INSERT IGNORE INTO Usuario_Rol (usuario_id, rol_id) VALUES
(1, 1),
(2, 2),
(3, 3), (4, 3), (5, 3), (6, 3), (7, 3), (8, 3),
(9, 3), (10, 3), (11, 3), (12, 3), (13, 3), (14, 3), (15, 3);

-- Teléfonos de Usuarios
INSERT INTO Telefono_Usuario (telefono, usuario_id) VALUES
('3027131281', 1), ('3153084721', 2), ('3123456789', 3), ('3027131490', 4), ('3145678901', 5),
('3156789012', 6), ('3167890123', 7), ('3178901234', 8), ('3189012345', 9), ('3190123456', 10),
('3201234567', 11), ('3212345678', 12), ('3223456789', 13), ('3234567890', 14), ('3245678901', 15);

-- Correos de Usuarios
INSERT INTO Correo_Usuario (email, usuario_id) VALUES
('marlenbe211@gmail.com', 1), ('santiago.morenob500@gmail.com', 2), ('carlos.ruiz@abbyac27.com', 3),
('stephanymoreno1826@gmail.com', 4), ('felipe.andrade@abbyac27.com', 5), ('gabriela.silva@abbyac27.com', 6),
('hector.diaz@abbyac27.com', 7), ('isabella.ramirez@abbyac27.com', 8), ('javier.ortiz@abbyac27.com', 9),
('karen.lopez@abbyac27.com', 10), ('luis.castro@abbyac27.com', 11), ('maria.perez@abbyac27.com', 12),
('nicolas.gomez@abbyac27.com', 13), ('olga.ruiz@abbyac27.com', 14), ('pablo.diaz@abbyac27.com', 15);

-- ==========================================
-- PROVEEDORES
-- ==========================================
INSERT INTO Proveedor (nombre, documento, fecha_inicio, minimo_compra, estado) VALUES
('Joyeria Aurora', '900987654-1', '2024-01-15', 5000000.00, 1),
('Joyeria samaritans', '800111222-3', '2024-03-10', 2000000.00, 1),
('Suministros Joyeros S.A.S', '901555444-5', '2023-11-20', 10000000.00, 1),
('Perlas del Caribe Ltda', '890333222-1', '2025-01-05', 1500000.00, 1),
('Herrajes diamante', '900777888-9', '2024-06-12', 500000.00, 1),
('Distribuidora Plata asaleya', '800444555-2', '2024-08-22', 3000000.00, 1),
('Distri acero fatima', '901222333-0', '2023-05-30', 0.00, 1),
('Empaques Elegance', '890666777-4', '2024-02-14', 1000000.00, 1),
('Insumos Joyeros del Eje', '900111000-8', '2024-09-01', 2500000.00, 1),
('Brillo Eterno Mayorista', '800999888-7', '2024-10-10', 6000000.00, 1),
('Cristales Glam', '901333444-6', '2025-02-01', 800000.00, 1),
('Willy Johns', '890888999-5', '2023-08-15', 4000000.00, 1),
('Relojería Continental', '900444333-2', '2024-04-18', 1200000.00, 1),
('Relojeria y accesorios delaida', '800222111-0', '2024-11-30', 2000000.00, 1),
('Importaciones Zafiro', '901666777-3', '2025-01-20', 7500000.00, 1);

INSERT INTO Telefono_Proveedor (telefono, proveedor_id) VALUES
('6014445566', 1), ('3101234567', 1), ('6028889900', 2), ('3159876543', 2), ('3001112233', 3),
('3204445555', 4), ('3116667777', 5), ('3182223344', 6), ('3015556677', 7), ('3129990000', 8),
('3141112222', 9), ('3173334444', 10), ('3007778888', 11), ('3134445555', 12), ('3162221111', 13);

INSERT INTO Correo_Proveedor (email, proveedor_id) VALUES
('contacto@joyeriaaurora.com', 1), ('ventas@joyeriasamaritans.co', 2), ('gerencia@suministrosjoyeros.com.co', 3),
('pedidos@perlasdelcaribe.com', 4), ('info@herrajesdiamante.com', 5), ('comercial@plataasaleya.com', 6),
('ventas@distriacerofatima.co', 7), ('comercial@eleganceestuches.com', 8), ('gerencia@insumosdeleje.co', 9),
('mayorista@brilloeterno.com', 10), ('pedidos@glamcristales.com', 11), ('willyjohns@atencion.com', 12),
('importaciones@continental.com', 13), ('delaida@accesorios.com', 14), ('importaciones@zafiro.com', 15);

-- ==========================================
-- CLIENTES
-- ==========================================
INSERT INTO Cliente (nombre, documento, minimo_compra, estado) VALUES
('María Fernanda Castro', '1234567890', 50000.00, 1), ('Juan Pablo Ramírez', '2345678901', 100000.00, 1),
('Ana Lucía Gómez', '3456789012', 75000.00, 1), ('Carlos Andrés Díaz', '4567890123', 200000.00, 1),
('Laura Sofía Martínez', '5678901234', 80000.00, 1), ('Diego Fernando López', '6789012345', 150000.00, 1),
('Valeria Alejandra Ruiz', '7890123456', 90000.00, 1), ('Sebastián Mora Silva', '8901234567', 120000.00, 1),
('Camila Andrea Pérez', '9012345678', 60000.00, 1), ('Andrés Felipe Torres', '1023456789', 180000.00, 1),
('Daniela Paola Vega', '1123456780', 70000.00, 1), ('Mateo Alejandro Herrera', '1223456781', 130000.00, 1),
('Sofía Isabel Mendoza', '1323456782', 85000.00, 1), ('Santiago David Castro', '1423456783', 160000.00, 1),
('Isabella María Ortiz', '1523456784', 95000.00, 1);

INSERT INTO Telefono_Cliente (telefono, cliente_id) VALUES
('3101112222', 1), ('3112223333', 2), ('3123334444', 3), ('3134445555', 4), ('3145556666', 5),
('3156667777', 6), ('3167778888', 7), ('3178889999', 8), ('3189990000', 9), ('3190001111', 10),
('3201112222', 11), ('3212223333', 12), ('3223334444', 13), ('3234445555', 14), ('3245556666', 15);

INSERT INTO Correo_Cliente (email, cliente_id) VALUES
('maria.castro@email.com', 1), ('juan.ramirez@email.com', 2), ('ana.gomez@email.com', 3),
('carlos.diaz@email.com', 4), ('laura.martinez@email.com', 5), ('diego.lopez@email.com', 6),
('valeria.ruiz@email.com', 7), ('sebastian.mora@email.com', 8), ('camila.perez@email.com', 9),
('andres.torres@email.com', 10), ('daniela.vega@email.com', 11), ('mateo.herrera@email.com', 12),
('sofia.mendoza@email.com', 13), ('santiago.castro@email.com', 14), ('isabella.ortiz@email.com', 15);

-- ==========================================
-- CATEGORÍAS
-- ==========================================
INSERT INTO Categoria (nombre, icono, subcategoria_id) VALUES
('Anillos', 'anillos.png', 1), ('Topitos', 'topitos.png', 2), ('Aretes Largos', 'aretes_largos.png', 3),
('Conjuntos', 'juegos.png', 4), ('Earcuff', 'earcuff.png', 5), ('Rosarios', 'rosario.png', 6),
('Collares', 'collar.png', 7), ('Tobilleras', 'tobillera.png', 8), ('Dijes', 'dijes.png', 9),
('Denarios', 'denarios.png', 10), ('Pulseras', 'pulsera.png', 11), ('Manillas', 'manillas.png', 12),
('Relojes', 'reloj.png', 13), ('Estuches', 'estuches.png', 14), ('Accesorios en lana', 'lanas.png', 15);

-- ==========================================
-- PRODUCTOS
-- ==========================================
INSERT INTO Producto (codigo, nombre, descripcion, stock, precio_unitario, precio_venta, fecha_registro, imagen, imagen_data, imagen_tipo, material_id, categoria_id, subcategoria_id, proveedor_id) VALUES
('ANI001', 'Anillo Plata Liso', 'Anillo en Plata Ley 950 fabricado en taller.', 10, 45000.00, 95000.00, CURDATE(), 'anillo_plata.jpg', NULL, NULL, 2, 1, 2, 2),
('ANI002', 'Anillo Brillo 15 Años', 'Diseño elegante con circonia para quinceañeras.', 8, 300000.00, 480000.00, CURDATE(), 'anillo_brillo.jpg', NULL, NULL, 2, 1, 2, 2),
('ANI003', 'Anillo Compromiso Celestial', 'Plata 925 con diamante sintético, estilo minimalista.', 6, 280000.00, 450000.00, CURDATE(), 'anillo_celestial.jpg', NULL, NULL, 2, 1, 3, 3),
('ARE001', 'Aretes Murano', 'Aretes largos fabricados con murano y plata.', 15, 12000.00, 35000.00, CURDATE(), 'aretes_murano.jpg', NULL, NULL, 5, 3, 7, 2),
('ARE002', 'Aretes Cristal Swarovski', 'Aretes earcuff con cristales austriacos.', 5, 400000.00, 650000.00, CURDATE(), 'aretes_cristal.jpg', NULL, NULL, 11, 5, 5, 5),
('COL001', 'Collar Dije Covergold', 'Collar corto con dije de corazón en covergold.', 12, 15000.00, 35000.00, CURDATE(), 'collar_cover.jpg', NULL, NULL, 2, 7, 7, 2),
('COL002', 'Rosario en Plata', 'Rosario de Plata Ley 950 tejido a mano.', 5, 80000.00, 180000.00, CURDATE(), 'rosario_plata.jpg', NULL, NULL, 1, 6, 5, 1),
('PUL001', 'Pulsera Hilo Chino', 'Pulsera tejida en hilo chino con balín de plata.', 30, 8000.00, 25000.00, CURDATE(), 'pulsera_hilo.jpg', NULL, NULL, 11, 11, 12, 2),
('PUL002', 'Manilla Miyuki', 'Manilla tejida en Miyuki con herrajes de rodio.', 10, 25000.00, 60000.00, CURDATE(), 'manilla_miyuki.jpg', NULL, NULL, 7, 12, 11, 3),
('MAN001', 'Manilla Titanio Hombre', 'Manilla resistente de titanio para uso diario.', 18, 150000.00, 280000.00, CURDATE(), 'manilla_titanio.jpg', NULL, NULL, 14, 12, 15, 9),
('DIJ001', 'Dije Corazón Oro', 'Dije en forma de corazón.', 14, 180000.00, 350000.00, CURDATE(), 'dije_corazon.jpg', NULL, NULL, 4, 9, 13, 10),
('DEN001', 'Denario Plata', 'Denario de Plata Ley 950 con balines.', 11, 35000.00, 75000.00, CURDATE(), 'denario_plata.jpg', NULL, NULL, 1, 10, 5, 1),
('TOP001', 'Topitos Covergold', 'Topitos básicos en covergold para uso diario.', 25, 5000.00, 15000.00, CURDATE(), 'topitos_cover.jpg', NULL, NULL, 2, 2, 12, 4),
('CON001', 'Conjunto Boda Completo', 'Set de anillo, aretes y collar para novia.', 3, 850000.00, 1500000.00, CURDATE(), 'conjunto_boda.jpg', NULL, NULL, 3, 4, 1, 13),
('TOB001', 'Tobillera Verano Plata', 'Tobillera delicada de plata con detalles de perlas.', 16, 130000.00, 260000.00, CURDATE(), 'tobillera_verano.jpg', NULL, NULL, 2, 8, 15, 14);

INSERT INTO Proveedor_Material (proveedor_id, material_id) VALUES
(1, 3), (1, 4), (1, 5), (2, 9), (2, 11), (3, 5), (3, 6), (4, 10), (4, 2),
(5, 14), (5, 15), (6, 2), (7, 3), (7, 4), (8, 13);

-- ==========================================
-- COMPRAS
-- ==========================================
INSERT INTO Compra (proveedor_id, usuario_id, fecha_compra, fecha_entrega) VALUES
(1, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(2, 2, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
(3, 3, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
(4, 4, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY)),
(5, 5, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(6, 6, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 DAY)),
(7, 7, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
(8, 8, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
(9, 9, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(10, 10, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY)),
(11, 11, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
(12, 12, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
(13, 13, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(14, 14, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY)),
(15, 15, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY));

INSERT INTO Detalle_Compra (compra_id, producto_id, precio_unitario, cantidad) VALUES
(1, 1, 150000.00, 20), (2, 2, 300000.00, 10), (3, 3, 280000.00, 5), (4, 4, 90000.00, 15),
(5, 5, 400000.00, 8), (6, 6, 250000.00, 12), (7, 7, 120000.00, 30), (8, 8, 310000.00, 4),
(9, 9, 270000.00, 10), (10, 10, 150000.00, 25), (11, 11, 180000.00, 15), (12, 12, 220000.00, 8),
(13, 13, 80000.00, 50), (14, 14, 850000.00, 3), (15, 15, 130000.00, 20);

INSERT INTO Inventario_Movimiento (producto_id, usuario_id, tipo, cantidad, referencia) VALUES
(1, 1, 'entrada', 20, 'COMPRA-001'), (2, 2, 'entrada', 10, 'COMPRA-002'), (3, 3, 'entrada', 5, 'COMPRA-003'),
(4, 4, 'entrada', 15, 'COMPRA-004'), (5, 5, 'entrada', 8, 'COMPRA-005'), (6, 6, 'entrada', 12, 'COMPRA-006'),
(7, 7, 'entrada', 30, 'COMPRA-007'), (8, 8, 'entrada', 4, 'COMPRA-008'), (9, 9, 'entrada', 10, 'COMPRA-009'),
(10, 10, 'entrada', 25, 'COMPRA-010'), (11, 11, 'entrada', 15, 'COMPRA-011'), (12, 12, 'entrada', 8, 'COMPRA-012'),
(13, 13, 'entrada', 50, 'COMPRA-013'), (14, 14, 'entrada', 3, 'COMPRA-014'), (15, 15, 'entrada', 20, 'COMPRA-015');

-- ==========================================
-- VENTAS
-- ==========================================
INSERT INTO Venta (usuario_id, cliente_id, fecha_emision) VALUES
(2, 1, CURDATE()), (3, 2, CURDATE()), (4, 3, CURDATE()), (5, 4, CURDATE()), (6, 5, CURDATE()),
(7, 6, CURDATE()), (8, 7, CURDATE()), (9, 8, CURDATE()), (10, 9, CURDATE()), (11, 10, CURDATE()),
(12, 11, CURDATE()), (13, 12, CURDATE()), (14, 13, CURDATE()), (15, 14, CURDATE()), (2, 15, CURDATE());

INSERT INTO Detalle_Venta (venta_id, producto_id, cantidad, precio_unitario) VALUES
(1, 1, 1, 320000.00), (2, 2, 1, 480000.00), (3, 3, 1, 450000.00), (4, 4, 2, 180000.00),
(5, 5, 1, 650000.00), (6, 6, 1, 390000.00), (7, 7, 1, 240000.00), (8, 8, 1, 500000.00),
(9, 9, 1, 420000.00), (10, 10, 1, 280000.00), (11, 11, 1, 350000.00), (12, 12, 1, 400000.00),
(13, 13, 3, 150000.00), (14, 14, 1, 1500000.00), (15, 15, 1, 260000.00);

INSERT INTO Pago_Venta (venta_id, metodo_pago_id, monto, estado) VALUES
(1, 1, 320000.00, 'confirmado'), (2, 2, 480000.00, 'confirmado'), (3, 3, 450000.00, 'confirmado'),
(4, 5, 360000.00, 'confirmado'), (5, 6, 650000.00, 'confirmado'), (6, 7, 390000.00, 'confirmado'),
(7, 1, 240000.00, 'confirmado'), (8, 2, 500000.00, 'confirmado'), (9, 8, 420000.00, 'confirmado'),
(10, 1, 280000.00, 'confirmado'), (11, 3, 350000.00, 'confirmado'), (12, 4, 400000.00, 'confirmado'),
(13, 1, 450000.00, 'confirmado'), (14, 2, 1500000.00, 'pendiente'), (15, 5, 260000.00, 'confirmado');

INSERT INTO Pago_Compra (compra_id, metodo_pago_id, monto, estado) VALUES
(1, 4, 3000000.00, 'confirmado'), (2, 7, 3000000.00, 'confirmado'), (3, 1, 1400000.00, 'confirmado'),
(4, 5, 1350000.00, 'confirmado'), (5, 2, 3200000.00, 'confirmado'), (6, 3, 3000000.00, 'confirmado'),
(7, 1, 3600000.00, 'confirmado'), (8, 4, 1240000.00, 'confirmado'), (9, 6, 2700000.00, 'confirmado'),
(10, 7, 3750000.00, 'confirmado'), (11, 8, 2700000.00, 'confirmado'), (12, 1, 1760000.00, 'confirmado'),
(13, 2, 4000000.00, 'confirmado'), (14, 4, 2550000.00, 'pendiente'), (15, 5, 2600000.00, 'confirmado');

-- ==========================================
-- CRÉDITOS
-- ==========================================
INSERT INTO Credito_Compra (compra_id, monto_total, saldo_pendiente, fecha_inicio, fecha_vencimiento, estado) VALUES
(1, 3000000.00, 0.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'pagado'),
(2, 3000000.00, 1500000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'activo'),
(3, 1400000.00, 0.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'pagado'),
(4, 1350000.00, 1350000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'activo'),
(5, 3200000.00, 0.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'pagado'),
(6, 3000000.00, 3000000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'activo'),
(7, 3600000.00, 0.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'pagado'),
(8, 1240000.00, 620000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'activo'),
(9, 2700000.00, 0.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'pagado'),
(10, 3750000.00, 3750000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'activo'),
(11, 2700000.00, 0.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'pagado'),
(12, 1760000.00, 880000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'activo'),
(13, 4000000.00, 0.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'pagado'),
(14, 2550000.00, 2550000.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'activo'),
(15, 2600000.00, 0.00, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'pagado');

INSERT INTO Abono_Credito (credito_id, metodo_pago_id, monto_abono, estado) VALUES
(1, 1, 3000000.00, 'confirmado'), (2, 2, 1500000.00, 'confirmado'), (3, 1, 1400000.00, 'confirmado'),
(4, 5, 0.00, 'pendiente'), (5, 3, 3200000.00, 'confirmado'), (6, 7, 0.00, 'pendiente'),
(7, 1, 3600000.00, 'confirmado'), (8, 4, 620000.00, 'confirmado'), (9, 6, 2700000.00, 'confirmado'),
(10, 8, 0.00, 'pendiente'), (11, 2, 2700000.00, 'confirmado'), (12, 3, 880000.00, 'confirmado'),
(13, 1, 4000000.00, 'confirmado'), (14, 5, 0.00, 'pendiente'), (15, 7, 2600000.00, 'confirmado');

-- ==========================================
-- POSTVENTA
-- ==========================================
INSERT INTO Caso_Postventa (venta_id, tipo, cantidad, motivo, fecha, estado) VALUES
(1, 'cambio', 1, 'Talla incorrecta del anillo', CURDATE(), 'aprobado'),
(2, 'devolucion', 1, 'Cliente cambió de opinión', CURDATE(), 'aprobado'),
(3, 'reclamo', 1, 'Pieza llegó con pequeño detalle', CURDATE(), 'en_proceso'),
(4, 'cambio', 2, 'Prefiere otro diseño de aretes', CURDATE(), 'aprobado'),
(5, 'devolucion', 1, 'Problema con el cierre', CURDATE(), 'en_proceso'),
(6, 'reclamo', 1, 'Demora en la entrega', CURDATE(), 'cancelado'),
(7, 'cambio', 1, 'Color no era el esperado', CURDATE(), 'aprobado'),
(8, 'devolucion', 1, 'Error en el pedido', CURDATE(), 'aprobado'),
(9, 'reclamo', 1, 'Empaque dañado', CURDATE(), 'en_proceso'),
(10, 'cambio', 1, 'Talla de manilla incorrecta', CURDATE(), 'aprobado'),
(11, 'devolucion', 1, 'Cliente insatisfecho', CURDATE(), 'cancelado'),
(12, 'reclamo', 1, 'Producto diferente al mostrado', CURDATE(), 'en_proceso'),
(13, 'cambio', 3, 'Cambio de modelo en topitos', CURDATE(), 'aprobado'),
(14, 'devolucion', 1, 'Cancelación de evento', CURDATE(), 'en_proceso'),
(15, 'reclamo', 1, 'Detalle en el broche', CURDATE(), 'aprobado');

INSERT INTO Caso_Postcompra (compra_id, tipo, cantidad, motivo, fecha, estado) VALUES
(1, 'reclamo', 5, 'Entrega parcial del pedido', CURDATE(), 'aprobado'),
(2, 'cambio', 3, 'Producto diferente al solicitado', CURDATE(), 'en_proceso'),
(3, 'devolucion', 2, 'Material no cumple especificaciones', CURDATE(), 'aprobado'),
(4, 'reclamo', 1, 'Demora en la entrega', CURDATE(), 'cancelado'),
(5, 'cambio', 4, 'Cambio de referencia de producto', CURDATE(), 'aprobado'),
(6, 'devolucion', 6, 'Defectos de fabricación', CURDATE(), 'en_proceso'),
(7, 'reclamo', 10, 'Facturación incorrecta', CURDATE(), 'aprobado'),
(8, 'cambio', 2, 'Empaque dañado en transporte', CURDATE(), 'aprobado'),
(9, 'devolucion', 3, 'Producto fuera de stock confirmado', CURDATE(), 'cancelado'),
(10, 'reclamo', 1, 'Error en precios acordados', CURDATE(), 'en_proceso'),
(11, 'cambio', 5, 'Solicitud de variante de color', CURDATE(), 'aprobado'),
(12, 'devolucion', 4, 'Calidad no esperada', CURDATE(), 'en_proceso'),
(13, 'reclamo', 15, 'Falta de documentos de importación', CURDATE(), 'aprobado'),
(14, 'cambio', 1, 'Cambio de fecha de entrega', CURDATE(), 'aprobado'),
(15, 'devolucion', 8, 'Producto no corresponde a muestra', CURDATE(), 'en_proceso');

INSERT INTO Historial_Caso_Postventa (caso_id, estado, observacion, usuario_id) VALUES
(1, 'en_proceso', 'Caso recibido para revisión', 2), (1, 'aprobado', 'Autorizado cambio de talla', 2),
(2, 'en_proceso', 'Solicitud de devolución recibida', 3), (2, 'aprobado', 'Devolución aprobada, reembolso procesado', 3),
(3, 'en_proceso', 'Reclamo en evaluación técnica', 4), (4, 'en_proceso', 'Cambio de aretes solicitado', 5),
(4, 'aprobado', 'Nuevos aretes enviados al cliente', 5), (5, 'en_proceso', 'Revisión de garantía en proceso', 6),
(6, 'en_proceso', 'Reclamo por demora registrado', 7), (6, 'cancelado', 'Cliente aceptó compensación', 7),
(7, 'en_proceso', 'Solicitud de cambio de color', 8), (7, 'aprobado', 'Cambio aprobado, nuevo producto enviado', 8),
(8, 'en_proceso', 'Error de pedido confirmado', 9), (8, 'aprobado', 'Producto correcto enviado', 9),
(9, 'en_proceso', 'Reclamo por empaque dañado', 10);

INSERT INTO Historial_Caso_Postcompra (caso_id, estado, observacion, usuario_id) VALUES
(1, 'en_proceso', 'Reclamo por entrega parcial', 1), (1, 'aprobado', 'Proveedor envió resto del pedido', 1),
(2, 'en_proceso', 'Solicitud de cambio de producto', 2), (3, 'en_proceso', 'Devolución por calidad', 3),
(3, 'aprobado', 'Devolución aceptada, crédito aplicado', 3), (4, 'en_proceso', 'Reclamo por demora', 4),
(4, 'cancelado', 'Proveedor compensó con descuento', 4), (5, 'en_proceso', 'Cambio de referencia solicitado', 5),
(5, 'aprobado', 'Cambio procesado con proveedor', 5), (6, 'en_proceso', 'Devolución por defectos', 6),
(7, 'en_proceso', 'Reclamo administrativo', 7), (7, 'aprobado', 'Factura corregida y enviada', 7),
(8, 'en_proceso', 'Daño en transporte reportado', 8), (8, 'aprobado', 'Proveedor reemplazó productos', 8),
(9, 'en_proceso', 'Cancelación por falta de stock', 9);

-- ==========================================
-- ROL_PERMISO (CORREGIDO - JERARQUÍA DE PERMISOS)
-- ==========================================
-- SUPERADMINISTRADOR (rol_id 1): TODOS los permisos (1 al 17)
INSERT INTO Rol_Permiso (rol_id, permiso_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(1, 16), (1, 17);

-- ADMINISTRADOR (rol_id 2): Todos EXCEPTO 'gestionar_administradores' (permiso 17)
INSERT INTO Rol_Permiso (rol_id, permiso_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8),
(2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15),
(2, 16);

-- VENDEDOR (rol_id 3): Solo permisos operativos básicos
INSERT INTO Rol_Permiso (rol_id, permiso_id) VALUES
(3, 4), (3, 5), (3, 7), (3, 8), (3, 11), (3, 13);

-- ==========================================
-- RECUPERACIÓN DE CONTRASEÑA
-- ==========================================
INSERT INTO Recuperacion_Contrasena (usuario_id, codigo_verificacion, fecha_solicitud, fecha_expiracion, estado)
VALUES (2, '452810', NOW(), DATE_ADD(NOW(), INTERVAL 15 MINUTE), 1);

-- ==========================================
-- 4. Confirmar Transacción
-- ==========================================
COMMIT;

-- ==========================================
-- 5. Reactivar validación de llaves foráneas
-- ==========================================
SET FOREIGN_KEY_CHECKS=1;

-- ==========================================
-- CONSULTAS DE VERIFICACIÓN
-- ==========================================
-- Ver roles asignados
SELECT u.nombre, r.cargo 
FROM Usuario u 
INNER JOIN Usuario_Rol ur ON u.usuario_id = ur.usuario_id 
INNER JOIN Rol r ON ur.rol_id = r.rol_id;

-- Ver permisos por rol
SELECT r.cargo, p.nombre 
FROM Rol r 
INNER JOIN Rol_Permiso rp ON r.rol_id = rp.rol_id 
INNER JOIN Permiso p ON rp.permiso_id = p.permiso_id 
ORDER BY r.rol_id, p.permiso_id;


select * from Historial_Caso_Postventa ;