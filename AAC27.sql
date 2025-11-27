-- ============================================================
-- BASE DE DATOS PRINCIPAL
-- ============================================================
CREATE DATABASE gestor_abbyac27;
USE gestor_abbyac27;

--Un usuario tiene un rol (administrador, vendedor), y este tiene unos permisos, entonces la tabla rol_permiso, relaciona el usuario con su rol y sus permisos y el usuario puede generar un token para recuperar su contraseña.

-- ============================================================
-- TABLA: Usuario
-- Usuarios del sistema (administrador / vendedor)
-- ============================================================
CREATE TABLE Usuario (
    id_usuario INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    contraseña VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL
);

-- ============================================================
-- TABLA: Roles
-- Roles del sistema (Administrador / Vendedor)
-- ============================================================
CREATE TABLE Roles (
    id_rol INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(255) NOT NULL UNIQUE
);

-- ============================================================
-- TABLA: Permisos
-- Permisos asignados a roles
-- ============================================================
CREATE TABLE Permisos (
    id_permiso INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso VARCHAR(255) NOT NULL,
    descripcion TEXT
);

-- ============================================================
-- TABLA INTERMEDIA: Usuario_roles
-- Asignación de usuarios a roles
-- ============================================================
CREATE TABLE Usuario_roles (
    id_usuario INT UNSIGNED NOT NULL,
    id_rol INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_rol) REFERENCES Roles(id_rol)
);

-- ============================================================
-- TABLA INTERMEDIA: rol_permiso
-- Asignación de permisos a roles
-- ============================================================
CREATE TABLE rol_permiso (
    id_rol INT UNSIGNED NOT NULL,
    id_permiso INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    FOREIGN KEY (id_rol) REFERENCES Roles(id_rol),
    FOREIGN KEY (id_permiso) REFERENCES Permisos(id_permiso)
);

-- ============================================================
-- TABLA: Recuperacion_contraseña
-- Tokens generados para recuperación de contraseña
-- ============================================================
CREATE TABLE Recuperacion_contraseña (
    id_recuperacion INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT UNSIGNED NOT NULL,
    token VARCHAR(255) NOT NULL,
    fecha_solicitud DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    estado BOOLEAN NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);


--un proveedor tiene sus datos, tiene un telefono y un correo, ofrece una categoria de los productos que se venden (ejemplo: anillos) y esta puede tener subcategorias (ejemplo: anillos de compromiso), el provee bien sea el material para fabricar (mostacillas, hilos) un producto o un producto con cierto material (anillos en acero).COMMENT

--se realiza compras al proveedor y este emite un detalle de compra que tiene una factura, las devoluciones, cambios o reclamos se emiten por medio de la factura generada en la compra

-- ============================================================
-- TABLA: Proveedor
-- Datos principales del proveedor
-- ============================================================
CREATE TABLE Proveedor (
    id_proveedor INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT 1,
    fecha_inicio DATE NOT NULL,
    minimo_compra DECIMAL(10,2) NOT NULL
);

-- ============================================================
-- TABLAS EXCLUSIVAS PARA PROVEEDORES: Teléfono y Correo
-- ============================================================
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

-- ============================================================
-- TABLAS: Categoría y Subcategoría
-- ============================================================
CREATE TABLE Categoria (
    id_categoria INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(255) NOT NULL
);

CREATE TABLE Subcategorias (
    id_subcategoria INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_subcategoria VARCHAR(255) NOT NULL,
    id_categoria INT UNSIGNED NOT NULL,
    FOREIGN KEY (id_categoria) REFERENCES Categoria(id_categoria)
);

-- ============================================================
-- TABLA: Material
-- ============================================================
CREATE TABLE Material (
    id_material INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_material VARCHAR(255) NOT NULL
);

-- ============================================================
-- TABLA: Producto
-- ============================================================
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
    FOREIGN KEY (id_subcategoria) REFERENCES Subcategorias(id_subcategoria)
);

-- ============================================================
-- TABLA RELACIONAL: Proveedor_material
-- Qué materiales provee cada proveedor
-- ============================================================
CREATE TABLE Proveedor_material (
    id_proveedor INT UNSIGNED NOT NULL,
    id_material INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_proveedor, id_material),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor),
    FOREIGN KEY (id_material) REFERENCES Material(id_material)
);

-- ============================================================
-- TABLA RELACIONAL: Proveedor_producto
-- Productos comprados a cada proveedor
-- ============================================================
CREATE TABLE Proveedor_producto (
    id_proveedor INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED NOT NULL,
    precio_compra DECIMAL(10,2) NOT NULL,
    estado_producto BOOLEAN NOT NULL,
    PRIMARY KEY (id_proveedor, id_producto),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- COMPRAS A PROVEEDORES
-- ============================================================
CREATE TABLE Compra_proveedores (
    id_compra INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT UNSIGNED NOT NULL,
    fecha_compra DATE NOT NULL,
    fecha_entrega DATE NOT NULL,
    total_compra DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

-- ============================================================
-- DETALLE DE COMPRA
-- ============================================================
CREATE TABLE Detalle_compra (
    id_detalle_compra INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_compra INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_compra) REFERENCES Compra_proveedores(id_compra),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- FACTURAS DE COMPRA
-- ============================================================
CREATE TABLE Facturas_compra_a_proveedores (
    id_factura INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_compra INT UNSIGNED NOT NULL,
    numero_factura VARCHAR(50) NOT NULL,
    fecha_emision DATE NOT NULL,
    FOREIGN KEY (id_compra) REFERENCES Compra_proveedores(id_compra)
);


--datos del cliente a ese cliente se le asignan a un vededor, las ventas realizadas tienen un detalle de la venta realizada y se emite una factura, la cual permite tramitar cambios, reclamos y devoluciones que solicite el cliente, el cliente puede pagar de contado en efectivo, transferencia o pagar a credito y si es credito se realiza el seguimiento de esa compra a credito.
-- ============================================================
-- CLIENTES
-- ============================================================
CREATE TABLE Clientes (
    id_cliente INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    documento VARCHAR(50) NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(50),
    email VARCHAR(255),
    fecha_registro DATE NOT NULL
);

-- ============================================================
-- VENTAS
-- ============================================================
CREATE TABLE Ventas (
    id_venta INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT UNSIGNED NOT NULL,
    id_cliente INT UNSIGNED NOT NULL,
    fecha_venta DATE NOT NULL,
    total_venta DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente)
);

-- ============================================================
-- DETALLE DE VENTA
-- ============================================================
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

-- ============================================================
-- FACTURAS DE VENTA
-- ============================================================
CREATE TABLE Facturas_venta_cliente (
    id_factura INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_venta INT UNSIGNED NOT NULL,
    numero_factura VARCHAR(50) NOT NULL,
    fecha_emision DATE NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES Ventas(id_venta)
);

-- ============================================================
-- CAMBIOS (Producto diferente al solicitado)
-- Basados en FACTURA y PRODUCTO
-- ============================================================
CREATE TABLE Cambios (
    id_cambio INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_factura INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    motivo TEXT NOT NULL,
    fecha_cambio DATE NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES Facturas_venta_cliente(id_factura),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- RECLAMOS CLIENTES (Producto con defecto)
-- Basado en FACTURA y CLIENTE
-- ============================================================
CREATE TABLE Reclamos_clientes (
    id_reclamo INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_factura INT UNSIGNED NOT NULL,
    id_cliente INT UNSIGNED NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_reclamo DATE NOT NULL,
    estado BOOLEAN NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES Facturas_venta_cliente(id_factura),
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente)
);

-- ============================================================
-- RECLAMOS A PROVEEDORES (Basado en factura de compra)
-- ============================================================
CREATE TABLE Reclamos_a_proveedores (
    id_reclamo INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_factura INT UNSIGNED NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_reclamo DATE NOT NULL,
    estado BOOLEAN NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES Facturas_compra_a_proveedores(id_factura)
);

-- ============================================================
-- DEVOLUCIONES CLIENTES
-- ============================================================
CREATE TABLE Devoluciones_clientes (
    id_devolucion INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_factura INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    motivo TEXT NOT NULL,
    fecha_devolucion DATE NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES Facturas_venta_cliente(id_factura),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- DEVOLUCIONES A PROVEEDORES
-- ============================================================
CREATE TABLE Devoluciones_a_proveedores (
    id_devolucion INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_factura INT UNSIGNED NOT NULL,
    id_producto INT UNSIGNED NOT NULL,
    cantidad INT NOT NULL,
    motivo TEXT NOT NULL,
    fecha_devolucion DATE NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES Facturas_compra_a_proveedores(id_factura),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- MÉTODOS DE PAGO
-- ============================================================
CREATE TABLE Metodos_pago (
    id_metodo INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre_metodo VARCHAR(255) NOT NULL,
    descripcion TEXT
);

-- ============================================================
-- PAGOS
-- ============================================================
CREATE TABLE Pago (
    id_pago INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_venta INT UNSIGNED NOT NULL,
    id_metodo INT UNSIGNED NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_pago DATE NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES Ventas(id_venta),
    FOREIGN KEY (id_metodo) REFERENCES Metodos_pago(id_metodo)
);

-- ============================================================
-- SEGUIMIENTO CRÉDITOS (Clientes / Proveedores)
-- ============================================================
CREATE TABLE Seguimiento_creditos (
    id_credito INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('cliente','proveedor') NOT NULL,
    id_cliente INT UNSIGNED,
    id_proveedor INT UNSIGNED,
    monto DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    estado BOOLEAN NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente),
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

-- ============================================================
-- SEGUIMIENTOS PENDIENTES (RELACIONADO A CLIENTES)
-- ============================================================
CREATE TABLE Seguimientos_pendientes (
    id_seguimiento INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT UNSIGNED NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_registro DATE NOT NULL,
    estado BOOLEAN NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente)
);

-- ============================================================
-- HISTORIAL DESEMPEÑO
-- Para usuario (vendedor) y proveedor
-- ============================================================
CREATE TABLE Historial_desempeño (
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

-- ============================================================
-- HISTORIAL VENTA (Acciones)
-- ============================================================
CREATE TABLE Historial_venta (
    id_historial INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_factura INT UNSIGNED NOT NULL,
    accion VARCHAR(255) NOT NULL,
    fecha_accion DATETIME NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES Facturas_venta_cliente(id_factura)
);

-- ============================================================
-- RESUMEN PROVEEDORES (Estadística)
-- ============================================================
CREATE TABLE Resumen_proveedores (
    id_resumen INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT UNSIGNED NOT NULL,
    total_compras DECIMAL(10,2) NOT NULL,
    promedio_compra DECIMAL(10,2) NOT NULL,
    estado BOOLEAN NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

-- ============================================================
-- FILTROS PROVEEDORES (Búsquedas realizadas)
-- ============================================================
CREATE TABLE Filtros_proveedores (
    id_filtro INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT UNSIGNED NOT NULL,
    criterio VARCHAR(255) NOT NULL,
    valor VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

-- ============================================================
-- ESTADÍSTICAS VENTAS
-- ============================================================
CREATE TABLE Est_ventas (
    id_est_venta INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_factura INT UNSIGNED NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    fecha DATE NOT NULL,
    FOREIGN KEY (id_factura) REFERENCES Facturas_venta_cliente(id_factura)
);

-- ============================================================
-- ESTADÍSTICAS PRODUCTOS
-- ============================================================
CREATE TABLE Est_productos (
    id_est_producto INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_producto INT UNSIGNED NOT NULL,
    ventas_totales INT NOT NULL,
    ingresos_totales DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- ESTADÍSTICAS CLIENTES
-- ============================================================
CREATE TABLE Est_clientes (
    id_est_cliente INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT UNSIGNED NOT NULL,
    compras_totales INT NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES Clientes(id_cliente)
);

-- ============================================================
-- ESTADÍSTICAS PROVEEDORES
-- ============================================================
CREATE TABLE Est_proveedores (
    id_est_proveedor INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT UNSIGNED NOT NULL,
    compras_totales INT NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES Proveedor(id_proveedor)
);

--ruta de imagenes 

CREATE TABLE Ruta_img (

)