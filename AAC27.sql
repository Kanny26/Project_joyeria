-- -----------------------------------
-- BASE DE DATOS PRINCIPAL
-- -----------------------------------
CREATE DATABASE gestor_abbyac27;
USE gestor_abbyac27;

-- -----------------------------------
-- USUARIOS
-- -----------------------------------
CREATE TABLE Usuario (
    id_usuario INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL
);

CREATE TABLE Roles (
    id_rol INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE Permisos (
    id_permiso INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso VARCHAR(255) NOT NULL,
    descripcion TEXT
);

CREATE TABLE Usuario_roles (
    id_usuario INT UNSIGNED NOT NULL,
    id_rol INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_rol) REFERENCES Roles(id_rol)
);

CREATE TABLE rol_permiso (
    id_rol INT UNSIGNED NOT NULL,
    id_permiso INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    FOREIGN KEY (id_rol) REFERENCES Roles(id_rol),
    FOREIGN KEY (id_permiso) REFERENCES Permisos(id_permiso)
);

CREATE TABLE Recuperacion_contrasena (
    id_recuperacion INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT UNSIGNED NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    fecha_solicitud DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);

-- -----------------------------------
-- PROVEEDORES 
-- -----------------------------------
CREATE TABLE Proveedor (
    id_proveedor INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    fecha_inicio DATE NOT NULL,
    minimo_compra DECIMAL(10,2) NOT NULL
);


CREATE TABLE Telefono_proveedor (
    id_telefono INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    telefono VARCHAR(255) NOT NULL,
    id_proveedor INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor) 
);


CREATE TABLE Correo_proveedor (
    id_correo INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    id_proveedor INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);


-- -----------------------------------
-- PROVEEDOR - PRODUCTOS
-- -----------------------------------
CREATE TABLE Categoria (
    id_categoria INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(255) NOT NULL
);


CREATE TABLE Subcategoria (
    id_subcategoria INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_subcategoria VARCHAR(255) NOT NULL,
    id_categoria INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_categoria) REFERENCES Categoria(id_categoria)
);

CREATE TABLE Material (
    id_material INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_material VARCHAR(255) NOT NULL
);

CREATE TABLE Producto (
    id_producto INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    stock INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    fecha_registro DATE NOT NULL,
    id_material INT UNSIGNED NOT NULL,
    id_subcategoria INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_material) REFERENCES Material(id_material),
    FOREIGN KEY (id_subcategoria) REFERENCES Subcategoria(id_subcategoria)
);

CREATE TABLE Proveedor_material (
    id_proveedor INT UNSIGNED NOT NULL,
    id_material INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_proveedor, id_material),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor),
    FOREIGN KEY (id_material) REFERENCES Material(id_material)
);

CREATE TABLE Proveedor_producto (
    id_proveedor INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED NOT NULL,
    precio_compra DECIMAL(10,2) NOT NULL,
    estado_producto BOOLEAN NOT NULL DEFAULT 1,
    PRIMARY KEY (id_proveedor, id_producto),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

CREATE TABLE Compra_proveedores (
    id_compra INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT UNSIGNED NOT NULL,
    fecha_compra DATE NOT NULL,
    fecha_entrega DATE NOT NULL,
    total_compra DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

CREATE TABLE Detalle_compra (
    id_detalle_compra INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_compra INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    fecha_emision DATE NOT NULL,
    total_factura DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_compra) REFERENCES Compra_proveedores(id_compra),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

CREATE TABLE Resumen_proveedores (
    id_resumen INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT UNSIGNED NOT NULL, 
    total_compras DECIMAL(10,2) NOT NULL,
    promedio_compra DECIMAL(10,2) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);
-- -----------------------------------
-- CLIENTES - VENTAS
-- -----------------------------------
CREATE TABLE Clientes (
    id_cliente INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    documento VARCHAR(50) NOT NULL UNIQUE,
    telefono VARCHAR(50),
    email VARCHAR(255),
    fecha_registro DATE NOT NULL
);

CREATE TABLE Ventas (
    id_venta INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT UNSIGNED NOT NULL,
    id_cliente INT UNSIGNED NOT NULL,
    fecha_venta DATE NOT NULL,
    total_venta DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente)
);

CREATE TABLE Detalle_venta (
    id_detalle_venta INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_venta INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES Ventas(id_venta),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

CREATE TABLE Facturas_venta_cliente (
    id_factura INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_venta INT UNSIGNED NOT NULL,
    id_usuario INT UNSIGNED NOT NULL, 
    id_cliente INT UNSIGNED NOT NULL, 
    fecha_emision DATE NOT NULL,
    total_factura DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente),
    FOREIGN KEY (id_venta) REFERENCES Ventas(id_venta)
);

CREATE TABLE casos_postventa (
    id_movimiento INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('cambio_cliente','devolucion_cliente','reclamo_cliente',
              'devolucion_proveedor','reclamo_proveedor') NOT NULL,
    id_factura INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED,
    id_cliente INT UNSIGNED,
    id_proveedor INT UNSIGNED,
    cantidad INT NOT NULL,
    motivo TEXT,
    fecha DATE NOT NULL,
    estado ENUM('en proceso', 'aprobado', 'cancelado'),
    FOREIGN KEY (id_factura) REFERENCES Facturas_venta_cliente(id_factura),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto),
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

-- -----------------------------------
-- PAGOS
-- ------------------------------------

CREATE TABLE Metodos_pago (
    id_metodo INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM ('efectivo', 'transferencia'),
    nombre_metodo VARCHAR(255) NOT NULL,
    descripcion TEXT
);

CREATE TABLE Pago (
    id_pago INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_venta INT UNSIGNED NOT NULL,
    id_metodo INT UNSIGNED NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_pago DATE NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES Ventas(id_venta),
    FOREIGN KEY (id_metodo) REFERENCES Metodos_pago(id_metodo)
);

CREATE TABLE Seguimiento_creditos (
    id_credito INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('cliente','proveedor') NOT NULL,
    estado ENUM('pagado','pendiente') NOT NULL,
    id_cliente INT UNSIGNED,
    id_proveedor INT UNSIGNED,
    monto DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

-- -----------------------------------
-- HISTORIAL DESEMPEÑO
-- -----------------------------------
CREATE TABLE Historial_desempeno (
    id_historial INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('usuario','proveedor') NOT NULL,
    id_usuario INT UNSIGNED,
    id_proveedor INT UNSIGNED,
    evaluacion TEXT NOT NULL,
    puntaje DECIMAL(5,2) NOT NULL,
    fecha_evaluacion DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

-- -----------------------------------
-- ESTADISTICAS
-- -----------------------------------

CREATE TABLE Estadisticas (
    id_estadistica INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('venta','producto','cliente','proveedor') NOT NULL,
    id_entidad INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha DATE NOT NULL
);

-- -----------------------------------
-- RUTA DE IMÁGENES
-- -----------------------------------
CREATE TABLE Ruta_img (
    id_img INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_producto INT UNSIGNED NOT NULL,
    ruta VARCHAR(255) NOT NULL,
    fecha_subida DATETIME NOT NULL,
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

