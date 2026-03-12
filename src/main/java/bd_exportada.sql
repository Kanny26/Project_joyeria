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
-- Table structure for table `abono_credito`
--

DROP TABLE IF EXISTS `abono_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `abono_credito` (
  `abono_id` int unsigned NOT NULL AUTO_INCREMENT,
  `credito_id` int unsigned NOT NULL,
  `metodo_pago_id` int unsigned NOT NULL,
  `monto_abono` decimal(12,2) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('pendiente','confirmado') NOT NULL DEFAULT 'pendiente',
  PRIMARY KEY (`abono_id`),
  KEY `credito_id` (`credito_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  CONSTRAINT `abono_credito_ibfk_1` FOREIGN KEY (`credito_id`) REFERENCES `credito_compra` (`credito_id`),
  CONSTRAINT `abono_credito_ibfk_2` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `abono_credito`
--

LOCK TABLES `abono_credito` WRITE;
/*!40000 ALTER TABLE `abono_credito` DISABLE KEYS */;
INSERT INTO `abono_credito` VALUES (1,1,1,3000000.00,'2026-03-12 00:05:23','confirmado'),(2,2,2,1500000.00,'2026-03-12 00:05:23','confirmado'),(3,3,1,1400000.00,'2026-03-12 00:05:23','confirmado'),(4,4,5,0.00,'2026-03-12 00:05:23','pendiente'),(5,5,3,3200000.00,'2026-03-12 00:05:23','confirmado'),(6,6,7,0.00,'2026-03-12 00:05:23','pendiente'),(7,7,1,3600000.00,'2026-03-12 00:05:23','confirmado'),(8,8,4,620000.00,'2026-03-12 00:05:23','confirmado'),(9,9,6,2700000.00,'2026-03-12 00:05:23','confirmado'),(10,10,8,0.00,'2026-03-12 00:05:23','pendiente'),(11,11,2,2700000.00,'2026-03-12 00:05:23','confirmado'),(12,12,3,880000.00,'2026-03-12 00:05:23','confirmado'),(13,13,1,4000000.00,'2026-03-12 00:05:23','confirmado'),(14,14,5,0.00,'2026-03-12 00:05:23','pendiente'),(15,15,7,2600000.00,'2026-03-12 00:05:23','confirmado');
/*!40000 ALTER TABLE `abono_credito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `auditoria_log`
--

DROP TABLE IF EXISTS `auditoria_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auditoria_log` (
  `log_id` int unsigned NOT NULL AUTO_INCREMENT,
  `usuario_id` int unsigned DEFAULT NULL,
  `accion` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entidad` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entidad_id` int unsigned DEFAULT NULL,
  `datos_anteriores` json DEFAULT NULL,
  `datos_nuevos` json DEFAULT NULL,
  `direccion_ip` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_usuario_fecha` (`usuario_id`,`fecha_hora`),
  KEY `idx_accion` (`accion`),
  KEY `idx_entidad` (`entidad`),
  KEY `idx_fecha` (`fecha_hora`),
  CONSTRAINT `auditoria_log_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auditoria_log`
--

LOCK TABLES `auditoria_log` WRITE;
/*!40000 ALTER TABLE `auditoria_log` DISABLE KEYS */;
INSERT INTO `auditoria_log` VALUES (1,1,'CREAR','Proveedor',16,NULL,'{\"valor\": \"Joyeria luz de sol\"}',NULL,'2026-03-12 00:09:25'),(2,1,'EDITAR','Proveedor',1,'{\"valor\": \"Joyeria Aurora\"}','{\"valor\": \"Joyeria Aurora\"}',NULL,'2026-03-12 00:10:33');
/*!40000 ALTER TABLE `auditoria_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `caso_postventa`
--

DROP TABLE IF EXISTS `caso_postventa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caso_postventa` (
  `caso_id` int unsigned NOT NULL AUTO_INCREMENT,
  `venta_id` int unsigned NOT NULL,
  `tipo` enum('cambio','devolucion','reclamo') NOT NULL,
  `cantidad` int NOT NULL,
  `motivo` text,
  `fecha` date NOT NULL DEFAULT (curdate()),
  `estado` enum('en_proceso','aprobado','cancelado') NOT NULL DEFAULT 'en_proceso',
  PRIMARY KEY (`caso_id`),
  KEY `venta_id` (`venta_id`),
  CONSTRAINT `caso_postventa_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta` (`venta_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caso_postventa`
--

LOCK TABLES `caso_postventa` WRITE;
/*!40000 ALTER TABLE `caso_postventa` DISABLE KEYS */;
INSERT INTO `caso_postventa` VALUES (1,1,'cambio',1,'Talla incorrecta del anillo','2026-03-12','aprobado'),(2,2,'devolucion',1,'Cliente cambió de opinión','2026-03-12','aprobado'),(3,3,'reclamo',1,'Pieza llegó con pequeño detalle','2026-03-12','en_proceso'),(4,4,'cambio',2,'Prefiere otro diseño de aretes','2026-03-12','aprobado'),(5,5,'devolucion',1,'Problema con el cierre','2026-03-12','en_proceso'),(6,6,'reclamo',1,'Demora en la entrega','2026-03-12','cancelado'),(7,7,'cambio',1,'Color no era el esperado','2026-03-12','aprobado'),(8,8,'devolucion',1,'Error en el pedido','2026-03-12','aprobado'),(9,9,'reclamo',1,'Empaque dañado','2026-03-12','en_proceso'),(10,10,'cambio',1,'Talla de manilla incorrecta','2026-03-12','aprobado'),(11,11,'devolucion',1,'Cliente insatisfecho','2026-03-12','cancelado'),(12,12,'reclamo',1,'Producto diferente al mostrado','2026-03-12','en_proceso'),(13,13,'cambio',3,'Cambio de modelo en topitos','2026-03-12','aprobado'),(14,14,'devolucion',1,'Cancelación de evento','2026-03-12','en_proceso'),(15,15,'reclamo',1,'Detalle en el broche','2026-03-12','aprobado');
/*!40000 ALTER TABLE `caso_postventa` ENABLE KEYS */;
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
  CONSTRAINT `categoria_ibfk_1` FOREIGN KEY (`subcategoria_id`) REFERENCES `subcategoria` (`subcategoria_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,'Anillos','anillos.png',1),(2,'Topitos','topitos.png',2),(3,'Aretes Largos','aretes_largos.png',3),(4,'Conjuntos','juegos.png',4),(5,'Earcuff','earcuff.png',5),(6,'Rosarios','rosario.png',6),(7,'Collares','collar.png',7),(8,'Tobilleras','tobillera.png',8),(9,'Dijes','dijes.png',9),(10,'Denarios','denarios.png',10),(11,'Pulseras','pulsera.png',11),(12,'Manillas','manillas.png',12),(13,'Relojes','reloj.png',13),(14,'Estuches','estuches.png',14),(15,'Accesorios en lana','lanas.png',15);
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `cliente_id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `documento` varchar(50) DEFAULT NULL,
  `fecha_registro` date NOT NULL DEFAULT (curdate()),
  `minimo_compra` decimal(10,2) DEFAULT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`cliente_id`),
  UNIQUE KEY `documento` (`documento`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
INSERT INTO `cliente` VALUES (1,'María Fernanda Castro','1234567890','2026-03-12',50000.00,1),(2,'Juan Pablo Ramírez','2345678901','2026-03-12',100000.00,1),(3,'Ana Lucía Gómez','3456789012','2026-03-12',75000.00,1),(4,'Carlos Andrés Díaz','4567890123','2026-03-12',200000.00,1),(5,'Laura Sofía Martínez','5678901234','2026-03-12',80000.00,1),(6,'Diego Fernando López','6789012345','2026-03-12',150000.00,1),(7,'Valeria Alejandra Ruiz','7890123456','2026-03-12',90000.00,1),(8,'Sebastián Mora Silva','8901234567','2026-03-12',120000.00,1),(9,'Camila Andrea Pérez','9012345678','2026-03-12',60000.00,1),(10,'Andrés Felipe Torres','1023456789','2026-03-12',180000.00,1),(11,'Daniela Paola Vega','1123456780','2026-03-12',70000.00,1),(12,'Mateo Alejandro Herrera','1223456781','2026-03-12',130000.00,1),(13,'Sofía Isabel Mendoza','1323456782','2026-03-12',85000.00,1),(14,'Santiago David Castro','1423456783','2026-03-12',160000.00,1),(15,'Isabella María Ortiz','1523456784','2026-03-12',95000.00,1);
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `compra`
--

DROP TABLE IF EXISTS `compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `compra` (
  `compra_id` int unsigned NOT NULL AUTO_INCREMENT,
  `proveedor_id` int unsigned NOT NULL,
  `fecha_compra` date NOT NULL,
  `fecha_entrega` date NOT NULL,
  PRIMARY KEY (`compra_id`),
  KEY `proveedor_id` (`proveedor_id`),
  CONSTRAINT `compra_ibfk_1` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedor` (`proveedor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `compra`
--

LOCK TABLES `compra` WRITE;
/*!40000 ALTER TABLE `compra` DISABLE KEYS */;
INSERT INTO `compra` VALUES (1,1,'2026-03-12','2026-03-15'),(2,2,'2026-03-12','2026-03-17'),(3,3,'2026-03-12','2026-03-14'),(4,4,'2026-03-12','2026-03-16'),(5,5,'2026-03-12','2026-03-15'),(6,6,'2026-03-12','2026-03-18'),(7,7,'2026-03-12','2026-03-14'),(8,8,'2026-03-12','2026-03-17'),(9,9,'2026-03-12','2026-03-15'),(10,10,'2026-03-12','2026-03-16'),(11,11,'2026-03-12','2026-03-14'),(12,12,'2026-03-12','2026-03-17'),(13,13,'2026-03-12','2026-03-15'),(14,14,'2026-03-12','2026-03-16'),(15,15,'2026-03-12','2026-03-14');
/*!40000 ALTER TABLE `compra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `correo_cliente`
--

DROP TABLE IF EXISTS `correo_cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `correo_cliente` (
  `correo_id` int unsigned NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `cliente_id` int unsigned NOT NULL,
  PRIMARY KEY (`correo_id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `correo_cliente_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `correo_cliente`
--

LOCK TABLES `correo_cliente` WRITE;
/*!40000 ALTER TABLE `correo_cliente` DISABLE KEYS */;
INSERT INTO `correo_cliente` VALUES (1,'maria.castro@email.com',1),(2,'juan.ramirez@email.com',2),(3,'ana.gomez@email.com',3),(4,'carlos.diaz@email.com',4),(5,'laura.martinez@email.com',5),(6,'diego.lopez@email.com',6),(7,'valeria.ruiz@email.com',7),(8,'sebastian.mora@email.com',8),(9,'camila.perez@email.com',9),(10,'andres.torres@email.com',10),(11,'daniela.vega@email.com',11),(12,'mateo.herrera@email.com',12),(13,'sofia.mendoza@email.com',13),(14,'santiago.castro@email.com',14),(15,'isabella.ortiz@email.com',15);
/*!40000 ALTER TABLE `correo_cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `correo_proveedor`
--

DROP TABLE IF EXISTS `correo_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `correo_proveedor` (
  `correo_id` int unsigned NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `proveedor_id` int unsigned NOT NULL,
  PRIMARY KEY (`correo_id`),
  KEY `proveedor_id` (`proveedor_id`),
  CONSTRAINT `correo_proveedor_ibfk_1` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedor` (`proveedor_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `correo_proveedor`
--

LOCK TABLES `correo_proveedor` WRITE;
/*!40000 ALTER TABLE `correo_proveedor` DISABLE KEYS */;
INSERT INTO `correo_proveedor` VALUES (2,'ventas@joyeriasamaritans.co',2),(3,'gerencia@suministrosjoyeros.com.co',3),(4,'pedidos@perlasdelcaribe.com',4),(5,'info@herrajesdiamante.com',5),(6,'comercial@plataasaleya.com',6),(7,'ventas@distriacerofatima.co',7),(8,'comercial@eleganceestuches.com',8),(9,'gerencia@insumosdeleje.co',9),(10,'mayorista@brilloeterno.com',10),(11,'pedidos@glamcristales.com',11),(12,'willyjohns@atencion.com',12),(13,'importaciones@continental.com',13),(14,'delaida@accesorios.com',14),(15,'importaciones@zafiro.com',15),(16,'joyeria_luz_sol@gmail.com',16),(17,'contacto@joyeriaaurora.com',1);
/*!40000 ALTER TABLE `correo_proveedor` ENABLE KEYS */;
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
  CONSTRAINT `correo_usuario_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `correo_usuario`
--

LOCK TABLES `correo_usuario` WRITE;
/*!40000 ALTER TABLE `correo_usuario` DISABLE KEYS */;
INSERT INTO `correo_usuario` VALUES (1,'marlenbe211@gmail.com',1),(2,'santiago.morenob500@gmail.com',2),(3,'carlos.ruiz@abbyac27.com',3),(4,'stephanymoreno1826@gmail.com',4),(5,'felipe.andrade@abbyac27.com',5),(6,'gabriela.silva@abbyac27.com',6),(7,'hector.diaz@abbyac27.com',7),(8,'isabella.ramirez@abbyac27.com',8),(9,'javier.ortiz@abbyac27.com',9),(10,'karen.lopez@abbyac27.com',10),(11,'luis.castro@abbyac27.com',11),(12,'maria.perez@abbyac27.com',12),(13,'nicolas.gomez@abbyac27.com',13),(14,'olga.ruiz@abbyac27.com',14),(15,'pablo.diaz@abbyac27.com',15);
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
  `estado` enum('activo','pagado','vencido') NOT NULL DEFAULT 'activo',
  PRIMARY KEY (`credito_id`),
  UNIQUE KEY `compra_id` (`compra_id`),
  CONSTRAINT `credito_compra_ibfk_1` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`compra_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `credito_compra`
--

LOCK TABLES `credito_compra` WRITE;
/*!40000 ALTER TABLE `credito_compra` DISABLE KEYS */;
INSERT INTO `credito_compra` VALUES (1,1,3000000.00,0.00,'2026-03-12','2026-04-11','pagado'),(2,2,3000000.00,1500000.00,'2026-03-12','2026-04-11','activo'),(3,3,1400000.00,0.00,'2026-03-12','2026-04-11','pagado'),(4,4,1350000.00,1350000.00,'2026-03-12','2026-04-11','activo'),(5,5,3200000.00,0.00,'2026-03-12','2026-04-11','pagado'),(6,6,3000000.00,3000000.00,'2026-03-12','2026-04-11','activo'),(7,7,3600000.00,0.00,'2026-03-12','2026-04-11','pagado'),(8,8,1240000.00,620000.00,'2026-03-12','2026-04-11','activo'),(9,9,2700000.00,0.00,'2026-03-12','2026-04-11','pagado'),(10,10,3750000.00,3750000.00,'2026-03-12','2026-04-11','activo'),(11,11,2700000.00,0.00,'2026-03-12','2026-04-11','pagado'),(12,12,1760000.00,880000.00,'2026-03-12','2026-04-11','activo'),(13,13,4000000.00,0.00,'2026-03-12','2026-04-11','pagado'),(14,14,2550000.00,2550000.00,'2026-03-12','2026-04-11','activo'),(15,15,2600000.00,0.00,'2026-03-12','2026-04-11','pagado');
/*!40000 ALTER TABLE `credito_compra` ENABLE KEYS */;
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
  PRIMARY KEY (`detalle_compra_id`),
  KEY `compra_id` (`compra_id`),
  KEY `producto_id` (`producto_id`),
  CONSTRAINT `detalle_compra_ibfk_1` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`compra_id`) ON DELETE CASCADE,
  CONSTRAINT `detalle_compra_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_compra`
--

LOCK TABLES `detalle_compra` WRITE;
/*!40000 ALTER TABLE `detalle_compra` DISABLE KEYS */;
INSERT INTO `detalle_compra` VALUES (1,1,1,150000.00,20),(2,2,2,300000.00,10),(3,3,3,280000.00,5),(4,4,4,90000.00,15),(5,5,5,400000.00,8),(6,6,6,250000.00,12),(7,7,7,120000.00,30),(8,8,8,310000.00,4),(9,9,9,270000.00,10),(10,10,10,150000.00,25),(11,11,11,180000.00,15),(12,12,12,220000.00,8),(13,13,13,80000.00,50),(14,14,14,850000.00,3),(15,15,15,130000.00,20);
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
  PRIMARY KEY (`detalle_venta_id`),
  KEY `venta_id` (`venta_id`),
  KEY `producto_id` (`producto_id`),
  CONSTRAINT `detalle_venta_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta` (`venta_id`) ON DELETE CASCADE,
  CONSTRAINT `detalle_venta_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_venta`
--

LOCK TABLES `detalle_venta` WRITE;
/*!40000 ALTER TABLE `detalle_venta` DISABLE KEYS */;
INSERT INTO `detalle_venta` VALUES (1,1,1,1,320000.00),(2,2,2,1,480000.00),(3,3,3,1,450000.00),(4,4,4,2,180000.00),(5,5,5,1,650000.00),(6,6,6,1,390000.00),(7,7,7,1,240000.00),(8,8,8,1,500000.00),(9,9,9,1,420000.00),(10,10,10,1,280000.00),(11,11,11,1,350000.00),(12,12,12,1,400000.00),(13,13,13,3,150000.00),(14,14,14,1,1500000.00),(15,15,15,1,260000.00);
/*!40000 ALTER TABLE `detalle_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historial_caso_postventa`
--

DROP TABLE IF EXISTS `historial_caso_postventa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_caso_postventa` (
  `historial_id` int unsigned NOT NULL AUTO_INCREMENT,
  `caso_id` int unsigned NOT NULL,
  `estado` enum('en_proceso','aprobado','cancelado') NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `observacion` text,
  `usuario_id` int unsigned NOT NULL,
  PRIMARY KEY (`historial_id`),
  KEY `caso_id` (`caso_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `historial_caso_postventa_ibfk_1` FOREIGN KEY (`caso_id`) REFERENCES `caso_postventa` (`caso_id`),
  CONSTRAINT `historial_caso_postventa_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historial_caso_postventa`
--

LOCK TABLES `historial_caso_postventa` WRITE;
/*!40000 ALTER TABLE `historial_caso_postventa` DISABLE KEYS */;
INSERT INTO `historial_caso_postventa` VALUES (1,1,'en_proceso','2026-03-12 00:05:24','Caso recibido para revisión',2),(2,1,'aprobado','2026-03-12 00:05:24','Autorizado cambio de talla',2),(3,2,'en_proceso','2026-03-12 00:05:24','Solicitud de devolución recibida',3),(4,2,'aprobado','2026-03-12 00:05:24','Devolución aprobada, reembolso procesado',3),(5,3,'en_proceso','2026-03-12 00:05:24','Reclamo en evaluación técnica',4),(6,4,'en_proceso','2026-03-12 00:05:24','Cambio de aretes solicitado',5),(7,4,'aprobado','2026-03-12 00:05:24','Nuevos aretes enviados al cliente',5),(8,5,'en_proceso','2026-03-12 00:05:24','Revisión de garantía en proceso',6),(9,6,'en_proceso','2026-03-12 00:05:24','Reclamo por demora registrado',7),(10,6,'cancelado','2026-03-12 00:05:24','Cliente aceptó compensación',7),(11,7,'en_proceso','2026-03-12 00:05:24','Solicitud de cambio de color',8),(12,7,'aprobado','2026-03-12 00:05:24','Cambio aprobado, nuevo producto enviado',8),(13,8,'en_proceso','2026-03-12 00:05:24','Error de pedido confirmado',9),(14,8,'aprobado','2026-03-12 00:05:24','Producto correcto enviado',9),(15,9,'en_proceso','2026-03-12 00:05:24','Reclamo por empaque dañado',10);
/*!40000 ALTER TABLE `historial_caso_postventa` ENABLE KEYS */;
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
  `usuario_id` int unsigned DEFAULT NULL,
  `tipo` enum('entrada','salida','ajuste') NOT NULL,
  `cantidad` int NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `referencia` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`movimiento_id`),
  KEY `producto_id` (`producto_id`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `inventario_movimiento_ibfk_1` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`producto_id`),
  CONSTRAINT `inventario_movimiento_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventario_movimiento`
--

LOCK TABLES `inventario_movimiento` WRITE;
/*!40000 ALTER TABLE `inventario_movimiento` DISABLE KEYS */;
INSERT INTO `inventario_movimiento` VALUES (1,1,1,'entrada',20,'2026-03-12 00:05:23','COMPRA-001'),(2,2,2,'entrada',10,'2026-03-12 00:05:23','COMPRA-002'),(3,3,3,'entrada',5,'2026-03-12 00:05:23','COMPRA-003'),(4,4,4,'entrada',15,'2026-03-12 00:05:23','COMPRA-004'),(5,5,5,'entrada',8,'2026-03-12 00:05:23','COMPRA-005'),(6,6,6,'entrada',12,'2026-03-12 00:05:23','COMPRA-006'),(7,7,7,'entrada',30,'2026-03-12 00:05:23','COMPRA-007'),(8,8,8,'entrada',4,'2026-03-12 00:05:23','COMPRA-008'),(9,9,9,'entrada',10,'2026-03-12 00:05:23','COMPRA-009'),(10,10,10,'entrada',25,'2026-03-12 00:05:23','COMPRA-010'),(11,11,11,'entrada',15,'2026-03-12 00:05:23','COMPRA-011'),(12,12,12,'entrada',8,'2026-03-12 00:05:23','COMPRA-012'),(13,13,13,'entrada',50,'2026-03-12 00:05:23','COMPRA-013'),(14,14,14,'entrada',3,'2026-03-12 00:05:23','COMPRA-014'),(15,15,15,'entrada',20,'2026-03-12 00:05:23','COMPRA-015');
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
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `material`
--

LOCK TABLES `material` WRITE;
/*!40000 ALTER TABLE `material` DISABLE KEYS */;
INSERT INTO `material` VALUES (1,'Plata Ley 950'),(2,'Covergold'),(3,'Rodio'),(4,'Acero Inoxidable'),(5,'Murano'),(6,'Mostacilla'),(7,'Miyuki'),(8,'Perlas de imitación'),(9,'Herrajes'),(10,'Hilo Chino'),(11,'Nylon'),(12,'Lana'),(13,'Balines de Acero'),(14,'Balines de Rodio'),(15,'Balines de Covergold');
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
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`metodo_pago_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metodo_pago`
--

LOCK TABLES `metodo_pago` WRITE;
/*!40000 ALTER TABLE `metodo_pago` DISABLE KEYS */;
INSERT INTO `metodo_pago` VALUES (1,'Efectivo'),(2,'Tarjeta de Crédito'),(3,'Tarjeta de Débito'),(4,'PSE'),(5,'Nequi'),(6,'Daviplata'),(7,'A la Mano'),(8,'Bancolombia (Transferencia)'),(9,'Nu (Nubank)'),(10,'Transfiya'),(11,'Efecty / La Perla'),(12,'Bold'),(13,'Contraentrega (Interrapidísimo)'),(14,'Contraentrega (Servientrega)'),(15,'Wompi / Link de Pago');
/*!40000 ALTER TABLE `metodo_pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago_compra`
--

DROP TABLE IF EXISTS `pago_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago_compra` (
  `pago_compra_id` int unsigned NOT NULL AUTO_INCREMENT,
  `compra_id` int unsigned NOT NULL,
  `metodo_pago_id` int unsigned NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('pendiente','confirmado','rechazado') NOT NULL DEFAULT 'pendiente',
  PRIMARY KEY (`pago_compra_id`),
  KEY `compra_id` (`compra_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  CONSTRAINT `pago_compra_ibfk_1` FOREIGN KEY (`compra_id`) REFERENCES `compra` (`compra_id`) ON DELETE CASCADE,
  CONSTRAINT `pago_compra_ibfk_2` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago_compra`
--

LOCK TABLES `pago_compra` WRITE;
/*!40000 ALTER TABLE `pago_compra` DISABLE KEYS */;
INSERT INTO `pago_compra` VALUES (1,1,4,3000000.00,'2026-03-12 00:05:23','confirmado'),(2,2,7,3000000.00,'2026-03-12 00:05:23','confirmado'),(3,3,1,1400000.00,'2026-03-12 00:05:23','confirmado'),(4,4,5,1350000.00,'2026-03-12 00:05:23','confirmado'),(5,5,2,3200000.00,'2026-03-12 00:05:23','confirmado'),(6,6,3,3000000.00,'2026-03-12 00:05:23','confirmado'),(7,7,1,3600000.00,'2026-03-12 00:05:23','confirmado'),(8,8,4,1240000.00,'2026-03-12 00:05:23','confirmado'),(9,9,6,2700000.00,'2026-03-12 00:05:23','confirmado'),(10,10,7,3750000.00,'2026-03-12 00:05:23','confirmado'),(11,11,8,2700000.00,'2026-03-12 00:05:23','confirmado'),(12,12,1,1760000.00,'2026-03-12 00:05:23','confirmado'),(13,13,2,4000000.00,'2026-03-12 00:05:23','confirmado'),(14,14,4,2550000.00,'2026-03-12 00:05:23','pendiente'),(15,15,5,2600000.00,'2026-03-12 00:05:23','confirmado');
/*!40000 ALTER TABLE `pago_compra` ENABLE KEYS */;
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
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('pendiente','confirmado') NOT NULL DEFAULT 'pendiente',
  PRIMARY KEY (`pago_venta_id`),
  KEY `venta_id` (`venta_id`),
  KEY `metodo_pago_id` (`metodo_pago_id`),
  CONSTRAINT `pago_venta_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `venta` (`venta_id`) ON DELETE CASCADE,
  CONSTRAINT `pago_venta_ibfk_2` FOREIGN KEY (`metodo_pago_id`) REFERENCES `metodo_pago` (`metodo_pago_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago_venta`
--

LOCK TABLES `pago_venta` WRITE;
/*!40000 ALTER TABLE `pago_venta` DISABLE KEYS */;
INSERT INTO `pago_venta` VALUES (1,1,1,320000.00,'2026-03-12 00:05:23','confirmado'),(2,2,2,480000.00,'2026-03-12 00:05:23','confirmado'),(3,3,3,450000.00,'2026-03-12 00:05:23','confirmado'),(4,4,5,360000.00,'2026-03-12 00:05:23','confirmado'),(5,5,6,650000.00,'2026-03-12 00:05:23','confirmado'),(6,6,7,390000.00,'2026-03-12 00:05:23','confirmado'),(7,7,1,240000.00,'2026-03-12 00:05:23','confirmado'),(8,8,2,500000.00,'2026-03-12 00:05:23','confirmado'),(9,9,8,420000.00,'2026-03-12 00:05:23','confirmado'),(10,10,1,280000.00,'2026-03-12 00:05:23','confirmado'),(11,11,3,350000.00,'2026-03-12 00:05:23','confirmado'),(12,12,4,400000.00,'2026-03-12 00:05:23','confirmado'),(13,13,1,450000.00,'2026-03-12 00:05:23','confirmado'),(14,14,2,1500000.00,'2026-03-12 00:05:23','pendiente'),(15,15,5,260000.00,'2026-03-12 00:05:23','confirmado');
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `permiso`
--

LOCK TABLES `permiso` WRITE;
/*!40000 ALTER TABLE `permiso` DISABLE KEYS */;
INSERT INTO `permiso` VALUES (1,'usuarios_crear','Crear nuevos usuarios del sistema'),(2,'usuarios_editar','Modificar datos de usuarios'),(3,'usuarios_eliminar','Eliminar usuarios del sistema'),(4,'productos_crear','Registrar nuevos productos'),(5,'productos_editar','Actualizar precios y stock'),(6,'productos_eliminar','Desactivar productos'),(7,'ventas_registrar','Crear nuevas ventas'),(8,'ventas_consultar','Ver historial de ventas'),(9,'compras_registrar','Registrar compras a proveedores'),(10,'inventario_ajustar','Realizar ajustes de inventario'),(11,'reportes_ver','Acceder a reportes del sistema'),(12,'creditos_aprobar','Aprobar créditos a clientes'),(13,'postventa_gestionar','Gestionar cambios y devoluciones'),(14,'desempeno_ver','Ver métricas de vendedores'),(15,'configuracion_sistema','Acceder a configuración global'),(16,'gestionar_vendedores','Crear y editar usuarios con rol Vendedor'),(17,'gestionar_administradores','Crear y editar usuarios con rol Administrador');
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
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  `precio_unitario` decimal(10,2) NOT NULL,
  `precio_venta` decimal(10,2) NOT NULL,
  `fecha_registro` date NOT NULL DEFAULT (curdate()),
  `imagen` varchar(255) DEFAULT NULL,
  `imagen_data` mediumblob,
  `imagen_tipo` varchar(50) DEFAULT NULL,
  `material_id` int unsigned NOT NULL,
  `categoria_id` int unsigned NOT NULL,
  `subcategoria_id` int unsigned DEFAULT NULL,
  `proveedor_id` int unsigned NOT NULL,
  PRIMARY KEY (`producto_id`),
  UNIQUE KEY `codigo` (`codigo`),
  KEY `material_id` (`material_id`),
  KEY `categoria_id` (`categoria_id`),
  KEY `subcategoria_id` (`subcategoria_id`),
  KEY `proveedor_id` (`proveedor_id`),
  CONSTRAINT `producto_ibfk_1` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`),
  CONSTRAINT `producto_ibfk_2` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`categoria_id`),
  CONSTRAINT `producto_ibfk_3` FOREIGN KEY (`subcategoria_id`) REFERENCES `subcategoria` (`subcategoria_id`) ON DELETE SET NULL,
  CONSTRAINT `producto_ibfk_4` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedor` (`proveedor_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES (1,'ANI001','Anillo Plata Liso','Anillo en Plata Ley 950 fabricado en taller.',10,1,45000.00,95000.00,'2026-03-12','anillo_plata.jpg',NULL,NULL,2,1,2,2),(2,'ANI002','Anillo Brillo 15 Años','Diseño elegante con circonia para quinceañeras.',8,1,300000.00,480000.00,'2026-03-12','anillo_brillo.jpg',NULL,NULL,2,1,2,2),(3,'ANI003','Anillo Compromiso Celestial','Plata 925 con diamante sintético, estilo minimalista.',6,1,280000.00,450000.00,'2026-03-12','anillo_celestial.jpg',NULL,NULL,2,1,3,3),(4,'ARE001','Aretes Murano','Aretes largos fabricados con murano y plata.',15,1,12000.00,35000.00,'2026-03-12','aretes_murano.jpg',NULL,NULL,5,3,7,2),(5,'ARE002','Aretes Cristal Swarovski','Aretes earcuff con cristales austriacos.',5,1,400000.00,650000.00,'2026-03-12','aretes_cristal.jpg',NULL,NULL,11,5,5,5),(6,'COL001','Collar Dije Covergold','Collar corto con dije de corazón en covergold.',12,1,15000.00,35000.00,'2026-03-12','collar_cover.jpg',NULL,NULL,2,7,7,2),(7,'COL002','Rosario en Plata','Rosario de Plata Ley 950 tejido a mano.',5,1,80000.00,180000.00,'2026-03-12','rosario_plata.jpg',NULL,NULL,1,6,5,1),(8,'PUL001','Pulsera Hilo Chino','Pulsera tejida en hilo chino con balín de plata.',30,1,8000.00,25000.00,'2026-03-12','pulsera_hilo.jpg',NULL,NULL,11,11,12,2),(9,'PUL002','Manilla Miyuki','Manilla tejida en Miyuki con herrajes de rodio.',10,1,25000.00,60000.00,'2026-03-12','manilla_miyuki.jpg',NULL,NULL,7,12,11,3),(10,'MAN001','Manilla Titanio Hombre','Manilla resistente de titanio para uso diario.',18,1,150000.00,280000.00,'2026-03-12','manilla_titanio.jpg',NULL,NULL,14,12,15,9),(11,'DIJ001','Dije Corazón Oro','Dije en forma de corazón.',14,1,180000.00,350000.00,'2026-03-12','dije_corazon.jpg',NULL,NULL,4,9,13,10),(12,'DEN001','Denario Plata','Denario de Plata Ley 950 con balines.',11,1,35000.00,75000.00,'2026-03-12','denario_plata.jpg',NULL,NULL,1,10,5,1),(13,'TOP001','Topitos Covergold','Topitos básicos en covergold para uso diario.',25,1,5000.00,15000.00,'2026-03-12','topitos_cover.jpg',NULL,NULL,2,2,12,4),(14,'CON001','Conjunto Boda Completo','Set de anillo, aretes y collar para novia.',3,1,850000.00,1500000.00,'2026-03-12','conjunto_boda.jpg',NULL,NULL,3,4,1,13),(15,'TOB001','Tobillera Verano Plata','Tobillera delicada de plata con detalles de perlas.',16,1,130000.00,260000.00,'2026-03-12','tobillera_verano.jpg',NULL,NULL,2,8,15,14);
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedor`
--

DROP TABLE IF EXISTS `proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedor` (
  `proveedor_id` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `documento` varchar(50) NOT NULL,
  `fecha_registro` date NOT NULL DEFAULT (curdate()),
  `fecha_inicio` date DEFAULT NULL,
  `minimo_compra` decimal(10,2) NOT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`proveedor_id`),
  UNIQUE KEY `documento` (`documento`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedor`
--

LOCK TABLES `proveedor` WRITE;
/*!40000 ALTER TABLE `proveedor` DISABLE KEYS */;
INSERT INTO `proveedor` VALUES (1,'Joyeria Aurora','900987654-1','2026-03-12','2024-01-15',5000000.00,1),(2,'Joyeria samaritans','800111222-3','2026-03-12','2024-03-10',2000000.00,1),(3,'Suministros Joyeros S.A.S','901555444-5','2026-03-12','2023-11-20',10000000.00,1),(4,'Perlas del Caribe Ltda','890333222-1','2026-03-12','2025-01-05',1500000.00,1),(5,'Herrajes diamante','900777888-9','2026-03-12','2024-06-12',500000.00,1),(6,'Distribuidora Plata asaleya','800444555-2','2026-03-12','2024-08-22',3000000.00,1),(7,'Distri acero fatima','901222333-0','2026-03-12','2023-05-30',0.00,1),(8,'Empaques Elegance','890666777-4','2026-03-12','2024-02-14',1000000.00,1),(9,'Insumos Joyeros del Eje','900111000-8','2026-03-12','2024-09-01',2500000.00,1),(10,'Brillo Eterno Mayorista','800999888-7','2026-03-12','2024-10-10',6000000.00,1),(11,'Cristales Glam','901333444-6','2026-03-12','2025-02-01',800000.00,1),(12,'Willy Johns','890888999-5','2026-03-12','2023-08-15',4000000.00,1),(13,'Relojería Continental','900444333-2','2026-03-12','2024-04-18',1200000.00,1),(14,'Relojeria y accesorios delaida','800222111-0','2026-03-12','2024-11-30',2000000.00,1),(15,'Importaciones Zafiro','901666777-3','2026-03-12','2025-01-20',7500000.00,1),(16,'Joyeria luz de sol','1099739953','2026-03-12','2026-03-12',120000.00,1);
/*!40000 ALTER TABLE `proveedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedor_material`
--

DROP TABLE IF EXISTS `proveedor_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedor_material` (
  `proveedor_id` int unsigned NOT NULL,
  `material_id` int unsigned NOT NULL,
  PRIMARY KEY (`proveedor_id`,`material_id`),
  KEY `material_id` (`material_id`),
  CONSTRAINT `proveedor_material_ibfk_1` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedor` (`proveedor_id`) ON DELETE CASCADE,
  CONSTRAINT `proveedor_material_ibfk_2` FOREIGN KEY (`material_id`) REFERENCES `material` (`material_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedor_material`
--

LOCK TABLES `proveedor_material` WRITE;
/*!40000 ALTER TABLE `proveedor_material` DISABLE KEYS */;
INSERT INTO `proveedor_material` VALUES (4,2),(6,2),(1,3),(7,3),(1,4),(7,4),(1,5),(3,5),(16,5),(3,6),(2,9),(4,10),(16,10),(2,11),(8,13),(5,14),(5,15);
/*!40000 ALTER TABLE `proveedor_material` ENABLE KEYS */;
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
  `codigo_verificacion` int NOT NULL,
  `fecha_solicitud` datetime NOT NULL,
  `fecha_expiracion` datetime NOT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`recuperacion_id`),
  UNIQUE KEY `codigo_verificacion` (`codigo_verificacion`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `recuperacion_contrasena_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recuperacion_contrasena`
--

LOCK TABLES `recuperacion_contrasena` WRITE;
/*!40000 ALTER TABLE `recuperacion_contrasena` DISABLE KEYS */;
INSERT INTO `recuperacion_contrasena` VALUES (1,2,452810,'2026-03-12 00:05:24','2026-03-12 00:20:24',1);
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
  `cargo` enum('superadministrador','administrador','vendedor') NOT NULL,
  PRIMARY KEY (`rol_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (1,'superadministrador'),(2,'administrador'),(3,'vendedor');
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
  CONSTRAINT `rol_permiso_ibfk_1` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`rol_id`) ON DELETE CASCADE,
  CONSTRAINT `rol_permiso_ibfk_2` FOREIGN KEY (`permiso_id`) REFERENCES `permiso` (`permiso_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_permiso`
--

LOCK TABLES `rol_permiso` WRITE;
/*!40000 ALTER TABLE `rol_permiso` DISABLE KEYS */;
INSERT INTO `rol_permiso` VALUES (1,1),(2,1),(1,2),(2,2),(1,3),(2,3),(1,4),(2,4),(3,4),(1,5),(2,5),(3,5),(1,6),(2,6),(1,7),(2,7),(3,7),(1,8),(2,8),(3,8),(1,9),(2,9),(1,10),(2,10),(1,11),(2,11),(3,11),(1,12),(2,12),(1,13),(2,13),(3,13),(1,14),(2,14),(1,15),(2,15),(1,16),(2,16),(1,17);
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
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subcategoria`
--

LOCK TABLES `subcategoria` WRITE;
/*!40000 ALTER TABLE `subcategoria` DISABLE KEYS */;
INSERT INTO `subcategoria` VALUES (1,'Matrimonio'),(2,'Compromiso'),(3,'15 Años'),(4,'Grados'),(5,'Religioso'),(6,'Aniversario'),(7,'Cumpleaños'),(8,'Día de la Madre'),(9,'Amor y Amistad'),(10,'Navidad'),(11,'Personalizados'),(12,'Uso Diario'),(13,'Parejas'),(14,'Protección / Amuletos'),(15,'Infantil / Bebés');
/*!40000 ALTER TABLE `subcategoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `telefono_cliente`
--

DROP TABLE IF EXISTS `telefono_cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `telefono_cliente` (
  `telefono_id` int unsigned NOT NULL AUTO_INCREMENT,
  `telefono` varchar(50) NOT NULL,
  `cliente_id` int unsigned NOT NULL,
  PRIMARY KEY (`telefono_id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `telefono_cliente_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `telefono_cliente`
--

LOCK TABLES `telefono_cliente` WRITE;
/*!40000 ALTER TABLE `telefono_cliente` DISABLE KEYS */;
INSERT INTO `telefono_cliente` VALUES (1,'3101112222',1),(2,'3112223333',2),(3,'3123334444',3),(4,'3134445555',4),(5,'3145556666',5),(6,'3156667777',6),(7,'3167778888',7),(8,'3178889999',8),(9,'3189990000',9),(10,'3190001111',10),(11,'3201112222',11),(12,'3212223333',12),(13,'3223334444',13),(14,'3234445555',14),(15,'3245556666',15);
/*!40000 ALTER TABLE `telefono_cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `telefono_proveedor`
--

DROP TABLE IF EXISTS `telefono_proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `telefono_proveedor` (
  `telefono_id` int unsigned NOT NULL AUTO_INCREMENT,
  `telefono` varchar(50) NOT NULL,
  `proveedor_id` int unsigned NOT NULL,
  PRIMARY KEY (`telefono_id`),
  KEY `proveedor_id` (`proveedor_id`),
  CONSTRAINT `telefono_proveedor_ibfk_1` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedor` (`proveedor_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `telefono_proveedor`
--

LOCK TABLES `telefono_proveedor` WRITE;
/*!40000 ALTER TABLE `telefono_proveedor` DISABLE KEYS */;
INSERT INTO `telefono_proveedor` VALUES (3,'6028889900',2),(4,'3159876543',2),(5,'3001112233',3),(6,'3204445555',4),(7,'3116667777',5),(8,'3182223344',6),(9,'3015556677',7),(10,'3129990000',8),(11,'3141112222',9),(12,'3173334444',10),(13,'3007778888',11),(14,'3134445555',12),(15,'3162221111',13),(16,'625478952',16),(17,'6014445566',1),(18,'3101234567',1);
/*!40000 ALTER TABLE `telefono_proveedor` ENABLE KEYS */;
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
  CONSTRAINT `telefono_usuario_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `telefono_usuario`
--

LOCK TABLES `telefono_usuario` WRITE;
/*!40000 ALTER TABLE `telefono_usuario` DISABLE KEYS */;
INSERT INTO `telefono_usuario` VALUES (1,'3027131281',1),(2,'3153084721',2),(3,'3123456789',3),(4,'3027131490',4),(5,'3145678901',5),(6,'3156789012',6),(7,'3167890123',7),(8,'3178901234',8),(9,'3189012345',9),(10,'3190123456',10),(11,'3201234567',11),(12,'3212345678',12),(13,'3223456789',13),(14,'3234567890',14),(15,'3245678901',15);
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
  `pass_temporal` tinyint(1) NOT NULL DEFAULT '1',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`usuario_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'AdminKS','$2a$12$X5/dP8Dv4BZ8GH8UH0iO9uvyaEjLEqHB/Bs42s6bgSFap9HJXwtq.',1,0,'2026-03-12 00:05:22'),(2,'Marlen Becerra','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(3,'Carlos Ruiz','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(4,'Stephany Moreno','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(5,'Felipe Mora','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(6,'Gabriela Silva','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(7,'Héctor Díaz','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(8,'Isabella Vega','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(9,'Javier Ortiz','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(10,'Karen López','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(11,'Luis Castro','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(12,'María Pérez','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(13,'Nicolás Silva','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(14,'Olga Ruiz','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22'),(15,'Pablo Díaz','$2a$12$jD20U/siCtFoC03VDdr80.i01..QnzNfW0/RoiSw91zcSjRUJSHBi',1,1,'2026-03-12 00:05:22');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario_rol`
--

DROP TABLE IF EXISTS `usuario_rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_rol` (
  `usuario_id` int unsigned NOT NULL,
  `rol_id` int unsigned NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `rol_id` (`rol_id`),
  CONSTRAINT `usuario_rol_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`) ON DELETE CASCADE,
  CONSTRAINT `usuario_rol_ibfk_2` FOREIGN KEY (`rol_id`) REFERENCES `rol` (`rol_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario_rol`
--

LOCK TABLES `usuario_rol` WRITE;
/*!40000 ALTER TABLE `usuario_rol` DISABLE KEYS */;
INSERT INTO `usuario_rol` VALUES (1,1),(2,2),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3),(9,3),(10,3),(11,3),(12,3),(13,3),(14,3),(15,3);
/*!40000 ALTER TABLE `usuario_rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `venta`
--

DROP TABLE IF EXISTS `venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `venta` (
  `venta_id` int unsigned NOT NULL AUTO_INCREMENT,
  `usuario_id` int unsigned NOT NULL,
  `cliente_id` int unsigned NOT NULL,
  `fecha_emision` date NOT NULL DEFAULT (curdate()),
  PRIMARY KEY (`venta_id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `cliente_id` (`cliente_id`),
  CONSTRAINT `venta_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`usuario_id`),
  CONSTRAINT `venta_ibfk_2` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`cliente_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `venta`
--

LOCK TABLES `venta` WRITE;
/*!40000 ALTER TABLE `venta` DISABLE KEYS */;
INSERT INTO `venta` VALUES (1,2,1,'2026-03-12'),(2,3,2,'2026-03-12'),(3,4,3,'2026-03-12'),(4,5,4,'2026-03-12'),(5,6,5,'2026-03-12'),(6,7,6,'2026-03-12'),(7,8,7,'2026-03-12'),(8,9,8,'2026-03-12'),(9,10,9,'2026-03-12'),(10,11,10,'2026-03-12'),(11,12,11,'2026-03-12'),(12,13,12,'2026-03-12'),(13,14,13,'2026-03-12'),(14,15,14,'2026-03-12'),(15,2,15,'2026-03-12');
/*!40000 ALTER TABLE `venta` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-12  0:12:19
