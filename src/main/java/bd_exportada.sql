-- MySQL dump 10.13  Distrib 8.0.33, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: gestor_abbyac27
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `abono_credito_compra`
--

DROP TABLE IF EXISTS `abono_credito_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `abono_credito_compra` (
  `abono_id` int unsigned NOT NULL AUTO_INCREMENT,
  `credito_id` int unsigned NOT NULL,
  `metodo_pago_id` int unsigned NOT NULL,
  `monto_abono` decimal(12,2) NOT NULL,
  `fecha` datetime NOT NULL,
  `estado` enum('pendiente','confirmado') NOT NULL,
  PRIMARY KEY (`abono_id`),
  KEY `credito_id` (`credito_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  CONSTRAINT `abono_credito_compra_ibfk_1` FOREIGN KEY (`credito_id`) REFERENCES `credito_compra` (`credito_id`),
  CONSTRAINT `abono_credito_compra_ibfk_2` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `abono_credito_compra`
--

LOCK TABLES `abono_credito_compra` WRITE;
/*!40000 ALTER TABLE `abono_credito_compra` DISABLE KEYS */;
/*!40000 ALTER TABLE `abono_credito_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `caso_postcompra_usuario`
--

DROP TABLE IF EXISTS `caso_postcompra_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caso_postcompra_usuario` (
  `caso_id` int unsigned NOT NULL AUTO_INCREMENT,
  `compra_id` int unsigned NOT NULL,
  `tipo` enum('cambio','devolucion','reclamo') NOT NULL,
  `cantidad` int NOT NULL,
  `motivo` text,
  `fecha` date NOT NULL,
  `estado` enum('en_proceso','aprobado','cancelado') DEFAULT NULL,
  PRIMARY KEY (`caso_id`),
  KEY `compra_id` (`compra_id`),
  CONSTRAINT `caso_postcompra_usuario_ibfk_1` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`compra_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caso_postcompra_usuario`
--

LOCK TABLES `caso_postcompra_usuario` WRITE;
/*!40000 ALTER TABLE `caso_postcompra_usuario` DISABLE KEYS */;
/*!40000 ALTER TABLE `caso_postcompra_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `caso_postventa_cliente`
--

DROP TABLE IF EXISTS `caso_postventa_cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caso_postventa_cliente` (
  `caso_id` int unsigned NOT NULL AUTO_INCREMENT,
  `venta_id` int unsigned NOT NULL,
  `tipo` enum('cambio','devolucion','reclamo') NOT NULL,
  `cantidad` int NOT NULL,
  `motivo` text,
  `fecha` date NOT NULL,
  `estado` enum('en_proceso','aprobado','cancelado') DEFAULT NULL,
  PRIMARY KEY (`caso_id`),
  KEY `venta_id` (`venta_id`),
  CONSTRAINT `caso_postventa_cliente_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta_factura` (`venta_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caso_postventa_cliente`
--

LOCK TABLES `caso_postventa_cliente` WRITE;
/*!40000 ALTER TABLE `caso_postventa_cliente` DISABLE KEYS */;
/*!40000 ALTER TABLE `caso_postventa_cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `categoria_id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `icono` varchar(255) DEFAULT NULL,
  `subcategoria_id` int unsigned DEFAULT NULL,
  PRIMARY KEY (`categoria_id`),
  KEY `subcategoria_id` (`subcategoria_id`),
  CONSTRAINT `categoria_ibfk_1` FOREIGN KEY (`subcategoria_id`) REFERENCES `subcategoria` (`subcategoria_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,'Anillos','anillos.png',NULL),(2,'Topitos','topitos.png',NULL),(3,'Aretes largos','aretes_largos.png',NULL),(4,'Conjuntos','juegos.png',NULL),(5,'Earcuff','earcuff.png',NULL),(6,'Rosarios','rosario.png',NULL),(7,'Collares','collar.png',NULL),(8,'Tobilleras','tobillera.png',NULL),(9,'Dijes','dijes.png',NULL),(10,'Denarios','denarios.png',NULL),(11,'Pulseras','pulsera.png',NULL),(12,'Manillas','manillas.png',NULL),(13,'Materiales','materiales.png',NULL);
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `compra`
--

DROP TABLE IF EXISTS `compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `compra` (
  `compra_id` int unsigned NOT NULL AUTO_INCREMENT,
  `usuario_proveedor_id` int unsigned NOT NULL,
  `fecha_compra` date NOT NULL,
  `fecha_entrega` date NOT NULL,
  `total` decimal(10,2) NOT NULL,
  PRIMARY KEY (`compra_id`),
  KEY `usuario_proveedor_id` (`usuario_proveedor_id`),
  CONSTRAINT `compra_ibfk_1` FOREIGN KEY (`usuario_proveedor_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compra`
--

LOCK TABLES `compra` WRITE;
/*!40000 ALTER TABLE `compra` DISABLE KEYS */;
/*!40000 ALTER TABLE `compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `correo_usuario`
--

DROP TABLE IF EXISTS `correo_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `correo_usuario` (
  `correo_id` int unsigned NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `usuario_id` int unsigned NOT NULL,
  PRIMARY KEY (`correo_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `correo_usuario_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `correo_usuario`
--

LOCK TABLES `correo_usuario` WRITE;
/*!40000 ALTER TABLE `correo_usuario` DISABLE KEYS */;
INSERT INTO `correo_usuario` VALUES (2,'marlenbe211@gmail.com',3);
/*!40000 ALTER TABLE `correo_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `credito_compra`
--

DROP TABLE IF EXISTS `credito_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `credito_compra` (
  `credito_id` int unsigned NOT NULL AUTO_INCREMENT,
  `compra_id` int unsigned NOT NULL,
  `monto_total` decimal(12,2) NOT NULL,
  `saldo_pendiente` decimal(12,2) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_vencimiento` date NOT NULL,
  `estado` enum('activo','pagado','vencido') NOT NULL,
  PRIMARY KEY (`credito_id`),
  KEY `compra_id` (`compra_id`),
  CONSTRAINT `credito_compra_ibfk_1` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`compra_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credito_compra`
--

LOCK TABLES `credito_compra` WRITE;
/*!40000 ALTER TABLE `credito_compra` DISABLE KEYS */;
/*!40000 ALTER TABLE `credito_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `desempeno_vendedor`
--

DROP TABLE IF EXISTS `desempeno_vendedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `desempeno_vendedor` (
  `desempeno_id` int unsigned NOT NULL AUTO_INCREMENT,
  `usuario_id` int unsigned NOT NULL,
  `ventas_totales` decimal(12,2) NOT NULL,
  `comision_porcentaje` decimal(5,2) NOT NULL,
  `comision_ganada` decimal(12,2) NOT NULL,
  `periodo` date NOT NULL,
  PRIMARY KEY (`desempeno_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `desempeno_vendedor_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `desempeno_vendedor`
--

LOCK TABLES `desempeno_vendedor` WRITE;
/*!40000 ALTER TABLE `desempeno_vendedor` DISABLE KEYS */;
/*!40000 ALTER TABLE `desempeno_vendedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_compra`
--

DROP TABLE IF EXISTS `detalle_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_compra` (
  `detalle_compra_id` int unsigned NOT NULL AUTO_INCREMENT,
  `compra_id` int unsigned NOT NULL,
  `producto_id` int unsigned NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `cantidad` int NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`detalle_compra_id`),
  KEY `compra_id` (`compra_id`),
  KEY `producto_id` (`producto_id`),
  CONSTRAINT `detalle_compra_ibfk_1` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`compra_id`),
  CONSTRAINT `detalle_compra_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_compra`
--

LOCK TABLES `detalle_compra` WRITE;
/*!40000 ALTER TABLE `detalle_compra` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalle_compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_venta`
--

DROP TABLE IF EXISTS `detalle_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_venta` (
  `detalle_venta_id` int unsigned NOT NULL AUTO_INCREMENT,
  `venta_id` int unsigned NOT NULL,
  `producto_id` int unsigned NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`detalle_venta_id`),
  KEY `venta_id` (`venta_id`),
  KEY `producto_id` (`producto_id`),
  CONSTRAINT `detalle_venta_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta_factura` (`venta_id`),
  CONSTRAINT `detalle_venta_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_venta`
--

LOCK TABLES `detalle_venta` WRITE;
/*!40000 ALTER TABLE `detalle_venta` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalle_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_caso_cliente`
--

DROP TABLE IF EXISTS `estado_caso_cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_caso_cliente` (
  `estado_id` int unsigned NOT NULL AUTO_INCREMENT,
  `caso_id` int unsigned NOT NULL,
  `estado` enum('en_proceso','aprobado','cancelado') NOT NULL,
  `fecha` datetime NOT NULL,
  `observacion` text,
  PRIMARY KEY (`estado_id`),
  KEY `caso_id` (`caso_id`),
  CONSTRAINT `estado_caso_cliente_ibfk_1` FOREIGN KEY (`caso_id`) REFERENCES `caso_postventa_cliente` (`caso_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_caso_cliente`
--

LOCK TABLES `estado_caso_cliente` WRITE;
/*!40000 ALTER TABLE `estado_caso_cliente` DISABLE KEYS */;
/*!40000 ALTER TABLE `estado_caso_cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_caso_proveedor`
--

DROP TABLE IF EXISTS `estado_caso_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_caso_proveedor` (
  `estado_id` int unsigned NOT NULL AUTO_INCREMENT,
  `caso_id` int unsigned NOT NULL,
  `estado` enum('en_proceso','aprobado','cancelado') NOT NULL,
  `fecha` datetime NOT NULL,
  `observacion` text,
  PRIMARY KEY (`estado_id`),
  KEY `caso_id` (`caso_id`),
  CONSTRAINT `estado_caso_proveedor_ibfk_1` FOREIGN KEY (`caso_id`) REFERENCES `caso_postcompra_usuario` (`caso_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_caso_proveedor`
--

LOCK TABLES `estado_caso_proveedor` WRITE;
/*!40000 ALTER TABLE `estado_caso_proveedor` DISABLE KEYS */;
/*!40000 ALTER TABLE `estado_caso_proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventario_movimiento`
--

DROP TABLE IF EXISTS `inventario_movimiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario_movimiento` (
  `movimiento_id` int unsigned NOT NULL AUTO_INCREMENT,
  `producto_id` int unsigned NOT NULL,
  `tipo` enum('entrada','salida','ajuste') NOT NULL,
  `estado` enum('activo','inactivo') DEFAULT NULL,
  `cantidad` int NOT NULL,
  `fecha` datetime NOT NULL,
  `referencia` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`movimiento_id`),
  KEY `producto_id` (`producto_id`),
  CONSTRAINT `inventario_movimiento_ibfk_1` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventario_movimiento`
--

LOCK TABLES `inventario_movimiento` WRITE;
/*!40000 ALTER TABLE `inventario_movimiento` DISABLE KEYS */;
/*!40000 ALTER TABLE `inventario_movimiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `material`
--

DROP TABLE IF EXISTS `material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `material` (
  `material_id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material`
--

LOCK TABLES `material` WRITE;
/*!40000 ALTER TABLE `material` DISABLE KEYS */;
INSERT INTO `material` VALUES (1,'Acero inoxidable'),(2,'Plata'),(3,'Oro laminado'),(4,'Mostacilla'),(5,'Hilo coreano'),(6,'Hilo encerado'),(7,'Nylon'),(8,'Dijes'),(9,'Perlas varias');
/*!40000 ALTER TABLE `material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metodo_pago`
--

DROP TABLE IF EXISTS `metodo_pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `metodo_pago` (
  `metodo_pago_id` int unsigned NOT NULL AUTO_INCREMENT,
  `venta_id` int unsigned DEFAULT NULL,
  `compra_id` int unsigned DEFAULT NULL,
  `monto` decimal(12,2) NOT NULL,
  `metodo` enum('efectivo','tarjeta') NOT NULL,
  `fecha` date NOT NULL,
  `estado` enum('pendiente','confirmado','rechazado') NOT NULL,
  PRIMARY KEY (`metodo_pago_id`),
  KEY `venta_id` (`venta_id`),
  KEY `compra_id` (`compra_id`),
  CONSTRAINT `metodo_pago_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta_factura` (`venta_id`),
  CONSTRAINT `metodo_pago_ibfk_2` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`compra_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metodo_pago`
--

LOCK TABLES `metodo_pago` WRITE;
/*!40000 ALTER TABLE `metodo_pago` DISABLE KEYS */;
/*!40000 ALTER TABLE `metodo_pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago_credito`
--

DROP TABLE IF EXISTS `pago_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago_credito` (
  `pago_credito_id` int unsigned NOT NULL AUTO_INCREMENT,
  `metodo_pago_id` int unsigned NOT NULL,
  `credito_id` int unsigned NOT NULL,
  `monto_abono` decimal(12,2) NOT NULL,
  `fecha` date NOT NULL,
  `fecha_vencimiento` date NOT NULL,
  `metodo` enum('efectivo','tarjeta') NOT NULL,
  `estado` enum('pendiente','pagado','vencido') NOT NULL,
  PRIMARY KEY (`pago_credito_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  CONSTRAINT `pago_credito_ibfk_1` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago_credito`
--

LOCK TABLES `pago_credito` WRITE;
/*!40000 ALTER TABLE `pago_credito` DISABLE KEYS */;
/*!40000 ALTER TABLE `pago_credito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago_venta`
--

DROP TABLE IF EXISTS `pago_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago_venta` (
  `pago_venta_id` int unsigned NOT NULL AUTO_INCREMENT,
  `venta_id` int unsigned NOT NULL,
  `metodo_pago_id` int unsigned NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `fecha` datetime NOT NULL,
  `estado` enum('pendiente','confirmado','rechazado') NOT NULL,
  PRIMARY KEY (`pago_venta_id`),
  KEY `venta_id` (`venta_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  CONSTRAINT `pago_venta_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta_factura` (`venta_id`),
  CONSTRAINT `pago_venta_ibfk_2` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago_venta`
--

LOCK TABLES `pago_venta` WRITE;
/*!40000 ALTER TABLE `pago_venta` DISABLE KEYS */;
/*!40000 ALTER TABLE `pago_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `permiso`
--

DROP TABLE IF EXISTS `permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permiso` (
  `permiso_id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `descripcion` text,
  PRIMARY KEY (`permiso_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permiso`
--

LOCK TABLES `permiso` WRITE;
/*!40000 ALTER TABLE `permiso` DISABLE KEYS */;
/*!40000 ALTER TABLE `permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `producto_id` int unsigned NOT NULL AUTO_INCREMENT,
  `codigo` varchar(10) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `stock` int NOT NULL DEFAULT '0',
  `precio_unitario` decimal(10,2) NOT NULL,
  `precio_venta` decimal(10,2) NOT NULL,
  `fecha_registro` date NOT NULL,
  `imagen` varchar(255) DEFAULT NULL,
  `imagen_data` mediumblob,
  `imagen_tipo` varchar(50) DEFAULT NULL,
  `material_id` int unsigned NOT NULL,
  `categoria_id` int unsigned NOT NULL,
  `usuario_proveedor_id` int unsigned NOT NULL,
  PRIMARY KEY (`producto_id`),
  KEY `material_id` (`material_id`),
  KEY `categoria_id` (`categoria_id`),
  KEY `usuario_proveedor_id` (`usuario_proveedor_id`),
  CONSTRAINT `producto_ibfk_1` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`),
  CONSTRAINT `producto_ibfk_2` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`categoria_id`),
  CONSTRAINT `producto_ibfk_3` FOREIGN KEY (`usuario_proveedor_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recuperacion_contrasena`
--

DROP TABLE IF EXISTS `recuperacion_contrasena`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recuperacion_contrasena` (
  `recuperacion_id` int unsigned NOT NULL AUTO_INCREMENT,
  `usuario_id` int unsigned NOT NULL,
  `token` varchar(255) NOT NULL,
  `fecha_solicitud` datetime NOT NULL,
  `fecha_expiracion` datetime NOT NULL,
  `estado` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`recuperacion_id`),
  UNIQUE KEY `token` (`token`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `recuperacion_contrasena_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recuperacion_contrasena`
--

LOCK TABLES `recuperacion_contrasena` WRITE;
/*!40000 ALTER TABLE `recuperacion_contrasena` DISABLE KEYS */;
/*!40000 ALTER TABLE `recuperacion_contrasena` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `rol_id` int unsigned NOT NULL AUTO_INCREMENT,
  `cargo` enum('vendedor','administrador','proveedor','cliente') NOT NULL,
  `usuario_id` int unsigned NOT NULL,
  PRIMARY KEY (`rol_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `rol_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'administrador',1),(3,'vendedor',3);
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol_permiso`
--

DROP TABLE IF EXISTS `rol_permiso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_permiso` (
  `rol_id` int unsigned NOT NULL,
  `permiso_id` int unsigned NOT NULL,
  PRIMARY KEY (`rol_id`,`permiso_id`),
  KEY `permiso_id` (`permiso_id`),
  CONSTRAINT `rol_permiso_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`rol_id`),
  CONSTRAINT `rol_permiso_ibfk_2` FOREIGN KEY (`permiso_id`) REFERENCES `permiso` (`permiso_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_permiso`
--

LOCK TABLES `rol_permiso` WRITE;
/*!40000 ALTER TABLE `rol_permiso` DISABLE KEYS */;
/*!40000 ALTER TABLE `rol_permiso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subcategoria`
--

DROP TABLE IF EXISTS `subcategoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subcategoria` (
  `subcategoria_id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`subcategoria_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subcategoria`
--

LOCK TABLES `subcategoria` WRITE;
/*!40000 ALTER TABLE `subcategoria` DISABLE KEYS */;
/*!40000 ALTER TABLE `subcategoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `telefono_usuario`
--

DROP TABLE IF EXISTS `telefono_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `telefono_usuario` (
  `telefono_id` int unsigned NOT NULL AUTO_INCREMENT,
  `telefono` varchar(50) NOT NULL,
  `usuario_id` int unsigned NOT NULL,
  PRIMARY KEY (`telefono_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `telefono_usuario_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `telefono_usuario`
--

LOCK TABLES `telefono_usuario` WRITE;
/*!40000 ALTER TABLE `telefono_usuario` DISABLE KEYS */;
INSERT INTO `telefono_usuario` VALUES (2,'3027131281',3);
/*!40000 ALTER TABLE `telefono_usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `usuario_id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `pass` varchar(255) NOT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL,
  `documento` varchar(50) DEFAULT NULL,
  `fecha_registro` date DEFAULT NULL,
  `fecha_inicio` date DEFAULT NULL,
  `minimo_compra` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`usuario_id`),
  UNIQUE KEY `documento` (`documento`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'AdminKS','$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.',1,'2026-02-26 19:22:06',NULL,NULL,NULL,NULL),(3,'Stephany','$2a$10$NDiZRh7T6HawCK1q3vxFp.Xa8QH58xT0wkDkcyYRknu7MIDurGBHC',1,'2026-02-26 19:32:30','1099739953',NULL,NULL,NULL);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_material`
--

DROP TABLE IF EXISTS `usuario_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_material` (
  `usuario_id` int unsigned NOT NULL,
  `material_id` int unsigned NOT NULL,
  PRIMARY KEY (`usuario_id`,`material_id`),
  KEY `material_id` (`material_id`),
  CONSTRAINT `usuario_material_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`),
  CONSTRAINT `usuario_material_ibfk_2` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_material`
--

LOCK TABLES `usuario_material` WRITE;
/*!40000 ALTER TABLE `usuario_material` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `venta_factura`
--

DROP TABLE IF EXISTS `venta_factura`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `venta_factura` (
  `venta_id` int unsigned NOT NULL AUTO_INCREMENT,
  `usuario_id` int unsigned NOT NULL,
  `usuario_cliente_id` int unsigned NOT NULL,
  `fecha_emision` date NOT NULL,
  `total` decimal(10,2) NOT NULL,
  PRIMARY KEY (`venta_id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `usuario_cliente_id` (`usuario_cliente_id`),
  CONSTRAINT `venta_factura_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`),
  CONSTRAINT `venta_factura_ibfk_2` FOREIGN KEY (`usuario_cliente_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `venta_factura`
--

LOCK TABLES `venta_factura` WRITE;
/*!40000 ALTER TABLE `venta_factura` DISABLE KEYS */;
/*!40000 ALTER TABLE `venta_factura` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-26 19:53:32
